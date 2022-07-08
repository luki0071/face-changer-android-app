package com.example.kwasheniak.zmieniacztwarzy.facetrackingutils;

import android.graphics.PointF;

public class FacePositionPoints {

    private static final String TAG = "FacePositionPoints";

    private PointF facePosition;
    private float faceWidth;
    private float faceHeight;

    private float eulerZ;

    public PointF getPosition() {
        return facePosition;
    }

    public void setPosition(PointF position) {
        facePosition = position;
    }

    public float getWidth() {
        return faceWidth;
    }

    public void setWidth(float width) {
        faceWidth = width;
    }

    public float getHeight() {
        return faceHeight;
    }

    public void setHeight(float height) {
        faceHeight = height;
    }

    public float getEulerZ() {
        return eulerZ;
    }

    public void setEulerZ(float eulerZ) {
        this.eulerZ = eulerZ;
    }
}
