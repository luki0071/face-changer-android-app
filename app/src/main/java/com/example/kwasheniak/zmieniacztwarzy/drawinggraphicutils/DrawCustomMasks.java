package com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import com.example.kwasheniak.zmieniacztwarzy.utils.Variables;

public class DrawCustomMasks extends DrawMask{

    private Drawable drawable;
    private Context context;

    public DrawCustomMasks(Context context, Drawable drawable){
        this.drawable = drawable;
        this.context = context;
    }
    @Override
    public Drawable draw(float centerX, float centerY, float offsetX, float offsetY) {

        SharedPreferences preferences = context.getSharedPreferences(Variables.SHARED_PREF, Activity.MODE_PRIVATE);
        float ratioWidth = preferences.getFloat(Variables.RATIO_WIDTH, 1.0f);
        float ratioHeight = preferences.getFloat(Variables.RATIO_HEIGHT, 1.0f);

        float left = centerX - offsetX * ratioWidth;
        float right = centerX + offsetX * ratioWidth;

        float top = centerY - offsetY * ratioHeight;
        float bottom = centerY + offsetY * ratioHeight;

        drawable.setBounds((int) left, (int) top, (int) right, (int) bottom);

        return drawable;
    }
}
