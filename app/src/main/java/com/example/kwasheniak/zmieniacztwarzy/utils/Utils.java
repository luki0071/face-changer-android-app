package com.example.kwasheniak.zmieniacztwarzy.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class Utils {

    public static SparseArray<Face> getFacesArray(Bitmap picture, Context context) {
        if (picture == null) {
            Toast.makeText(context, "Nie wykryto żadnego obrazu", Toast.LENGTH_SHORT).show();
            return null;
        }
        FaceDetector faceDetector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setTrackingEnabled(false)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();
        if (!faceDetector.isOperational()) {
            Toast.makeText(context, "Nie można użyć detektora twarzy", Toast.LENGTH_SHORT).show();
            return null;
        }

        Frame frame = new Frame.Builder().setBitmap(picture).build();
        return faceDetector.detect(frame);
    }

    public static String getFilePathFromUri(Context context, Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }

    public static void setupProgressDialog(ProgressDialog progress) {

        progress.setMessage("Czekaj...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(false);
        progress.setCanceledOnTouchOutside(false);
    }

    public static int getNavigationBarHeight(Context context) {

        int resourceIdNavi = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceIdNavi > 0) {
            return context.getResources().getDimensionPixelSize(resourceIdNavi);
        }
        return 0;
    }

    public static int getStatusBarHeight(Context context) {

        int resourceIdStat = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceIdStat > 0) {
            return context.getResources().getDimensionPixelSize(resourceIdStat);
        }
        return 0;
    }

    public static Point getRealScreenSize(Context context) {

        WindowManager windowsManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        windowsManager.getDefaultDisplay().getRealSize(size);
        return size;
    }

    public static int getDensity(Context context) {

        return context.getResources().getDisplayMetrics().densityDpi;
    }

    public static DisplayMetrics getDisplayMetrics(Activity activity){
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }
}
