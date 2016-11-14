package com.example.markos.cameraresearchdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;

/**
 * Created by korenos on 07/11/16.
 */

public class MediaLocationsAndSettings {

    private static String baseLocation = "/storage/emulated/0/DCIM/Camera/";
    private static String photoType = ".jpeg";
    private static String videoType = ".mp4";
    private static String photoBaseName = "photo";
    private static String videoBaseName = "video";
    private static int counterPhoto = 0;
    private static int counterVideo = 0;
    private static int videoFrameRate = 30;
    private static boolean soundOn = true;

    public static void setBaseLocation(String location){
        baseLocation = location;
    }

    public static void setpPotoBaseName(String baseName){
        photoBaseName = baseName;
    }

    public static void setpVideoBaseName(String baseName){
        videoBaseName = baseName;
    }

    public static void setVideoType(videoTypes type){
        switch (type){
            case mp4: videoType = ".mp4";
            case threeGpp: videoType = ".3gp";
        }
    }

    public static void setVideoFrameRate(int frameRate){
        videoFrameRate = frameRate;
    }

    public static int getVideoFrameRate(){
        return videoFrameRate;
    }

    public static void setSoundOn(boolean soundStatus){
        soundOn = soundStatus;
    }

    public static boolean getSoundOn(){
        return soundOn;
    }

    public static int orientationChange(Context context){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int momentalRotation = manager.getDefaultDisplay().getRotation();

        return MediaLocationsAndSettings.orientationCalculatior(momentalRotation);
    }

    public static int orientationCalculatior(int momentalRotation){
        switch(momentalRotation){
            case Surface.ROTATION_0:
                return 90;
            case Surface.ROTATION_90:
                return 0;
            case Surface.ROTATION_180:
                return 270;
            case Surface.ROTATION_270:
                return 180;
            default: return 0;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, int rotationDegree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.setRotate(rotationDegree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public static int selectedVideoFormat(){
        switch(videoType){
            case ".mp4": return MediaRecorder.OutputFormat.MPEG_4;
            case ".webm": return MediaRecorder.OutputFormat.WEBM;
            case ".3gp": return MediaRecorder.OutputFormat.THREE_GPP;
            default: return MediaRecorder.OutputFormat.MPEG_4;
        }
    }

    public static String getVideoName(){
        counterVideo = findNextName(baseLocation, videoType, videoBaseName, counterVideo);
        StringBuilder temp = new StringBuilder(baseLocation);
        temp.append(videoBaseName);
        temp.append(counterVideo);
        temp.append(videoType);
        return temp.toString();
    }

    public static String getPhotoName(){
        counterPhoto = findNextName(baseLocation, photoType, photoBaseName, counterPhoto);
        if(counterPhoto == -1){
            return null;
        }
        StringBuilder temp = new StringBuilder(baseLocation);
        temp.append(photoBaseName);
        temp.append(counterPhoto);
        temp.append(photoType);
        return temp.toString();
    }

    private static int findNextName(String baseLocation, String type, String baseName, int count){
        String basePath = baseLocation+baseName;

        StringBuilder fileID = new StringBuilder();
        fileID.append(count);
        fileID.append(type);

        StringBuilder temp = new StringBuilder(basePath);
        temp.append(fileID);
        File file;

        while(count < 100000) {
            file = new File(temp.toString());
            if (file.exists()) {
                count++;
                fileID.setLength(0);
                fileID.append(count);
                fileID.append(type);
                temp.setLength(0);
                temp.append(basePath);
                temp.append(fileID);
            } else {
                return count;
            }
        }
        return -1;
    }

    public enum videoTypes{
        mp4,
        threeGpp
    }

    public enum videoQuality {
        QUALITY_HIGH,
        QUALITY_720P,
        QUALITY_480P,
        QUALITY_1080P,
        QUALITY_CIF,
        QUALITY_LOW,
        QUALITY_QCIF,
        QUALITY_QVGA,
    }
}
