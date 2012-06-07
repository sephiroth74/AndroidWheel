#	Wheel widget for Android #

To include the wheel widget in the current layout, you should add in the layout xml this lines:

            <it.sephiroth.android.wheel.view.Wheel
                android:id="@+id/wheel"
                xmlns:sephiroth="http://schemas.android.com/apk/res-auto"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                sephiroth:numRotations="6"
                sephiroth:ticks="28" />
				
Where **numRotations** is the max number of rotations the wheel can perform and **ticks** is the total number of ticks the wheel will display.

In your activity you can add a **OnScrollListener** listener to the wheel widget, in this way:

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
		
Where **float value** is a value between -1.0 and 1.0 of the current indicator position and **int roundValue** is a value between -(ticks*numRotations) and (ticks*numRotations)

## Screen Shots ##

![Wheel running on ICS](https://github.com/sephiroth74/AndroidWheel/raw/master/Screenshot_2012-01-28-13-33-04.png "Screenshot 1")