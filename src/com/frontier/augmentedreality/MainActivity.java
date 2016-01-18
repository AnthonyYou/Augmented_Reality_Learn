package com.frontier.augmentedreality;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private SurfaceView cameraPreview;
	private SurfaceHolder previewHolder;
	private Camera camera;
	private boolean inPreview;

	private final String TAG = "PARR";
	private SensorManager sensorManager;
	private int orientationSensor, accelerometerSensor;
	private float headingAngle, pitchAngle, rollAngle;
	private float xAxis, yAxis, zAxis;

	private TextView headingTV, pitchTV, rollTV, xAxisTV, yAxisTV, zAxisTV,
			latitudeTV, longitudeTV, altitudeTV;

	private LocationManager locationManager;
	private double latitude, longitude, altitude;

	private Handler handler = new Handler();

	private Runnable updateRunnable = new Runnable() {

		@Override
		public void run() {
			updateInfo();
			handler.postDelayed(updateRunnable, 1000);
		}

	};

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

	private SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				headingAngle = event.values[0];
				pitchAngle = event.values[1];
				rollAngle = event.values[2];

				Log.d(TAG, "Heading : " + String.valueOf(headingAngle));
				Log.d(TAG, "pitch : " + String.valueOf(pitchAngle));
				Log.d(TAG, "roll : " + String.valueOf(rollAngle));
			} else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				xAxis = event.values[0];
				yAxis = event.values[1];
				zAxis = event.values[2];

				Log.d(TAG, "xAxis : " + String.valueOf(xAxis));
				Log.d(TAG, "yAxis : " + String.valueOf(yAxis));
				Log.d(TAG, "zAxis : " + String.valueOf(zAxis));
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

	};

	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			altitude = location.getAltitude();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inPreview = false;

		setContentView(R.layout.activity_main);

		headingTV = (TextView) findViewById(R.id.heading);
		pitchTV = (TextView) findViewById(R.id.pinch);
		rollTV = (TextView) findViewById(R.id.roll);

		xAxisTV = (TextView) findViewById(R.id.x_axis);
		yAxisTV = (TextView) findViewById(R.id.y_axis);
		zAxisTV = (TextView) findViewById(R.id.z_axis);
		
		latitudeTV = (TextView) findViewById(R.id.latitude);
		longitudeTV = (TextView) findViewById(R.id.longitude);
		altitudeTV = (TextView) findViewById(R.id.altitude);

		cameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
		previewHolder = cameraPreview.getHolder();
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		previewHolder.addCallback(surfaceCallback);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		orientationSensor = Sensor.TYPE_ORIENTATION;

		accelerometerSensor = Sensor.TYPE_ACCELEROMETER;

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location != null) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			altitude = location.getAltitude();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(sensorEventListener,
				sensorManager.getDefaultSensor(orientationSensor),
				SensorManager.SENSOR_DELAY_NORMAL);

		sensorManager.registerListener(sensorEventListener,
				sensorManager.getDefaultSensor(accelerometerSensor),
				SensorManager.SENSOR_DELAY_NORMAL);
		
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
				0, 0, locationListener);
		
		camera = Camera.open();

		handler.post(updateRunnable);
	}

	@Override
	protected void onPause() {
		if (inPreview) {
			camera.stopPreview();
		}
		sensorManager.unregisterListener(sensorEventListener);
		locationManager.removeUpdates(locationListener);
		camera.release();
		camera = null;
		inPreview = false;

		handler.removeCallbacks(updateRunnable);
		super.onPause();
	}

	private void updateInfo() {
		headingTV.setText("heading : " + String.valueOf(headingAngle));
		pitchTV.setText("pitch : " + String.valueOf(pitchAngle));
		rollTV.setText("roll : " + String.valueOf(rollAngle));

		xAxisTV.setText("xAxis : " + String.valueOf(xAxis));
		yAxisTV.setText("yAxis : " + String.valueOf(yAxis));
		zAxisTV.setText("zAxis : " + String.valueOf(zAxis));
		
		latitudeTV.setText("latitude : " + String.valueOf(latitude));
		longitudeTV.setText("longitude : " + String.valueOf(longitude));
		altitudeTV.setText("altitude : " + String.valueOf(altitude));
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
