package com.example.kwasheniak.zmieniacztwarzy.librarylayoututils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kwasheniak.zmieniacztwarzy.R;
import com.example.kwasheniak.zmieniacztwarzy.utils.Utils;
import com.example.kwasheniak.zmieniacztwarzy.utils.Variables;
import com.example.kwasheniak.zmieniacztwarzy.activity.EditorActivity;
import com.example.kwasheniak.zmieniacztwarzy.activity.PreviewPictureActivity;

import java.io.File;
import java.util.ArrayList;

public class DirectoryFragment extends Fragment {

    private final static String KEY = "directory_path";

    private ArrayList<File> pictureArrayList;
    private TextView textViewInfo;
    private GridView gridView;
    private static Class TARGET_ACTIVITY;

    private ProgressDialog progress;

    public DirectoryFragment() {
    }

    public static DirectoryFragment newInstance(String file, int activity) {
        if (activity == Variables.SHOW_PREVIEW_PICTURE_ACTIVITY) {
            TARGET_ACTIVITY = PreviewPictureActivity.class;
        } else {
            TARGET_ACTIVITY = EditorActivity.class;
        }
        DirectoryFragment fragment = new DirectoryFragment();
        final Bundle args = new Bundle();
        args.putString(KEY, file);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.directory_fragment, container, false);
        textViewInfo = view.findViewById(R.id.textViewInfo);
        gridView = view.findViewById(R.id.gridView);

        progress = new ProgressDialog(getContext());
        Utils.setupProgressDialog(progress);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                new Task(i).execute();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (progress.isShowing()) {
            progress.dismiss();
        }
        imageReader();
    }

    public void imageReader() {
        gridView.setVisibility(View.VISIBLE);
        textViewInfo.setVisibility(View.GONE);
        pictureArrayList = new ArrayList<>();
        File directory = new File(getArguments().getString(KEY));

        if (directory.exists()) {
            setListOfPictures(directory, pictureArrayList);
            if (pictureArrayList.isEmpty()) {
                gridView.setVisibility(View.GONE);
                textViewInfo.setVisibility(View.VISIBLE);
                textViewInfo.setText(R.string.library_text_view_info);
            } else {
                gridView.setAdapter(new GridAdapter());
            }
        } else {
            gridView.setVisibility(View.GONE);
            textViewInfo.setVisibility(View.VISIBLE);
            textViewInfo.setText(R.string.library_text_view_info_2);
        }
    }

    public void setListOfPictures(File file, ArrayList list) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length > 0) {
                for (File picture : files) {
                    setListOfPictures(picture, list);
                }
            }
        } else {
            if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
                list.add(file);
            }
        }
    }

    private class GridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return pictureArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return pictureArrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            view = getLayoutInflater().inflate(R.layout.item_grid_view, viewGroup, false);
            ImageView iv = view.findViewById(R.id.image_grid_view);
            Glide.with(getContext())
                    .load(pictureArrayList.get(i))
                    .thumbnail(0.1f)
                    .centerCrop()
                    .into(iv);

            return view;
        }
    }

    private class Task extends AsyncTask<Void, Void, Void> {

        private int id;

        private Task(int id) {
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... args) {
            startActivity(new Intent(getContext(), TARGET_ACTIVITY).putExtra(Variables.PUT_EXTRA_PICTURE_NAME, pictureArrayList.get(id).toString()));
            return null;
        }
    }
}