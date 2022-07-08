package com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.example.kwasheniak.zmieniacztwarzy.camerasurfaces.GraphicOverlay;
import com.example.kwasheniak.zmieniacztwarzy.facetrackingutils.FacePositionPoints;
import com.example.kwasheniak.zmieniacztwarzy.utils.Variables;

public class DrawGraphic extends GraphicOverlay.Graphic {

    private static final String TAG = "FaceGraphic";

    private boolean mIsFrontFacing;

    private volatile FacePositionPoints mFaceData;

    public DrawGraphic(GraphicOverlay overlay, boolean isFrontFacing) {
        super(overlay);
        mIsFrontFacing = isFrontFacing;
    }

    public void update(FacePositionPoints faceData) {
        mFaceData = faceData;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {

        if (mFaceData == null) {
            return;
        }

        float centerX = translateX(mFaceData.getPosition().x + mFaceData.getWidth() / 2.0f);
        float centerY = translateY(mFaceData.getPosition().y + mFaceData.getHeight() / 2.0f);
        float offsetX = scaleX(mFaceData.getWidth() / 2.0f);
        float offsetY = scaleY(mFaceData.getHeight() / 2.0f);

        canvas.save();

        if (mIsFrontFacing) {
            canvas.rotate(mFaceData.getEulerZ(), centerX, centerY);
        } else {
            canvas.rotate(-mFaceData.getEulerZ(), centerX, centerY);
        }

        for(String key : Variables.MASKS_ORDER){
            if(Variables.MASK_TYPES.containsKey(key)){
                Drawable mask = Variables.MASK_TYPES.get(key).draw(centerX, centerY, offsetX, offsetY);
                mask.draw(canvas);
            }
        }

        canvas.restore();
    }

}
