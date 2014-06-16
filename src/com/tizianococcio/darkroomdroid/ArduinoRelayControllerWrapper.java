package com.tizianococcio.darkroomdroid;

public class ArduinoRelayControllerWrapper 
{
	
	private BluetoothBuddy btBuddy;

	public ArduinoRelayControllerWrapper(BluetoothBuddy btBuddy)
	{
		this.btBuddy = btBuddy;
	}
	
	public void switchOn()
	{
		this.btBuddy.write("H");
	}
	
	public void switchOff()
	{
		this.btBuddy.write("L");
	}
	
}
