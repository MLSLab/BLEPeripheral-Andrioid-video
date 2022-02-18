package com.example.camera2videowithble;

import static com.example.camera2videowithble.Constants.REQUEST_ENABLE_BT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private Size mPreviewSize;

    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private TextureView mTextureView;

    private int mTotalRotation;
    private Size mVideoSize;
    private MediaRecorder mMediaRecorder;
    private CaptureRequest.Builder mCaptureRequestBuilder;

    private Button mRecordButton;
    private boolean mIsRecording = false;

    private File mVideoFolder;
    private String mVideoFilename;

    // connected devices
    private ArrayList<BluetoothDevice> mConnectedDevices;
    private ArrayAdapter<BluetoothDevice> mConnectedDevicesAdapter;

    private TextView txView1;
    private ScrollView scrollView;

    private Button orientationLeftButton;
    private Button orientationCenterButton;
    private Button orientationRightButton;

    private int customOrientation;



    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
            setupCamera(i, i1);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

        }
    };

    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            if(mIsRecording) {
                try {
                    createVideoFileName();
                } catch (IOException e){
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
            } else {
                //startPreview();
            }
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private String mCameraId;
    private static SparseIntArray ORIENTATONS = new SparseIntArray();
    static {
        ORIENTATONS.append(Surface.ROTATION_0, 0);
        ORIENTATONS.append(Surface.ROTATION_90, 90);
        ORIENTATONS.append(Surface.ROTATION_180, 180);
        ORIENTATONS.append(Surface.ROTATION_270, 270);
    }

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs){
            return Long.signum((long) lhs.getWidth() * rhs.getHeight() /
                    (long) rhs.getWidth() * lhs.getHeight());
        }

    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createVideoFolder();


        mMediaRecorder = new MediaRecorder();

        mTextureView = (TextureView) findViewById(R.id.textureView);


        // Bluetooth Setting
        // 연결된 목록을 보여준다
        mConnectedDevices = new ArrayList<BluetoothDevice>();
        mConnectedDevicesAdapter = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, mConnectedDevices);


        txView1 = (TextView) findViewById(R.id.logView);

        scrollView = (ScrollView)findViewById(R.id.scrollView2) ;


        initServer();





        mRecordButton = (Button) findViewById(R.id.record);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsRecording){
                    Toast.makeText(getApplicationContext(), "녹화종료", Toast.LENGTH_SHORT).show();
                    mIsRecording = false;
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    startPreview();
                } else {
                    checkWriteStoragePermission();
                }
            }
        });

        customOrientation = 90;

        orientationCenterButton = (Button) findViewById(R.id.orientation_center);
        orientationCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customOrientation = 90;

                orientationCenterButton.setBackgroundTintList(getResources().getColorStateList(R.color.mandarin));
                orientationLeftButton.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
                orientationRightButton.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
            }
        });

        orientationLeftButton = (Button) findViewById(R.id.orientation_left);
        orientationLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customOrientation = 0;

                orientationCenterButton.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
                orientationLeftButton.setBackgroundTintList(getResources().getColorStateList(R.color.mandarin));
                orientationRightButton.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
            }
        });

        orientationRightButton = (Button) findViewById(R.id.orientation_right);
        orientationRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customOrientation = 180 ;

                orientationCenterButton.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
                orientationLeftButton.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
                orientationRightButton.setBackgroundTintList(getResources().getColorStateList(R.color.mandarin));
            }
        });


    }

    @Override
    protected void onResume(){
        super.onResume();

        startBackgroundThread();

        if(mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();
        }  else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not run without camera services", Toast.LENGTH_SHORT).show();
            }

            if(grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not have audio on record", Toast.LENGTH_SHORT).show();
            }

            if(grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "App needs to save video to run", Toast.LENGTH_SHORT).show();
            }

            if(grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "App needs to find your location", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onPause() {
        closeCamera();

        stopBackgroundThread();
        super.onPause();

    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            for(String CameraId: cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(CameraId);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }

                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if(swapRotation){
                    rotatedWidth = height;
                    rotatedHeight = width;
                }

                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                mCameraId = CameraId;

                return;

            }
        } catch (CameraAccessException e){
            e.printStackTrace();
        }

    }

    private void connectCamera() {

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED){

                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                } else {
                    if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                        Toast.makeText(MainActivity.this, "Video app required access to camera", Toast.LENGTH_SHORT).show();
                    }

                    requestPermissions(
                            new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION
                            },
                            REQUEST_CAMERA_PERMISSION_RESULT);
                }

            } else {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startRecord() {

        try {
            setupMediaRecorder();
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {


                            try {
                                cameraCaptureSession.setRepeatingRequest(
                                        mCaptureRequestBuilder.build(), null, null
                                );
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                        }
                    }, mBackgroundHandler);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            try {
                                cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(getApplicationContext(), "Unable to setup camera preview", Toast.LENGTH_SHORT).show();
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if(mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Camera2VideoImage");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for(Size option : choices){
            if(option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height){
                bigEnough.add(option);
            }
        }
        if(bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }

    private void createVideoFolder() {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFolder = new File(movieFile, "camera2VideoImage");
        if(!mVideoFolder.exists()){
            mVideoFolder.mkdirs();
        }
    }

    private File createVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //String prepend = "VIDEO_" + timestamp + "_";

        File videoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "/camera2VideoImage/" + timestamp+".mp4");

        //File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);

        mVideoFilename = videoFile.getAbsolutePath();
        Log.i("TAG", mVideoFilename);
        return videoFile;
    }

    private void checkWriteStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;
                try {
                    createVideoFileName();
                } catch  (IOException e){
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            mIsRecording = true;
            try {
                createVideoFileName();
            } catch  (IOException e){
                e.printStackTrace();
            }
            startRecord();
            mMediaRecorder.start();

        }
    }

    private void setupMediaRecorder() throws IOException{
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFilename);
        mMediaRecorder.setVideoEncodingBitRate(50000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(customOrientation);
        mMediaRecorder.prepare();
    }


    ////////////////////// BLUETOOTH //////////////////////

    /**
     * Gatt Server 시작.
     * Peripheral Callback 을 셋팅해준다.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initServer() {
        PeripheralManager.getInstance(MainActivity.this).setCallBack(peripheralCallback);
        PeripheralManager.getInstance(MainActivity.this).initServer();
    }

    private void closeServer() {
        PeripheralManager.getInstance(MainActivity.this).close();
    }

    /**
     * 불루투스 기능을 켠다.
     */
    private void requestEnableBLE() {
        Intent ble_enable_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(ble_enable_intent, REQUEST_ENABLE_BT);
    }

    /**
     * 텍스트뷰에 메세지를 표시한다.
     * @param message
     */
    private void showStatusMsg(final String message) {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Date now = new Date();
                SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
                String nowStr = "[ "+format1.format(now)+" ] ";

                String oldMsg = txView1.getText().toString();
                txView1.setText(oldMsg + "\n" + nowStr + message);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });

            }
        };
        handler.sendEmptyMessage(1);
    }

    /**
     * 토스트를 표시한다.
     * @param message
     */
    private void showToast(final String message) {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        };
        handler.sendEmptyMessage(1);
    }

    /**
     * Peripheral Callback'
     */

    PeripheralCallback peripheralCallback = new PeripheralCallback() {
        @Override
        public void requestEnableBLE() {
            MainActivity.this.requestEnableBLE();
        }

        @Override
        public void onStatusMsg(String message) {
            showStatusMsg(message);



            if (message.equals("1")) {
                if(mIsRecording == false) {
                    checkWriteStoragePermission();
                    showToast("녹화시작");
                }
            }

            if (message.equals("0")){
                if(mIsRecording == true) {
                    mIsRecording = false;
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    startPreview();
                    showToast("녹화종료");
                }
            }
        }

        @Override
        public void onToast(String message) {
            showToast(message);
        }

    };






}