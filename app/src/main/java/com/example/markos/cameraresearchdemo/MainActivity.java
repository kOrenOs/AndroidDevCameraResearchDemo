package com.example.markos.cameraresearchdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView iv = (ImageView) findViewById(R.id.imageResult);
        if(data != null) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            iv.setImageBitmap(image);
        }
    }

    public void cameraOpen(View view){
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }

    public void callSecondActivity(View view){
        Intent intent = new Intent(this, CameraSpecification.class);
        startActivity(intent);
    }

    public void openVideoActivity(View view){
        Intent intent = new Intent(this, VideoSpecification.class);
        startActivity(intent);
    }

    public void openCamera2APIApplication(View view){
        Intent intent = new Intent(this,  Camera2APIApplicationTutorial.class);
        startActivity(intent);
    }
}
