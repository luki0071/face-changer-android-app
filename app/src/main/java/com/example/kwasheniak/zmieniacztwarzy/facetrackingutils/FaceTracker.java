package com.example.kwasheniak.zmieniacztwarzy.facetrackingutils;

import android.content.Context;

import com.example.kwasheniak.zmieniacztwarzy.camerasurfaces.GraphicOverlay;
import com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils.DrawGraphic;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class FaceTracker extends Tracker<Face> {

    private static final String TAG = "FaceTracker";

    private GraphicOverlay mOverlay;
    private boolean mIsFrontFacing;
    private DrawGraphic mDrawGraphic;
    private FacePositionPoints mFaceData;
    private int iter;

    public FaceTracker(GraphicOverlay overlay, boolean isFrontFacing) {
        //Log.d("TAG","Face tracker");
        mOverlay = overlay;
        mIsFrontFacing = isFrontFacing;
        mFaceData = new FacePositionPoints();
    }

    @Override
    public void onNewItem(int id, Face face) {
        //Log.d("TAG","new item ");
        mDrawGraphic = new DrawGraphic(mOverlay, mIsFrontFacing);
        mOverlay.add(mDrawGraphic);
    }

    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
        //Log.d("TAG","update");
        mFaceData.setPosition(face.getPosition());
        mFaceData.setWidth(face.getWidth());
        mFaceData.setHeight(face.getHeight());
        mFaceData.setEulerZ(face.getEulerZ());

        mDrawGraphic.update(mFaceData);
    }

    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        //Log.d("TAG","missing");
        mOverlay.remove(mDrawGraphic);
    }

    @Override
    public void onDone() {
        //Log.d("TAG","done");
        mOverlay.remove(mDrawGraphic);
    }
}
