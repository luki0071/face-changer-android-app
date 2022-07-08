package com.example.kwasheniak.zmieniacztwarzy.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

public class ManageFilesUtils {

    public static void createFileFolder(File file, Context context){
        if (!file.exists()) {
            file.mkdirs();
            Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaStoreUpdateIntent.setData(Uri.fromFile(file));
            context.sendBroadcast(mediaStoreUpdateIntent);
        }
    }

    public static void deleteFile(File file, Context context) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length > 0) {
                for (File picture : files) {
                    deleteFile(picture, context);
                }
            }
        }
        file.delete();
        Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaStoreUpdateIntent.setData(Uri.fromFile(file));
        context.sendBroadcast(mediaStoreUpdateIntent);
    }
}
