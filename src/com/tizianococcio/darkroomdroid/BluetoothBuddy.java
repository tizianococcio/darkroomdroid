package com.tizianococcio.darkroomdroid;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;


public class BluetoothBuddy
{

	// Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter;
	
	// Bluetooth device
	private BluetoothDevice mDevice;
	
	// UUID
	private UUID serviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	// Connection socket
	private BluetoothSocket mSocket;
	
	// Output stream
	private OutputStream outStream;
	
	// Log tag
	private final String LOG_TAG = "DCB";
	
	// Constructor
	public BluetoothBuddy(String deviceAddress)
	{
		this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		this.mDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
	}
	
	// Connection to the device
	public void connect()
	{
		
		this.disconnect();
		
		// Cancel discovery - Just to be sure
		this.mBluetoothAdapter.cancelDiscovery();

		// Create a socket
		try {
			this.mSocket = this.mDevice.createRfcommSocketToServiceRecord(this.serviceUUID);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// Attempts connection
		try {
			this.mSocket.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Opens an output stream
		try {
			this.outStream = this.mSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Disconnect
	public void disconnect()
	{
		this.mBluetoothAdapter.cancelDiscovery();
		
		if (this.outStream != null)
		{
			try {
				this.outStream.close();
			} catch (IOException e) {
				//Toast.makeText(context, "Errore nella chiusura dell stream", Toast.LENGTH_LONG).show();
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (this.mSocket != null)
		{
			try {
				this.mSocket.close();
			} catch (IOException e) {
				//Toast.makeText(context, "Errore nella chiusura del socket", Toast.LENGTH_LONG).show();
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.outStream = null;
		this.mSocket = null;
		this.mBluetoothAdapter.cancelDiscovery();
	}
	
	// whether there is a connection established
	public boolean isConnected()
	{
		return (this.mSocket != null);
	}
	
	// Writes to output stream
	public void write(String s)
	{
		new WriterTask(this.outStream).execute(s);
	}
	
	// Output writer
	class WriterTask extends AsyncTask<String, Void, Void> {

		private OutputStream oStream = null;
		
		public WriterTask(OutputStream s)
		{
			this.oStream = s;
		}
		
		@Override
		protected Void doInBackground(String... s) {
			try {
				this.oStream.write(s[0].getBytes(Charset.forName("UTF-8")));
			} catch (IOException e) {
				Log.d(LOG_TAG, "Errore nella scrittura in output");
				e.printStackTrace();
			}
			return null;
		}
		
	}
}
