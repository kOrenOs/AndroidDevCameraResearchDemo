package com.example.markos.cameraresearchdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Markos on 13. 10. 2016.
 */

public class CameraBuildIn extends AppCompatActivity{

    private Camera camera = null;
    private CameraShow cameraShow;
    private FrameLayout displayArea;
    private String photoPathBase = "/storage/emulated/0/DCIM/Camera/";
    private String photoNameBase = "photo";
    private String imageType = ".jpeg";
    private StringBuilder finalPath;
    private int momentalIndexName = 0;

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

    public void takeCameraPictureAction(View view) throws IOException {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                try {
                    if(!setUpPhotoName()){
                        Log.e("Error", "Not possible to find proper photo name.");
                        return;
                    }
                    FileOutputStream fos = new FileOutputStream(finalPath.toString());
                    fos.write(bytes);
                    fos.close();
                    momentalIndexName++;
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

    private boolean setUpPhotoName(){
        String basePath = photoPathBase+photoNameBase;

        StringBuilder fileID = new StringBuilder();
        fileID.append(momentalIndexName);
        fileID.append(imageType);

        StringBuilder temp = new StringBuilder(basePath);
        temp.append(fileID);
        File file;

        while(momentalIndexName < 100000) {
            file = new File(temp.toString());
            if (file.exists()) {
                momentalIndexName++;
                fileID.setLength(0);
                fileID.append(momentalIndexName);
                fileID.append(imageType);
                temp.setLength(0);
                temp.append(basePath);
                temp.append(fileID);
            } else {
                finalPath = temp;
                return true;
            }
        }
        return false;
    }

    private void restartCamera(){
        turnOffCamera();
        if(!bindCameraInstance()){
            return;
        }
        cameraShow = new CameraShow(this, camera);
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
}
