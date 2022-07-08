package com.example.kwasheniak.zmieniacztwarzy.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
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
import java.util.List;

public class MasksCreatorActivity extends AppCompatActivity {

    private static Bitmap BITMAP;

    private static int paintViewId = 1;

    private SparseArray<Face> detectedFaces;

    private ImageView backgroundImageView;
    private ImageButton buttonSave;

    private PaintView paintView;


    private ImageButton buttonNewProject;
    private RelativeLayout buttonEraser;
    private RelativeLayout buttonBrush;
    private RelativeLayout buttonAddImage;
    private ImageButton buttonQuitMenu;
    private ImageButton buttonAddImageMenu;

    private LinearLayout imageOnTopBottomLayout;
    private ImageButton buttonImageOnTop;
    private ImageButton buttonImageOnBottom;

    private LinearLayout optionMenuLayout;
    private LinearLayout optionMenuScrollViewLinearLayout;
    private LinearLayout seekBarsLinearLayout;
    private RelativeLayout imageViewHolder;

    private SeekBar seekBarBrushSize;
    private SeekBar seekBarBrushAlpha;
    private TextView textViewBrushAlpha;

    private boolean isEraser = false;

    private SharedPreferences preferences;

    private ImageView currentImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masks_creator);
        imageViewHolder = findViewById(R.id.masks_creator_relative_layout_holder);
        backgroundImageView = findViewById(R.id.masks_creator_image_view);

        preferences = getSharedPreferences(Variables.SHARED_PREF, Activity.MODE_PRIVATE);

        backgroundImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                backgroundImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initBackgroundImage();
            }
        });
        setImageViewHolderSize();
        initTopLayoutButtons();
        initBottomLayoutButtons();

    }

    private void setImageViewHolderSize(){
        imageViewHolder.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.d("ivh", "wysokosc: " + imageViewHolder.getHeight() + " szerokosc: " + imageViewHolder.getWidth());
                if(imageViewHolder.getHeight() > 0 && imageViewHolder.getWidth() > 0){
                    imageViewHolder.getViewTreeObserver().removeOnPreDrawListener(this);
                    paintView = new PaintView(MasksCreatorActivity.this);
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
                addSelectedImage(data);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addSelectedImage(Intent data){
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
        for(int i = 0; i < imageViewHolder.getChildCount(); i++) {
            if(imageViewHolder.getChildAt(i) instanceof ImageView){
                //Log.d("viewholder", "" + imageViewHolder.getChildAt(i) + " id: " + imageViewHolder.getChildAt(i).getId());
                imageViewHolder.getChildAt(i).setEnabled(false);
            }
        }
    }

    private void initNewProjectDialog() {
        try {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(MasksCreatorActivity.this);
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

                    for(int i = count-1; i >= 0; i--){
                        Log.d("viewholder", "i: " + i);
                        if(imageViewHolder.getChildAt(i) instanceof ImageView){
                            imageViewHolder.removeView(imageViewHolder.getChildAt(i));
                        }
                    }

                    /*while(imageViewHolder.getChildAt(0) != null && imageViewHolder.getChildAt(0) instanceof ImageView){
                        imageViewHolder.removeView(imageViewHolder.getChildAt(0));
                    }*/

                    imageViewHolder.getChildAt(0).setId(paintViewId);

                    optionMenuLayout.setVisibility(View.GONE);
                    buttonAddImageMenu.setVisibility(View.GONE);
                }
            });

            dialog.show();
        } catch (Exception e) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(MasksCreatorActivity.this);
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
            final AlertDialog.Builder dialog = new AlertDialog.Builder(MasksCreatorActivity.this);
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
            final AlertDialog.Builder dialog = new AlertDialog.Builder(MasksCreatorActivity.this);
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

    private void initSaveDialog(){
        try {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(MasksCreatorActivity.this);
            dialog.setTitle("Zapisywanie");
            dialog.setMessage("Czy chcesz zapisać plik?");

            dialog.setCancelable(true);
            dialog.setNegativeButton("ANULUJ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
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
            final AlertDialog.Builder dialog = new AlertDialog.Builder(MasksCreatorActivity.this);
            dialog.setTitle("Błąd");
            dialog.setMessage("Nie można zapisać pliku: " + e.toString());
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
        }
    }

    private void saveFile(){
        try {
            ManageFilesUtils.createFileFolder(Variables.CUSTOM_MASKS_DIRECTORY, MasksCreatorActivity.this);

            imageViewHolder.setDrawingCacheEnabled(true);
            imageViewHolder.buildDrawingCache();
            Bitmap bitmap = imageViewHolder.getDrawingCache();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            File imageFile = new File(Variables.CUSTOM_MASKS_DIRECTORY, simpleDateFormat.format(new Date()) + ".png");
            imageFile.createNewFile();

            FileOutputStream fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, fout);

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

        if(imageViewHolder.getChildCount() >= 2){
            imageOnTopBottomLayout.setVisibility(View.VISIBLE);
            int viewId = currentImageView.getId();
            if(viewId >= imageViewHolder.getChildCount()){
                buttonImageOnTop.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
                buttonImageOnTop.setEnabled(false);
                buttonImageOnBottom.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorYellow)));
                buttonImageOnBottom.setEnabled(true);
            }else if(viewId <= 1){
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

        for(int i = 0; i < imageViewHolder.getChildCount(); i++){
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

    private void initBrush(final ViewGroup layout) {

        layout.removeAllViews();

        for(final int color : PaintView.BRUSH_COLORS){
            Button imageButton = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(5,5,5,5);
            params.gravity = Gravity.CENTER;

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

    private void initTopLayoutButtons(){
        buttonSave = findViewById(R.id.masks_creator_image_button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSaveDialog();
            }
        });

        buttonNewProject = findViewById(R.id.masks_creator_new_project);
        buttonNewProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initNewProjectDialog();
            }
        });
    }

    private void initBottomLayoutButtons(){

        optionMenuLayout = findViewById(R.id.masks_creator_linear_layout_scroll);
        optionMenuScrollViewLinearLayout = findViewById(R.id.masks_creator_menu_linear_layout);
        seekBarsLinearLayout = findViewById(R.id.masks_creator_seek_bars_layout);

        buttonQuitMenu = findViewById(R.id.masks_creator_button_quit_menu);
        buttonQuitMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableImageViewHolderViews();
                optionMenuLayout.setVisibility(View.GONE);
                seekBarsLinearLayout.setVisibility(View.GONE);
                buttonAddImageMenu.setVisibility(View.GONE);
                textViewBrushAlpha.setVisibility(View.VISIBLE);
                seekBarBrushAlpha.setVisibility(View.VISIBLE);
                imageOnTopBottomLayout.setVisibility(View.GONE);
            }
        });

        buttonAddImageMenu = findViewById(R.id.masks_creator_button_add_image_option_menu);
        buttonAddImageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture(Variables.RESULT_LOAD_IMAGE);
                initAddImagesLayout(optionMenuScrollViewLinearLayout);
            }
        });

        buttonEraser = findViewById(R.id.masks_creator_button_eraser);
        buttonEraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEraser = true;
                paintView.eraser();
                textViewBrushAlpha.setVisibility(View.GONE);
                seekBarBrushAlpha.setVisibility(View.GONE);
                optionMenuLayout.setVisibility(View.VISIBLE);
                seekBarsLinearLayout.setVisibility(View.VISIBLE);
            }
        });

        imageOnTopBottomLayout = findViewById(R.id.masks_creator_button_add_image_on_top_bottom_layout);
        buttonImageOnTop = findViewById(R.id.masks_creator_button_add_image_on_top);
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
        buttonImageOnBottom = findViewById(R.id.masks_creator_button_add_image_on_bottom);
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
                    view.setId(viewId++); //++viewId
                    imageViewHolder.addView(view);
                }
                initAddImagesLayout(optionMenuScrollViewLinearLayout);

            }
        });

        buttonAddImage = findViewById(R.id.masks_creator_button_add_image);
        buttonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionMenuLayout.setVisibility(View.VISIBLE);
                buttonAddImageMenu.setVisibility(View.VISIBLE);
                initAddImagesLayout(optionMenuScrollViewLinearLayout);
            }
        });

        buttonBrush = findViewById(R.id.masks_creator_button_brush);
        buttonBrush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEraser = false;
                optionMenuLayout.setVisibility(View.VISIBLE);
                seekBarsLinearLayout.setVisibility(View.GONE);
                initBrush(optionMenuScrollViewLinearLayout);
            }
        });

        textViewBrushAlpha = findViewById(R.id.masks_creator_seek_bar_brush_alpha_text);
        seekBarBrushAlpha = findViewById(R.id.masks_creator_seek_bar_brush_alpha);
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

        seekBarBrushSize = findViewById(R.id.masks_creator_seek_bar_brush_size);
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
    }

    private void initBackgroundImage(){
        Variables.IMAGE_VIEW_SIZE = new Size(backgroundImageView.getWidth(), backgroundImageView.getHeight());

        float centerViewX = Variables.IMAGE_VIEW_SIZE.getWidth() / 2.0f;
        float centerViewY = Variables.IMAGE_VIEW_SIZE.getHeight() / 2.0f;

        backgroundImageView.setDrawingCacheEnabled(true);
        backgroundImageView.buildDrawingCache();
        Bitmap imageViewBitmap = backgroundImageView.getDrawingCache();
        detectedFaces = Utils.getFacesArray(imageViewBitmap, MasksCreatorActivity.this);

        if(detectedFaces != null){
            Face face = detectedFaces.valueAt(0);
            float centerX = face.getPosition().x + face.getWidth() / 2.0f;
            float centerY = face.getPosition().y + face.getHeight() / 2.0f;

            float ratioHeight = Variables.IMAGE_VIEW_SIZE.getHeight() / face.getHeight();
            float ratioWidth = Variables.IMAGE_VIEW_SIZE.getWidth() / face.getWidth();

            SharedPreferences.Editor preferencesEditor = preferences.edit();
            preferencesEditor.putFloat(Variables.RATIO_HEIGHT, ratioHeight);
            preferencesEditor.putFloat(Variables.RATIO_WIDTH, ratioWidth);
            preferencesEditor.apply();

            float cutX = centerViewX - centerX;
            float cutY = centerViewY - centerY;

            BITMAP = centerBitmap(imageViewBitmap, cutX, cutY);

            Glide.with(getApplicationContext())
                    .load(BITMAP)
                    .into(backgroundImageView);

        }else{
            Toast.makeText(MasksCreatorActivity.this, "Nie wykryto twarzy", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap centerBitmap(Bitmap originalBitmap, float cutX, float cutY) {
        Bitmap cutBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        cutBitmap.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(cutBitmap);
        canvas.drawBitmap(originalBitmap, cutX, cutY, null);
        return cutBitmap;
    }
}
