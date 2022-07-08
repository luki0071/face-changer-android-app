package com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils;

import android.graphics.drawable.Drawable;

public class DrawHeadMasks extends DrawMask{

    private String fileName;
    private Drawable drawable;

    public DrawHeadMasks(String fileName, Drawable drawable){
        this.fileName = fileName;
        this.drawable = drawable;
    }
    @Override
    public Drawable draw(float centerX, float centerY, float offsetX, float offsetY) {

        float left = 0, right = 0, top = 0, bottom = 0;
        float scaleHeight;

        switch (fileName) {
            case "hair.png":
                scaleHeight = 0.84f;
                left = centerX - offsetX;
                right = centerX + offsetX;

                top = centerY - (offsetY * scaleHeight) - (offsetY / 1.5f);
                bottom = centerY + (offsetY * scaleHeight) - (offsetY / 1.5f);
                break;
            case "pirate.png":
                scaleHeight = 0.53f;
                left = centerX - offsetX * 1.2f;
                right = centerX + offsetX * 1.2f;

                top = centerY - (offsetY * 1.2f * scaleHeight) - (offsetY);
                bottom = centerY + (offsetY * 1.2f * scaleHeight) - (offsetY);
                break;
            case "santa.png":
                scaleHeight = 0.83f;
                left = centerX - (offsetX * 1.2f) - (offsetX / 4.f);
                right = centerX + (offsetX * 1.2f) - (offsetX / 4.f);

                top = centerY - (offsetY * 1.2f * scaleHeight) - (offsetY * 1.2f);
                bottom = centerY + (offsetY * 1.2f * scaleHeight) - (offsetY * 1.2f);
                break;
        }

        drawable.setBounds((int) left, (int) top, (int) right, (int) bottom);

        return drawable;
    }
}
