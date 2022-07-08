package com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils;

import android.graphics.drawable.Drawable;

public class DrawNoseMasks extends DrawMask {

    private String fileName;
    private Drawable drawable;

    public DrawNoseMasks(String fileName, Drawable drawable){
        this.fileName = fileName;
        this.drawable = drawable;
    }
    @Override
    public Drawable draw(float centerX, float centerY, float offsetX, float offsetY) {

        float left = 0, right = 0, top = 0, bottom = 0;

        if ("pig.png".equals(fileName)) {
            left = centerX - (offsetX / 2.5f);
            right = centerX + (offsetX / 2.5f);

            top = centerY - (offsetY / 2.5f) + (offsetY / 8.0f);
            bottom = centerY + (offsetY / 2.5f) + (offsetY / 8.0f);
        }

        drawable.setBounds((int) left, (int) top, (int) right, (int) bottom);

        return drawable;
    }
}
