package com.example.CameraTest;

import static com.example.CameraTest.MyActivity.*;

import android.view.MotionEvent;
import android.view.View;
import com.example.lib.CameraPreviewCvCallback;
import com.googlecode.javacv.cpp.opencv_core;

import java.nio.ByteBuffer;

public class CameraHandler implements CameraPreviewCvCallback, View.OnTouchListener {
    long processTime;
    byte[] pixelsArray;
    int w, h, step, ccnt;
    float colorError;

    int pixelCount;

    private boolean colorIsIdeal(int colorR, int colorG, int colorB) {
//        Старое определение. Работает быстро, но плохо.
//        colorR -= colorR % colorError;
//        colorG -= colorG % colorError;
//        colorB -= colorB % colorError;
//
//        return (colorR == idealR - idealR % colorError) && (colorG == idealG - idealG % colorError) && (colorB == idealB - idealB % colorError);

//        Новое определение. Работает хорошо, но медленно.
        float colorRG = (float) colorR / (float) colorG;
        float colorRB = (float) colorR / (float) colorB;
        float colorGB = (float) colorG / (float) colorB;

        float idealRG = (float) idealR / (float) idealG;
        float idealRB = (float) idealR / (float) idealB;
        float idealGB = (float) idealG / (float) idealB;

        return
                StrictMath.abs(colorRG - idealRG) < colorError &&
                StrictMath.abs(colorRB - idealRB) < colorError &&
                StrictMath.abs(colorGB - idealGB) < colorError;
    }

    @Override
    public void onPreviewFrame(opencv_core.IplImage argbImage) {
        long startTime = System.currentTimeMillis();
        ByteBuffer pixels = argbImage.getByteBuffer();
        pixelsArray = new byte[pixels.limit()];
        pixels.get(pixelsArray);

        step = argbImage.widthStep();
        ccnt = argbImage.nChannels();

        colorError = (float) colorErrorSeekBar.getProgress() / (float) colorErrorSeekBar.getMax();
        pixelSize = pixelSizeSeekBar.getProgress() + 1;
        int color = colorSeekBar.getProgress();
        foundR = ((color & 8) * 0x7f) | ((color & 1) * 0x80);
        foundG = ((color & 4) * 0x7f) | ((color & 1) * 0x80);
        foundB = ((color & 2) * 0x7f) | ((color & 1) * 0x80);

        w = StrictMath.min(argbImage.width(), screenWidth);
        h = StrictMath.min(argbImage.height(), screenHeight);

        pixelCount = 0;

        for (int x = 0; pixelSize * x < w; x++)
        for (int y = 0; pixelSize * y < h; y++) {
            int squareNumber = pixelSize * (y * step + x * ccnt);
            int r = pixelsArray[squareNumber + 0] & 0xff;
            int g = pixelsArray[squareNumber + 1] & 0xff;
            int b = pixelsArray[squareNumber + 2] & 0xff;

            if (colorIsIdeal(r, g, b)) {
                foundColor[x][y] = true;

//                middleX += pixelSize * x;
//                middleY += pixelSize * y;
//                pixelCount++;
            } else {
                foundColor[x][y] = false;
            }
        }

        circleColor = 0x00000000;

        if (/*pixelCount != 0*/ true) {
            int ss = (pixelSizeSeekBar.getMax() + 1) / pixelSize, plusPlus = 0;

//            middleX /= pixelCount;
//            middleY /= pixelCount;

            int minX = StrictMath.max(0, w / 2 / pixelSize - ss / 2);
            int minY = StrictMath.max(0, h / 2 / pixelSize - ss / 2);
            int maxX = StrictMath.min(w - 1, minX + ss);
            int maxY = StrictMath.min(h - 1, minY + ss);

            ss = (maxX - minX) * (maxY - minY);

            for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                if (foundColor[x][y]) plusPlus++;

            foundSquare = plusPlus >= ss * 3 / 4;
            circleColor = 0xff000000 | ((~foundR & 0xff) << 020) | ((~foundG & 0xff) << 010) | (~foundB & 0xff);
        }

        processTime = System.currentTimeMillis() - startTime;
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
