package com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils;

import android.graphics.drawable.Drawable;

public class DrawMouthMasks extends DrawMask{

    private String fileName;
    private Drawable drawable;

    public DrawMouthMasks(String fileName, Drawable drawable){
        this.fileName = fileName;
        this.drawable = drawable;
    }
    @Override
    public Drawable draw(float centerX, float centerY, float offsetX, float offsetY) {

        float left = 0, right = 0, top = 0, bottom = 0;
        float scaleHeight;

        switch (fileName) {
            case "beard.png":
                scaleHeight = 0.6f;
                left = centerX - offsetX / 1.5f;
                right = centerX + offsetX / 1.5f;

                top = centerY - (offsetY * scaleHeight) + (offsetY / 2.3f);
                bottom = centerY + (offsetY * scaleHeight) + (offsetY / 2.3f);
                break;
            case "lips.png":
                scaleHeight = 0.5f;
                left = centerX - offsetX / 2.f;
                right = centerX + offsetX / 2.f;

                top = centerY - (offsetY * scaleHeight) / 2.f + (offsetY / 2.f);
                bottom = centerY + (offsetY * scaleHeight) / 2.f + (offsetY / 2.f);
                break;
            case "mustache.png":
                scaleHeight = 0.5f;
                left = centerX - offsetX;
                right = centerX + offsetX;

                top = centerY - (offsetY * scaleHeight) + (offsetY / 2.f);
                bottom = centerY + (offsetY * scaleHeight) + (offsetY / 2.f);
                break;
            case "smile.png":
                scaleHeight = 0.72f;
                left = centerX - offsetX / 1.5f;
                right = centerX + offsetX / 1.5f;

                top = centerY - (offsetY / 1.5f * scaleHeight) + (offsetY / 2.4f);
                bottom = centerY + (offsetY / 1.5f * scaleHeight) + (offsetY / 2.4f);
                break;
        }

        drawable.setBounds((int) left, (int) top, (int) right, (int) bottom);

        return drawable;
    }
}
