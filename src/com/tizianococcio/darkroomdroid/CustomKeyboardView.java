package com.tizianococcio.darkroomdroid;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class CustomKeyboardView extends KeyboardView {

    public CustomKeyboardView(Context context, AttributeSet attrs) {
            super(context, attrs);
            isInEditMode();
    }

    public void showWithAnimation(Animation animation) {
            animation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                            // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                            // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                            setVisibility(View.VISIBLE);
                    }
            });
            
            setAnimation(animation);
    }
    
    public void hide()
    {
    	setVisibility(View.INVISIBLE);
    }
    
    public void show()
    {
    	this.bringToFront();
    	setVisibility(View.VISIBLE);
    }
}