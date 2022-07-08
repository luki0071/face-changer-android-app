package com.example.kwasheniak.zmieniacztwarzy.imageeditorutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.kwasheniak.zmieniacztwarzy.R;

import java.util.Stack;

public class PaintView extends View {

    public static int[] BRUSH_COLORS = {
            R.color.colorBrushWhite,
            R.color.colorBrushBlack,
            R.color.colorBrushBrown,
            R.color.colorBrushRed,
            R.color.colorBrushPink,
            R.color.colorBrushPurple,
            R.color.colorBrushLightBlue,
            R.color.colorBrushDarkBlue,
            R.color.colorBrushLightGreen,
            R.color.colorBrushDarkGreen,
            R.color.colorBrushYellow,
            R.color.colorBrushOrange
    };

    public static final int DEFAULT_BRUSH_SIZE = 20;
    public static final int DEFAULT_OPACITY = 255;

    public static final int DEFAULT_COLOR = Color.BLACK;
    private static final float TOUCH_TOLERANCE = 4;

    private int mBrushSize = DEFAULT_BRUSH_SIZE;
    private int mOpacity = DEFAULT_OPACITY;
    private int currentColor = DEFAULT_COLOR;

    private final Stack<LinePath> mDrawnPaths = new Stack<>();
    private final Stack<LinePath> mRedoPaths = new Stack<>();
    private final Paint mDrawPaint = new Paint();

    private Canvas mDrawCanvas;

    private Path mPath;
    private float x, y;

    private Context context;

    private boolean isDrawing = false;

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setupPathAndPaint();
    }

    public void setDrawing(boolean bool){
        isDrawing = bool;
    }

    private void setupPathAndPaint() {
        mPath = new Path();
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setDither(true);
        mDrawPaint.setColor(currentColor);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mDrawPaint.setStrokeWidth(mBrushSize);
        mDrawPaint.setAlpha(mOpacity);
        mDrawPaint.setXfermode(null);

    }

    public void init(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mDrawCanvas = new Canvas(bitmap);
    }

    public void eraser() {
        mDrawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void setEraserSize(int size){
        mBrushSize = size;
        setupPathAndPaint();
        mDrawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void setOpacity(int opacity) {
        mOpacity = opacity;
        setupPathAndPaint();
    }

    public void setBrushSize(int size) {
        mBrushSize = size;
        setupPathAndPaint();
    }

    public void setBrushColor(int color) {
        currentColor = color;
        setupPathAndPaint();
    }

    public void clear() {
        mDrawnPaths.clear();
        mRedoPaths.clear();
        if (mDrawCanvas != null) {
            mDrawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (LinePath linePath : mDrawnPaths) {
            canvas.drawPath(linePath.getDrawPath(), linePath.getDrawPaint());
        }
        canvas.drawPath(mPath, mDrawPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isDrawing){
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStart(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchMove(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    touchUp();
                    break;
            }
            invalidate();
            return true;
        }
        return false;
    }

    private void touchStart(float x, float y) {
        mRedoPaths.clear();
        mPath.reset();
        mPath.moveTo(x, y);
        this.x = x;
        this.y = y;
    }

    private void touchMove(float x, float y) {
        float deltaX = Math.abs(x - this.x);
        float deltaY = Math.abs(y - this.y);
        if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
            mPath.quadTo(this.x, this.y, (x + this.x) / 2, (y + this.y) / 2);
            this.x = x;
            this.y = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(x, y);
        //mDrawCanvas.drawPath(mPath, mDrawPaint);
        mDrawnPaths.push(new LinePath(mPath, mDrawPaint));
        mPath = new Path();
    }

    class LinePath {
        private final Paint mDrawPaint;
        private final Path mDrawPath;

        LinePath(final Path drawPath, final Paint drawPaints) {
            mDrawPaint = new Paint(drawPaints);
            mDrawPath = new Path(drawPath);
        }

        Paint getDrawPaint() {
            return mDrawPaint;
        }

        Path getDrawPath() {
            return mDrawPath;
        }
    }
}
