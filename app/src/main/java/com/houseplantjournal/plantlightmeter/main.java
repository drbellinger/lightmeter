package com.houseplantjournal.plantlightmeter;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class main extends AppCompatActivity implements SensorEventListener, SurfaceHolder.Callback {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor lightSensor;

    private static SurfaceView surfaceView;
    private static SurfaceHolder surfaceHolder;

    static Camera camera = null;
    Camera.PictureCallback rawCallback;
    Camera.ShutterCallback shutterCallback;
    Camera.PictureCallback jpegCallback;

    private Size imageDimension;
    protected CaptureRequest.Builder captureRequestBuilder;
    protected CameraCaptureSession cameraCaptureSessions;
    private Handler mBackgroundHandler;

    TextView accelerometerTextView;
    TextView lightTextView;
    ImageView angleImageView;
    TextView dateTimeTextView;
    TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instantiateView();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Register Accelerometer (if available)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        registerSensorIfAvailable(accelerometer);

        // Register Light Sensor (if available)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        registerSensorIfAvailable(lightSensor);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        rawCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d("Log", "onPictureTaken - raw");
            }
        };

        /** Handles data for jpeg picture */
        shutterCallback = new Camera.ShutterCallback() {
            public void onShutter() {
                Log.i("Log", "onShutter'd");
            }
        };
        jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(String.format(
                            "/sdcard/%d.jpg", System.currentTimeMillis()));
                    outStream.write(data);
                    outStream.close();
                    Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                Log.d("Log", "onPictureTaken - jpeg");
            }
        };


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(calendar.getTime());

        // textView is the TextView view that should display it
        dateTimeTextView.setText(formattedDate);

        TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //open your camera here
                start_camera();
            }
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                // Transform you image captured size according to the surface width and height
            }
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        };

        textureView.setSurfaceTextureListener(textureListener);
    }

    private boolean checkCamera2Support() {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            int numberOfCameras = 0;
            CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

            try {
                numberOfCameras =  manager.getCameraIdList().length;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch(AssertionError e) {
                e.printStackTrace();
            }

            if( numberOfCameras == 0 ) {
                Log.d("[PLM]", "0 cameras");
            } else {
                Log.d("[PLM]", "I have cameras");
            }
            return true;
        }
        return false;
    }

    private void instantiateView() {
        accelerometerTextView = (TextView) findViewById(R.id.accelerometer_label);
        lightTextView = (TextView) findViewById(R.id.light_label);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        textureView = (TextureView) findViewById(R.id.texture_view);
        angleImageView = (ImageView) findViewById(R.id.angle_image);
        dateTimeTextView = (TextView) findViewById(R.id.datetime_label);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        registerSensorIfAvailable(accelerometer);
        registerSensorIfAvailable(lightSensor);
    }

    private void registerSensorIfAvailable(Sensor sensor) {
        if(sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.d("[PLM]","Sensor not found.");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mSensor = sensorEvent.sensor;

        if(mSensor.getType() == Sensor.TYPE_LIGHT) {
            DecimalFormat df = new DecimalFormat("##");
            lightTextView.setText("Light: " + df.format(sensorEvent.values[0] * 0.092903));
        }

        if (mSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            DecimalFormat df = new DecimalFormat("##");
            Float rotationAngle = (Math.max(Math.abs(x), Math.abs(y)) * 10);

            if((x < 0 && Math.abs(x) > Math.abs(y)) || (y < 0 && Math.abs(y) > Math.abs(x))) {
                rotationAngle *= -1;
            }

//            if(rotationAngle > 180) {
//                rotationAngle = 180.0f;
//            }

            accelerometerTextView.setText("Angle: " + df.format( rotationAngle ));

            Matrix matrix = new Matrix();
            angleImageView.setScaleType(ImageView.ScaleType.MATRIX);
            matrix.postRotate( rotationAngle, angleImageView.getDrawable().getBounds().width()/2, angleImageView.getDrawable().getBounds().height()/2);
            angleImageView.setImageMatrix(matrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            start_camera();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private void start_camera()
    {
        if (checkCamera2Support()){

            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                String frontCameraId = new String();
                for (String cameraId : manager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
                    if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                        frontCameraId = cameraId;
                    }
                }
                if (frontCameraId == new String()) {
                    Log.e("[PLM]","No front facing camera found");
                    textureView.setVisibility(View.GONE);
                } else {
                    textureView.setVisibility(View.VISIBLE);
                }

                CameraCharacteristics characteristics = manager.getCameraCharacteristics(frontCameraId);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
                // Add permission for camera and let user grant the permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(main.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                    return;
                }
                manager.openCamera(frontCameraId, stateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            releaseCameraAndPreview();

            try{
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                surfaceView.setVisibility(View.VISIBLE);
            }catch(RuntimeException e){
                Log.e("log", "init_camera1: " + e);
                return;
            }
            Camera.Parameters param;
            param = camera.getParameters();
            //modify parameter
            param.setPreviewFrameRate(20);
            param.setPreviewSize(176, 144);
            camera.setParameters(param);
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                //camera.takePicture(shutter, raw, jpeg)
            } catch (Exception e) {
                Log.e("log", "init_camera2: " + e);
                return;
            }
        }
    }

    protected CameraDevice cameraDevice;

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.d("[PLM]", "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(main.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e("[PLM]", "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void releaseCameraAndPreview() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);

            try {
                camera.setPreviewDisplay(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.release();
            camera = null;
        }
    }
}
