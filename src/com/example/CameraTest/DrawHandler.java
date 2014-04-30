package com.example.CameraTest;

import static com.example.CameraTest.MyActivity.*;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.example.lib.DrawCallback;
import com.googlecode.javacv.cpp.opencv_core;

public class DrawHandler implements DrawCallback {
    public static final int CIRCLE_SIZE = 25;

    @Override
    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        int w = StrictMath.min(cameraPreview.getWidth(), screenWidth);
        int h = StrictMath.min(cameraPreview.getHeight(), screenHeight);

        if (drawingBitmap != null) {
            paint.setColor(0xffffffff);
            canvas.drawBitmap(drawingBitmap,
                    (w - drawingBitmap.getWidth()) / 2, (h - drawingBitmap.getHeight()) / 2, paint);
        }

        paint.setColor(circleColor);
        if (foundSquare)
            canvas.drawCircle(w / 2, h / 2, CIRCLE_SIZE, paint);

        processTime = System.currentTimeMillis() - processTime;

        paint.setColor(0xffff0000);
        canvas.drawText("process time is " + processTime, 10, 20, paint);
        paint.setColor(0xff000000 | (idealR << 020) | (idealG << 010) | idealB);
        canvas.drawRect(10, 30, 60, 80, paint);
        paint.setColor(0xff000000 | (idealR << 020));
        canvas.drawRect(60, 30, 110, 80, paint);
        paint.setColor(0xff000000 | (idealG << 010));
        canvas.drawRect(10, 80, 60, 130, paint);
        paint.setColor(0xff000000 | idealB);
        canvas.drawRect(60, 80, 110, 130, paint);

    }
}
