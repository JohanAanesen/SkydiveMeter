package net.eksde.skydivemeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mPressure;
    private Sensor mGravity;

    private TextView pressureText;
    private TextView pressureText2;
    private TextView pressureText3;
    private TextView baseLineText;
    private TextView baseLineText2;
    private TextView baseLineText3;
    private TextView gravityText;

    private TextView altiText;
    private TextView graviText;


    private int groundCounter = 900;
    private int airCounter = 0;
    private float baseLinePressure;
    private double baseLineFeet;
    private double baseLineMeter;

    private boolean boot = true;
    private String sessionKey = null;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private double test = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //TextViews being initialized and variable mapped
        pressureText = (TextView) findViewById(R.id.pressureText);
        pressureText2 = (TextView) findViewById(R.id.pressureText2);
        pressureText3 = (TextView) findViewById(R.id.pressureText3);
        baseLineText = (TextView) findViewById(R.id.baseLineText);
        baseLineText2 = (TextView) findViewById(R.id.baseLineText2);
        baseLineText3 = (TextView) findViewById(R.id.baseLineText3);
        gravityText = (TextView) findViewById(R.id.gravityText);

        altiText = (TextView) findViewById(R.id.altiText);
        graviText = (TextView) findViewById(R.id.graviText);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        altiText.setText("0.0 ft");
        graviText.setText("0.0 M/S^2");


        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("altitude");


    }


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_PRESSURE) {

            float pressure = event.values[0];

            double meter = mSensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure);
            double feet = meter / 0.3048;
            pressureText.setText(String.format("%.3f", pressure) + " hPa / mBar");
            pressureText2.setText(String.format("%.3f", feet) + " feet");
            pressureText3.setText(String.format("%.3f", meter) + " meter");
            // Do something with this sensor data.

            //rebase if the counter goes up
            if (groundCounter >= 900) {
                baseLinePressure = pressure;
                baseLineMeter = meter;
                baseLineFeet = feet;

                baseLineText.setText(String.format("%.3f", baseLinePressure) + " hPa / mBar");
                baseLineText2.setText(String.format("%.3f", baseLineFeet) + " feet");
                baseLineText3.setText(String.format("%.3f", baseLineMeter) + " meter");
                groundCounter = 0;
            }

            if (airCounter > 9000) {
                Log.d("SHIT BOI", "you fly");
            }

            if (airCounter > 0 && (airCounter % 5) == 0) {

                if (boot) {
                    sessionKey = myRef.push().getKey();
                    boot = false;
                }

                //altitude relative to the base
                Double altitude = Math.floor(feet - baseLineFeet);
                altiText.setText(Double.toString(altitude) + " ft");

                if (sessionKey != null && (airCounter % 50) == 0) {
                    myRef.child(sessionKey).child(Integer.toString(airCounter / 5)).setValue(altitude);
                }
            }

            if (meter < baseLineMeter + 20) {
                airCounter = 0;
                groundCounter++;
            } else {
                groundCounter = 0;
                airCounter++;
            }

        }else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double gravity = Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));


            if(gravity < test){test = gravity;}

            gravityText.setText(Double.toString(test) + " M/S^2");
            graviText.setText(String.format("%.2f", gravity) + " M/S^2");
        }
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
        mSensorManager.unregisterListener(this);
    }


    //legacy mBar/pha to feet formula
    public double pressureToFeet(float pressure){
        //Altitude = (10^(log(P/P_0)/5.2558797)-1/(-6.8755856*10^-6)

        float pressure0 = 1013.25f;

       // return (Math.pow(10, (Math.log(pressure/pressure0))/5.2558797)-1)/(-6.8755856*Math.pow(10, -6));

        return (1-Math.pow(pressure/1013.25, 0.190284))*145366.45;

    }

}
