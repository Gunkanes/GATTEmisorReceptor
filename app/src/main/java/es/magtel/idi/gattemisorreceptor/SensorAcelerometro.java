package es.magtel.idi.gattemisorreceptor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

/**
 * Captura datos de acelerometro y los manda a receptor
 *
 * Created by SAMUAN on 03/08/2016.
 */
public class SensorAcelerometro implements SensorEventListener {

    private EmisorActivity activity;

    public SensorAcelerometro(EmisorActivity activity){
        this.activity = activity;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float curX = 0, curY = 0, curZ = 0;
        synchronized (this){
            long current_time = event.timestamp;

            curX = event.values[0];
            curY = event.values[1];
            curZ = event.values[2];

            activity.indicarGravedad(curX, curY, curZ);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void activar(){
        SensorManager sm = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if( sensors.size()>0) sm.registerListener( this, sensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void desactivar(){
        SensorManager sm = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(this);
    }
}
