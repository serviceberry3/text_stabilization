package weiner.noah.noshake;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import weiner.noah.Constants;
import weiner.noah.utils.Utils;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnTouchListener {
    //naive implementation
    private final float[] tempAcc = new float[3];
    private final float[] acc = new float[3];
    private final float[] velocity = new float[3];
    private final float[] position = new float[3];
    private long timestamp = 0;

    private View layoutSensor;
    private TextView noShakeText;
    private RelativeLayout.LayoutParams originalLayoutParams;
    private RelativeLayout.LayoutParams editedLayoutParams;
    private int ogLeftMargin, ogTopMargin;

    //value of e
    private final double e = 2.71828;

    private Sensor accelerometer;
    private SensorManager sensorManager;
    private int _xDelta, _yDelta;
    private float spring_const = (float) 0.5;
    private float dampener_frix_const = (float) (2.0 * Math.sqrt(spring_const));
    private float alpha = (float) 0.8, yFactor = 50;
    private double HofT, YofT, startTime, timeElapsed;

    private int times=0;

    private float[] gravity = new float[3];

    //working on circular buffer for the data
    private float[] accelBuffer = new float[3];

    private float deltaX, deltaY, deltaZ;

    private List<Float> buffer1 = new ArrayList<>();

    //testing scheduling a task every 4 seconds
    Timer timer;
    TimerTask timerTask;

    Handler handler = new Handler();

    private long bufferStart;

    //load up native C code
    static {
        System.loadLibrary("circ_buffer");
    }

    private native void circular_buffer(long sz);

    //reset the circular buffer to empty, head == tail
    private native void circular_buf_reset();

    private native void retreat_pointer();

    private native void advance_pointer();

    //put version 1 continues to add data if the buffer is full
    //old data is overwritten
    private native void circular_buf_put(float data);

    //retrieve a value from the buffer
    //returns 0 on success, -1 if the buffer is empty
    private native float circular_buf_get();

    //returns true if the buffer is empty
    private native boolean circular_buf_empty();

    //returns true if the buffer is full
    private native boolean circular_buf_full();

    //returns the maximum capacity of the buffer
    private native long circular_buf_capacity();

    //returns the current number of elements in the buffer
    private native long circular_buf_size();

    private native float aggregate_last_n_entries(int n);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutSensor = findViewById(R.id.layout_sensor);
        noShakeText = findViewById(R.id.movable_text); //get the NoShake sample text as a TextView so we can move it around (TextView)
        originalLayoutParams = (RelativeLayout.LayoutParams) noShakeText.getLayoutParams();
        ogLeftMargin = originalLayoutParams.leftMargin;
        ogTopMargin = originalLayoutParams.topMargin;

        //get pixel dimensions of screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        Log.d("DBUG", String.format("%d by %d", height, width));

        //initialize a circular buffer of 10k floats
        circular_buffer(85);
        boolean test = circular_buf_empty();
        if (test) {
            Log.d("DBUG", "Circular buffer initialized in C++");
        }

        gravity[0]=gravity[1]=gravity[2] = 0;
        accelBuffer[0]=accelBuffer[1]=accelBuffer[2] = 0;

        ((TextView)findViewById(R.id.movable_text)).setOnTouchListener(this);
        //initialize a SensorEvent
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager==null) {
            Log.e("DBUG", "Sensor manager came up null");
        }

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        //check for accelerometers present
        if (checkAccelerometer()<0) {
            Toast.makeText(MainActivity.this, "No accelerometer found.", Toast.LENGTH_SHORT).show();
        }
/*

        while (true) {
            ((TextView) findViewById(R.id.x_axis)).setText("Testing");
            ((TextView) findViewById(R.id.y_axis)).setText("Testing");
            ((TextView) findViewById(R.id.z_axis)).setText("Testing");
        }

 */
    //set click listener for the button
        ((Button)findViewById(R.id.move_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "RESETTING", Toast.LENGTH_SHORT).show();
                reset();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int X = (int) event.getRawX();
        int Y = (int) event.getRawY();
        View view = (TextView)findViewById(R.id.movable_text);


        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.leftMargin = X - _xDelta;
                layoutParams.topMargin = Y - _yDelta;
                layoutParams.rightMargin = -250;
                layoutParams.bottomMargin = -250;
                view.setLayoutParams(layoutParams);
                break;
        }
        view.invalidate();
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            //NoShake implementation


            //layoutSensor.setVisibility(View.INVISIBLE);
            getAccelerometer(event);


            //more naive implementation
            //naivePhysicsImplementation(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() { //stuff to do when app comes back from background
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
    }


    public void naivePhysicsImplementation(SensorEvent event) {
        if (timestamp != 0)
        {
            //fill the temporary acceleration vector with the current sensor readings
            tempAcc[0] = Utils.rangeValue(event.values[0], -Constants.MAX_ACC, Constants.MAX_ACC);
            tempAcc[1] = Utils.rangeValue(event.values[1], -Constants.MAX_ACC, Constants.MAX_ACC);
            tempAcc[2] = Utils.rangeValue(event.values[2], -Constants.MAX_ACC, Constants.MAX_ACC);

            //apply lowpass filter and store results in acc float array
            Utils.lowPassFilter(tempAcc, acc, Constants.LOW_PASS_ALPHA_DEFAULT);

            //get change in time, convert from nanoseconds to seconds
            float dt = (event.timestamp - timestamp) * Constants.NS2S;


            //get velocity and position
            for (int i = 0; i < 3; i++)
            {
                //find friction to be applied using last velocity reading
                float vFrictionToApply = Constants.VELOCITY_FRICTION_DEFAULT * velocity[i];
                velocity[i] += (acc[i] * dt) - vFrictionToApply;

                //if resulting value is Nan or infinity, just change it to 0
                velocity[i] = Utils.fixNanOrInfinite(velocity[i]);

                //find position friction to be applied using last position reading
                float pFrictionToApply = Constants.POSITION_FRICTION_DEFAULT * position[i];
                position[i] += (velocity[i] * Constants.VELOCITY_AMPL_DEFAULT * dt) - pFrictionToApply;

                //set max limits on the position change
                position[i] = Utils.rangeValue(position[i], -Constants.MAX_POS_SHIFT, Constants.MAX_POS_SHIFT);
            }
        }

        //if timestamp is 0, we just started
        else
        {
            velocity[0] = velocity[1] = velocity[2] = 0f;
            position[0] = position[1] = position[2] = 0f;

            //fill in the acceleration float array
            acc[0] = Utils.rangeValue(event.values[0], -Constants.MAX_ACC, Constants.MAX_ACC);
            acc[1] = Utils.rangeValue(event.values[1], -Constants.MAX_ACC, Constants.MAX_ACC);
            acc[2] = Utils.rangeValue(event.values[2], -Constants.MAX_ACC, Constants.MAX_ACC);
        }

        //set timestamp to the current time of the sensor reading in nanoseconds
        timestamp = event.timestamp;

        //set the position of the text based on x and y axis values in position float array
        layoutSensor.setTranslationX(-position[0]);
        layoutSensor.setTranslationY(position[1]);
    }

    private void reset()
    {
        position[0] = position[1] = position[2] = 0;
        velocity[0] = velocity[1] = velocity[2] = 0;
        timestamp = 0;

        layoutSensor.setTranslationX(0);
        layoutSensor.setTranslationY(0);
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 0, 4000);
    }

    public void stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    //this is a function to initialize the timertask with a specific runnable/action to do
    public void initializeTimerTask() {
        //instantiate a new timertask
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() { //post it to be run immediately by Looper
                    public void run() {
                        //reset the clock
                        startTime = System.currentTimeMillis();
                    }
                });
            }
        };
    }

    //check to see if accelerometer is connected; print out the sensors found via Toasts
    public int checkAccelerometer() {
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        //nothing found, something's wrong, error
        if (sensors.isEmpty()) {
            return -1;
        }

        Toast.makeText(MainActivity.this, String.format("%d accelerometers found", sensors.size()), Toast.LENGTH_SHORT).show();

        int index=0;
        for (Sensor thisSensor : sensors) {
            Toast.makeText(MainActivity.this, String.format("Sensor #%d: ", index++) + thisSensor.getName(), Toast.LENGTH_SHORT).show();
        }
        return 0;
    }

    private void getAccelerometer(SensorEvent event) {
        times++;

        timeElapsed = (System.currentTimeMillis() - startTime)/1000;

        //plug in t to the system impulse response equation
        HofT = Math.pow(timeElapsed*e, (-1*timeElapsed*Math.sqrt(spring_const)));

        //Log.d("TIME", String.format("%f", timeElapsed));
        //Log.d("H VALUE", String.format("%f", HofT));

        //get the accelerometer readings (3 axes)
        float[] values = event.values;

        //use low-pass filter to affect the gravity readings slightly based on what they were before
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        //subtract the gravity affect from the actual accelerometer reading on each axis
        float x = values[0]-gravity[0];
        float y = values[1]-gravity[1];
        float z = values[2]-gravity[2];

        //add the x acceleration value into the circular buffer
        circular_buf_put(x);

        //calculate Y(t) using H(t) and inverse of acceleration as specified in the paper
        YofT = x * HofT;

        //Log.d("Y of T", String.format("%f", YofT));

        //calculate how much the acceleration changed from what it was before
        deltaX = x - accelBuffer[0];
        deltaY = y - accelBuffer[1];
        deltaZ = z - accelBuffer[2];

        //calculate overall acceleration vector
        float accelSqRt = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);


        //update the stats on the UI to show the accelerometer readings
        ((TextView) findViewById(R.id.x_axis)).setText(String.format("X accel: %f", x));
        ((TextView) findViewById(R.id.y_axis)).setText(String.format("Y accel: %f", y));
        ((TextView) findViewById(R.id.z_axis)).setText(String.format("Z accel: %f", z));
        ((TextView) findViewById(R.id.overall)).setText(String.format("Overall: %f", accelSqRt));

        //this is the time the event occurred in nanoseconds
        long actualTime = event.timestamp;

        //get current layout parameters (current position, etc) of the NoShake sample text
        editedLayoutParams = (RelativeLayout.LayoutParams) noShakeText.getLayoutParams();

        //this is a check to see whether the device is shaking
        if (aggregate_last_n_entries(15) >= 0.1) {
            //Log.d("SHAKER", "Device shaken");
            startTime = System.currentTimeMillis();

            float toMoveX = Utils.rangeValue((float)(yFactor * YofT), -Constants.MAX_POS_SHIFT, Constants.MAX_POS_SHIFT);

            layoutSensor.setTranslationX(toMoveX);

            /* INCORRECT IMPLEMENTATION
            //adjust the x position of the NoShake text, making sure it doesn't go off the screen
            editedLayoutParams.leftMargin+=yFactor * YofT;

            //adjust the y position of the NoShake text, making sure it doesn't go off the screen
            editedLayoutParams.topMargin+=yFactor * YofT;

            //set right and bottom margins to avoid compression
            editedLayoutParams.rightMargin = -250;
            editedLayoutParams.bottomMargin = -250;

            //set new layout parameters for the view (save changes)
            noShakeText.setLayoutParams(editedLayoutParams);

            //refresh the view
            noShakeText.invalidate();
             */
        }

        //reset the clock after 4 seconds if there was no shaking start??
        else if (timeElapsed>=4) {
            //Log.d("TIMES", String.format("%d", times));
            times=0;
            startTime = System.currentTimeMillis();
        }
    }
}
