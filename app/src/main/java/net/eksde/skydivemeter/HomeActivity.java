package net.eksde.skydivemeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mPressure;
    private TextView pressureText;
    private TextView pressureText2;
    private TextView pressureText3;
    private TextView altiText;
    private int test = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        pressureText = (TextView) findViewById(R.id.pressureText);
        pressureText2 = (TextView) findViewById(R.id.pressureText2);
        pressureText3 = (TextView) findViewById(R.id.pressureText3);
        altiText = (TextView) findViewById(R.id.altiText);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mSensorManager.getAltitude(SensorManager.)
    }


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float millibars_of_pressure = event.values[0];
        double feet = pressureToFeet(millibars_of_pressure);
        double meter = feet * 0.3048;
        pressureText.setText(String.format("%f" ,millibars_of_pressure) + " hPa / mBar");
        pressureText2.setText(String.format("%f", pressureToFeet(millibars_of_pressure)) + " feet");
        pressureText3.setText(String.format("%f", meter) + " meter");
        // Do something with this sensor data.

        altiText.setText(Integer.toString(test));
        test++;
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public double pressureToFeet(float pressure){
        //Altitude = (10^(log(P/P_0)/5.2558797)-1/(-6.8755856*10^-6)

        float pressure0 = 1013.25f;

       // return (Math.pow(10, (Math.log(pressure/pressure0))/5.2558797)-1)/(-6.8755856*Math.pow(10, -6));

        return (1-Math.pow(pressure/1013.25, 0.190284))*145366.45;

    }

}
