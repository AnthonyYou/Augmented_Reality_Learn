package com.frontier.augmentedreality;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity {

	private SurfaceView cameraPreview;
	private SurfaceHolder previewHolder;
	private Camera camera;
	private boolean inPreview;

	private final String TAG = "PARR";
	private SensorManager sensorManager;
	private int orientationSensor;
	private float headingAngle, pitchAngle, rollAngle;

	private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			try {
				camera.setPreviewDisplay(previewHolder);
			} catch (Throwable t) {
				Log.e("surfaceCreated", "exception in setPreviewDisplay", t);
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
			Camera.Parameters parameters = camera.getParameters();
			Camera.Size size = getBestPreviewSize(width, height, parameters);
			if (size != null) {
				parameters.setPreviewSize(size.width, size.height);
				camera.startPreview();
				inPreview = true;
			}
		}
	};
	
	private SensorEventListener sensorEventListener = new SensorEventListener(){

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if(event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				headingAngle = event.values[0];
				pitchAngle = event.values[1];
				rollAngle = event.values[2];
				
				Log.d(TAG, "Heading : " + String.valueOf(headingAngle));
				Log.d(TAG, "pitch : " + String.valueOf(pitchAngle));
				Log.d(TAG, "roll : " + String.valueOf(rollAngle));
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		inPreview = false;

		setContentView(R.layout.activity_main);
		cameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
		previewHolder = cameraPreview.getHolder();
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		previewHolder.addCallback(surfaceCallback);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		orientationSensor = Sensor.TYPE_ORIENTATION;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		sensorManager.registerListener(sensorEventListener,
				sensorManager.getDefaultSensor(orientationSensor),
				SensorManager.SENSOR_DELAY_NORMAL);
		camera = Camera.open();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (inPreview) {
			camera.stopPreview();
		}
		sensorManager.unregisterListener(sensorEventListener);
		camera.release();
		camera = null;
		inPreview = false;
		super.onPause();
	}

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;
					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return result;
	}
}
