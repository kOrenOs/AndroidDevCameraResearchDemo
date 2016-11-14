package com.example.markos.cameraresearchdemo;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Markos on 13. 11. 2016.
 */

public class VideoSpecification extends MediaCommonClass {

    private MediaRecorder mediaRecorder;
    private boolean recording = false;

    @Override
    public void onDestroy() {
        if(recording){
            stopRecording();
        }
        super.onDestroy();
    }

    @Override
    protected void mediaFunctions() {
        super.mediaFunctions();

        final Button actionButton = (Button) findViewById(R.id.actionButton);
        actionButton.setText("Record");
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recording){
                    stopRecording();
                    actionButton.setText("Record");
                    recording = false;
                }else{
                    startRecording();
                    actionButton.setText("Stop");
                    recording = true;
                }
            }
        });
    }

    private void startRecording(){
        if (!prepareMediaRecorder()) {
            Toast.makeText(this, "Failure!", Toast.LENGTH_LONG).show();
            finish();
        }
        Toast.makeText(this, "Recording started", Toast.LENGTH_LONG).show();
        Runnable videoRecording = new Runnable() {
            public void run() {
                try {
                    mediaRecorder.start();
                } catch (final Exception ex) {
                }
            }
        };
        executeOnProcessThread(videoRecording);
    }

    private void stopRecording(){
        mediaRecorder.stop();
        closeMediaRecorder();
        Toast.makeText(this, "Video captured!", Toast.LENGTH_LONG).show();
        restartPreview();
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

        int rotationDegrees = MediaLocationsAndSettings.orientationChange(this);
        if(cameraActiveSide == CameraSides.front){
            rotationDegrees = (rotationDegrees+180)%360;
        }

        mediaRecorder.setOrientationHint(rotationDegrees);
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            closeMediaRecorder();
            return false;
        } catch (IOException e) {
            closeMediaRecorder();
            return false;
        }
        return true;
    }

    private void closeMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    private void executeOnProcessThread(Runnable task){
        Thread procesThread = new Thread(task);
        procesThread.start();
    }
}
