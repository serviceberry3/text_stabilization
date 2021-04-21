package weiner.noah.noshake;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import weiner.noah.NaiveConstants;
import weiner.noah.NoShakeConstants;
import weiner.noah.openglbufftesting.OpenGLRenderer;
import weiner.noah.openglbufftesting.OpenGLView;
import weiner.noah.utils.Utils;
import weiner.noah.ctojavaconnector.*;
//import weiner.noah.openglbufftesting;


public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnTouchListener {
    //NAIVE IMPLEMENTATION ACCEL ARRAYS

    //temporary array to store raw linear accelerometer data before low-pass filter applied
    private final float[] NtempAcc = new float[3];

    //acceleration array for data after filtering
    private final float[] Nacc = new float[3];

    //velocity array (calculated from acceleration values)
    private final float[] Nvelocity = new float[3];

    //position (displacement) array (calculated from dVelocity values)
    private final float[] Nposition = new float[3];

    //NOSHAKE SPRING IMPLEMENTATION ACCEL ARRAYS
    private final float[] StempAcc = new float[3];
    private final float[] Sacc = new float[3];
    private final float[] accAfterFrix = new float[3];

    //long to use for keeping track of thyme
    private long timestamp = 0;

    //the view to be stabilized
    private View layoutSensor, waitingText;

    //the text that can be dragged around (compare viewing of this text to how the stabilized text looks)
    private TextView noShakeText;

    //original vs. changed layout parameters of the draggable text
    private RelativeLayout.LayoutParams originalLayoutParams;
    private RelativeLayout.LayoutParams editedLayoutParams;
    private int ogLeftMargin, ogTopMargin;

    //the accelerometer and its manager
    private Sensor accelerometer;
    private SensorManager sensorManager;

    //changes in x and y to be used to move the draggable text based on user's finger
    private int _xDelta, _yDelta;

    //time variables, and the results of H(t) and Y(t) functions
    private double HofT, YofT, startTime, timeElapsed;

    //the raw values that the low-pass filter is applied to
    private float[] gravity = new float[3];

    //working on circular buffer for the data
    private float[] accelBuffer = new float[3];

    private float impulseSum;

    //is the device shaking?
    private volatile int shaking = 0;

    private int index = 0, check = 0, times = 0;

    private Thread outputPlayerThread = null;

    private OpenGLRenderer myRenderer;

    private OpenGLView openGLView;

    public static float toMoveX, toMoveY;

    //whether to use OpenGL for rendering
    private boolean USE_OPENGL_SQUARE_VERS = true;

    //whether to apply the NoShake correction or leave text/square stagnant
    private boolean APPLY_CORRECTION = true;

    //log tag
    private final String TAG = "MainActivity";

    //load up native C code. lib includes circ_buffer.cpp, convolve.cpp, and impulse_response.cpp
    static {
        System.loadLibrary("circ_buffer");
    }

    //thread that writes data to the circular buffer. Haven't figured out whether or not to use this
    class getDataWriteBuffer implements Runnable {
        float xAccel;

        getDataWriteBuffer(float x) {
            this.xAccel = x;
        }

        @Override
        public void run() {
            //write the x acceleration data into the circular buffer
            Log.d("WRITER", String.format("Putting value %f", xAccel));
            CircBuffer.circular_buf_put(xAccel, 0);
        }
    }

    //EXPERIMENTAL: thread that reads/aggregates the last ~1 second of data from the buffer and determines if the device is shaking
    class detectShaking implements Runnable {
        @Override
        public void run() {
            while (true) {
                //DEBUG
                //Log.d("DBUG", "Running thread");
                //Log.d("HEAD", String.format("Head is %d", CircBuffer.circular_buf_get_head()));

                //aggregate the last 50 entries in the circular buffer, taking average of x and y aggregations
                float aggregation = (CircBuffer.aggregate_last_n_entries(50, 0) + CircBuffer.aggregate_last_n_entries(50, 1))/2;

                //LOG the aggregation
                //Log.d("AVERAGE", String.format("%f", aggregation));

                //set the shaking flag appropriately
                if (aggregation >= 0) {
                    if (aggregation >= NoShakeConstants.shaking_threshold) {
                        shaking = 1;
                    }
                    else {
                        shaking = 0;
                    }
                }
            }
        }
    }

    //thread that displays "Please wait..." until the circular buffer is full so that convolution can begin
    class bufferWait implements Runnable {
        @Override
        public void run() {
            //wait until the size of the buffer is equal to the requested size
            while (CircBuffer.circular_buf_size(0) < NoShakeConstants.buffer_size) {
                ;
            }
            waitingText.setVisibility(View.INVISIBLE);
        }
    }

    //set up view constants and button
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //hide nav bar and status bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);


        //requesting to turn the title OFF
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        //making it full screen
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //getSupportActionBar().hide(); //hide the title bar.

        Log.i(TAG, "Initiating openglview...");

        //initiate the openGLView and create an instance with this activity
        openGLView = new OpenGLView(this);

        openGLView.setEGLContextClientVersion(2);

        openGLView.setPreserveEGLContextOnPause(true);

        myRenderer = new OpenGLRenderer(this, MainActivity.this);

        openGLView.setRenderer(myRenderer);

        //openGLView = (OpenGLView) findViewById(R.id.openGLView);

        if (USE_OPENGL_SQUARE_VERS) {
            //FOR OPENGL VERSION
            setContentView(openGLView);
        }
        else {
            //FOR ANDROID GRAPHICS VIEW VSION
            setContentView(R.layout.activity_main);
        }


        layoutSensor = findViewById(R.id.layout_sensor);
        //noShakeText = findViewById(R.id.movable_text); //get the NoShake sample text as a TextView so we can move it around (TextView)
        waitingText = findViewById(R.id.waiting_text);

        /*
        originalLayoutParams = (RelativeLayout.LayoutParams) noShakeText.getLayoutParams();
        ogLeftMargin = originalLayoutParams.leftMargin;
        ogTopMargin = originalLayoutParams.topMargin;*/

        //get pixel dimensions of screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        //initialize a circular buffer of 211 floats
        CircBuffer.circular_buffer(NoShakeConstants.buffer_size, 0);
        CircBuffer.circular_buffer(NoShakeConstants.buffer_size, 1);

        //initialize an impulse response array also of size 211
        ImpulseResponse.impulse_resp_arr(NoShakeConstants.buffer_size, NoShakeConstants.e, NoShakeConstants.spring_const);

        //populate the H(t) impulse response array in C++ based on the selected spring constant
        ImpulseResponse.impulse_response_arr_populate();

        //store sum of filter
        impulseSum = ImpulseResponse.impulse_response_arr_get_sum();
        Log.d("DBUG", String.format("Impulse sum is %f", impulseSum));

        //instantiate a convolver which has a pointer to both the circular buffer and the impulse response array
        Convolve.convolver(CircBuffer.circular_buf_address(0), 0);
        Convolve.convolver(CircBuffer.circular_buf_address(1), 1);


        //immediately start a looping thread that constantly reads the last 50 data and sets the "shaking" flag accordingly
        detectShaking shakeListener = new detectShaking();
        new Thread(shakeListener).start();


        //display PLEASE WAIT text while circ buffer is filling up with data
        if (!USE_OPENGL_SQUARE_VERS) {
            bufferWait waitingTextThread = new bufferWait();
            new Thread(waitingTextThread).start();
        }


        gravity[0] = gravity[1] = gravity[2] = 0;
        accelBuffer[0] = accelBuffer[1] = accelBuffer[2] = 0;

        //set the draggable text to listen, according to onTouch function (defined below)
        //((TextView)findViewById(R.id.movable_text)).setOnTouchListener(this);

        //initialize a SensorEvent
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //get the linear accelerometer as object from the system
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        //check for accelerometers present
        if (checkAccelerometer() < 0) {
            Toast.makeText(MainActivity.this, "No accelerometer found.", Toast.LENGTH_SHORT).show();
        }

        /*
        //OPTIONAL: set click listener for the RESET button
        ((Button)findViewById(R.id.move_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "RESETTING", Toast.LENGTH_SHORT).show();

                //reset everything
                reset();
            }
        });
        */
    }


    //function to move the "ClickandDrag" text around the screen when the user touches on it and drags
    //use to compare against the self-adjusting text
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        /*
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
                //make live adjustment
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.leftMargin = X - _xDelta;
                layoutParams.topMargin = Y - _yDelta;
                layoutParams.rightMargin = -250;
                layoutParams.bottomMargin = -250;
                view.setLayoutParams(layoutParams);
                break;
        }

        //refresh the text
        view.invalidate();
        return true;*/
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            //layoutSensor.setVisibility(View.INVISIBLE);

            //noShake implementation
            noShake钟林(event);

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

        //register listener for the accelerometer
        sensorManager.registerListener(this, accelerometer, 1);

        openGLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        openGLView.onPause();
    }

    //a naive and heavily error-prone implementation of NoShake which attempts to calculate the displacement of the phone using the accelerometer data
    public void naivePhysicsImplementation(SensorEvent event) {
        if (timestamp != 0)
        {
            //fill the temporary acceleration vector with the current sensor readings
            NtempAcc[0] = Utils.rangeValue(event.values[0], -NaiveConstants.MAX_ACC, NaiveConstants.MAX_ACC);
            NtempAcc[1] = Utils.rangeValue(event.values[1], -NaiveConstants.MAX_ACC, NaiveConstants.MAX_ACC);
            NtempAcc[2] = Utils.rangeValue(event.values[2], -NaiveConstants.MAX_ACC, NaiveConstants.MAX_ACC);

            //apply lowpass filter and store results in acc float array
            Utils.lowPassFilter(NtempAcc, Nacc, NaiveConstants.LOW_PASS_ALPHA);

            //get change in time, convert from nanoseconds to seconds
            float dt = (event.timestamp - timestamp) * NaiveConstants.NANOSEC_TO_SEC;

            //get velocity and position
            for (int i = 0; i < 3; i++)
            {
                //find friction to be applied using last velocity reading
                float vFrictionToApply = NaiveConstants.VELOCITY_FRICTION_DEFAULT * Nvelocity[i];
                Nvelocity[i] += (Nacc[i] * dt) - vFrictionToApply;

                //if resulting value is Nan or infinity, just change it to 0
                Nvelocity[i] = Utils.fixNanOrInfinite(Nvelocity[i]);

                //find position friction to be applied using last position reading
                float pFrictionToApply = NaiveConstants.POSITION_FRICTION_DEFAULT * Nposition[i];
                Nposition[i] += (Nvelocity[i] * NaiveConstants.VELOCITY_AMPL_DEFAULT * dt) - pFrictionToApply;

                //set max limits on the position change
                Nposition[i] = Utils.rangeValue(Nposition[i], -NaiveConstants.MAX_POS_SHIFT, NaiveConstants.MAX_POS_SHIFT);
            }
        }

        //if timestamp is 0, we just started
        else
        {
            Nvelocity[0] = Nvelocity[1] = Nvelocity[2] = 0f;
            Nposition[0] = Nposition[1] = Nposition[2] = 0f;

            //fill in the acceleration float array
            Nacc[0] = Utils.rangeValue(event.values[0], -NaiveConstants.MAX_ACC, NaiveConstants.MAX_ACC);
            Nacc[1] = Utils.rangeValue(event.values[1], -NaiveConstants.MAX_ACC, NaiveConstants.MAX_ACC);
            Nacc[2] = Utils.rangeValue(event.values[2], -NaiveConstants.MAX_ACC, NaiveConstants.MAX_ACC);
        }

        //set timestamp to the current time of the sensor reading in nanoseconds
        timestamp = event.timestamp;

        //set the position of the text based on x and y axis values in position float array
        layoutSensor.setTranslationX(-Nposition[0]);
        layoutSensor.setTranslationY(Nposition[1]);
    }

    //reset everything
    private void reset()
    {
        //FOR NAIVE PHYSICS IMPLEMENTATION: reset position and velocity arrays, along with timestamp, to 0
        Nposition[0] = Nposition[1] = Nposition[2] = 0;
        Nvelocity[0] = Nvelocity[1] = Nvelocity[2] = 0;
        timestamp = 0;

        //destroy our instance of circular buffer and convolver
        CircBuffer.circular_buffer_destroy(0);
        CircBuffer.circular_buffer_destroy(1);

        Convolve.convolver_destroy(0);
        Convolve.convolver_destroy(1);

        //initialize a NEW circular buffer of 211 floats, for both x and y axes
        CircBuffer.circular_buffer(NoShakeConstants.buffer_size, 0);
        CircBuffer.circular_buffer(NoShakeConstants.buffer_size, 1);

        //initialize a NEW convolver for both x and y axes
        Convolve.convolver(CircBuffer.circular_buf_address(0), 0);
        Convolve.convolver(CircBuffer.circular_buf_address(1), 1);

        //set the NoShake text back to its original position
        layoutSensor.setTranslationX(0);
        layoutSensor.setTranslationY(0);
    }

    //check to see if accelerometer is connected; print out the sensors found via Toasts
    public int checkAccelerometer() {
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        //nothing found, something's wrong, error
        if (sensors.isEmpty()) {
            Toast.makeText(MainActivity.this, "No accelerometers found, try restarting device.", Toast.LENGTH_SHORT).show();
            return -1;
        }

        /*OPTIONAL: PRINT OUT ACCELEROMETERS THAT WERE FOUND
        Toast.makeText(MainActivity.this, String.format("%d accelerometers found", sensors.size()), Toast.LENGTH_SHORT).show();

        int index=0;
        for (Sensor thisSensor : sensors) {
            Toast.makeText(MainActivity.this, String.format("Sensor #%d: ", index++) + thisSensor.getName(), Toast.LENGTH_SHORT).show();
        }
        */
        return 0;
    }

    //implementation of ZL's NoShake version
    private void noShake钟林(SensorEvent event) {
        StempAcc[0] = Utils.rangeValue(event.values[0], -NaiveConstants.MAX_ACC, NaiveConstants.MAX_ACC);
        StempAcc[1] = Utils.rangeValue(event.values[1], -NaiveConstants.MAX_ACC, NaiveConstants.MAX_ACC);

        //apply the low pass filter to reduce noise
        Utils.lowPassFilter(StempAcc, Sacc, NoShakeConstants.low_pass_alpha);

        /*
        //EXPERIMENTAL: to speed things up, start a separate thread to go write the acceleration data to the buffer while we finish calculations here
        getDataWriteBuffer writerThread = new getDataWriteBuffer(Sacc[0]);
        new Thread(writerThread).start();
         */

        //try to eliminate noise by knocking low acceleration values down to 0 (also make text re-center faster)
        if (Math.abs(Sacc[0]) <= 0.5) {
            Sacc[0] = 0;
        }
        if (Math.abs(Sacc[1]) <= 0.5) {
            Sacc[1] = 0;
        }

        //apply some extra friction (hope is to make text return to center of screen a little faster)
        //rapid decreases will be highlighted by this
        float xFrixToApply = accAfterFrix[0] * NoShakeConstants.extra_frix_const;
        float yFrixToApply = accAfterFrix[1] * NoShakeConstants.extra_frix_const;

        //apply the friction to get new x and y acceleration values
        accAfterFrix[0] = Sacc[0] - xFrixToApply;
        accAfterFrix[1] = Sacc[1] - yFrixToApply;

        //add the x and y acceleration values to the circular buffer
        int h = CircBuffer.circular_buf_put(accAfterFrix[0], 0);
        int l = CircBuffer.circular_buf_put(accAfterFrix[1], 1);

        //DEBUGGING
        //Log.d("TIME", String.format("%f", timeElapsed));
        //Log.d("H VALUE", String.format("%f", HofT));

        /*IF USING NORMAL ACCELEROMETER
        //use low-pass filter to affect the gravity readings only slightly based on what they were before
        gravity[0] = NoShakeConstants.alpha * gravity[0] + (1 - NoShakeConstants.alpha) * event.values[0];
        gravity[1] = NoShakeConstants.alpha * gravity[1] + (1 - NoShakeConstants.alpha) * event.values[1];
        gravity[2] = NoShakeConstants.alpha * gravity[2] + (1 - NoShakeConstants.alpha) * event.values[2];
         */

        //Log.d("Y of T", String.format("%f", YofT));

        /*
        //OPTIONAL: calculate how much the acceleration changed from what it was before
        deltaX = x - accelBuffer[0];
        deltaY = y - accelBuffer[1];
        deltaZ = z - accelBuffer[2];

        //OPTIONAL: calculate overall acceleration vector
        float accelSqRt = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
         */

        //OPTIONAL: update the stats on the UI to show the accelerometer readings
        //((TextView) findViewById(R.id.x_axis)).setText(String.format("X accel: %f", Sacc[0]));
        //((TextView) findViewById(R.id.y_axis)).setText(String.format("Y accel: %f", Sacc[1]));
        //((TextView) findViewById(R.id.z_axis)).setText(String.format("Z accel: %f", z));

        //OPTIONAL: check to see whether the device is shaking
        //if (shaking==1) { //empirically-determined threshold in order to keep text still when not really shaking

            //convolve the circular buffer of acceleration data with the impulse response array to get Y(t) array
            float f = Convolve.convolve(0, CircBuffer.circular_buf_get_head(0));
            float y = Convolve.convolve(1, CircBuffer.circular_buf_get_head(1));

            float deltaX = 0;
            float deltaY = 0;

            //do calculations
            for (int i=0; i < NoShakeConstants.buffer_size; i++) {
                float impulseValue = ImpulseResponse.impulse_response_arr_get_value(i);
                deltaX += impulseValue * Convolve.getTempXMember(i, 0);
                deltaY += impulseValue * Convolve.getTempXMember(i, 1);
            }

            //normalize the scale of filters with arbitrary length/magnitude
            deltaX /= impulseSum;
            deltaY /= impulseSum;

            //calculate how much we need to move text in Y direction for this frame
            toMoveY = -1 *                                                              //flip
                    (deltaX - NaiveConstants.POSITION_FRICTION_DEFAULT * deltaX)        //reduce deltaX by adding some friction. Use deltaX for Y displacement b/c screen is horizontal
                    * NoShakeConstants.yFactor;                                         //arbitrary scaling factor
            //Log.d("DBUG", String.format("To move x is %f", toMoveX));


            //calculate how much we needto move text in x direction for this frame
            toMoveX = -1 *                                                               //flip
                (deltaY - NaiveConstants.POSITION_FRICTION_DEFAULT * deltaY)         //reduce deltaY by adding some friction. Use deltaY for X displacement b/c screen is horizontal
                * NoShakeConstants.yFactor;                                          //arbitrary scaling factor



            if (APPLY_CORRECTION) {
                if (USE_OPENGL_SQUARE_VERS) {
                    //OPENGL VERSION
                    myRenderer.toMoveX = toMoveX / 1000f;

                    myRenderer.toMoveY = toMoveY / 1000f;
                } else {

                    long time = System.nanoTime();

                    //ANDROID GRAPHICS VIEW VERSION
                    layoutSensor.setTranslationX(toMoveX);

                    time = System.nanoTime() - time;
                    Log.i("TIMER", String.format("setTranslationX took %d ns", time));

                    //ANDROID GRAPHICS VIEW VERSION
                    layoutSensor.setTranslationY(toMoveY);
                }
            }

            /*
            //OPTIONAL: print out convolved signal array on the log -- x direction
            for (int i=0; i<NoShakeConstants.buffer_size; i++) {
                Log.d("XARRAY", String.format("Index %d: %f", i, Convolve.getXMember(i, 0)));
            }


            //OPTIONAL: print out convolved signal array on the log -- y direction
            for (int i=0; i<Convolve.getYSize(); i++) {
                Log.d("YARRAY", String.format("Index %d: %f", i, Convolve.getYMember(i)));
            }
             */

        //} //OPTIONAL: check to see whether the device is shaking
    }
}
