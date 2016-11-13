package com.example.markos.cameraresearchdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Markos on 13. 10. 2016.
 */

public class CameraBuildIn extends AppCompatActivity{

    private Camera camera = null;
    private CameraView cameraShow;
    private FrameLayout displayArea;
    private int zoomLevel = 0;
    private int zoomStep = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_camera);

        if(!checkCameraDevice(getApplicationContext())){
            Toast.makeText(getApplicationContext(), "There is not camera device.", Toast.LENGTH_SHORT).show();
            Log.e("Error", "There is not camera device.");
            return;
        }

        displayArea = (FrameLayout) findViewById(R.id.cameraViewContainer);
        restartCamera();

        Button button = (Button) findViewById(R.id.lightButton);
        Camera.Parameters param = camera.getParameters();
        if(param.getFlashMode().compareTo(Camera.Parameters.FLASH_MODE_OFF)==0){
            button.setText("Turn On light");
        }else{
            button.setText("Turn Off light");
        }

        zoomLevel = param.getZoom();
        camera.setParameters(param);
    }

    @Override
    protected void onPause() {
        super.onPause();
        turnOffCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        turnOffCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        turnOffCamera();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void takeCameraPictureAction(View view) throws IOException {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                try {
                    String path = MediaLocationsAndSettings.getPhotoName();
                    if(path == null){
                        Log.e("Error", "Not possible to find proper photo name.");
                        return;
                    }
                    FileOutputStream fos = new FileOutputStream(path);
                    fos.write(bytes);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.e("Error", "File not found: " + e.getMessage());
                    return;
                } catch (IOException e) {
                    Log.e("Error", "Error accessing file: " + e.getMessage());
                    return;
                }
                restartCamera();
            }
        });
    }

    private boolean checkCameraDevice(Context context){
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        }else{
            return false;
        }
    }

    private boolean bindCameraInstance() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        try {
            camera = Camera.open();
            return true;
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "Failed to open Camera.");
            e.printStackTrace();
            return false;
        }
    }

    private void turnOffCamera(){
        if(camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void restartCamera(){
        turnOffCamera();
        if(!bindCameraInstance()){
            return;
        }
        cameraShow = new CameraView(this, camera, true);
        displayArea.removeAllViews();
        displayArea.addView(cameraShow);
    }

    public void turnOnLight(View view){
        Button button = (Button) findViewById(R.id.lightButton);
        Camera.Parameters param = camera.getParameters();
        if(param.getFlashMode().compareTo(Camera.Parameters.FLASH_MODE_OFF)==0){
            param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            button.setText("Turn Off light");
        }else{
            param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            button.setText("Turn On light");
        }

        camera.setParameters(param);
    }


    public void zoomIn(View view){
        Camera.Parameters param = camera.getParameters();
        int maxZoom;
        if(param.isZoomSupported()){
            maxZoom = param.getMaxZoom();

            if(zoomLevel+zoomStep >= maxZoom){
                zoomLevel = maxZoom;
            }else{
                zoomLevel += zoomStep;
            }
            param.setZoom(zoomLevel);
        }
        camera.setParameters(param);
    }

    public void zoomOut(View view){
        Camera.Parameters param = camera.getParameters();
        if(param.isZoomSupported()){
            if(zoomLevel-zoomStep <= 0){
                zoomLevel = 0;
            }else{
                zoomLevel -= zoomStep;
            }
            param.setZoom(zoomLevel);
        }
        camera.setParameters(param);
    }
}
