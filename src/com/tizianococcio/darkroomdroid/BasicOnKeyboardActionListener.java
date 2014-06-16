package com.tizianococcio.darkroomdroid;

import android.app.Activity;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.view.KeyEvent;

public class BasicOnKeyboardActionListener implements OnKeyboardActionListener {

    private Activity mTargetActivity;
    private CustomKeyboardView customKeyboardView;
    
    public BasicOnKeyboardActionListener(Activity targetActivity, CustomKeyboardView ckv) {
        mTargetActivity = targetActivity;
        this.customKeyboardView = ckv;
    }
    
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        long eventTime = System.currentTimeMillis();
        KeyEvent event = new KeyEvent(
        				eventTime, 
        				eventTime,
                        KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0, 0,
                        KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);

        // Back key has been pressed
        if (primaryCode == 6)
        {
        	this.customKeyboardView.hide();
        }
        else
        {
        	mTargetActivity.dispatchKeyEvent(event);
        }
            
    }

	@Override
	public void onPress(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRelease(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onText(CharSequence arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void swipeDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void swipeLeft() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void swipeRight() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void swipeUp() {
		// TODO Auto-generated method stub
		
	}
}
