package com.codefixia.input;

import com.codefixia.drumcloud.DrumCloud;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Use this class to retrieve data about the movement of the device
 */
public class Accelerometer implements SensorEventListener {
  /**
	 * 
	 */
	private final DrumCloud drumCloud;
private SensorManager sensorManager;
  private Sensor accelerometer;
  private float[] values;

  public Accelerometer(DrumCloud drumCloud) {
    this.drumCloud = drumCloud;
	sensorManager = (SensorManager)this.drumCloud.getSystemService(DrumCloud.SENSOR_SERVICE);
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    values = new float[3];
  }


  public float[] getValues() {
    return values;
  }

  public float getX() {
    return values[0];
  }

  public float getY() {
    return values[1];
  }

  public float getZ() {
    return values[2];
  }

  /**
   * SensorEventListener interace
   */
  public void onSensorChanged(SensorEvent event) {
    values = event.values;
    //float[] vals = event.values;
    //for (int i=0; i<vals.length;i++){
    //  println(" sensor! "+vals[i]);
    //}
  }

  /**
   * SensorEventListener interace
   */
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }
}