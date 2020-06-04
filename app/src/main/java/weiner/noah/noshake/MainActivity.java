package weiner.noah.noshake;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
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

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnTouchListener {
    private Sensor accelerometer;
    private SensorManager sensorManager;
    private int _xDelta, _yDelta;
    private float spring_const = 20;
    private float dampener_frix_const = (float) (2.0 * Math.sqrt(spring_const));
    private float alpha = (float) 0.8;

    private float[] gravity = new float[3];

    //working on circular buffer for the data
    private float[] accelBuffer = new float[3];

    private float deltaX, deltaY, deltaZ;

    private float 


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }


    public int checkAccelerometer() {
        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

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
        float[] values = event.values;

        //use low-pass filter to affect the gravity readings slightly based on what they were before
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];


        // Movement
        float x = values[0]-gravity[0];
        float y = values[1]-gravity[1];
        float z = values[2]-gravity[2];

        deltaX = x - accelBuffer[0];
        deltaY = y - accelBuffer[1];
        deltaZ = z - accelBuffer[2];

        float accelSqRt = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

        ((TextView) findViewById(R.id.x_axis)).setText(String.format("X accel: %f", x));
        ((TextView) findViewById(R.id.y_axis)).setText(String.format("Y accel: %f", y));
        ((TextView) findViewById(R.id.z_axis)).setText(String.format("Z accel: %f", z));
        ((TextView) findViewById(R.id.overall)).setText(String.format("Overall: %f", accelSqRt));

        long actualTime = event.timestamp;

        if (accelSqRt >= 1.2) {
            Toast.makeText(MainActivity.this, "Device shaken.", Toast.LENGTH_SHORT).show();
        }

        TextView view = (TextView) findViewById(R.id.movable_text);
        //update position of text based on acceleration
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.leftMargin+=deltaX * 100;
        layoutParams.topMargin+=deltaY * 100;
        layoutParams.rightMargin = -250;
        layoutParams.bottomMargin = -250;
        view.setLayoutParams(layoutParams);
        view.invalidate();
    }
}
