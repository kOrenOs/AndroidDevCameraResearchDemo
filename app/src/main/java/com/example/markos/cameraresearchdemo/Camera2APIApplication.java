package com.example.markos.cameraresearchdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.Arrays;

/**
 * Created by korenos on 08/11/16.
 */

public class Camera2APIApplication extends AppCompatActivity{

    private final int cameraPermissionRequest = 0;
    private TextureView displayLayout;
    private CameraDevice camera;
    private String cameraID;
    private CaptureRequest captureRequest;
    private CaptureRequest.Builder requestBuilder;
    private CameraCaptureSession captureSession;
    private Size displaySize;

    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            camera = cameraDevice;
            createPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            camera.close();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            camera.close();
            camera = null;
        }
    };

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setUpCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera2_api_application_layout);

        displayLayout = (TextureView) findViewById(R.id.camera2DisplayLayout);
        displayLayout.setSurfaceTextureListener(textureListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission declined.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(camera != null) {
            camera.close();
        }
    }

    private void setUpCamera(){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraID = cameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            displaySize = map.getOutputSizes(SurfaceTexture.class)[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Camera2APIApplication.this, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, cameraPermissionRequest);
                return;
            }
            cameraManager.openCamera(cameraID, cameraStateCallback, null);
        }catch (CameraAccessException e){
            Log.e("Error", "Camera exception");
            e.printStackTrace();
        }
    }

    private void createPreview(){
        try {
            SurfaceTexture texture = displayLayout.getSurfaceTexture();
            texture.setDefaultBufferSize(displaySize.getWidth(), displaySize.getHeight());
            Surface surface = new Surface(texture);
            requestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            requestBuilder.addTarget(surface);
            camera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == camera) {
                        return;
                    }
                    captureSession = cameraCaptureSession;
                    previewUpdate();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.e("Error", "Configuration change");
                }
            }, null);
        }catch (CameraAccessException e){
            Log.e("Error", "Unable to access camera");
            e.getStackTrace();
        }
    }

    private void previewUpdate(){
        if (null == camera) {
            Log.e("Error", "updatePreview error, return");
        }
        requestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            captureSession.setRepeatingRequest(requestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
