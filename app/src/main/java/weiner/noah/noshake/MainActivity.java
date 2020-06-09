package weiner.noah.noshake;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
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

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnTouchListener {
    //value of e
    private final double e = 2.71828;

    private Sensor accelerometer;
    private SensorManager sensorManager;
    private int _xDelta, _yDelta;
    private float spring_const = 5;
    private float dampener_frix_const = (float) (2.0 * Math.sqrt(spring_const));
    private float alpha = (float) 0.8, yFactor = 300;
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

        //get pixel dimensions of screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        Log.d("DBUG", String.format("%d by %d", height, width));

        //initialize a circular buffer of 10k floats
        circular_buffer(60);
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
        //checkAccelerometer();
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
                //Toast.makeText(MainActivity.this, "button", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int X = (int) event.getRawX();
        int Y = (int) event.getRawY();
        View view = (TextView)findViewById(R.id.movable_text);

        Log.d("TOUCHED", String.format("Touched at x coord %d, y coord %d", X, Y));

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                Log.d("LAYOUTPARAMS", String.format("Left margin is %d, top margin is %d", lParams.leftMargin, lParams.topMargin));
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() { //stuff to do when app comes back from background
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        startTimer();
    }



    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 0, 4000);
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //reset the clock
                        startTime = System.currentTimeMillis();

                        if (circular_buf_full()) {
                            //show a toast
                            Toast.makeText(getApplicationContext(), String.format("%f", aggregate_last_n_entries(15)), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), "fuk", Toast.LENGTH_SHORT).show();
                        }
                        times=0;
                        //clear out the acceleration buffer
                        //buffer1.clear();
                    }
                });
            }
        };
    }

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
        timeElapsed = (System.currentTimeMillis() - startTime)/1000;

        //plug in t to the system impulse response equation
        HofT = Math.pow(timeElapsed*e, (-1*timeElapsed*Math.sqrt(spring_const)));
        //Log.d("TIME", String.format("%f", timeElapsed));
        Log.d("H VALUE", String.format("%f", HofT));

        times++;
        float[] values = event.values;

        //use low-pass filter to affect the gravity readings slightly based on what they were before
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];


        // Movement
        float x = values[0]-gravity[0];
        float y = values[1]-gravity[1];
        float z = values[2]-gravity[2];

        //add the x acceleration value into the circular buffer
        circular_buf_put(x);

        //calculate Y(t) using H(t) and inverse of acceleration
        YofT = -1 * x * HofT;
        Log.d("Y of T", String.format("%f", YofT));

        deltaX = x - accelBuffer[0];
        deltaY = y - accelBuffer[1];
        deltaZ = z - accelBuffer[2];

        float accelSqRt = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

        ((TextView) findViewById(R.id.x_axis)).setText(String.format("X accel: %f", x));
        ((TextView) findViewById(R.id.y_axis)).setText(String.format("Y accel: %f", y));
        ((TextView) findViewById(R.id.z_axis)).setText(String.format("Z accel: %f", z));
        ((TextView) findViewById(R.id.overall)).setText(String.format("Overall: %f", accelSqRt));

        long actualTime = event.timestamp;

        if (aggregate_last_n_entries(15) >= 1.2) {
            Toast.makeText(MainActivity.this, "Device shaken.", Toast.LENGTH_SHORT).show();
        }

        TextView view = (TextView) findViewById(R.id.movable_text);

        //update position of text based on acceleration
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

        layoutParams.leftMargin+=yFactor * YofT;
        layoutParams.topMargin+=yFactor * YofT;
        layoutParams.rightMargin = -250;
        layoutParams.bottomMargin = -250;
        view.setLayoutParams(layoutParams);
        view.invalidate();
    }
}
