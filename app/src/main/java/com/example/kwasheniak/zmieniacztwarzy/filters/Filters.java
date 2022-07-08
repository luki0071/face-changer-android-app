package com.example.kwasheniak.zmieniacztwarzy.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class Filters {

    public static Bitmap getFilter(Bitmap bitmap, int id){
        switch (id){
            case 2:
                return negative(bitmap);
            case 3:
                return sepia(bitmap);
            case 4:
                return gray(bitmap);
            case 5:
                return binary(bitmap);
            case 6:
                return binaryBW(bitmap);
            case 7:
                return emboss(bitmap);
            case 8:
                return GBR(bitmap);
            case 9:
                return BRG(bitmap);
            default:
                return bitmap;
        }
    }

    private static Bitmap negative(Bitmap bitmap) {
        float[] negative_colour_matrix = {
                -1, 0, 0, 0, 255,
                0, -1, 0, 0, 255,
                0, 0, -1, 0, 255,
                0, 0, 0, 1, 0};
        ColorMatrix colorMatrix = new ColorMatrix(negative_colour_matrix);
        return getBitmapFromColorMatrix(colorMatrix, bitmap);
    }

    private static Bitmap sepia(Bitmap bitmap) {
        float[] negative_colour_matrix = {
                0.39f, 0.769f, 0.189f, 0, 0,
                0.349f, 0.686f, 0.168f, 0, 0,
                0.272f, 0.534f, 0.131f, 0, 0,
                0, 0, 0, 1, 0};
        ColorMatrix colorMatrix = new ColorMatrix(negative_colour_matrix);
        return getBitmapFromColorMatrix(colorMatrix, bitmap);
    }

    private static Bitmap gray(Bitmap bitmap) {
        float[] negative_colour_matrix = {
                0.33f, 0.33f, 0.33f, 0, 0,
                0.33f, 0.33f, 0.33f, 0, 0,
                0.33f, 0.33f, 0.33f, 0, 0,
                0, 0, 0, 1, 0};
        ColorMatrix colorMatrix = new ColorMatrix(negative_colour_matrix);
        return getBitmapFromColorMatrix(colorMatrix, bitmap);
    }

    private static Bitmap binary(Bitmap bitmap) {
        float[] negative_colour_matrix = {
                255, 0, 0, 0, -128 * 255,
                0, 255, 0, 0, -128 * 255,
                0, 0, 255, 0, -128 * 255,
                0, 0, 0, 1, 0};
        ColorMatrix colorMatrix = new ColorMatrix(negative_colour_matrix);
        return getBitmapFromColorMatrix(colorMatrix, bitmap);
    }

    private static Bitmap binaryBW(Bitmap bitmap) {
        float[] negative_colour_matrix = {
                85, 85, 85, 0, -128 * 255,
                85, 85, 85, 0, -128 * 255,
                85, 85, 85, 0, -128 * 255,
                0, 0, 0, 1, 0};
        ColorMatrix colorMatrix = new ColorMatrix(negative_colour_matrix);
        return getBitmapFromColorMatrix(colorMatrix, bitmap);
    }

    private static Bitmap GBR(Bitmap bitmap) {
        float[] negative_colour_matrix = {
                0, 0, 1, 0, 0,
                1, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 0, 1, 0};
        ColorMatrix colorMatrix = new ColorMatrix(negative_colour_matrix);
        return getBitmapFromColorMatrix(colorMatrix, bitmap);
    }

    private static Bitmap BRG(Bitmap bitmap) {
        float[] negative_colour_matrix = {
                0, 1, 0, 0, 0,
                0, 0, 1, 0, 0,
                1, 0, 0, 0, 0,
                0, 0, 0, 1, 0};
        ColorMatrix colorMatrix = new ColorMatrix(negative_colour_matrix);
        return getBitmapFromColorMatrix(colorMatrix, bitmap);
    }

    private static Bitmap emboss(Bitmap bitmap) {
        int width, height, r,g, b, pixel,c1,r1,g1,b1,red,green,blue;
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        Bitmap bmpEmboss = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(bmpEmboss);
        canvas.drawBitmap(bitmap, 0, 0, null);
        for(int y=1; y< height-1; y++) {
            for(int x=1; x < width-1; x++) {
                pixel = bitmap.getPixel(x, y);

                r = Color.red(pixel);
                g = Color.green(pixel);
                b = Color.blue(pixel);

                c1 = bitmap.getPixel(x-1, y-1);

                r1 = Color.red(c1);
                g1 = Color.green(c1);
                b1 = Color.blue(c1);

                red = Math.max(67, Math.min(255, Math.abs(r - r1 + 128)));
                green = Math.max(67, Math.min(255, Math.abs(g - g1 + 128)));
                blue = Math.max(67, Math.min(255, Math.abs(b - b1 + 128)));
                if (red > 255) {
                    red = 255;
                }
                else if (red < 0) {
                    red = 0;
                }

                if (green > 255) {
                    green = 255;
                }
                else if (green < 0) {
                    green = 0;
                }

                if (blue > 255) {
                    blue = 255;
                }
                else if (blue < 0) {
                    blue = 0;
                }

                bmpEmboss.setPixel(x, y, Color.rgb(red, green, blue));
            }
        }
        return bmpEmboss;
    }

    private static Bitmap getBitmapFromColorMatrix(ColorMatrix colorMatrix, Bitmap bitmap) {
        Bitmap ret = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return ret;
    }
}
