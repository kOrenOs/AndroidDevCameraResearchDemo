package com.example.markos.cameraresearchdemo;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Markos on 13. 10. 2016.
 */

public class CameraShow extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceHolder holder;
    private int areaHeightResulotion = 2000;
    private int areaWidthResulotion = 2000;
    private boolean touchLock = false;

    public CameraShow(Context paContext, Camera paCamera){
        super(paContext);

        camera = paCamera;
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        setFocusable(true);

        Camera.Parameters param = camera.getParameters();
        param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(param);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!touchLock) {
                    int Xup = (int) motionEvent.getX();
                    int Yup = (int) motionEvent.getY();

                    Camera.Parameters param = camera.getParameters();
                    param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                    List<Camera.Area> list = new ArrayList<Camera.Area>();
                    list.add(new Camera.Area(countRect(Xup, Yup, 100, 100), 1000));
                    param.setFocusAreas(list);
                    param.setMeteringAreas(list);

                    camera.setParameters(param);
                    camera.autoFocus(null);
                }
                return false;
            }
        });

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                touchLock = true;

                Camera.Parameters param = camera.getParameters();
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                param.setFocusAreas(null);
                param.setMeteringAreas(null);

                camera.setParameters(param);
                camera.autoFocus(null);

                Thread timer = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.currentThread().sleep(10000);
                        }catch (InterruptedException e){
                        }
                        touchLock = false;
                    }
                });
                timer.start();
                return true;
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }catch (IOException e){
            Log.d("Error",  e.getMessage()+"Not successful to preview camera.");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null){
            return;
        }
        try {
            camera.stopPreview();
        } catch (Exception e){
        }

        camera.setDisplayOrientation(orientationChange());

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e){
            Log.d("Error", "Error starting camera preview: " + e.getMessage());
        }
    }

    private int orientationChange(){
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int momentalRotation = manager.getDefaultDisplay().getRotation();
        int rotation = 0;

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);

        switch(momentalRotation){
            case Surface.ROTATION_0:
                rotation = 90;
                break;
            case Surface.ROTATION_90:
                rotation = 0;
                break;
            case Surface.ROTATION_180:
                rotation = 270;
                break;
            case Surface.ROTATION_270:
                rotation = 180;
                break;
        }

        return rotation;
    }

    private Rect countRect(float xCoordination, float yCoordination, int moveX, int moveY){
        int maxHeight = getHeight();
        int maxWidth = getWidth();


        int resultXup = (int)((xCoordination/maxWidth)*areaWidthResulotion-(areaWidthResulotion/2.0));
        int resultYup = (int)((yCoordination/maxHeight)*areaHeightResulotion-(areaHeightResulotion/2.0));
        int resultXdown;
        int resultYdown;

        if(resultXup+moveX<areaWidthResulotion/2){
            resultXdown = resultXup+moveX;
        }else{
            resultXdown = areaWidthResulotion/2;
        }

        if(resultYup+moveY<areaHeightResulotion/2){
            resultYdown = resultYup+moveY;
        }else{
            resultYdown = areaHeightResulotion/2;
        }

        System.out.println(resultXup+", "+resultYup+", "+resultXdown+", "+resultYdown);

        return new Rect(resultXup, resultYup, resultXdown, resultYdown);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}
