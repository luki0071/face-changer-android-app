package com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils;

import android.graphics.drawable.Drawable;

public class DrawEyesMasks extends DrawMask{

    private String fileName;
    private Drawable drawable;

    public DrawEyesMasks(String fileName, Drawable drawable){
        this.fileName = fileName;
        this.drawable = drawable;
    }
    @Override
    public Drawable draw(float centerX, float centerY, float offsetX, float offsetY) {

        float left = 0, right = 0, top = 0, bottom = 0;
        float scaleHeight;

        switch (fileName) {
            case "carnival.png":
                scaleHeight = 0.74f;
                left = centerX - offsetX / 1.2f;
                right = centerX + offsetX / 1.2f;

                top = centerY - (offsetY / 1.2f * scaleHeight) - (offsetY / 2.3f);
                bottom = centerY + (offsetY / 1.2f * scaleHeight) - (offsetY / 2.3f);
                break;
            case "cat.png":
                scaleHeight = 0.5f;
                left = centerX - offsetX / 1.5f;
                right = centerX + offsetX / 1.5f;

                top = centerY - (offsetY / 1.5f * scaleHeight) - (offsetY / 3.5f);
                bottom = centerY + (offsetY / 1.5f * scaleHeight) - (offsetY / 3.5f);
                break;
            case "sunglasses.png":
                scaleHeight = 0.5f;
                left = centerX - offsetX / 1.3f;
                right = centerX + offsetX / 1.3f;

                top = centerY - (offsetY / 1.3f * scaleHeight) - (offsetY / 5.f);
                bottom = centerY + (offsetY / 1.3f * scaleHeight) - (offsetY / 5.f);
                break;
        }

        drawable.setBounds((int) left, (int) top, (int) right, (int) bottom);

        return drawable;
    }
}
