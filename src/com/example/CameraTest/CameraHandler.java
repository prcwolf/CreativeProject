package com.example.CameraTest;

import static com.example.CameraTest.MyActivity.*;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.view.View;
import com.example.lib.CameraPreviewCvCallback;
import com.googlecode.javacv.cpp.opencv_core;

import java.nio.ByteBuffer;

public class CameraHandler implements CameraPreviewCvCallback, View.OnTouchListener {
    long processTime;
    byte[] pixelsArray;
    int w, h, step, ccnt;
    int colorError;

    int pixelCount;

    private boolean colorIsIdeal(int colorR, int colorG, int colorB, int idealR, int idealG, int idealB) {
        /* Самое первое определение. Работает быстро, но плохо.
        Другие варианты работают пока что еще хуже, поэтому оставил этот */

//        return  StrictMath.abs(colorR - idealR) < colorError &&
//                StrictMath.abs(colorG - idealG) < colorError &&
//                StrictMath.abs(colorB - idealB) < colorError;

        /* Другое определение. Работает медленнее, но, вроде бы, лучше */

        colorR /= colorError; idealR /= colorError;
        colorG /= colorError; idealG /= colorError;
        colorB /= colorError; idealB /= colorError;

        return  colorR == idealR &&
                colorG == idealG &&
                colorB == idealB;
    }

    @Override
    public void onPreviewFrame(opencv_core.IplImage argbImage) {
        long startTime = System.currentTimeMillis();
        ByteBuffer pixels = argbImage.getByteBuffer();
        pixelsArray = new byte[pixels.limit()];
        pixels.get(pixelsArray);

        step = argbImage.widthStep();
        ccnt = argbImage.nChannels();

        colorError = colorErrorSeekBar.getProgress() + 1;
        pixelSize = pixelSizeSeekBar.getProgress() + 1;
        int color = colorSeekBar.getProgress();
        foundR = ((color & 8) * 0x7f) | ((color & 1) * 0x80);
        foundG = ((color & 4) * 0x7f) | ((color & 1) * 0x80);
        foundB = ((color & 2) * 0x7f) | ((color & 1) * 0x80);

        w = StrictMath.min(argbImage.width(),  screenWidth);
        h = StrictMath.min(argbImage.height(), screenHeight);

        pixelCount = 0;

        for (int x = 0; pixelSize * x < w; x++)
        for (int y = 0; pixelSize * y < h; y++) {
            int squareNumber = pixelSize * (x * ccnt + y * step);
            int r = pixelsArray[squareNumber + 0] & 0xff;
            int g = pixelsArray[squareNumber + 1] & 0xff;
            int b = pixelsArray[squareNumber + 2] & 0xff;

            if (colorIsIdeal(r, g, b, idealR, idealG, idealB))
                for (int x1 = 0; x1 < pixelSize; x1++)
                for (int y1 = 0; y1 < pixelSize; y1++) {
                    squareNumber = ccnt * (x * pixelSize + x1) + step * (y * pixelSize + y1);
                    pixelsArray[squareNumber + 0] = (byte) foundR;
                    pixelsArray[squareNumber + 1] = (byte) foundG;
                    pixelsArray[squareNumber + 2] = (byte) foundB;
                }
        }

        circleColor = 0x00000000;

        int ss = 2 * (pixelSizeSeekBar.getMax() + 1), plusPlus = 0;

        int minX = StrictMath.max(0, (w - ss) / 2);
        int minY = StrictMath.max(0, (h - ss) / 2);
        int maxX = StrictMath.min(w - 1, minX + ss);
        int maxY = StrictMath.min(h - 1, minY + ss);

        ss = (maxX - minX) * (maxY - minY);

        for (int x = minX; x <= maxX; x++)
        for (int y = minY; y <= maxY; y++) {
            int squareNumber = x * ccnt + y * step;

            if (    (pixelsArray[squareNumber + 0] & 0xff) == foundR &&
                    (pixelsArray[squareNumber + 1] & 0xff) == foundG &&
                    (pixelsArray[squareNumber + 2] & 0xff) == foundB)
                plusPlus++;
        }

        foundSquare = plusPlus >= ss * 3 / 4;
        circleColor = 0xff000000 | ((~foundR & 0xff) << 020) | ((~foundG & 0xff) << 010) | (~foundB & 0xff);

        if (    drawingBitmap.getWidth()  != argbImage.width() ||
                drawingBitmap.getHeight() != argbImage.height())
            drawingBitmap = Bitmap.createBitmap(argbImage.width(), argbImage.height(), Bitmap.Config.RGB_565);
        pixels.clear();
        pixels.put(pixelsArray);
        drawingBitmap.copyPixelsFromBuffer(pixels);

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
        }

        return false;
    }

}
