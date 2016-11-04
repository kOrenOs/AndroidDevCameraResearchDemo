package com.example.markos.cameraresearchdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.VideoView;

/**
 * Created by Markos on 30. 10. 2016.
 */

public class VideoMainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_main);
    }

    public void recordVideoFromCamera(View view){
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if(videoIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(videoIntent, 0);
        }
    }

    public void turnOnBuildInVideo(View view){
        Intent intent = new Intent(this, VideoBuildIn.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            VideoView videoDisplay = (VideoView) findViewById(R.id.basicVideoView);
            Uri videoUri = data.getData();
            videoDisplay.setVideoURI(videoUri);
            videoDisplay.start();
        }
    }
}
