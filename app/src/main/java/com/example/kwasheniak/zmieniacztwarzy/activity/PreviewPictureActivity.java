package com.example.kwasheniak.zmieniacztwarzy.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.bumptech.glide.Glide;
import com.example.kwasheniak.zmieniacztwarzy.utils.ManageFilesUtils;
import com.example.kwasheniak.zmieniacztwarzy.R;
import com.example.kwasheniak.zmieniacztwarzy.utils.Utils;
import com.example.kwasheniak.zmieniacztwarzy.utils.Variables;

import java.io.File;

public class PreviewPictureActivity extends AppCompatActivity {

    private final static String TAG = "PreviewPictureActivity";

    private RelativeLayout buttonDelete;
    private RelativeLayout buttonEdit;

    private ImageView pictureView;

    private String pictureFilePath;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_picture);

        pictureView = findViewById(R.id.preview_picture_picture_view);

        buttonEdit = findViewById(R.id.preview_picture_button_edit);
        buttonDelete = findViewById(R.id.preview_picture_button_delete);

        if (getIntent().hasExtra(Variables.PUT_EXTRA_PICTURE_NAME)) {
            pictureFilePath = getIntent().getStringExtra(Variables.PUT_EXTRA_PICTURE_NAME);
            Glide.with(getApplicationContext())
                    .load(pictureFilePath)
                    .into(pictureView);
        }

        initializeButtons();

        progress = new ProgressDialog(PreviewPictureActivity.this);
        Utils.setupProgressDialog(progress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progress.dismiss();
    }

    private void initializeButtons() {
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Task().execute();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initializeDeleteDialog();
            }
        });
    }

    private void initializeDeleteDialog(){
        try {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(PreviewPictureActivity.this);
            dialog.setTitle("Usuwanie");
            dialog.setMessage("Na pewno chcesz usunąć zdjęcie?");
            dialog.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ManageFilesUtils.deleteFile(new File(pictureFilePath), PreviewPictureActivity.this);
                    finish();
                }
            });
            dialog.setCancelable(true);
            dialog.setNegativeButton("NIE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
        } catch (Exception e) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(PreviewPictureActivity.this);
            dialog.setTitle("Błąd");
            dialog.setMessage("Nie można usunąć pliku: " + e.toString());
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.show();
        }
    }

    private class Task extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... args) {
            startActivity(new Intent(PreviewPictureActivity.this, EditorActivity.class).putExtra(Variables.PUT_EXTRA_PICTURE_NAME, pictureFilePath));
            return null;
        }
    }
}
