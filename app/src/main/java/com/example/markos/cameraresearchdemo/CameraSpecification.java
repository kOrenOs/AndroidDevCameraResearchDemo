package com.example.markos.cameraresearchdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Markos on 13. 11. 2016.
 */

public class CameraSpecification extends MediaCommonClass {

    @Override
    protected void mediaFunctions() {
        super.mediaFunctions();

        Button actionButton = (Button) findViewById(R.id.actionButton);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeCameraPictureAction();
            }
        });
    }

    public void takeCameraPictureAction(){
        camera.setDisplayOrientation(MediaLocationsAndSettings.orientationChange(this));
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

                    int rotationDegrees = MediaLocationsAndSettings.orientationChange(getApplicationContext());
                    if(cameraActiveSide == CameraSides.front){
                        rotationDegrees = (rotationDegrees+180)%360;
                    }

                    Bitmap realImage = MediaLocationsAndSettings.rotate(BitmapFactory.decodeByteArray(bytes, 0, bytes.length)
                            , rotationDegrees);
                    realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.e("Error", "File not found: " + e.getMessage());
                    return;
                } catch (IOException e) {
                    Log.e("Error", "Error accessing file: " + e.getMessage());
                    return;
                }
                restartPreview();
            }
        });
    }
}
