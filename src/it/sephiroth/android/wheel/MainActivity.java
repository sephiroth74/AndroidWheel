package it.sephiroth.android.wheel;

import it.sephiroth.android.wheel.view.Wheel;
import it.sephiroth.android.wheel.view.Wheel.OnLayoutListener;
import it.sephiroth.android.wheel.view.Wheel.OnScrollListener;
import it.sephiroth.android.wheel.view.WheelRadio;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity implements OnScrollListener, OnLayoutListener {

	Wheel mWheel;
	WheelRadio mRadio;
	TextView mText;

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView( R.layout.main );

		mWheel.setOnScrollListener( this );
		mWheel.setOnLayoutListener( this );
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();

		mWheel = (Wheel) findViewById( R.id.wheel );
		mWheel.setOnScrollListener( new OnScrollListener() {
			
			@Override
			public void onScrollStarted( Wheel view, float value, int roundValue ) {
			}
			
			@Override
			public void onScrollFinished( Wheel view, float value, int roundValue ) {
			}
			
			@Override
			public void onScroll( Wheel view, float value, int roundValue ) {
			}
		} );
		
		mRadio = (WheelRadio) findViewById( R.id.wheel_radio );
		mText = (TextView) findViewById( R.id.text );
	}

	@Override
	public void onScrollStarted( Wheel view, float value, int roundValue ) {
		mRadio.setValue( value );
		mText.setText( "started.\nvalue = " + mRadio.getValue() + "\nroundValue = " + roundValue );
	}

	@Override
	public void onScroll( Wheel view, float value, int roundValue ) {
		mRadio.setValue( value );
		mText.setText( "scrolling.\nvalue = " + mRadio.getValue() + "\nroundValue = " + roundValue );
	}

	@Override
	public void onScrollFinished( Wheel view, float value, int roundValue ) {
		mRadio.setValue( value );
		mText.setText( "finished.\nvalue = " + mRadio.getValue() + "\nroundValue = " + roundValue );
	}

	@Override
	public void onLayout( View view ) {
		if( mWheel.equals( view )){
			int ticksCount = mWheel.getTicksCount();
			mRadio.setTicksNumber( ticksCount / 2, mWheel.getWheelScaleFactor() );			
		}
	}
}