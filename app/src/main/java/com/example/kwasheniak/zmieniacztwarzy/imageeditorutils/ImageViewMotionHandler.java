package com.example.kwasheniak.zmieniacztwarzy.imageeditorutils;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ImageViewMotionHandler {

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private float pointerFingerDistance = 1f;
    private float degrees = 0f;

    private float x, y; //coordinates

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private View.OnTouchListener ImageOnTouchListener = new View.OnTouchListener() {

        float angle = 0;

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:

                    x = view.getX() - event.getRawX();
                    y = view.getY() - event.getRawY();
                    mode = DRAG;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    pointerFingerDistance = spacing(event);
                    if (pointerFingerDistance > 2f) {
                        mode = ZOOM;
                    }
                    degrees = rotation(event);
                    Log.d("degre", "" + degrees);
                    break;

                case MotionEvent.ACTION_UP:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {

                        view.animate().x(event.getRawX() + x).y(event.getRawY() + y).setDuration(0).start();

                    } else if (mode == ZOOM) {
                        if (event.getPointerCount() == 2) {
                            angle = rotation(event) - degrees;
                            float movingFingerDistance = spacing(event);
                            if (movingFingerDistance > 2f) {
                                float scale = movingFingerDistance / pointerFingerDistance * view.getScaleX();
                                if (scale > 0.2) {
                                    view.animate().scaleX(scale).setDuration(0).start();
                                    view.animate().scaleY(scale).setDuration(0).start();
                                }
                            }
                            view.animate().rotationBy(angle).setDuration(0).start();
                        }
                    }
                    break;
            }
            return true;
        }
    };

    public View.OnTouchListener getImageOnTouchListener() {
        return ImageOnTouchListener;
    }
}
