package weiner.noah.noshake;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private Sensor accelerometer;
    private SensorManager sensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize a SensorEvent
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //get the accelerometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        while (true) {
            ((TextView)findViewById(R.id.x_axis)).setText("Testing");
            ((TextView)findViewById(R.id.y_axis)).setText("Testing");
            ((TextView)findViewById(R.id.z_axis)).setText("Testing");
        }

    }

    @Override
    public void onSensorChanged (SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
