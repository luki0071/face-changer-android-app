package com.example.kwasheniak.zmieniacztwarzy.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Environment;
import android.util.Size;
import android.view.Display;
import android.view.WindowManager;

import com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils.DrawMask;

import java.io.File;
import java.util.HashMap;

public class Variables {

    public final static String PICTURES_DIRECTORY_NAME = "Images";
    public final static File APP_PICTURES_DIRECTORY = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), PICTURES_DIRECTORY_NAME);

    public static String CUSTOM_MASKS_DIRECTORY_NAME = "CustomMasks";
    public static File CUSTOM_MASKS_DIRECTORY = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), CUSTOM_MASKS_DIRECTORY_NAME);

    public final static String PUT_EXTRA_PICTURE_NAME = "PICTURE";

    public final static int SHOW_PREVIEW_PICTURE_ACTIVITY = 1;

    public static final String[] MASKS_ORDER = new String[]{"masks", "CustomMasks", "eyes", "mouth", "nose", "head"};
    public static HashMap<String, DrawMask> MASK_TYPES = new HashMap<String, DrawMask>();
    public static Size IMAGE_VIEW_SIZE;

    public final static String SHARED_PREF = "SharedPreferences";
    public static String RATIO_WIDTH = "ratioWidth";
    public static String RATIO_HEIGHT = "ratioHeight";

    public final static int RESULT_LOAD_IMAGE = 101;

    public static Size PREVIEW_SIZE;

    public static int getScreenHeight(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getScreenWidth(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
}
