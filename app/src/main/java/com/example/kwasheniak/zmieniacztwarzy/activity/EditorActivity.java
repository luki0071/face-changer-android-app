package com.example.kwasheniak.zmieniacztwarzy.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kwasheniak.zmieniacztwarzy.filters.Filters;
import com.example.kwasheniak.zmieniacztwarzy.imageeditorutils.ImageViewMotionHandler;
import com.example.kwasheniak.zmieniacztwarzy.utils.ManageFilesUtils;
import com.example.kwasheniak.zmieniacztwarzy.imageeditorutils.PaintView;
import com.example.kwasheniak.zmieniacztwarzy.R;
import com.example.kwasheniak.zmieniacztwarzy.utils.Utils;
import com.example.kwasheniak.zmieniacztwarzy.utils.Variables;
import com.google.android.gms.vision.face.Face;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class EditorActivity extends AppCompatActivity {

    private static final String TAG = "EditorActivity";
    public final static int RESULT_ADDITIONAL_IMAGE = 10;
    private static final int pictureViewId = 1;
    private static int paintViewId = 2;

    private ImageButton buttonSave;
    private ImageButton buttonNewProject;

    private RelativeLayout buttonFilter;
    private RelativeLayout buttonBrush;
    private RelativeLayout buttonEraser;
    private RelativeLayout buttonAddImage;

    private LinearLayout optionMenuLayout;
    private LinearLayout optionMenuScrollViewLinearLayout;
    private ImageButton buttonQuitMenu;
    private ImageButton buttonAddImageMenu;
    private LinearLayout imageOnTopBottomLayout;
    private ImageButton buttonImageOnTop;
    private ImageButton buttonImageOnBottom;

    private LinearLayout seekBarsLinearLayout;
    private SeekBar seekBarBrushSize;
    private SeekBar seekBarBrushAlpha;
    private TextView textViewBrushAlpha;

    private ImageView pictureView;
    private File pictureFilePath;
    private File tempFilepath;

    private PaintView paintView;

    private Bitmap originalBitmap;
    private Bitmap temporaryBitmap;

    private ProgressDialog progress;

    private RelativeLayout imageViewHolder;

    private DisplayMetrics metrics;

    private boolean isEraser = false;

    private ImageView currentImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        imageViewHolder = findViewById(R.id.editor_relative_layout_holder);

        metrics = Utils.getDisplayMetrics(this);

        initTopLayoutButtons();

        progress = new ProgressDialog(EditorActivity.this);
        Utils.setupProgressDialog(progress);

        if (getIntent().hasExtra(Variables.PUT_EXTRA_PICTURE_NAME)) {
            Log.d("ivh", "has extra content");

            pictureFilePath = new File(getIntent().getStringExtra(Variables.PUT_EXTRA_PICTURE_NAME));
            originalBitmap = BitmapFactory.decodeFile(getIntent().getStringExtra(Variables.PUT_EXTRA_PICTURE_NAME));

            initPictureView();
            loadBitmap(originalBitmap);
            initBottomLayoutButtons();

            temporaryBitmap = originalBitmap;

        } else {
            selectPicture(Variables.RESULT_LOAD_IMAGE);
        }
        setImageViewHolderSize();
    }

    private void setImageViewHolderSize(){
        imageViewHolder.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.d("ivh", "wysokosc: " + imageViewHolder.getHeight() + " szerokosc: " + imageViewHolder.getWidth());
                if(imageViewHolder.getHeight() > 0 && imageViewHolder.getWidth() > 0){
                    imageViewHolder.getViewTreeObserver().removeOnPreDrawListener(this);
                    imageViewHolder.getLayoutParams().height = imageViewHolder.getHeight();
                    imageViewHolder.getLayoutParams().width = imageViewHolder.getWidth();
                    paintView = new PaintView(EditorActivity.this);
                    paintView.setId(paintViewId);
                    paintView.init(imageViewHolder.getWidth(), imageViewHolder.getHeight());
                    imageViewHolder.addView(paintView);

                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Variables.RESULT_LOAD_IMAGE) {
            if (resultCode == RESULT_OK) {
                try {
                    Log.d("ivh", "load image");
                    Uri imageUri = data.getData();
                    String picturePath = Utils.getFilePathFromUri(this, imageUri);
                    pictureFilePath = new File(picturePath);
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    originalBitmap = BitmapFactory.decodeStream(imageStream);
                    initPictureView();
                    loadBitmap(originalBitmap);
                    initBottomLayoutButtons();
                    temporaryBitmap = originalBitmap;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else{
                finish();
            }
        }
        if (requestCode == RESULT_ADDITIONAL_IMAGE) {
            if (resultCode == RESULT_OK) {
                addSelectedImage(data);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addSelectedImage(Intent data) {
        try {
            final Uri imageUri = data.getData();
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

            final ImageView img = new ImageView(this);

            img.setImageBitmap(selectedImage);

            img.setOnTouchListener(new ImageViewMotionHandler().getImageOnTouchListener());

            imageViewHolder.addView(img);
            img.setId(imageViewHolder.getChildCount());

            currentImageView = img;

            disableImageViewHolderViews();
            img.setEnabled(true);
            initAddImagesLayout(optionMenuScrollViewLinearLayout);
            paintView.setDrawing(false);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void selectPicture(int requestCode) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, requestCode);
    }

    private void disableImageViewHolderViews(){
        for(int i = 1; i < imageViewHolder.getChildCount(); i++) {
            if(imageViewHolder.getChildAt(i) instanceof ImageView){
                //Log.d("viewholder", "" + imageViewHolder.getChildAt(i) + " id: " + imageViewHolder.getChildAt(i).getId());
                imageViewHolder.getChildAt(i).setEnabled(false);
            }
        }
    }

    private void initNewProjectDialog() {
        try {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(EditorActivity.this);
            dialog.setTitle("Nowy projekt");
            dialog.setMessage("Czy chcesz stworzyć nowy projekt?");

            dialog.setCancelable(true);
            dialog.setNegativeButton("ANULUJ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            dialog.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    paintView.clear();

                    int count = imageViewHolder.getChildCount();

                    for(int i = count-1; i > 0; i--){
                        Log.d("viewholder", "i: " + i);
                        if(imageViewHolder.getChildAt(i) instanceof ImageView){
                            imageViewHolder.removeView(imageViewHolder.getChildAt(i));
                        }
                    }
                    imageViewHolder.getChildAt(1).setId(paintViewId);
                    optionMenuLayout.setVisibility(View.GONE);
                    buttonAddImageMenu.setVisibility(View.GONE);
                    loadBitmap(originalBitmap);
                }
            });

            dialog.show();
        } catch (Exception e) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(EditorActivity.this);
            dialog.setTitle("Błąd");
            dialog.setMessage("Nie można zapisać pliku: " + e.toString());
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.show();
        }
    }

    private void initDeleteImageViewLayerDialog(final ImageView imageView) {
        try {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(EditorActivity.this);
            dialog.setTitle("Usuń warstwę");
            dialog.setMessage("Czy chcesz usunąć warstwę?");

            dialog.setCancelable(true);
            dialog.setNegativeButton("ANULUJ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            dialog.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("id", "" + imageView.getId());
                    int id = imageView.getId();
                    imageViewHolder.removeView(imageView);
                    for(int i = id - 1; i < imageViewHolder.getChildCount(); i++){
                        imageViewHolder.getChildAt(i).setId(i+1);
                    }
                    initAddImagesLayout(optionMenuScrollViewLinearLayout);
                }
            });

            dialog.show();
        } catch (Exception e) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(EditorActivity.this);
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

    private void initSaveDialog() {
        try {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(EditorActivity.this);
            dialog.setTitle("Zapisywanie");
            dialog.setMessage("Czy chcesz stworzyć nowy plik?");

            dialog.setCancelable(true);
            dialog.setNegativeButton("ANULUJ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            dialog.setNeutralButton("NADPISZ PLIK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    ManageFilesUtils.deleteFile(pictureFilePath, EditorActivity.this);
                    saveFile();
                    //originalBitmap = temporaryBitmap;
                    originalBitmap = BitmapFactory.decodeFile(tempFilepath.getPath());
                }
            });

            dialog.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    saveFile();
                }
            });

            dialog.show();
        } catch (Exception e) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(EditorActivity.this);
            dialog.setTitle("Błąd");
            dialog.setMessage("Nie można zapisać pliku: " + e.toString());
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.show();
        }
    }

    private void saveFile() {
        try {
            ManageFilesUtils.createFileFolder(Variables.APP_PICTURES_DIRECTORY, EditorActivity.this);

            imageViewHolder.setDrawingCacheEnabled(true);
            imageViewHolder.buildDrawingCache();
            Bitmap bitmap = imageViewHolder.getDrawingCache();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            File imageFile = new File(Variables.APP_PICTURES_DIRECTORY.getAbsolutePath() + File.separator + simpleDateFormat.format(new Date()) + ".jpg");
            imageFile.createNewFile();

            FileOutputStream fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);

            tempFilepath = imageFile;

            fout.flush();
            fout.close();
            bitmap.recycle();

            imageViewHolder.destroyDrawingCache();

            Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaStoreUpdateIntent.setData(Uri.fromFile(new File(String.valueOf(imageFile))));
            sendBroadcast(mediaStoreUpdateIntent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initAddImagesLayout(final ViewGroup layout) {

        layout.removeAllViews();

        paintView.setDrawing(false);

        if(imageViewHolder.getChildCount() >= 3){
            imageOnTopBottomLayout.setVisibility(View.VISIBLE);
            int viewId = currentImageView.getId();
            if(viewId >= imageViewHolder.getChildCount()){
                buttonImageOnTop.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
                buttonImageOnTop.setEnabled(false);
                buttonImageOnBottom.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorYellow)));
                buttonImageOnBottom.setEnabled(true);
            }else if(viewId <= 2){
                buttonImageOnTop.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorYellow)));
                buttonImageOnTop.setEnabled(true);
                buttonImageOnBottom.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
                buttonImageOnBottom.setEnabled(false);
            }else{
                buttonImageOnTop.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorYellow)));
                buttonImageOnTop.setEnabled(true);
                buttonImageOnBottom.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorYellow)));
                buttonImageOnBottom.setEnabled(true);
            }
        }else{
            imageOnTopBottomLayout.setVisibility(View.GONE);
        }

        for(int i = 1; i < imageViewHolder.getChildCount(); i++){
            if(imageViewHolder.getChildAt(i) instanceof ImageView){
                final ImageView view = (ImageView)imageViewHolder.getChildAt(i);

                final ImageButton imageButton = new ImageButton(this);
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.dim60dp), LinearLayout.LayoutParams.MATCH_PARENT);
                param.gravity = Gravity.CENTER;

                imageButton.setLayoutParams(param);
                imageButton.setImageDrawable(view.getDrawable());
                if(currentImageView == view){
                    imageButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));
                    view.setEnabled(true);
                }else{
                    imageButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhiteTransparent)));
                }
                imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentImageView = view;
                        initAddImagesLayout(layout);
                        disableImageViewHolderViews();
                        view.setEnabled(true);
                    }
                });
                imageButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        initDeleteImageViewLayerDialog(view);
                        return false;
                    }
                });

                layout.addView(imageButton);
            }
        }
    }

    private void initBrush(ViewGroup layout) {

        layout.removeAllViews();

        for (final int color : PaintView.BRUSH_COLORS) {
            Button imageButton = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(5, 5, 5, 5);

            imageButton.setLayoutParams(params);
            imageButton.setBackgroundColor(getResources().getColor(color));
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    paintView.setDrawing(true);
                    paintView.setBrushColor(getResources().getColor(color));
                    seekBarsLinearLayout.setVisibility(View.VISIBLE);

                }
            });

            layout.addView(imageButton);
        }

    }

    private void initTopLayoutButtons() {
        buttonSave = findViewById(R.id.editor_image_button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (originalBitmap != null) {
                    initSaveDialog();
                }
            }
        });

        buttonNewProject = findViewById(R.id.editor_image_new_project);
        buttonNewProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap == null) {
                    selectPicture(Variables.RESULT_LOAD_IMAGE);
                } else {
                    initNewProjectDialog();
                }
            }
        });
    }

    private void initBottomLayoutButtons() {

        optionMenuLayout = findViewById(R.id.editor_option_menu_layout);
        optionMenuScrollViewLinearLayout = findViewById(R.id.editor_filter_menu_linear_layout);

        buttonQuitMenu = findViewById(R.id.editor_button_quit_menu);
        buttonQuitMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableImageViewHolderViews();
                seekBarsLinearLayout.setVisibility(View.GONE);
                textViewBrushAlpha.setVisibility(View.VISIBLE);
                seekBarBrushAlpha.setVisibility(View.VISIBLE);
                buttonAddImageMenu.setVisibility(View.GONE);
                optionMenuLayout.setVisibility(View.GONE);
                imageOnTopBottomLayout.setVisibility(View.GONE);
            }
        });

        buttonAddImageMenu = findViewById(R.id.editor_button_add_image_option_menu);
        buttonAddImageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture(RESULT_ADDITIONAL_IMAGE);
                initAddImagesLayout(optionMenuScrollViewLinearLayout);
            }
        });

        buttonFilter = findViewById(R.id.editor_button_filter);
        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionMenuLayout.setVisibility(View.VISIBLE);
                initFilters(optionMenuScrollViewLinearLayout);
            }
        });

        seekBarsLinearLayout = findViewById(R.id.editor_seek_bars_layout);

        seekBarBrushSize = findViewById(R.id.editor_seek_bar_brush_size);
        seekBarBrushSize.setProgress(paintView.DEFAULT_BRUSH_SIZE);
        seekBarBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isEraser) {
                    paintView.setEraserSize(progress);
                } else {
                    paintView.setBrushSize(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        textViewBrushAlpha = findViewById(R.id.editor_seek_bar_brush_alpha_text);
        seekBarBrushAlpha = findViewById(R.id.editor_seek_bar_brush_alpha);
        seekBarBrushAlpha.setProgress(paintView.DEFAULT_OPACITY);
        seekBarBrushAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paintView.setOpacity(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        buttonBrush = findViewById(R.id.editor_button_brush);
        buttonBrush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEraser = false;
                optionMenuLayout.setVisibility(View.VISIBLE);
                seekBarsLinearLayout.setVisibility(View.GONE);
                initBrush(optionMenuScrollViewLinearLayout);
            }
        });

        buttonEraser = findViewById(R.id.editor_button_eraser);
        buttonEraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEraser = true;
                optionMenuLayout.setVisibility(View.VISIBLE);
                textViewBrushAlpha.setVisibility(View.GONE);
                seekBarBrushAlpha.setVisibility(View.GONE);
                seekBarsLinearLayout.setVisibility(View.VISIBLE);
                paintView.eraser();
            }
        });

        imageOnTopBottomLayout = findViewById(R.id.editor_button_add_image_on_top_bottom_layout);
        buttonImageOnTop = findViewById(R.id.editor_button_add_image_on_top);
        buttonImageOnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int viewId = currentImageView.getId();
                List<View> tempList = new ArrayList<>();
                Log.d("btb", "" + currentImageView.getId());
                tempList.add(imageViewHolder.getChildAt(viewId - 1));
                imageViewHolder.getChildAt(viewId).setId(viewId);
                imageViewHolder.removeView(imageViewHolder.getChildAt(viewId - 1));

                while(imageViewHolder.getChildAt(viewId) != null){
                    tempList.add(imageViewHolder.getChildAt(viewId));
                    imageViewHolder.removeView(imageViewHolder.getChildAt(viewId));
                }
                for(View view : tempList){
                    Log.d("temp", "" + view);
                    view.setId(++viewId); //++viewId
                    imageViewHolder.addView(view);
                }
                initAddImagesLayout(optionMenuScrollViewLinearLayout);
            }
        });
        buttonImageOnBottom = findViewById(R.id.editor_button_add_image_on_bottom);
        buttonImageOnBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int viewId = currentImageView.getId();
                List<View> tempList = new ArrayList<>();
                Log.d("btb", "" + currentImageView.getId());
                tempList.add(imageViewHolder.getChildAt(viewId - 2));
                imageViewHolder.getChildAt(viewId-1).setId(viewId-1);
                imageViewHolder.removeView(imageViewHolder.getChildAt(viewId - 2));

                while(imageViewHolder.getChildAt(viewId-1) != null){
                    tempList.add(imageViewHolder.getChildAt(viewId-1));
                    imageViewHolder.removeView(imageViewHolder.getChildAt(viewId-1));
                }
                for(View view : tempList){
                    Log.d("temp", "" + view);
                    view.setId(viewId++);
                    imageViewHolder.addView(view);
                }
                initAddImagesLayout(optionMenuScrollViewLinearLayout);

            }
        });
        buttonAddImage = findViewById(R.id.editor_button_add_image);
        buttonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionMenuLayout.setVisibility(View.VISIBLE);
                buttonAddImageMenu.setVisibility(View.VISIBLE);
                initAddImagesLayout(optionMenuScrollViewLinearLayout);
            }
        });
    }

    private void initPictureView() {

        pictureView = new ImageView(this);
        pictureView.setId(pictureViewId);
        pictureView.setAdjustViewBounds(true);

        imageViewHolder.addView(pictureView);

    }

    private void initFilters(ViewGroup layout) {

        layout.removeAllViews();

        String[] filesNames;
        int id = 1;
        try {
            filesNames = getAssets().list("filters");
            for (final String filesName : filesNames) {
                Log.d("Asset", filesName);

                AssetManager assetManager = getAssets();
                InputStream ims = assetManager.open("filters" + "/" + filesName);
                Drawable image = Drawable.createFromStream(ims, null);

                ImageButton imageButton = new ImageButton(this);
                LinearLayout.LayoutParams paramsImageButton = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

                imageButton.setLayoutParams(paramsImageButton);
                imageButton.setImageDrawable(image);
                imageButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTransparent)));
                imageButton.setId(id++);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Task(originalBitmap, v.getId()).execute();
                    }
                });
                layout.addView(imageButton);
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBitmap(Bitmap bitmap) {
        Glide.with(getApplicationContext())
                .load(bitmap)
                .into(pictureView);
    }

    private class Task extends AsyncTask<Void, Void, Void> {
        private int id;
        private Bitmap bitmap;

        Task(Bitmap bitmap, int id) {
            this.id = id;
            this.bitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... args) {
            temporaryBitmap = Filters.getFilter(bitmap, id);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            loadBitmap(temporaryBitmap);
            if (progress.isShowing()) {
                progress.dismiss();
            }
        }
    }
}
