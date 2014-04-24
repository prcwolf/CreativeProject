package com.example.CameraTest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.example.lib.NxtBluetoothController;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Set;

public class ButtonHandler implements View.OnClickListener {
    @Override
    public void onClick(View view) {
        if (view == MyActivity.nxtConnectButton) {
            BluetoothDevice device = null;
            Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
            for (BluetoothDevice d: devices)
                if (d.getName().equals(MyActivity.nxtAddressEditText.getText().toString()))
                    device = d;
            NxtBluetoothController bt = null;
            try {
                bt = MyActivity.nxtBluetooth == null ? new NxtBluetoothController(device) : MyActivity.nxtBluetooth;
                bt.connect();
                MyActivity.nxtBluetooth = bt;
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            try {
                String programName = "Program.rxe";
                byte length = (byte) programName.length();
                byte[] message = new byte[22];
                message[0] = (byte) 0x00;
                message[1] = (byte) 0x00;

                for (int i = 0; i < length; i++)
                    message[2 + i] = (byte) programName.charAt(i);

                bt.sendDirectCommand(message);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.exit(1);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.exit(1);
            }
        } else if (view == MyActivity.nxtDisconnectButton) {
            NxtBluetoothController bt = MyActivity.nxtBluetooth;

            try {
                byte[] message = new byte[2];
                message[0] = (byte) 0x80;
                message[1] = (byte) 0x01;

                bt.sendDirectCommand(message);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            bt.disconnect();
        } else if (view == MyActivity.nxtTryButton) {
            NxtBluetoothController bt = MyActivity.nxtBluetooth;
            try {
                byte[] message = new byte[20];
                message[0] = (byte) 0x80;
                message[1] = (byte) 0x09;
                message[2] = (byte) 0x00;

                message[ 3] = 16;

                message[ 4] = (byte) MyActivity.rand.nextInt(256);
                message[ 5] = (byte) MyActivity.rand.nextInt(256);
                message[ 6] = (byte) MyActivity.rand.nextInt(256);
                message[ 7] = (byte) MyActivity.rand.nextInt(256);

                message[ 8] = (byte) MyActivity.rand.nextInt(256);
                message[ 9] = (byte) MyActivity.rand.nextInt(256);
                message[10] = (byte) MyActivity.rand.nextInt(256);
                message[11] = (byte) MyActivity.rand.nextInt(256);

                message[12] = (byte) MyActivity.rand.nextInt(256);
                message[13] = (byte) MyActivity.rand.nextInt(256);
                message[14] = (byte) MyActivity.rand.nextInt(256);
                message[15] = (byte) MyActivity.rand.nextInt(256);

                message[16] = (byte) MyActivity.rand.nextInt(256);
                message[17] = (byte) MyActivity.rand.nextInt(256);
                message[18] = (byte) MyActivity.rand.nextInt(256);
                message[19] = (byte) MyActivity.rand.nextInt(256);

                bt.sendDirectCommand(message);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.exit(1);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.exit(1);
            }
        }
    }
}
