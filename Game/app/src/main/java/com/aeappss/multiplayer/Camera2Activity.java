/*package com.aeappss.multiplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
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
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aeappss.multiplayer.Model;
import com.aeappss.multiplayer.ModelSurfaceView;
import com.aeappss.multiplayer.ModelViewerApplication;
import com.aeappss.multiplayer.stl.StlModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

import static java.lang.Math.abs;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Activity extends AppCompatActivity implements SensorEventListener {

    private CameraDevice mCameraDevice = null;
    private CaptureRequest.Builder mCaptureRequestBuilder = null;
    private CameraCaptureSession mCameraCaptureSession  = null;
    private TextureView mTextureView = null;
    private Size mPreviewSize = null;

    private int maxThrowingForce = 0;

    private SensorManager senSensorManager;
    private ProgressBar firstBar = null;
    private Sensor senAccelerometer;

    private TextView textViewThrowing;
    private Button throwingButton;

    private long lastUpdate = 0;
    private float last_x = 0, last_y = 0, last_z = 0;
    private boolean pressedThrow = false;

    private Bitmap[] mSpots, mBlips;
    private Bitmap mRadar;
    Paint mPaint = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);


        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        firstBar = (ProgressBar)findViewById(R.id.firstBar);


        textViewThrowing = (TextView) findViewById(R.id.textViewThrowing);
        throwingButton = (Button) findViewById(R.id.throwingButton);
        throwingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                firstBar.setVisibility(View.VISIBLE);
                firstBar.setMax(35);
                firstBar.setProgress(0);
                //firstBar.setProgress(3);
                throwingButton.setVisibility(View.INVISIBLE);
                textViewThrowing.setVisibility(View.INVISIBLE);
                pressedThrow = true;
            }
        });

        mTextureView = (TextureView) findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

        final FloatingActionButton mapAction = (FloatingActionButton) findViewById(R.id.action_map);
        mapAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /// MAP INTENT
                //Toast.makeText(Camera2Activity.this, "AS CIA", Toast.LENGTH_LONG).show();
                Intent homeIntent = new Intent(Camera2Activity.this, MapsActivity.class);
                startActivity(homeIntent);
            }
        });
    }

    static class CompareSizesByArea implements Comparator<Size> {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            if (texture == null) {
                return;
            }
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);
            try {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            } catch (CameraAccessException e){
                e.printStackTrace();
            }
            mCaptureRequestBuilder.addTarget(surface);
            try {
                mCameraDevice.createCaptureSession(Arrays.asList(surface), mPreviewStateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onError(CameraDevice camera, int error) {}
        @Override
        public void onDisconnected(CameraDevice camera) {

        }
    };

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }
    };

    private CameraCaptureSession.StateCallback mPreviewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            startPreview(session);
        }
        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        //senSensorManager.unregisterListener(this);

    }
    @Override
    public void onResume() {
        super.onResume();
        if (mTextureView.isAvailable()) {
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(
                    mSurfaceTextureListener);
        }

        //senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try{
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class) [0];
            manager.openCamera(cameraId, mStateCallback, null);
        } catch(CameraAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void startPreview(CameraCaptureSession session) {
        mCameraCaptureSession = session;
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread backgroundThread = new HandlerThread("CameraPreview");
        backgroundThread.start();
        Handler backgroundHandler = new Handler(backgroundThread. getLooper());
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    ///// ACCELEROMETER SPEED
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    float maxDown = 0;
    boolean doneDown = false;
    boolean doneUp = false;
    boolean was = false;
    boolean was1 = false;
    float downX = 0;
    float upX = 0;
    long curTimeThrow;
    long allThrowingTime;
    float accelerometerDistance;
    float accelerometerSpeed;
    boolean throwUp = false;
    boolean wasUp = false;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            // 1 budas
            if(last_x > x && (last_x <= 0 && x <= 0 && last_x - x > 0.5) && !doneDown && pressedThrow && !throwUp && !wasUp){ // atgal
                    firstBar.setProgress(Math.abs(Math.round(Math.abs(x))));
                    lastUpdate = curTime;
                    was = true;
                    last_x = x;
                    last_y = y;
                    last_z = z;
            } // priesingu atveju jei padaugeja paklaida einant i kita puse, nustojama, issaugomi duomenys ir vykdoma i prieki
            else if(last_x < x && (last_x <= 0 && x <= 0 && (abs(last_x) - abs(x) > 2) || abs(last_x) - abs(x) > 0.5 && (curTime/1000 - lastUpdate/1000 > 0.75)) && !doneDown && was && pressedThrow && !throwUp && !wasUp){   // laika iki 0.5 gal sumazint???
                accelerometerSpeed = (abs(last_x) + abs(last_y) + abs(last_z));  // ar nereikejo last_x vetoj x ir kitu likusiu?
                lastUpdate = curTime;
                doneDown = true;
                downX = last_x;
                curTimeThrow = System.currentTimeMillis();   // nustatome laika, kai pasiekiame galines koord
            }

            if(doneDown && last_x < x && abs(last_x) - abs(x) > 0.5 && abs(last_x) - abs(x) < 2 && !doneUp && pressedThrow && !throwUp && !wasUp){
                lastUpdate = curTime;
                was1 = true;
                last_x = x;
            }else if(abs(x) - abs(last_x) > 5 && !doneUp && was1 && pressedThrow && !throwUp && !wasUp){
                lastUpdate = curTime;
                doneUp = true;
                upX = last_x;
                allThrowingTime = System.currentTimeMillis() - curTimeThrow;
            }else if((curTime/1000 - lastUpdate/1000 > 0.75) && !doneUp && was1 && pressedThrow && !throwUp && !wasUp){ // laukiama 0.75 sek
                lastUpdate = curTime;
                doneUp = true;
                upX = last_x;
                allThrowingTime = System.currentTimeMillis() - curTimeThrow;
            }

            if(doneDown && doneUp && !wasUp){
                accelerometerDistance = abs(downX) + abs(upX);
                Log.i("PASUKIMAS ", "GREITIS " + accelerometerSpeed + "m/s");
                Toast.makeText(this, "GREITIS " + accelerometerSpeed, Toast.LENGTH_LONG).show();
                pressedThrow = false;
                doneDown = false;
                doneUp = false;
                firstBar.setVisibility(View.INVISIBLE);
                throwingButton.setVisibility(View.VISIBLE);
                last_x = 0;
            }
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

}*/

package com.aeappss.multiplayer;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
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
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.xw.repo.BubbleSeekBar;

import java.util.Arrays;
import java.util.Comparator;

import android.content.Intent;

import static java.lang.Math.abs;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Activity extends AppCompatActivity implements SensorEventListener {

    private CameraDevice mCameraDevice = null;
    private CaptureRequest.Builder mCaptureRequestBuilder = null;
    private CameraCaptureSession mCameraCaptureSession  = null;
    private TextureView mTextureView = null;
    private Size mPreviewSize = null;

    private int maxThrowingForce = 0;

    private SensorManager senSensorManager;
    private ProgressBar firstBar = null;
    private Sensor senAccelerometer;

    private TextView textViewThrowing;
    //private Button throwingButton;
    private ImageButton imageButton; //  ball
    private TextView ballCounterText;
    //private TextView ballCounterText1;

    private long lastUpdate = 0;
    private float last_x = 0, last_y = 0, last_z = 0;
    private boolean pressedThrow = false;

    private int heartNum = 3;
    private ImageView heart1;
    private ImageView heart2;
    private ImageView heart3;

    private ImageButton mapButton;
    private ImageButton addButton;
    //private ImageView personView;
    private float[] mValues = new float[3];
    RelativeLayout.LayoutParams params;
    //ImageView person;
    ImageView blip;
    RelativeLayout rlMain;

    Dialog epicDialog;
    TextView titleTv, messageTv;
    ImageView closePopupPositiveImg, closePopupNegativeImg;
    Button positiveButton, negativeButton, buttonAccept, buttonRetry;
    int correctShoot = 0;
    TextView scoreText;
    int ballNum;
    BubbleSeekBar bubbleSeekBar;
    TextView seekbarTextView;
    ImageButton changeLocationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

        Typeface myFont = Typeface.createFromAsset(getAssets(), "fonts/rocko.ttf");

        bubbleSeekBar = (BubbleSeekBar) findViewById(R.id.seekBar);
        seekbarTextView = (TextView) findViewById(R.id.txtView);
        seekbarTextView.setTypeface(myFont);
        changeLocationBtn = (ImageButton) findViewById(R.id.changeLocationBtn);

        changeLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bubbleSeekBar.setVisibility(View.VISIBLE);
                bubbleSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress, float progressFloat) {
                        seekbarTextView.setText((String.valueOf(progressFloat)) + " m");
                        MAX_THROW_DISTANCE = progressFloat;
                    }

                    @Override
                    public void getProgressOnActionUp(int progress, float progressFloat) {
                        seekbarTextView.setText((String.valueOf(progressFloat)) + " m");
                        MAX_THROW_DISTANCE = progressFloat;
                        android.os.SystemClock.sleep(1000);
                        bubbleSeekBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void getProgressOnFinally(int progress, float progressFloat) {
                        seekbarTextView.setText(String.format("P"));
                    }
                });
            }
        });


        epicDialog = new Dialog(this);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        firstBar = (ProgressBar)findViewById(R.id.firstBar);

        textViewThrowing = (TextView) findViewById(R.id.textViewThrowing);

        heart1 = (ImageView) findViewById(R.id.heart1);
        heart2 = (ImageView) findViewById(R.id.heart2);
        heart3 = (ImageView) findViewById(R.id.heart3);
        ballCounterText = (TextView) findViewById(R.id.textView);
        ballCounterText.setTypeface(myFont);
        //ballCounterText1 = (TextView) findViewById(R.id.textView1);

        //
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ballNum = Integer.valueOf((String) ballCounterText.getText());
                if(ballNum > 0){
                    --ballNum;
                    ballCounterText.setText(String.valueOf(ballNum));
                    //ballCounterText1.setText(String.valueOf(ballNum));
                    firstBar.setVisibility(View.VISIBLE);
                    firstBar.setMax(35);
                    firstBar.setProgress(0);
                    //firstBar.setProgress(3);
                    imageButton.setVisibility(View.INVISIBLE);
                    ballCounterText.setVisibility(View.INVISIBLE);
                    textViewThrowing.setVisibility(View.INVISIBLE);
                    pressedThrow = true;
                    ////////////////////////////////////////////////
                    //
                    if (heartNum == 3){
                        heartNum--;
                        heart1.setVisibility(View.GONE);
                    }else if (heartNum == 2){
                        heartNum--;
                        heart2.setVisibility(View.GONE);
                    }else if (heartNum == 1){
                        heartNum--;
                        heart3.setVisibility(View.GONE);
                    }else{
                        //
                    }
                    ///////////////////////////////////////////////
                }

                if(ballNum == 0){
                    imageButton.setClickable(false);
                    if(correctShoot >= 1){
                        ShowPositivePopup();
                        correctShoot = 0;
                    }else{
                        ShowNegativePopup();
                        correctShoot = 0;
                    }

                }
            }
        });



        mTextureView = (TextureView) findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

        final FloatingActionButton mapAction = (FloatingActionButton) findViewById(R.id.action_map1);
        mapAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /// MAP INTENT
                Intent homeIntent = new Intent(Camera2Activity.this, MapsActivity.class);
                startActivity(homeIntent);
            }
        });

    }

    public void ShowPositivePopup(){
        epicDialog.setContentView(R.layout.epic_popup_positive);
        closePopupPositiveImg = (ImageView) epicDialog.findViewById(R.id.closePopupPositiveImg);
        buttonAccept = (Button) epicDialog.findViewById(R.id.btnAccept);
        titleTv = (TextView) epicDialog.findViewById(R.id.titleTv);
        messageTv = (TextView) epicDialog.findViewById(R.id.messageTv);
        scoreText = (TextView) epicDialog.findViewById(R.id.scoreText);
        scoreText.setText("+" + correctShoot + "/" + 10 + " ball(s)");

        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();finish();
            }
        });

        closePopupPositiveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();
            }
        });
        epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        epicDialog.show();
        MainActivity.addBalls(correctShoot);

    }

    public void ShowNegativePopup(){
        epicDialog.setContentView(R.layout.epic_popup_negative);
        closePopupNegativeImg = (ImageView) epicDialog.findViewById(R.id.closePopupNegativeImg);
        buttonRetry = (Button) epicDialog.findViewById(R.id.btnRetry);
        titleTv = (TextView) epicDialog.findViewById(R.id.titleTv);
        messageTv = (TextView) epicDialog.findViewById(R.id.messageTv);
        buttonRetry = (Button) epicDialog.findViewById(R.id.btnRetry);

        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correctShoot = 0;
                ballNum = 10;
                epicDialog.dismiss();
                imageButton.setClickable(true);
                ballCounterText.setText(String.valueOf(ballNum));
            }
        });

        closePopupNegativeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();
            }
        });
        epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        epicDialog.show();
    }

    static class CompareSizesByArea implements Comparator<Size> {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            if (texture == null) {
                return;
            }
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);
            try {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            } catch (CameraAccessException e){
                e.printStackTrace();
            }
            mCaptureRequestBuilder.addTarget(surface);
            try {
                mCameraDevice.createCaptureSession(Arrays.asList(surface), mPreviewStateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onError(CameraDevice camera, int error) {}
        @Override
        public void onDisconnected(CameraDevice camera) {

        }
    };

    private double screenWidth;
    private double screenHeight;

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //double angle = bearing(54.9901267, 25.779396899999938, 54.9823894, 25.76502240000002);
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }
    };

    // AZIMUTAS
    protected static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double longDiff = Math.toRadians(lon2 - lon1);
        double la1 = Math.toRadians(lat1);
        double la2 = Math.toRadians(lat2);
        double y = Math.sin(longDiff) * Math.cos(la2);
        double x = Math.cos(la1) * Math.sin(la2) - Math.sin(la1) * Math.cos(la2) * Math.cos(longDiff);

        double result = Math.toDegrees(Math.atan2(y, x));
        return (result+360.0d)%360.0d;
    }

    private CameraCaptureSession.StateCallback mPreviewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            startPreview(session);
        }
        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        //senSensorManager.unregisterListener(this);

    }
    @Override
    public void onResume() {
        super.onResume();
        if (mTextureView.isAvailable()) {
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(
                    mSurfaceTextureListener);
        }

        //senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try{
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class) [0];
            manager.openCamera(cameraId, mStateCallback, null);
        } catch(CameraAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void startPreview(CameraCaptureSession session) {
        mCameraCaptureSession = session;
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread backgroundThread = new HandlerThread("CameraPreview");
        backgroundThread.start();
        Handler backgroundHandler = new Handler(backgroundThread. getLooper());
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    float maxDown = 0;
    boolean doneDown = false;
    boolean doneUp = false;
    boolean was = false;
    boolean was1 = false;
    float downX = 0;
    float upX = 0;
    long curTimeThrow;
    long allThrowingTime;
    float accelerometerDistance;
    float accelerometerSpeed;
    boolean throwUp = false;
    boolean wasUp = false;
    float MAX_THROW_DISTANCE = 200; // max throwing distance in game
    float MAX_DEVICE_SPEED; // max device range * max device speed coefficient
    int MAX_DEVICE_SPEED_COEFF = 2; // max device range * 2
    int THROW_SPEED_WITH_COEFFICIENT;  // received speed * coefficient
    float MAX_DEVICE_ERROR; // paklaida
    float myDeviceMinError = (float) 0.882; // creator's device
    float myDeviceMaxError = (float) 10.95707; // creator's device
    float myDeviceMaxSpeed = 64; // creator's device
    float myDeviceMinSpeed = (float) 10.411; // creator's device
    float coeff; //
    float errorSum; // paklaidu suma min + max
    float ERROR;
    float accelerometerMinSpeed; // with ERRROR
    float accelerometerMaxSpeed; // with ERRROR
    float distanceBetweenMyOpponent; // rasti kas yra toje kriptyje, imti kuris yra arciausiai arba klausi zaidejo i kuri taikomasi
    float throwingMinDistance; // atstumas paskaiciuotas taip, kad max negaletu buti daugiau nei 200 (MAX_THROW_DISTANCE)
    float throwingMaxDistance;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();
            long myTime = 0;


            // 1
            if(last_x > x && (last_x <= 0 && x <= 0 && last_x - x > 0.5) && !doneDown && pressedThrow && !throwUp && !wasUp){ // atgal
                firstBar.setProgress(Math.abs(Math.round(Math.abs(x))));
                lastUpdate = curTime;
                was = true;
                last_x = x;
                last_y = y;
                last_z = z;
            } // priesingu atveju jei padaugeja paklaida einant i kita puse, nustojama, issaugomi duomenys ir vykdoma i prieki
            else if(last_x < x && (last_x <= 0 && x <= 0 && (abs(last_x) - abs(x) > 2) || abs(last_x) - abs(x) > 0.5 && (curTime/1000 - lastUpdate/1000 > 0.75)) && !doneDown && was && pressedThrow && !throwUp && !wasUp){   // laika iki 0.5 gal sumazint???
                accelerometerSpeed = (abs(last_x) + abs(last_y) + abs(last_z));  // ar nereikejo last_x vetoj x ir kitu likusiu?
                lastUpdate = curTime;
                doneDown = true;
                downX = last_x;
                curTimeThrow = System.currentTimeMillis();   // nustatome laika, kai pasiekiame galines koord
            }

            if(doneDown && last_x < x && abs(last_x) - abs(x) > 0.5 && abs(last_x) - abs(x) < 2 && !doneUp && pressedThrow && !throwUp && !wasUp){
                lastUpdate = curTime;
                was1 = true;
                last_x = x;
            }else if(abs(x) - abs(last_x) > 5 && !doneUp && was1 && pressedThrow && !throwUp && !wasUp){
                lastUpdate = curTime;
                doneUp = true;
                upX = last_x;
                allThrowingTime = System.currentTimeMillis() - curTimeThrow;
            }else if((curTime/1000 - lastUpdate/1000 > 0.75) && !doneUp && was1 && pressedThrow && !throwUp && !wasUp){ // laukiama 0.75 sek
                lastUpdate = curTime;
                doneUp = true;
                upX = last_x;
                allThrowingTime = System.currentTimeMillis() - curTimeThrow;
            }

            if(doneDown && doneUp && !wasUp){
                accelerometerDistance = abs(downX) + abs(upX);
                Log.i("PASUKIMAS ", "GREITIS " + accelerometerSpeed + "m/s");
                //Toast.makeText(this, "GREITIS " + accelerometerSpeed, Toast.LENGTH_LONG).show();
                pressedThrow = false;
                doneDown = false;
                doneUp = false;
                firstBar.setVisibility(View.INVISIBLE);
                //throwingButton.setVisibility(View.VISIBLE);
                imageButton.setVisibility(View.VISIBLE);
                ballCounterText.setVisibility(View.VISIBLE);
                last_x = 0;
                // CALCULATED or hit
                MAX_DEVICE_SPEED = mySensor.getMaximumRange() * 2;
                Log.i("METIMAS", "MAX_DEVICE_SPEED = " + String.valueOf(MAX_DEVICE_SPEED));
                MAX_DEVICE_ERROR = (myDeviceMaxError * mySensor.getMaximumRange() * 2) / myDeviceMaxSpeed; // kiekvieno irenginio max paklaida randama
                // min device error not found for all devices
                Log.i("METIMAS", "MAX_DEVICE_ERROR = " + String.valueOf(MAX_DEVICE_ERROR));
                coeff = (myDeviceMinSpeed + MAX_DEVICE_SPEED) / accelerometerSpeed;
                errorSum = myDeviceMinError + MAX_DEVICE_ERROR;
                ERROR = errorSum / coeff;
                // speed bounds
                accelerometerMinSpeed = accelerometerSpeed - ERROR;
                accelerometerMaxSpeed = accelerometerSpeed + ERROR;

                throwingMinDistance = (MAX_THROW_DISTANCE * accelerometerMinSpeed) / MAX_DEVICE_SPEED;
                throwingMaxDistance = (MAX_THROW_DISTANCE * accelerometerMaxSpeed) / MAX_DEVICE_SPEED;

                // patikrinti ar neateina cia
                if (throwingMinDistance > MAX_THROW_DISTANCE){
                    throwingMinDistance = MAX_THROW_DISTANCE;
                }else if (throwingMaxDistance > MAX_THROW_DISTANCE){
                    throwingMaxDistance = MAX_THROW_DISTANCE;
                }

                //
                distanceBetweenMyOpponent = 100;  // atstumas turi irgi paklaida
                if (throwingMinDistance <= distanceBetweenMyOpponent && throwingMaxDistance >= distanceBetweenMyOpponent){
                    correctShoot++;
                    // print info. oppenent is shoot
                    Log.i("METIMAS", "Pataikyta min = " + throwingMinDistance + ", max = " + throwingMaxDistance);
                    Log.i("METIMAS", "Pataikyta tikrasis greitis " + accelerometerSpeed);
                    /*textViewThrowing.setVisibility(View.VISIBLE);
                    textViewThrowing.setText("Pataikyta \nmin = " + throwingMinDistance + ", \nmax = " + throwingMaxDistance + "\n" +
                            "tikrasis greitis " + accelerometerSpeed + "\n Atstumu skirtumas = " + distanceBetweenMyOpponent);*/
                }else{
                    // print info. not shoot
                    Log.i("METIMAS", "Nepataikyta min = " + throwingMinDistance + ", max = " + throwingMaxDistance);
                    Log.i("METIMAS", "Nepataikyta tikrasis greitis " + accelerometerSpeed);
                    /*textViewThrowing.setVisibility(View.VISIBLE);
                    textViewThrowing.setText("Nepataikyta \nmin = " + throwingMinDistance + ", \nmax = " + throwingMaxDistance + "\n" +
                            "tikrasis greitis " + accelerometerSpeed + "\n Atstumu skirtumas = " + distanceBetweenMyOpponent);*/


                    if(ballNum == 0){
                        // galbut zaidimo eigoje gaus kamuoliu daugiau, tada atsetinti
                        imageButton.setClickable(false);
                        if(correctShoot >= 1){
                            ShowPositivePopup();
                            correctShoot = 0;
                        }else{
                            ShowNegativePopup();
                            correctShoot = 0;
                        }

                    }
                }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void changeScreen() {
        setContentView(R.layout.activity_main);
        findViewById(R.id.screen_menu).setVisibility(R.id.screen_menu == R.id.screen_menu ? View.VISIBLE : View.GONE);
    }

    Dialog exitDialog;
    ImageView closePopupImg;
    ImageButton buttonCheck;
    ImageButton closeButton;
    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        exitDialog = new Dialog(this);
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            exitDialog.setContentView(R.layout.exit_dialog_layout);
            closePopupImg = (ImageView) exitDialog.findViewById(R.id.closeImg);
            closeButton = (ImageButton) exitDialog.findViewById(R.id.closeButton);
            buttonCheck = (ImageButton) exitDialog.findViewById(R.id.checkButton);
            buttonCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exitDialog.dismiss();
                    System.exit(0);
                }
            });

            closePopupImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exitDialog.dismiss();
                }
            });

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exitDialog.dismiss();
                }
            });
            exitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            exitDialog.show();

            //leaveRoom();
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

}