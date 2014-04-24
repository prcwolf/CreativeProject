package yeputons.robotics.libs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class NxtBluetoothController
{
	final protected UUID NXT_SERVER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // NXT's server UUID
	final public int MAX_DIRECT_COMMAND_LENGTH = 64;
	
	protected BluetoothDevice mBtDevice;
	protected BluetoothSocket mSocket;
	protected Thread mListenerThread;
	protected ArrayList<BlockingQueue<byte[]>> receivedResponses;
	private boolean mIsConnected;
	
	public NxtBluetoothController(BluetoothDevice btDevice)
    {
		mBtDevice = btDevice;
		mIsConnected = false;
	}
	public synchronized void connect() throws IOException
    {
		if (isConnected())
			throw new IllegalStateException("Already connected");
			
		mSocket = mBtDevice.createRfcommSocketToServiceRecord(NXT_SERVER_UUID);
		mSocket.connect();
		mIsConnected = true;
		receivedResponses = new ArrayList<BlockingQueue<byte[]>>();
		for (int i = 0; i < 256; i++)
			receivedResponses.add(new LinkedBlockingQueue<byte[]>());
		mListenerThread = new Thread(new ListenerThread());
		mListenerThread.start();
	}
	public synchronized void disconnect()
    {
		if (!isConnected())
			throw new IllegalStateException("Not connected");
		try
        {
			mSocket.close();
		} catch (IOException e)
        {
		}
		mIsConnected = false;
		while (mListenerThread.isAlive())
        {
			try
            {
				Thread.sleep(10);
			} catch (InterruptedException e)
            {
			}
		}
		mSocket = null;
		receivedResponses = null;
	}
	
	public boolean isConnected()
    {
		return mIsConnected;
	}
	
	static private void readBytes(InputStream stream, byte[] buffer, int len) throws IOException
    {
		if (len < 0 || buffer.length < len)
			throw new IllegalArgumentException("len should be <= buffer.length");
		int bytesRead = 0;
		while (bytesRead < len)
        {
			int curRead = stream.read(buffer, bytesRead, len - bytesRead);
			if (curRead == -1) break;
			bytesRead += curRead;
		}
	} 

	class ListenerThread implements Runnable
    {
		public void run()
        {
			final int bufferSize = MAX_DIRECT_COMMAND_LENGTH;
			final byte[] buffer = new byte[bufferSize];
			
			try
            {
				InputStream stream = mSocket.getInputStream();
				
				while (true)
                {
					readBytes(stream, buffer, 2);
					
					int len = buffer[0] | (buffer[1] << 8);
					assert len <= 64;
					readBytes(stream, buffer, len);
					
					if (buffer.length < 2 || buffer[0] != 0x02 )
                    {
						final StringBuilder sb = new StringBuilder();
						for (int i = 0; i < len; i++)
                        {
							if (i > 0) sb.append(", ");
							sb.append(String.format("0x%02X", buffer[i]));
						}
						Log.w("NxtBluetoothController", "Received invalid message: " + sb.toString());
						continue;
					}
					receivedResponses.get(buffer[1] & 0xFF).add(buffer);
				}
			} catch (IOException e)
            {
			}
		}
	};
	
	private final byte[] toSend = new byte[MAX_DIRECT_COMMAND_LENGTH + 2];
	public byte[] sendDirectCommand(byte[] command) throws IOException, InterruptedException
    {
		if (!isConnected())
			throw new IllegalStateException("Not connected");
		
		if (command.length < 2 || command.length > 64)
			throw new IllegalArgumentException("command.length should be >= 2 && <= MAX_DIRECT_COMMAND_LENGTH");
		if (command[0] != (byte)0x00 && command[0] != (byte)0x80)
			throw new IllegalArgumentException("command[0] should be either 0x00 or 0x80");

		synchronized(this)
        {
			toSend[0] = (byte)(command.length & 0xFF);
			toSend[1] = (byte)((command.length >> 8) & 0xFF);
			System.arraycopy(command,  0,  toSend,  2,  command.length);
			mSocket.getOutputStream().write(toSend, 0, 2 + command.length);
		}

		if (command[0] == (byte)0x80) return null; // Response is not required
		return receivedResponses.get(command[1] & 0xFF).take();
	}
}
