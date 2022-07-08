package com.example.kwasheniak.zmieniacztwarzy.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.widget.Toast;

import com.example.kwasheniak.zmieniacztwarzy.camerasurfaces.CameraPreview;
import com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils.DrawCustomMasks;
import com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils.DrawEyesMasks;
import com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils.DrawFullMasks;
import com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils.DrawHeadMasks;
import com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils.DrawMouthMasks;
import com.example.kwasheniak.zmieniacztwarzy.drawinggraphicutils.DrawNoseMasks;
import com.example.kwasheniak.zmieniacztwarzy.facetrackingutils.FaceTracker;
import com.example.kwasheniak.zmieniacztwarzy.camerasurfaces.GraphicOverlay;
import com.example.kwasheniak.zmieniacztwarzy.utils.ManageFilesUtils;
import com.example.kwasheniak.zmieniacztwarzy.R;
import com.example.kwasheniak.zmieniacztwarzy.utils.Utils;
import com.example.kwasheniak.zmieniacztwarzy.utils.Variables;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {

    private final static String TAG = "CameraActivity";
    private final static int REQUEST_CODE = 100;
    private final static int VIRTUAL_DISPLAY = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private final static Size DEFAULT_SIZE = new Size(1080, 1920);

    private static Intent screenshotPermission = null;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;

    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;

    private int mNavigationBarHeight;
    private int mStatusBarHeight;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mDensity;

    private Button buttonCapture;
    private ImageButton buttonMenuMasks;
    private ImageButton buttonSwitchCamera;
    private HorizontalScrollView maskChooseMenuScrollView;
    private LinearLayout maskTypeMenuLinearLayout;
    private LinearLayout activityBottomLinearLayout;
    private ConstraintLayout activityTopLinearLayout;

    private Handler mHandler;

    private CameraSource mCameraSource = null;
    private CameraPreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private boolean mIsFrontFacing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        buttonCapture = findViewById(R.id.camera_button_capture);
        buttonMenuMasks = findViewById(R.id.camera_button_stickers);
        buttonSwitchCamera = findViewById(R.id.camera_button_switch_camera);

        maskChooseMenuScrollView = findViewById(R.id.camera_menu_scroll_view);
        maskTypeMenuLinearLayout = findViewById(R.id.camera_menu_linear_layout);

        if (!isCameraExists(CameraMetadata.LENS_FACING_FRONT) && !isCameraExists(CameraMetadata.LENS_FACING_BACK)) {
            initAlertCameraDialog();
        }

        mIsFrontFacing = isCameraExists(CameraMetadata.LENS_FACING_FRONT);

        mPreview = findViewById(R.id.camera_camera_preview);
        mGraphicOverlay = findViewById(R.id.camera_graphic_overlay);

        activityBottomLinearLayout = findViewById(R.id.camera_linear_layout_bottom);
        activityTopLinearLayout = findViewById(R.id.camera_constraint_layout_top);

        mHandler = new Handler();

        mNavigationBarHeight = Utils.getNavigationBarHeight(this);
        mStatusBarHeight = Utils.getStatusBarHeight(this);
        Point screenSize = Utils.getRealScreenSize(this);
        mScreenWidth = screenSize.x;
        mScreenHeight = screenSize.y;
        mDensity = Utils.getDensity(this);

        ManageFilesUtils.createFileFolder(Variables.APP_PICTURES_DIRECTORY,this);

        getCameraSize();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                setupMediaProjection();
                initButtonsCallbacks();
            }
        }, 300);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
                screenshotPermission = data;
            }
        }
    }

    @Override
    protected void onResume() {
        //Log.d(TAG, "onResume");
        super.onResume();

        createCameraSource();
        startCameraSource();
        showLayout();
        Log.d("onLayout", "onResume");
    }

    @Override
    protected void onPause() {
        //Log.d(TAG, "onPause");
        super.onPause();
        destroyCameraSource();
    }

    @Override
    protected void onDestroy() {
        //Log.d(TAG, "onDestroy");
        super.onDestroy();
        destroyCameraSource();
        stopMediaProjection();
    }

    private void initAlertCameraDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(CameraActivity.this);
        dialog.setTitle("Uwaga");
        dialog.setMessage("Nie znaleziono kamery");

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.show();
    }

    private boolean isCameraExists(int camera) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if(camera == facing){
                    return true;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void getCameraSize() {
        Log.d("onLayout", "getcamerasize");
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {

            Size optimalSize = DEFAULT_SIZE;
            int previewWidth = mScreenWidth;
            int previewHeight = mScreenHeight - (mNavigationBarHeight + mStatusBarHeight);

            int diff = previewHeight;
            boolean isWidth = false;

            for (String cameraId : manager.getCameraIdList()) {

                CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);

                if(CameraMetadata.LENS_FACING_FRONT == facing || CameraMetadata.LENS_FACING_BACK == facing){
                    StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    Size[] outputSizes = map.getOutputSizes(SurfaceTexture.class);
                    for(Size size : outputSizes){
                        Log.d("size", size.getWidth() + " " + size.getHeight());
                        if(size.getHeight() == previewWidth){
                            isWidth = true;
                            int temp = Math.abs(previewHeight - size.getWidth());
                            if(temp < diff){
                                diff = temp;
                                optimalSize = size;
                            }
                        }
                    }
                    if(!isWidth){
                        for(Size size : outputSizes){
                            if(size.getHeight() < previewWidth && size.getHeight() > previewWidth/2){
                                int temp = previewWidth - size.getHeight();
                                if(temp < diff){
                                    diff = temp;
                                    optimalSize = size;
                                }
                            }
                        }
                    }
                    Log.d("size", mScreenWidth + " " + (mScreenHeight - (mNavigationBarHeight + mStatusBarHeight)));
                }
            }
            Variables.PREVIEW_SIZE = optimalSize;
            Log.d("size", "optimal: " + optimalSize.getWidth() + " " + optimalSize.getHeight());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void showLayout() {
        activityBottomLinearLayout.setVisibility(View.VISIBLE);
        activityTopLinearLayout.setVisibility(View.VISIBLE);
    }

    private void hideLayout() {
        activityBottomLinearLayout.setVisibility(View.GONE);
        activityTopLinearLayout.setVisibility(View.GONE);
    }

    private void initButtonsCallbacks() {

        buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        buttonMenuMasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maskChooseMenuScrollView.getVisibility() == View.GONE) {
                    initMaskTypeMenuButtons();
                    maskChooseMenuScrollView.setVisibility(View.VISIBLE);
                } else {
                    maskChooseMenuScrollView.setVisibility(View.GONE);
                }
            }
        });

        if(!mIsFrontFacing){
            buttonSwitchCamera.setVisibility(View.INVISIBLE);
        }

        buttonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFrontFacing = !mIsFrontFacing;
                destroyCameraSource();
                getCameraSize();
                createCameraSource();
                startCameraSource();
            }
        });
    }

    private void initMaskTypeMenuButtons() {

        maskTypeMenuLinearLayout.removeAllViews();

        MaterialButton eraseButton = createMaterialButton(R.string.menu_scrollView_erase, R.color.colorWhite, R.drawable.icon_erase);
        MaterialButton masksButton = createMaterialButton(R.string.menu_scrollView_masks, R.color.colorYellow, R.drawable.icon_mask);
        MaterialButton headButton = createMaterialButton(R.string.menu_scrollView_head, R.color.colorYellow, R.drawable.icon_head);
        MaterialButton eyesButton = createMaterialButton(R.string.menu_scrollView_eyes, R.color.colorYellow, R.drawable.icon_eyes);
        MaterialButton noseButton = createMaterialButton(R.string.menu_scrollView_nose, R.color.colorYellow, R.drawable.icon_nose);
        MaterialButton mouthButton = createMaterialButton(R.string.menu_scrollView_mouth, R.color.colorYellow, R.drawable.icon_mouth);
        MaterialButton customMaskButton = createMaterialButton(R.string.menu_scrollView_custom_mask, R.color.colorYellow, R.drawable.icon_mask);

        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Variables.MASK_TYPES.clear();
            }
        });

        masksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFunctionalButtons("masks");
                initDefaultMasks("masks");
            }
        });
        headButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFunctionalButtons("head");
                initDefaultMasks("head");
            }
        });
        eyesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFunctionalButtons("eyes");
                initDefaultMasks("eyes");
            }
        });

        noseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFunctionalButtons("nose");
                initDefaultMasks("nose");
            }
        });
        mouthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFunctionalButtons("mouth");
                initDefaultMasks("mouth");
            }
        });
        customMaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFunctionalButtons(Variables.CUSTOM_MASKS_DIRECTORY_NAME);
                initCustomMasks();
            }
        });

        maskTypeMenuLinearLayout.addView(eraseButton);
        maskTypeMenuLinearLayout.addView(masksButton);
        maskTypeMenuLinearLayout.addView(headButton);
        maskTypeMenuLinearLayout.addView(eyesButton);
        maskTypeMenuLinearLayout.addView(noseButton);
        maskTypeMenuLinearLayout.addView(mouthButton);
        maskTypeMenuLinearLayout.addView(customMaskButton);
    }

    private void initDefaultMasks(final String folderName) {

        String[] filesNames;
        try {
            filesNames = getAssets().list(folderName);
            for (final String fileName : filesNames) {
                AssetManager assetManager = getAssets();
                InputStream ims = assetManager.open(folderName + "/" + fileName);
                Drawable image = Drawable.createFromStream(ims, null);

                ImageButton imageButton = new ImageButton(this);
                LinearLayout.LayoutParams paramsImageButton = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.dim60dp), LinearLayout.LayoutParams.MATCH_PARENT);
                paramsImageButton.setMargins((int) getResources().getDimension(R.dimen.dim2dp), (int) getResources().getDimension(R.dimen.dim2dp), (int) getResources().getDimension(R.dimen.dim2dp), (int) getResources().getDimension(R.dimen.dim2dp));
                paramsImageButton.gravity = Gravity.CENTER;

                imageButton.setLayoutParams(paramsImageButton);
                imageButton.setImageDrawable(image);
                imageButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhiteTransparent)));
                imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Drawable drawable = null;
                        try {
                            AssetManager assetManager = getAssets();
                            InputStream ims = assetManager.open(folderName + "/" + fileName);
                            drawable = Drawable.createFromStream(ims, null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        switch (folderName) {
                            case "mouth":
                                Variables.MASK_TYPES.put(folderName, new DrawMouthMasks(fileName, drawable));
                                break;
                            case "nose":
                                Variables.MASK_TYPES.put(folderName, new DrawNoseMasks(fileName, drawable));
                                break;
                            case "eyes":
                                Variables.MASK_TYPES.put(folderName, new DrawEyesMasks(fileName, drawable));
                                break;
                            case "head":
                                Variables.MASK_TYPES.put(folderName, new DrawHeadMasks(fileName, drawable));
                                break;
                            case "masks":
                                Variables.MASK_TYPES.put(folderName, new DrawFullMasks(fileName, drawable));
                                break;
                        }
                    }
                });
                maskTypeMenuLinearLayout.addView(imageButton);
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    private void initCustomMasks() {
        String[] filesNames;
        try {
            filesNames = Variables.CUSTOM_MASKS_DIRECTORY.list();
            for (final String filesName : filesNames) {

                Drawable image = Drawable.createFromPath(new File(Variables.CUSTOM_MASKS_DIRECTORY, filesName).getAbsolutePath());

                ImageButton imageButton = new ImageButton(CameraActivity.this);
                LinearLayout.LayoutParams paramsImageButton = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.dim60dp), LinearLayout.LayoutParams.MATCH_PARENT);
                paramsImageButton.setMargins((int) getResources().getDimension(R.dimen.dim2dp), (int) getResources().getDimension(R.dimen.dim2dp), (int) getResources().getDimension(R.dimen.dim2dp), (int) getResources().getDimension(R.dimen.dim2dp));
                paramsImageButton.gravity = Gravity.CENTER;

                imageButton.setLayoutParams(paramsImageButton);
                imageButton.setImageDrawable(image);
                imageButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhiteTransparent)));
                imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Drawable drawable = Drawable.createFromPath(new File(Variables.CUSTOM_MASKS_DIRECTORY, filesName).getAbsolutePath());
                        Variables.MASK_TYPES.put(Variables.CUSTOM_MASKS_DIRECTORY_NAME, new DrawCustomMasks(CameraActivity.this, drawable));
                    }
                });
                maskTypeMenuLinearLayout.addView(imageButton);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void initFunctionalButtons(final String folderName) {

        maskTypeMenuLinearLayout.removeAllViews();

        MaterialButton backButton = new MaterialButton(new ContextThemeWrapper(CameraActivity.this, R.style.Widget_AppCompat_Button_Borderless));
        LinearLayout.LayoutParams paramsBackButton = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.dim40dp), LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsBackButton.setMargins((int) getResources().getDimension(R.dimen.dim2dp), (int) getResources().getDimension(R.dimen.dim2dp), (int) getResources().getDimension(R.dimen.dim2dp), (int) getResources().getDimension(R.dimen.dim2dp));
        paramsBackButton.gravity = Gravity.CENTER;

        backButton.setLayoutParams(paramsBackButton);
        backButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTransparent)));
        backButton.setCornerRadius((int) getResources().getDimension(R.dimen.dim10dp));
        backButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));
        backButton.setStrokeWidth((int) getResources().getDimension(R.dimen.dim1dp));
        backButton.setIcon(getResources().getDrawable(R.drawable.icon_back));
        backButton.setIconSize((int) getResources().getDimension(R.dimen.dim17dp));
        backButton.setIconGravity(MaterialButton.ICON_GRAVITY_START);
        backButton.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));

        MaterialButton eraseButton = createMaterialButton(R.string.menu_scrollView_erase, R.color.colorWhite, R.drawable.icon_erase);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMaskTypeMenuButtons();
            }
        });

        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Variables.MASK_TYPES.remove(folderName);
            }
        });

        maskTypeMenuLinearLayout.addView(backButton);
        maskTypeMenuLinearLayout.addView(eraseButton);
    }

    private MaterialButton createMaterialButton(int text, int color, int icon) {
        MaterialButton materialButton = new MaterialButton(new ContextThemeWrapper(CameraActivity.this, R.style.Widget_AppCompat_Button_Borderless));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) getResources().getDimension(R.dimen.dim2dp), (int) getResources().getDimension(R.dimen.dim2dp), (int) getResources().getDimension(R.dimen.dim2dp), (int) getResources().getDimension(R.dimen.dim2dp));
        params.gravity = Gravity.CENTER;

        materialButton.setLayoutParams(params);
        materialButton.setText(text);
        materialButton.setTextColor(getResources().getColor(color));
        materialButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.dim12sp));
        materialButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTransparent)));
        materialButton.setCornerRadius((int) getResources().getDimension(R.dimen.dim10dp));
        materialButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(color)));
        materialButton.setStrokeWidth((int) getResources().getDimension(R.dimen.dim1dp));
        materialButton.setIcon(getResources().getDrawable(icon));
        materialButton.setIconSize((int) getResources().getDimension(R.dimen.dim17dp));
        materialButton.setIconGravity(MaterialButton.ICON_GRAVITY_START);
        materialButton.setIconTint(ColorStateList.valueOf(getResources().getColor(color)));

        return materialButton;
    }

    private void takePicture() {

        if (mMediaProjection != null) {

            hideLayout();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
                    try {
                        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture", mScreenWidth, mScreenHeight, mDensity, VIRTUAL_DISPLAY, mImageReader.getSurface(), null, mHandler);
                    } catch (SecurityException e) {
                        startMediaProjection();
                    }
                    mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                        @Override
                        public void onImageAvailable(ImageReader reader) {
                            try {
                                reader.setOnImageAvailableListener(null, mHandler);

                                Image image = reader.acquireLatestImage();

                                Image.Plane[] planes = image.getPlanes();
                                ByteBuffer buffer = planes[0].getBuffer();

                                int pixelStride = planes[0].getPixelStride();
                                int rowStride = planes[0].getRowStride();
                                int rowPadding = rowStride - pixelStride * mScreenWidth;

                                Bitmap bitmap = Bitmap.createBitmap(mScreenWidth + rowPadding / pixelStride, mScreenHeight, Bitmap.Config.ARGB_8888);
                                bitmap.copyPixelsFromBuffer(buffer);

                                image.close();
                                reader.close();

                                Bitmap modifiedBitmap = Bitmap.createBitmap(bitmap, 0, mStatusBarHeight, mScreenWidth, bitmap.getHeight() - (mNavigationBarHeight + mStatusBarHeight));
                                bitmap.recycle();

                                ManageFilesUtils.createFileFolder(Variables.APP_PICTURES_DIRECTORY,CameraActivity.this);

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                                File imageFile = new File(Variables.APP_PICTURES_DIRECTORY.getAbsolutePath() + File.separator + simpleDateFormat.format(new Date()) + ".jpg");
                                imageFile.createNewFile();

                                FileOutputStream fout = new FileOutputStream(imageFile);
                                modifiedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                                fout.flush();
                                fout.close();
                                modifiedBitmap.recycle();
                                mVirtualDisplay.release();

                                Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                mediaStoreUpdateIntent.setData(Uri.fromFile(new File(String.valueOf(imageFile))));
                                sendBroadcast(mediaStoreUpdateIntent);

                                Intent intent = new Intent(CameraActivity.this, PreviewPictureActivity.class);
                                CameraActivity.this.startActivity(intent.putExtra(Variables.PUT_EXTRA_PICTURE_NAME, imageFile.getAbsolutePath()));

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }, mHandler);
                }
            }, 50);
        } else {
            startMediaProjection();
        }
    }

    private void setupMediaProjection(){
        if (screenshotPermission == null) {
            startMediaProjection();
        } else {
            mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mMediaProjection = mProjectionManager.getMediaProjection(RESULT_OK, screenshotPermission);
        }
    }

    private void startMediaProjection() {
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopMediaProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMediaProjection != null) {
                    mMediaProjection.stop();
                }
            }
        });
    }

    private void startCameraSource() {
        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void destroyCameraSource() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    private void createCameraSource() {

        FaceDetector detector = createFaceDetector(getApplicationContext());

        int facing = CameraSource.CAMERA_FACING_FRONT;
        if (!mIsFrontFacing) {
            facing = CameraSource.CAMERA_FACING_BACK;
        }

        Log.d("taf", "create: " + mPreview.getWidth() + "x" + mPreview.getHeight());

        mCameraSource = new CameraSource.Builder(getApplicationContext(), detector)
                .setFacing(facing)
                .setRequestedPreviewSize(Variables.PREVIEW_SIZE.getWidth(), Variables.PREVIEW_SIZE.getHeight())
                .setRequestedFps(60.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    private FaceDetector createFaceDetector(final Context context) {

        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setTrackingEnabled(true)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setProminentFaceOnly(true)
                //.setMinFaceSize(mIsFrontFacing ? 0.35f : 0.15f)
                .setMinFaceSize(0.35f)
                .build();



        MultiProcessor.Factory<Face> factory = new MultiProcessor.Factory<Face>() {
            @Override
            public Tracker<Face> create(Face face) {
                Log.d("Tracker", "tracker ");
                return new FaceTracker(mGraphicOverlay, mIsFrontFacing);
            }
        };

        Detector.Processor<Face> processor = new MultiProcessor.Builder<>(factory).build();
        detector.setProcessor(processor);

        return detector;
    }

}
