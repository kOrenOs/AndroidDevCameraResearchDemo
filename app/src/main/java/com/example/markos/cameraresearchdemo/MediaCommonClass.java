package com.example.markos.cameraresearchdemo;


import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Created by Markos on 13. 11. 2016.
 */

public class MediaCommonClass extends AppCompatActivity{

    protected Camera camera = null;
    protected CameraView cameraShow;
    protected CameraSides cameraActiveSide;
    private FrameLayout displayArea;
    private int zoomLevel = 0;
    private int zoomStep = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_layout);

        if(!checkCameraDevice(getApplicationContext())){
            Toast.makeText(getApplicationContext(), "There is not camera device.", Toast.LENGTH_SHORT).show();
            Log.e("Error", "There is not camera device.");
            return;
        }

        displayArea = (FrameLayout) findViewById(R.id.cameraViewContainer);

        cameraActiveSide = CameraSides.back;
        startPreview(findCameraCode(cameraActiveSide));

        mediaFunctions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraClose();
        cameraShow = null;
        displayArea.removeAllViews();
    }

    public void turnOnLight(View view){
        Button button = (Button) findViewById(R.id.lightButton);
        Camera.Parameters param = camera.getParameters();
        if(param.getFlashMode().compareTo(Camera.Parameters.FLASH_MODE_OFF)==0){
            param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            button.setText("Flash on");
        }else{
            param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            button.setText("Flash off");
        }

        camera.setParameters(param);
    }

    public void changeCamera(View view){
        Button button = (Button) findViewById(R.id.changeButton);
        switch (cameraActiveSide){
            case back:
                cameraActiveSide = CameraSides.front;
                button.setText("Back camera");
                break;
            case front:
                cameraActiveSide = CameraSides.back;
                button.setText("Front camera");
                break;
        }
        startPreview(findCameraCode(cameraActiveSide));
    }

    private int findCameraCode(CameraSides cameraCode){
        switch (cameraCode){
            case back:
                return 0;
            case front:
                return 1;
            default: return 0;
        }
    }

    private boolean checkCameraDevice(Context context){
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        }else{
            return false;
        }
    }

    private boolean cameraOpen(int cameraID) {
        if (camera != null) {
            cameraClose();
        }
        try {
            camera = Camera.open(cameraID);
            return true;
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "Failed to open Camera.");
            e.printStackTrace();
            return false;
        }
    }

    private void cameraClose(){
        if(camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void startPreview(int cameraID){
        cameraClose();
        if(!cameraOpen(cameraID)){
            return;
        }
        cameraShow = new CameraView(getApplicationContext(), camera, true);
        displayArea.removeAllViews();
        displayArea.addView(cameraShow);
    }

    protected void restartPreview(){
        startPreview(findCameraCode(cameraActiveSide));
    }

    protected void mediaFunctions(){
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


    protected enum CameraSides {
        back, front
    }
}