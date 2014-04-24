package com.example.lib;

import static com.googlecode.javacv.cpp.opencv_core.IplImage;

public interface CameraPreviewCvCallback {
	public void onPreviewFrame(IplImage argbImage);
}
