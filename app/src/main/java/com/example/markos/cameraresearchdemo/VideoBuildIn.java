package com.example.markos.cameraresearchdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Markos on 31. 10. 2016.
 */

public class VideoBuildIn extends AppCompatActivity {

    private MediaRecorder mediaRecorder;
    private Camera camera = null;       //copied from CameraBuildIn
    private CameraView videoShow;
    private FrameLayout displayArea;    //copied from CameraBuildIn

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_video);

        if(!checkCameraDevice(getApplicationContext())){
            Toast.makeText(getApplicationContext(), "There is not camera device.", Toast.LENGTH_SHORT).show();
            Log.e("Error", "There is not camera device.");
            return;
        }

        displayArea = (FrameLayout) findViewById(R.id.videoViewContainer);
        restartCamera();
    }

    //copied from CameraBuildIn
    private boolean checkCameraDevice(Context context){
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        }else{
            return false;
        }
    }

    //copied from CameraBuildIn
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

    //copied from CameraBuildIn
    private void turnOffCamera(){
        if(camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    //copied from CameraBuildIn
    private void restartCamera(){
        turnOffCamera();
        if(!bindCameraInstance()){
            return;
        }
        videoShow = new CameraView(this, camera, false);
        displayArea.removeAllViews();
        displayArea.addView(videoShow);
    }

    //copied from CameraBuildIn
    @Override
    protected void onPause() {
        super.onPause();
        restartCamera();
    }

    //copied from CameraBuildIn
    @Override
    protected void onStop() {
        super.onStop();
        restartCamera();
    }

    //copied from CameraBuildIn
    @Override
    protected void onDestroy() {
        super.onDestroy();
        restartCamera();
    }

    boolean recording = false;
    public void recordAction(View view){
        if (recording) {
            // stop recording and release camera
            mediaRecorder.stop(); // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            Toast.makeText(this, "Video captured!", Toast.LENGTH_LONG).show();
            restartCamera();
            recording = false;
        } else {
            if (!prepareMediaRecorder()) {
                Toast.makeText(this, "Failure!", Toast.LENGTH_LONG).show();
                finish();
            }
            Toast.makeText(this, "Recording started", Toast.LENGTH_LONG).show();
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        mediaRecorder.start();
                    } catch (final Exception ex) {
                    }
                }
            });

            recording = true;
        }
    }

    private boolean prepareMediaRecorder()
    {
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        profile.fileFormat = MediaLocationsAndSettings.selectedVideoFormat();
        mediaRecorder.setProfile(profile);
        mediaRecorder.setOutputFile(MediaLocationsAndSettings.getVideoName());
        mediaRecorder.setOrientationHint(videoShow.orientationChange());
        mediaRecorder.setMaxDuration(600000); // set maximum duration
        mediaRecorder.setMaxFileSize(50000000); // set maximum file size
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock(); // lock camera for later use
        }
    }
}
