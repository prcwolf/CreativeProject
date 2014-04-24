package com.example.CameraTest;

import static com.example.CameraTest.MyActivity.*;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.example.lib.DrawCallback;

public class DrawHandler implements DrawCallback {
    public static final int CIRCLE_SIZE = 25;

    @Override
    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        if (pixelSize == 0) pixelSize = 1;
        int w = StrictMath.min(cameraPreview.getWidth(), screenWidth);   w -= w % pixelSize;
        int h = StrictMath.min(cameraPreview.getHeight(), screenHeight); h -= h % pixelSize;

//        paint.setColor(0xff000000 | (foundR << 020) | (foundG << 010) | foundB);
//        for (int x = 0; pixelSize * x < w; x++)
//        for (int y = 0; pixelSize * y < h; y++)
//            if (foundColor[x][y])
//                canvas.drawRect(pixelSize * x, pixelSize * y, pixelSize * (x + 1), pixelSize * (y + 1), paint);

        paint.setStrokeWidth(3);
        paint.setColor(circleColor);

        if (foundSquare)
            canvas.drawCircle(w / 2, h / 2, CIRCLE_SIZE, paint);
        else {
            canvas.drawLine(w / 2, h / 2 - CIRCLE_SIZE, w / 2, h / 2 + CIRCLE_SIZE, paint);
            canvas.drawLine(w / 2 - CIRCLE_SIZE, h / 2, w / 2 + CIRCLE_SIZE, h / 2, paint);
        }

        paint.setColor(0xffff0000);
        canvas.drawText("process time is " + processTime, 10, 20, paint);
        canvas.drawText(String.format("r is %d, g is %d, b is %d", idealR, idealG, idealB), 10, 40, paint);
    }
}
