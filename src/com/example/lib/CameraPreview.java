package com.example.lib;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_YUV2BGRA_NV12;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvTranspose;
import static com.googlecode.javacv.cpp.opencv_core.cvFlip;

import android.util.Log;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements Callback, PreviewCallback {
	protected SurfaceHolder mHolder;
	protected Camera mCamera;
	protected CameraPreviewCvCallback mPreviewCallback;
	protected DrawCallback mDrawCallback;
	protected int mDisplayOrientation = 0;

	public CameraPreview(Context context, CameraPreviewCvCallback previewCallback, DrawCallback drawCallback) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mPreviewCallback = previewCallback;
		mDrawCallback = drawCallback;
	}
	
	public void setDisplayOrientation(int displayOrientation) {
		if (displayOrientation < 0 || displayOrientation >= 360 || displayOrientation % 90 != 0)
			throw new IllegalArgumentException("displayOrientation should be one of the 0, 90, 180, 270");
		mDisplayOrientation = displayOrientation;
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera = Camera.open();
			mCamera.setDisplayOrientation(mDisplayOrientation);
			mCamera.setPreviewDisplay(holder);
		} catch (Exception e) {
            mCamera.release();
            mCamera = null;
            Log.e("CameraPreview", "", e);
            new AlertDialog.Builder(getContext()).setMessage(e.getMessage()).create().show();
		}
		setWillNotDraw(false);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (mCamera == null) return;
		
		Camera.Parameters parameters = mCamera.getParameters();
		if (mPreviewCallback != null)
			mCamera.setPreviewCallbackWithBuffer(this);
		Camera.Size size = parameters.getPreviewSize();
		byte[] data = new byte[(size.width * size.height* ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) + 7) / 8];
		mCamera.addCallbackBuffer(data);
		mCamera.startPreview();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera == null) return;
		
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mDrawCallback != null)
			mDrawCallback.onDraw(canvas);
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);

        if (mCamera != null) {
            if (mDisplayOrientation == 90 || mDisplayOrientation == 270) {
                int tmp = width;
                width = height;
                height = tmp;
            }

            Camera.Size size = mCamera.getParameters().getPreviewSize();
            double k = Math.min((double)height / size.height, (double)width / size.width);
            int w = (int)(size.width * k);
            int h = (int)(size.height * k);
            if (mDisplayOrientation == 90 || mDisplayOrientation == 270) {
                int tmp = w;
                w = h;
                h = tmp;
            }
            setMeasuredDimension(w, h);
        } else {
            setMeasuredDimension(width, height);
        }
    }

	private IplImage yuvImage, rgbaImage, turnedRgbaImage;
	public void onPreviewFrame(byte[] data, Camera camera) {
		Camera.Size size;
		try {
			size = camera.getParameters().getPreviewSize();
		} catch (RuntimeException e) { return; }
		int w = size.width, h = size.height;
		int w2 = w, h2 = h;
		if (mDisplayOrientation == 90 || mDisplayOrientation == 270) {
		  w2 = h; h2 = w;
		}
		
		if (yuvImage == null || yuvImage.width() != w || yuvImage.height() != h + h / 2) {
			if (yuvImage != null) {
				yuvImage.release();
				rgbaImage.release();
				turnedRgbaImage.release();
			}
			yuvImage = IplImage.create(w, h + h / 2, IPL_DEPTH_8U, 1);
			rgbaImage = IplImage.create(w, h, IPL_DEPTH_8U, 4);
			turnedRgbaImage = IplImage.create(w2, h2, IPL_DEPTH_8U, 4);
		}

		yuvImage.getByteBuffer().put(data);
		cvCvtColor(yuvImage, rgbaImage, CV_YUV2BGRA_NV12);

		if (mDisplayOrientation == 0 || mDisplayOrientation == 180) {
			cvCopy(rgbaImage, turnedRgbaImage);
			if (mDisplayOrientation == 180) {
				cvFlip(turnedRgbaImage, turnedRgbaImage, 0);
				cvFlip(turnedRgbaImage, turnedRgbaImage, 1);
			}
		} else {
			cvTranspose(rgbaImage,  turnedRgbaImage);
			cvFlip(turnedRgbaImage, turnedRgbaImage, mDisplayOrientation == 90 ? 1 : 0);
		}
		mPreviewCallback.onPreviewFrame(turnedRgbaImage);

		try {
			camera.addCallbackBuffer(data);
		} catch (RuntimeException e) { return; }
	}
}
