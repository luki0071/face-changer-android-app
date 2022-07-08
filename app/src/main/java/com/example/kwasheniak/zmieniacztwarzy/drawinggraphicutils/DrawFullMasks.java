package com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils;

import android.graphics.drawable.Drawable;

public class DrawFullMasks extends DrawMask {

    private String fileName;
    private Drawable drawable;

    public DrawFullMasks(String fileName, Drawable drawable){
        this.fileName = fileName;
        this.drawable = drawable;
    }
    @Override
    public Drawable draw(float centerX, float centerY, float offsetX, float offsetY) {

        float left = 0, right = 0, top = 0, bottom = 0;
        float scaleWidth;
        float scaleHeight;

        switch (fileName) {
            case "fawkes.png":
                scaleWidth = 0.77f;
                left = centerX - offsetX * scaleWidth;
                right = centerX + offsetX * scaleWidth;

                top = centerY - offsetY;
                bottom = centerY + offsetY;
                break;
            case "gasmask.png":
                scaleWidth = 0.90f;
                left = centerX - (offsetX * scaleWidth) * 1.4f;
                right = centerX + (offsetX * scaleWidth) * 1.4f;

                top = centerY - offsetY * 1.4f + (offsetY / 10.f);
                bottom = centerY + offsetY * 1.4f + (offsetY / 10.f);
                break;
            case "medical.png":
                scaleHeight = 0.67f;
                left = centerX - offsetX / 1.2f;
                right = centerX + offsetX / 1.2f;

                top = centerY - (offsetY * scaleHeight) / 1.2f + (offsetY / 3.f);
                bottom = centerY + (offsetY * scaleHeight) / 1.2f + (offsetY / 3.f);
                break;
            case "op.png":
                scaleHeight = 1.08f;
                left = centerX - offsetX;
                right = centerX + offsetX;

                top = centerY - (offsetY * scaleHeight) - (offsetY / 2.f) - (offsetY / 4.f);
                bottom = centerY + (offsetY * scaleHeight) - (offsetY / 4.f);
                break;
            case "smiley.png":
                left = centerX - offsetX / 1.3f + (offsetY / 20.f);
                right = centerX + offsetX / 1.3f + (offsetY / 20.f);

                top = centerY - (offsetY / 1.3f) + (offsetY / 10.f);
                bottom = centerY + (offsetY / 1.3f) + (offsetY / 10.f);
                break;
        }

        drawable.setBounds((int) left, (int) top, (int) right, (int) bottom);

        return drawable;
    }
}
