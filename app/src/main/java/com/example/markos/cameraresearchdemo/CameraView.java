package com.example.markos.cameraresearchdemo;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by korenos on 07/11/16.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    private boolean cameraMediaFormat;
    private SurfaceHolder holder;
    private int areaHeightResulotion = 2000;
    private int areaWidthResulotion = 2000;
    private boolean touchLock = false;

    public CameraView(Context paContext, Camera paCamera, boolean paCameraMediaFormat) {
        super(paContext);

        camera = paCamera;
        cameraMediaFormat = paCameraMediaFormat;

        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        setFocusable(true);

        setFocusmodeByFormat();

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!touchLock) {
                    int Xup = (int) motionEvent.getX();
                    int Yup = (int) motionEvent.getY();

                    Camera.Parameters param = camera.getParameters();
                    param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                    List<Camera.Area> list = new ArrayList<Camera.Area>();
                    list.add(new Camera.Area(countRect(Xup, Yup, 100, 100), 700));
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

                setFocusmodeByFormat();

                Camera.Parameters param = camera.getParameters();
                param.setFocusAreas(null);
                param.setMeteringAreas(null);

                camera.setParameters(param);
                camera.autoFocus(null);

                Thread timer = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.currentThread().sleep(5000);
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
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try{
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }catch (IOException e){
            Log.d("Error",  e.getMessage()+" Not successful to preview camera.");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (holder.getSurface() == null){
            return;
        }
        try {
            camera.stopPreview();
        } catch (Exception e){
        }

        camera.setDisplayOrientation(MediaLocationsAndSettings.orientationChange(getContext()));

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e){
            Log.d("Error", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    private void setFocusmodeByFormat(){
        Camera.Parameters param = camera.getParameters();
        if(cameraMediaFormat){
            param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }else{
            param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        camera.setParameters(param);
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

        return new Rect(resultXup, resultYup, resultXdown, resultYdown);
    }
}
