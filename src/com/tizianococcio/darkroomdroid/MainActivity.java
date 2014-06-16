package com.tizianococcio.darkroomdroid;


import com.tizianococcio.darkroomdroid.CustomKeyboardView;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.inputmethodservice.Keyboard;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
	
	// Contex
	Context context;
	
	private CustomKeyboardView customKeyboardView;
    private Keyboard mKeyboard;

	
	// Buttons
	private Button startButton;
	private Button pauseButton;
	private Button resetButton;
	private Button bluetoothToggleButton;
	private Button increaseTimeButton;
	private Button decreaseTimeButton;
	
	// TextView
	private TextView timerValue;
	
	// EditText
	private EditText txtTimeoutValue;
	
	// Countdown timer
	private CountDownTimer cTimer;
	
	// current time
	private long currentTime;
	
	// is the countdown running
	private boolean isRunning = false;
	
	
	// Bluetooth device address
	private final static String BLUETOOTH_DEVICE_ADDRESS = "00:14:01:21:12:73";
	
	// Bluetooth helper class
	private BluetoothBuddy bluetoothBuddy;
	
	// Relay Controller
	private ArduinoRelayControllerWrapper relayController;
	
	// PowerManager to prevent sleep
	PowerManager.WakeLock wl;
	
	// Debug Tag
	private final String LOG_TAG = "DCB";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // init application
        this.init();
        
        // Initialize bluetooth connection
        this.initBluetooth();
        
        // Set UI components
        this.initUIComponents();
        
        // Handling click on start button
		startButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				
				// Ignore action if timer value is empty
				if (txtTimeoutValue.getText().length() == 0)
				{
					return;
				}

				// Get timeout value
				long timeoutValue = Integer.parseInt(txtTimeoutValue.getText()
						.toString()) * 1000;

				// Resume countdown if paused while still running
				if (isRunning) {
					timeoutValue = currentTime;
				}
				
				// Send a switch-on signal
				relayController.switchOn();
				
				// Initialize the timer
				cTimer = new CountDownTimer(timeoutValue, 1) {

					@Override
					public void onTick(long millisUntilFinished) {
						isRunning = true;
						currentTime = millisUntilFinished;
						int secs = (int) (millisUntilFinished / 1000);
						int mins = secs / 60;
						secs = secs % 60;
						int milliseconds = (int) (millisUntilFinished % 1000);
						timerValue.setText("" + mins + ":"
								+ String.format("%02d", secs) + ":"
								+ String.format("%03d", milliseconds));
					}

					@Override
					public void onFinish() {
						isRunning = false;
						timerValue.setText("00:00:00");
						
						// Send a switch-off signal
						relayController.switchOff();
					}
					
				// Starts the timer
				}.start();
			}
		});
		
		// Handle click on the decrease time button
		this.decreaseTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View view) {
            	
            	// If empty
				if (txtTimeoutValue.getText().length() == 0)
				{
					txtTimeoutValue.setText("1");
				}
				
				// Get the current value as an integer
            	Integer currentValue = Integer.parseInt(txtTimeoutValue.getText().toString());
            	
            	// Only decreasing if greater than zero
            	if (currentValue > 0)
            	{
            		currentValue--;
            		txtTimeoutValue.setText(currentValue.toString());
            	}
            }
        });
		
		// Handle click on the increase time button
		this.increaseTimeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				
				// Holder the current value as an Integer
				Integer currentValue;
				
				// If empty
				if (txtTimeoutValue.getText().length() == 0)
				{
					txtTimeoutValue.setText("1");
				}
				else
				{
					currentValue = Integer.parseInt(txtTimeoutValue.getText().toString());
					currentValue++;
					txtTimeoutValue.setText(currentValue.toString());
				}
			}
		});
		
        // Handling click on pause button
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View view) {
            	
            	// Send a switch-off signal
            	relayController.switchOff();
            	
            	cTimer.cancel();
            }
        });
        
        // Handling click on reset button
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View view) {
            	isRunning = false;
            	timerValue.setText("00:00:00");
            }
        });
        
        // Handling click on bluetooth toggle button
        bluetoothToggleButton.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View view) {
        		String message = "";
        		
        		// If connected, disconnects and otherwise
        		if (MainActivity.this.bluetoothBuddy.isConnected())
        		{
        			MainActivity.this.bluetoothBuddy.disconnect();
        			message = "Disconnesso";
        			bluetoothToggleButton.setText("Connetti");
        		}
        		else
        		{
        			OpenBluetoothConnectionAndShowProgress task = new OpenBluetoothConnectionAndShowProgress(MainActivity.this);
        			task.execute();
        			message = "Connesso";
        			bluetoothToggleButton.setText("Disconnetti");
        		}
        		
        		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        	}
        });
        
    	// Custom keybord events
    	txtTimeoutValue.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
            		customKeyboardView.setVisibility(View.VISIBLE);
            		customKeyboardView.bringToFront();
            		
            		// Select the text in the timer value box
            		MainActivity.this.txtTimeoutValue.selectAll();
                    return true;
            }
    	});

    	txtTimeoutValue.setOnClickListener(new View.OnClickListener() {
    		
    		@Override
    		public void onClick(View arg0) {
    			customKeyboardView.show();
    			
				// Select the text in the timer value box
				MainActivity.this.txtTimeoutValue.selectAll();
    		}
    	});        

    }
    
    // Application initialization
    @SuppressLint("Wakelock")
	@SuppressWarnings("deprecation")
	private void init()
    {
    	
    	Log.d(LOG_TAG, "Application started");
    	
        // Contex
        this.context = getApplicationContext();
        
        // Force portrait orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        // Screen brightness
        Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 10);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness =0.1f;// 100 / 100.0f;
        getWindow().setAttributes(lp);
        
        // Prevents display to sleep
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "DarkroomDroid Locker");
        this.wl.acquire();

        // Custom keyboard
        mKeyboard = new Keyboard(this, R.xml.ckeyboard);
        this.customKeyboardView = (CustomKeyboardView) findViewById(R.id.keyboard_view);
        this.customKeyboardView.setKeyboard(this.mKeyboard);
        this.customKeyboardView.setOnKeyboardActionListener(new BasicOnKeyboardActionListener(this, this.customKeyboardView));
        this.customKeyboardView.setPreviewEnabled(false);
        
    }
    
    // Bootstraps bluetooth
    private void initBluetooth()
    {
    	this.bluetoothBuddy = new BluetoothBuddy(MainActivity.BLUETOOTH_DEVICE_ADDRESS);
		OpenBluetoothConnectionAndShowProgress task = new OpenBluetoothConnectionAndShowProgress(MainActivity.this);
		task.execute();
		
		// Arduino relay interface
		this.relayController = new ArduinoRelayControllerWrapper(MainActivity.this.bluetoothBuddy);

    }
    
    // UI components
    private void initUIComponents()
    {
    	// TextView
        timerValue = (TextView) findViewById(R.id.timerValue);
        
        // Button
        startButton = (Button) findViewById(R.id.startButton);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        resetButton = (Button) findViewById(R.id.resetButton);
        bluetoothToggleButton = (Button) findViewById(R.id.btnBluetoothToggle);
        increaseTimeButton = (Button) findViewById(R.id.btnIncreaseTime);
        decreaseTimeButton = (Button) findViewById(R.id.btnDecreaseTime);
        
        // EditText
        txtTimeoutValue = (EditText) findViewById(R.id.timeoutValue);
    }
    
    // Async Task to open a bluetooth connection while showing progress dialog 
	private class OpenBluetoothConnectionAndShowProgress extends AsyncTask <Void, Void, Void> {
	    private ProgressDialog dialog;
	     
	    public OpenBluetoothConnectionAndShowProgress(MainActivity activity) {
	        dialog = new ProgressDialog(activity, R.style.CustomAlertDialogStyle);
	    }
	 
	    @Override
	    protected void onPreExecute() {
	        dialog.setMessage("Connessione in corso...");
	        dialog.show();
	    }
	     
	    @Override
	    protected void onPostExecute(Void result) {
	        if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
	    }
	     
	    @Override
	    protected Void doInBackground(Void... params) {
	        MainActivity.this.bluetoothBuddy.connect();
	        return null;
	    }
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
