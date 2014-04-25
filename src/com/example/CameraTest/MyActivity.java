package com.example.CameraTest;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import com.example.lib.CameraPreview;
import com.example.lib.NxtBluetoothController;

import java.util.Random;

public class MyActivity extends Activity {
    public static Random rand = new Random();

    public static Bitmap drawingBitmap;
    public static boolean foundSquare;
    public static int screenWidth, screenHeight;
    public static int pixelSize, circleColor;
    public static int foundR, foundG, foundB, idealR, idealG, idealB;
    public static long processTime;

    public static Button nxtConnectButton, nxtDisconnectButton, nxtTryButton;
    public static EditText nxtAddressEditText;
    public static SeekBar pixelSizeSeekBar, colorErrorSeekBar, colorSeekBar;

    public static ButtonHandler buttonHandler;
    public static CameraHandler cameraHandler;
    public static DrawHandler drawHandler;

    public static BluetoothDevice[] devices;
    public static NxtBluetoothController nxtBluetooth;
    public static CameraPreview cameraPreview;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        buttonHandler = new ButtonHandler();
        cameraHandler = new CameraHandler();
        drawHandler = new DrawHandler();

        cameraPreview = new CameraPreview(this, cameraHandler, drawHandler);
        cameraPreview.setDisplayOrientation(90);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.cameraLayout);
        layout.addView(cameraPreview);

        nxtBluetooth = null;

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        nxtConnectButton    = (Button)   findViewById(R.id.buttonNxtConnect);
        nxtDisconnectButton = (Button)   findViewById(R.id.buttonNxtDisconnect);
        nxtTryButton        = (Button)   findViewById(R.id.button);
        nxtAddressEditText  = (EditText) findViewById(R.id.editTextNxtAddress);
        pixelSizeSeekBar    = (SeekBar)  findViewById(R.id.seekBarPixelSize);
        colorErrorSeekBar   = (SeekBar)  findViewById(R.id.seekBarColorError);
        colorSeekBar        = (SeekBar)  findViewById(R.id.seekBarColor);

        nxtConnectButton.setOnClickListener(buttonHandler);
        nxtDisconnectButton.setOnClickListener(buttonHandler);
        nxtTryButton.setOnClickListener(buttonHandler);
        cameraPreview.setOnTouchListener(cameraHandler);
    }
}