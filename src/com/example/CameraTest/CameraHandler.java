package com.example.CameraTest;

import static com.example.CameraTest.MyActivity.*;

import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import com.example.lib.CameraPreviewCvCallback;
import com.googlecode.javacv.cpp.opencv_core;

import java.nio.ByteBuffer;

public class CameraHandler implements CameraPreviewCvCallback, View.OnTouchListener {
    byte[] pixelsArray;
    int w, h, step, ccnt;
    int colorError;

    int pixelCount;

    private boolean colorIsIdeal(int colorR, int colorG, int colorB, int idealR, int idealG, int idealB) {
//        Старое определение. Работает быстро, но плохо. Нет, все же лучше, чем новое
        colorR /= colorError; idealR /= colorError;
        colorG /= colorError; idealG /= colorError;
        colorB /= colorError; idealB /= colorError;

        return colorR == idealR && colorG == idealG && colorB == idealB;
    }


    @Override
    public void onPreviewFrame(opencv_core.IplImage argbImage) {
        processTime = System.currentTimeMillis();
        ByteBuffer pixels = argbImage.getByteBuffer();
        pixelsArray = new byte[pixels.limit()];
        pixels.get(pixelsArray);

        step = argbImage.widthStep();
        ccnt = argbImage.nChannels();

        colorError = colorErrorSeekBar.getProgress() + 1;
        int color = colorSeekBar.getProgress();
        foundR = ((color & 8) * 0x7f) | ((color & 1) * 0x80);
        foundG = ((color & 4) * 0x7f) | ((color & 1) * 0x80);
        foundB = ((color & 2) * 0x7f) | ((color & 1) * 0x80);

        w = StrictMath.min(argbImage.width(), screenWidth);
        h = StrictMath.min(argbImage.height(), screenHeight);

        int ss = squareSizeSeekBar.getProgress() + 1, plusPlus = 0;

        int minX = StrictMath.max(0, (w - ss) / 2);
        int minY = StrictMath.max(0, (h - ss) / 2);
        int maxX = StrictMath.min(w - 1, minX + ss);
        int maxY = StrictMath.min(h - 1, minY + ss);

        int w = maxX - minX + 1;
        int h = maxY - minY + 1;

        pixelCount = 0;

        for (int x = minX; x <= maxX; x++)
        for (int y = minY; y <= maxY; y++) {
            int squareNumber = y * step + x * ccnt;
            int r = pixelsArray[squareNumber + 0] & 0xff;
            int g = pixelsArray[squareNumber + 1] & 0xff;
            int b = pixelsArray[squareNumber + 2] & 0xff;

            if (colorIsIdeal(r, g, b, idealR, idealG, idealB)) {
                squareNumber = x * ccnt + y * step;
                pixelsArray[squareNumber + 0] = (byte) foundR;
                pixelsArray[squareNumber + 1] = (byte) foundG;
                pixelsArray[squareNumber + 2] = (byte) foundB;
            }
        }

        circleColor = 0x00000000;

        ss = (maxX - minX) * (maxY - minY);

        for (int x = minX; x <= maxX; x++)
        for (int y = minY; y <= maxY; y++) {
            int squareNumber = x * ccnt + y * step;
            if (    pixelsArray[squareNumber + 0] == (byte) foundR &&
                    pixelsArray[squareNumber + 1] == (byte) foundG &&
                    pixelsArray[squareNumber + 2] == (byte) foundB)
                plusPlus++;
        }

        foundSquare = (plusPlus >= ss * 3 / 4);
        circleColor = 0xff000000 | ((~foundR & 0xff) << 020) | ((~foundG & 0xff) << 010) | (~foundB & 0xff);

        if (drawingBitmap == null || drawingBitmap.getWidth() != w || drawingBitmap.getHeight() != h)
            drawingBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        int[] colors = new int[w * h];
        for (int x = 0; x < w; x++)
        for (int y = 0; y < h; y++) {
            int squareNumber = (minX + x) * ccnt + (minY + y) * step;
            colors[x + y * w] = 0xff000000 |
                    ((pixelsArray[squareNumber + 0] & 0xff) << 020) |
                    ((pixelsArray[squareNumber + 1] & 0xff) << 010) |
                    ((pixelsArray[squareNumber + 2] & 0xff) << 000);
        }
        drawingBitmap.setPixels(colors, 0, w, 0, 0, w, h);
        cameraPreview.invalidate();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view == cameraPreview) {
            int touchX = (int) motionEvent.getX();
            int touchY = (int) motionEvent.getY();
            if (view == cameraPreview && touchX < w && touchY < h && touchX >= 0 && touchY >= 0) {
                int squareNumber = touchX * ccnt + touchY * step;
                idealR = pixelsArray[squareNumber + 0] & 0xff;
                idealG = pixelsArray[squareNumber + 1] & 0xff;
                idealB = pixelsArray[squareNumber + 2] & 0xff;
            }
        } return false;
    }
}
