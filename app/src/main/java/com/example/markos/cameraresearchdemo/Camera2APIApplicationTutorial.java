package com.example.markos.cameraresearchdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by korenos on 11/9/16.
 */

public class Camera2APIApplicationTutorial extends AppCompatActivity {

    private final int previewState = 0;
    private final int focusLock = 1;
    private int currentState = -1;

    private Size displaySize;
    private Size[] JPGimageSizes;
    private final int cameraPermissionRequest = 0;
    private TextureView displayLayout;
    private TextureView.SurfaceTextureListener displayListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            cameraOpen();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
    private CameraDevice camera;
    private CameraDevice.StateCallback cameraStateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            camera = cameraDevice;
            createPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            camera.close();
            camera = null;

        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            camera.close();
            camera= null;
        }
    };
    private CaptureRequest previewCaptureRequest;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession captureSession;
    private CameraCaptureSession.CaptureCallback sessionCallBack = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result){
            switch(currentState){
                case previewState:
                    break;
                case focusLock:
                    Integer state = result.get(CaptureResult.CONTROL_AF_STATE);
                    if(state == CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED){
                        /*Toast.makeText(getApplicationContext(), "Focus successful",Toast.LENGTH_SHORT).show();
                        unlockFocus();*/
                        captureImage();
                    }
                    break;
            }
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            process(result);
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);

            Toast.makeText(getApplicationContext(), "Focus unsuccessful",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }
    };
    private HandlerThread processThread;
    private Handler threadHandler;
    private static File file;
    private ImageReader imageReader;
    private final ImageReader.OnImageAvailableListener onImageAvalaible =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    threadHandler.post(new ImageSaver(imageReader.acquireNextImage()));
                }
            };

    private static class ImageSaver implements Runnable{

        private Image image;

        private ImageSaver(Image paImage){
            image = paImage;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);

            FileOutputStream fileOutputStream = null;
            file = new File(MediaLocationsAndSettings.getPhotoName());
            try {
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bytes);
            }catch (FileNotFoundException e){
                e.getStackTrace();
            }catch(IOException e){
                e.getStackTrace();
            }finally {
                image.close();
                if(fileOutputStream != null){
                    try {
                        fileOutputStream.close();
                    }catch (IOException e){
                        e.getStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera2_api_application_layout);

        displayLayout = (TextureView) findViewById(R.id.camera2DisplayLayout);
        displayLayout.setSurfaceTextureListener(displayListener);

        currentState = previewState;
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraClose();
        closeProcessThread();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cameraClose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraClose();
    }

    @Override
    protected void onResume() {
        super.onResume();

        openProcessThread();

        if(displayLayout.isAvailable()){
            cameraOpen();
        }else{
            displayLayout.setSurfaceTextureListener(displayListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (cameraPermissionRequest == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission declined.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void cameraOpen(){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraID = cameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            displaySize = map.getOutputSizes(SurfaceTexture.class)[0];
            JPGimageSizes = map.getOutputSizes(ImageFormat.JPEG);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, cameraPermissionRequest);
                return;
            }

            Size maxImageSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new Comparator<Size>() {
                        @Override
                        public int compare(Size size, Size t1) {
                            return Long.signum(size.getHeight()*size.getWidth()
                                    -t1.getHeight()*t1.getWidth());
                        }
                    });
            imageReader = imageReader.newInstance(maxImageSize.getWidth(),
                    maxImageSize.getHeight(), ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(onImageAvalaible,threadHandler);

            cameraManager.openCamera(cameraID, cameraStateCallBack, threadHandler);
        }catch (CameraAccessException e){
            Log.e("Error", "Camera exception");
            e.printStackTrace();
        }
    }

    private void cameraClose(){
        if(captureSession != null){
            captureSession.close();
            captureSession = null;
        }
        if(camera != null){
            camera.close();
            camera = null;
        }
        if(imageReader != null){
            imageReader.close();
            imageReader = null;
        }
    }

    private void createPreview(){
        try{
            SurfaceTexture surfaceTexture = displayLayout.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(displaySize.getWidth(), displaySize.getHeight());
            System.out.println(displaySize.getWidth()+", "+displaySize.getHeight());
            Surface surface = new Surface(surfaceTexture);
            captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            camera.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if(camera == null){
                        return;
                    }
                    try{
                        previewCaptureRequest = captureRequestBuilder.build();
                        captureSession = cameraCaptureSession;
                        captureSession.setRepeatingRequest(previewCaptureRequest, sessionCallBack, threadHandler);
                    }catch(CameraAccessException e){
                        e.getStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Log.e("Error", "Failed to create preview");
                }
            }, null);
        }catch(CameraAccessException e){
            e.getStackTrace();
        }
    }

    public void takePictureAction(View view){
        lockFocus();
        //image file = create
    }

    private void captureImage(){
        try {
            CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(imageReader.getSurface());

            int orientationValue = getWindowManager().getDefaultDisplay().getOrientation();
            builder.set(CaptureRequest.JPEG_ORIENTATION, MediaLocationsAndSettings.orientationCalculatior(orientationValue));
            CameraCaptureSession.CaptureCallback captureCallBack = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    Toast.makeText(getApplicationContext(), "Picture captured.", Toast.LENGTH_SHORT).show();
                    unlockFocus();
                }
            };

            captureSession.capture(builder.build(), captureCallBack, null);
        }catch (CameraAccessException e){
            e.getStackTrace();
        }
    }

    private void lockFocus(){
        try {
            captureRequestBuilder.set(previewCaptureRequest.CONTROL_AF_TRIGGER,
                    previewCaptureRequest.CONTROL_AF_TRIGGER_START);
            currentState = focusLock;
            captureSession.capture(captureRequestBuilder.build(), sessionCallBack, threadHandler);
        }catch (CameraAccessException e){
            e.getStackTrace();
        }
    }

    private void unlockFocus(){
        try {
            captureRequestBuilder.set(previewCaptureRequest.CONTROL_AF_TRIGGER,
                    previewCaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
            currentState = previewState;
            captureSession.capture(captureRequestBuilder.build(), sessionCallBack, threadHandler);
        }catch (CameraAccessException e){
            e.getStackTrace();
        }
    }

    private void openProcessThread(){
        processThread = new HandlerThread("Process thread");
        processThread.start();
        threadHandler = new Handler(processThread.getLooper());
    }

    private void closeProcessThread(){
        processThread.quitSafely();
        try{
            processThread.join();
            processThread = null;
            threadHandler = null;
        }catch(InterruptedException e){
            e.getStackTrace();
        }
    }
}