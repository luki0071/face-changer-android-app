package com.example.kwasheniak.zmieniacztwarzy.camerasurfaces;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.example.kwasheniak.zmieniacztwarzy.utils.Variables;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback{

    private static final String TAG = "CameraPreview";

    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    private int screenWidth;
    private int screenHeight;

    private GraphicOverlay mOverlay;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mStartRequested = false;
        mSurfaceAvailable = false;

        screenHeight = Variables.getScreenHeight(context);
        screenWidth = Variables.getScreenWidth(context);

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(this);
        addView(mSurfaceView);

    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
        mOverlay = overlay;
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            Log.d("onLayout", "start");
            startIfReady();
        }
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    private void startIfReady() throws IOException {
        if (mStartRequested && mSurfaceAvailable) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mCameraSource.start(mSurfaceView.getHolder());
            if (mOverlay != null) {
                Size size = mCameraSource.getPreviewSize();
                Log.e(TAG, size.getWidth() + " " + size.getHeight());
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());

                mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
                mOverlay.clear();
            }
            mStartRequested = false;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("onLayout", "surfaceCreated");
        mSurfaceAvailable = true;
        try {
            startIfReady();
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceAvailable = false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        Log.d("onLayout", "onLayout : " + left + " " + top + " " + right + " " + bottom);

        int previewWidth = Variables.PREVIEW_SIZE.getHeight() / 2;
        int previewHeight = Variables.PREVIEW_SIZE.getWidth() / 2;

        final int viewWidth = right - left;
        final int viewHeight = bottom - top;

        Log.d("onLayout", "View size w " + viewWidth + " h " + viewHeight);

        int childWidth;
        int childHeight;
        int childXOffset = 0;
        int childYOffset = 0;
        float widthRatio = (float) viewWidth / (float) previewWidth;
        float heightRatio = (float) viewHeight / (float) previewHeight;

        Log.d("onLayout", "ratio w " + widthRatio + " h " + heightRatio);

        if (widthRatio > heightRatio) {
            childWidth = viewWidth;
            childHeight = (int) ((float) previewHeight * widthRatio);
            childYOffset = (childHeight - viewHeight) / 2;
        } else {
            childWidth = (int) ((float) previewWidth * heightRatio);
            childHeight = viewHeight;
            childXOffset = (childWidth - viewWidth) / 2;
        }
        Log.d("onLayout", "CHILD w " + childWidth + " h " + childHeight + " offset y " + childYOffset + " offset x " + childXOffset);

        Log.d("onLayout", "Childcount " + getChildCount());

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(
                    -1 * childXOffset, -1 * childYOffset,
                    childWidth - childXOffset, childHeight - childYOffset);
        }

        try {
            Log.d("CameraPreviewS", "childWidth " + childWidth + " childHeight " + childHeight);
            startIfReady();
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

}
