package it.sephiroth.android.wheel.view;

import it.sephiroth.android.wheel.R;
import it.sephiroth.android.wheel.easing.Easing;
import it.sephiroth.android.wheel.easing.Sine;
import it.sephiroth.android.wheel.graphics.LinearGradientDrawable;
import it.sephiroth.android.wheel.utils.ReflectionUtils;
import it.sephiroth.android.wheel.utils.ReflectionUtils.ReflectionException;
import it.sephiroth.android.wheel.view.IFlingRunnable.FlingRunnableView;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class Wheel extends View implements OnGestureListener, FlingRunnableView, VibrationWidget {

	/** The Constant LOG_TAG. */
	static final String LOG_TAG = "wheel";

	/**
	 * The listener interface for receiving onScroll events. The class that is interested in processing a onScroll event implements
	 * this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnScrollListener<code> method. When
	 * the onScroll event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnScrollEvent
	 */
	public interface OnScrollListener {

		/**
		 * On scroll started.
		 * 
		 * @param view
		 *           the view
		 * @param value
		 *           the value
		 * @param roundValue
		 *           the round value
		 */
		void onScrollStarted( Wheel view, float value, int roundValue );

		/**
		 * On scroll.
		 * 
		 * @param view
		 *           the view
		 * @param value
		 *           the value
		 * @param roundValue
		 *           the round value
		 */
		void onScroll( Wheel view, float value, int roundValue );

		/**
		 * On scroll finished.
		 * 
		 * @param view
		 *           the view
		 * @param value
		 *           the value
		 * @param roundValue
		 *           the round value
		 */
		void onScrollFinished( Wheel view, float value, int roundValue );
	}

	public interface OnLayoutListener {

		void onLayout( View view );
	}

	static final int MSG_VIBRATE = 1;
	int mPaddingLeft = 0;
	int mPaddingRight = 0;
	int mPaddingTop = 0;
	int mPaddingBottom = 0;
	int mWidth, mHeight;
	boolean mInLayout = false;
	int mMaxX, mMinX;
	OnScrollListener mScrollListener;
	OnLayoutListener mLayoutListener;
	Paint mPaint;
	Shader mShader3;
	Bitmap mTickBitmap;
	Bitmap mIndicator;
	DrawFilter mFast, mDF;
	GestureDetector mGestureDetector;
	boolean mIsFirstScroll;
	IFlingRunnable mFlingRunnable;
	int mAnimationDuration = 200;
	boolean mToLeft;
	int mTouchSlop;
	float mIndicatorX = 0;
	int mOriginalDeltaX = 0;
	float mTickSpace = 30;
	int mWheelSizeFactor = 2;
	int mTicksCount = 18;
	float mTicksSize = 7.0f;
	Vibrator mVibrator;
	static Handler mVibrationHandler;
	Easing mTicksEasing = new Sine();
	Matrix mDrawMatrix = new Matrix();
	boolean mForceLayout;
	private int[] mBgColors = { 0xffa1a1a1, 0xffa1a1a1, 0xffffffff, 0xffa1a1a1, 0xffa1a1a1 };
	private float[] mBgPositions = { 0, 0.2f, 0.5f, 0.8f, 1f };


	/**
	 * Instantiates a new wheel.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 * @param defStyle
	 *           the def style
	 */
	public Wheel( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );
		init( context, attrs, defStyle );
	}

	/**
	 * Instantiates a new wheel.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public Wheel( Context context, AttributeSet attrs ) {
		this( context, attrs, 0 );
	}

	/**
	 * Instantiates a new wheel.
	 * 
	 * @param context
	 *           the context
	 */
	public Wheel( Context context ) {
		this( context, null );
	}

	/**
	 * Sets the on scroll listener.
	 * 
	 * @param listener
	 *           the new on scroll listener
	 */
	public void setOnScrollListener( OnScrollListener listener ) {
		mScrollListener = listener;
	}

	public void setOnLayoutListener( OnLayoutListener listener ) {
		mLayoutListener = listener;
	}
	
	/**
	 * change the current wheel position and value
	 * @param value - the new value. it should be between -1.0f and 1.0f
	 * @param fireScrollEvent - if true this will call the scrollCompletion listener
	 */
	public void setValue( float value, boolean fireScrollEvent ) {
		if( value >= -1 && value <= 1 ) {
			int w = getRealWidth();
			mFlingRunnable.stop( false );
			mOriginalDeltaX = (int) ( value * ( w * mWheelSizeFactor ) );
			invalidate();
			
			if( fireScrollEvent ) {
				scrollCompleted();
			}
		}
	}	

	/**
	 * Initializer.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 * @param defStyle
	 *           the def style
	 */
	private void init( Context context, AttributeSet attrs, int defStyle ) {

		if ( android.os.Build.VERSION.SDK_INT > 8 ) {
			try {
				mFlingRunnable = (IFlingRunnable) ReflectionUtils.newInstance( "it.sephiroth.android.wheel.view.Fling9Runnable",
						new Class<?>[] { FlingRunnableView.class, int.class }, this, mAnimationDuration );
			} catch ( ReflectionException e ) {
				mFlingRunnable = new Fling8Runnable( this, mAnimationDuration );
			}
		} else {
			mFlingRunnable = new Fling8Runnable( this, mAnimationDuration );
		}

		TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.Wheel, defStyle, 0 );

		mTicksCount = a.getInteger( R.styleable.Wheel_ticks, 18 );
		mWheelSizeFactor = a.getInteger( R.styleable.Wheel_numRotations, 2 );

		a.recycle();

		mFast = new PaintFlagsDrawFilter( Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG, 0 );
		mPaint = new Paint( Paint.FILTER_BITMAP_FLAG );

		mGestureDetector = new GestureDetector( context, this );
		mGestureDetector.setIsLongpressEnabled( false );
		setFocusable( true );
		setFocusableInTouchMode( true );

		mTouchSlop = ViewConfiguration.get( context ).getScaledTouchSlop();

		try {
			mVibrator = (Vibrator) context.getSystemService( Context.VIBRATOR_SERVICE );
		} catch ( Exception e ) {
			Log.e( LOG_TAG, e.toString() );
		}

		if ( mVibrator != null ) {
			setVibrationEnabled( true );
		}
		setBackgroundDrawable( new LinearGradientDrawable( Orientation.LEFT_RIGHT, mBgColors, mBgPositions ) );
	}

	/**
	 * Enable/Disable the vibration feedback
	 */
	@Override
	public synchronized void setVibrationEnabled( boolean value ) {
		if ( !value ) {
			mVibrationHandler = null;
		} else {
			if ( null == mVibrationHandler ) {
				mVibrationHandler = new Handler() {

					@Override
					public void handleMessage( Message msg ) {
						super.handleMessage( msg );

						switch ( msg.what ) {
							case MSG_VIBRATE:
								try {
									mVibrator.vibrate( 10 );
								} catch ( SecurityException e ) {
									// missing VIBRATE permission
								}
						}
					}
				};
			}
		}
	}

	/**
	 * Get the current vibration status
	 */
	@Override
	public synchronized boolean getVibrationEnabled() {
		return mVibrationHandler != null;
	}

	/**
	 * Make bitmap3.
	 * 
	 * @param width
	 *           the width
	 * @param height
	 *           the height
	 * @return the bitmap
	 */
	private Bitmap makeBitmap3( int width, int height ) {

		Bitmap bm = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
		Canvas c = new Canvas( bm );
		Paint p = new Paint( Paint.ANTI_ALIAS_FLAG );

		int colors[] = { 0xdd000000, 0x00000000, 0x00000000, 0xdd000000 };
		float positions[] = { 0f, 0.2f, 0.8f, 1f };
		LinearGradient gradient = new LinearGradient( 0, 0, width, 0, colors, positions, TileMode.REPEAT );

		p.setShader( gradient );
		p.setDither( true );
		c.drawRect( 0, 0, width, height, p );

		return bm;
	}

	/**
	 * Make ticker bitmap.
	 * 
	 * @param width
	 *           the width
	 * @param height
	 *           the height
	 * @return the bitmap
	 */
	private Bitmap makeTickerBitmap( int width, int height ) {
		float ellipse = width / 2;

		Bitmap bm = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
		Canvas c = new Canvas( bm );

		Paint p = new Paint( Paint.ANTI_ALIAS_FLAG );
		p.setDither( true );

		p.setColor( 0xFF888888 );

		float h = (float) height;
		float y = ( h + 10.0f ) / 10.0f;
		float y2 = y * 2.5f;

		RectF rect = new RectF( 0, y, width, height - y2 );
		c.drawRoundRect( rect, ellipse, ellipse, p );

		p.setColor( 0xFFFFFFFF );
		rect = new RectF( 0, y2, width, height - y );
		c.drawRoundRect( rect, ellipse, ellipse, p );

		p.setColor( 0xFFCCCCCC );
		rect = new RectF( 0, y + 2, width, height - ( y + 2 ) );
		c.drawRoundRect( rect, ellipse, ellipse, p );
		return bm;
	}

	/**
	 * Make bitmap indicator.
	 * 
	 * @param width
	 *           the width
	 * @param height
	 *           the height
	 * @return the bitmap
	 */
	private Bitmap makeBitmapIndicator( int width, int height ) {

		float ellipse = width / 2;
		float h = (float) height;
		float y = ( h + 10.0f ) / 10.0f;
		float y2 = y * 2.5f;

		Bitmap bm = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
		Canvas c = new Canvas( bm );

		Paint p = new Paint( Paint.ANTI_ALIAS_FLAG );
		p.setDither( true );

		p.setColor( 0xFF666666 );
		RectF rect = new RectF( 0, y, width, height - y2 );
		c.drawRoundRect( rect, ellipse, ellipse, p );

		p.setColor( 0xFFFFFFFF );
		rect = new RectF( 0, y2, width, height - y );
		c.drawRoundRect( rect, ellipse, ellipse, p );

		rect = new RectF( 0, y + 2, width, height - ( y + 2 ) );
		int colors[] = { 0xFF0076E7, 0xFF00BBFF, 0xFF0076E7 };
		float positions[] = { 0f, 0.5f, 1f };
		LinearGradient gradient = new LinearGradient( 0, 0, width, 0, colors, positions, TileMode.REPEAT );

		p.setShader( gradient );
		c.drawRoundRect( rect, ellipse, ellipse, p );

		return bm;
	}

	@Override
	protected void onDraw( Canvas canvas ) {
		super.onDraw( canvas );

		if ( mShader3 != null ) {
			canvas.setDrawFilter( mDF );

			final int w = mWidth;
			int total = mTicksCount;
			float x2;
			float scale, scale2;

			mPaint.setShader( null );

			for ( int i = 0; i < total; i++ ) {
				float x = ( mOriginalDeltaX + ( ( (float) i / total ) * w ) );

				if ( x < 0 ) {
					x = w - ( -x % w );
				} else {
					x = x % w;
				}

				scale = (float) mTicksEasing.easeInOut( x, 0, 1.0, mWidth );
				scale2 = (float) ( Math.sin( Math.PI * ( x / mWidth ) ) );

				mDrawMatrix.reset();
				mDrawMatrix.setScale( scale2, 1 );
				mDrawMatrix.postTranslate( (int) ( scale * mWidth ) - ( mTicksSize / 2 ), 0 );
				canvas.drawBitmap( mTickBitmap, mDrawMatrix, mPaint );
			}

			float indicatorx = ( mIndicatorX + mOriginalDeltaX );

			if ( indicatorx < 0 ) {
				indicatorx = ( mWidth * 2 ) - ( -indicatorx % ( mWidth * 2 ) );
			} else {
				indicatorx = indicatorx % ( mWidth * 2 );
			}

			if ( indicatorx > 0 && indicatorx < mWidth ) {

				x2 = (float) mTicksEasing.easeInOut( indicatorx, 0, mWidth, w );
				scale2 = (float) ( Math.sin( Math.PI * ( indicatorx / mWidth ) ) );

				mDrawMatrix.reset();
				mDrawMatrix.setScale( scale2, 1 );
				mDrawMatrix.postTranslate( x2 - ( mTicksSize / 2 ), 0 );
				canvas.drawBitmap( mIndicator, mDrawMatrix, mPaint );
			}

			mPaint.setShader( mShader3 );
			canvas.drawPaint( mPaint );
		}
	}

	/**
	 * Change the background gradient colors. the size of the colors array must be the same as the size of the positions array.
	 * 
	 * @param colors
	 * @param positions
	 */
	public void setBackgroundColors( int[] colors, float[] positions ) {
		if ( colors != null && positions != null && colors.length == positions.length ) {
			mBgColors = colors;
			mBgPositions = positions;
			setBackgroundDrawable( new LinearGradientDrawable( Orientation.LEFT_RIGHT, mBgColors, mBgPositions ) );
		}
	}

	/**
	 * Sets the wheel scale factor.
	 * 
	 * @param value
	 *           the new wheel scale factor
	 */
	public void setWheelScaleFactor( int value ) {
		mWheelSizeFactor = value;
		mForceLayout = true;
		requestLayout();
		postInvalidate();
	}


	/**
	 * Gets the wheel scale factor.
	 * 
	 * @return the wheel scale factor
	 */
	public int getWheelScaleFactor() {
		return mWheelSizeFactor;
	}

	/**
	 * Gets the tick space.
	 * 
	 * @return the tick space
	 */
	public float getTickSpace() {
		return mTickSpace;
	}

	@Override
	protected void onLayout( boolean changed, int left, int top, int right, int bottom ) {
		super.onLayout( changed, left, top, right, bottom );

		mInLayout = true;

		if ( changed || mForceLayout ) {

			mWidth = right - left;
			mHeight = bottom - top;

			mTickSpace = (float) mWidth / mTicksCount;
			mTicksSize = mWidth / mTicksCount / 4.0f;
			mTicksSize = Math.min( Math.max( mTicksSize, 3.5f ), 6.0f );

			mIndicatorX = (float) mWidth / 2.0f;

			mMaxX = mWidth * mWheelSizeFactor;

			mIndicator = makeBitmapIndicator( (int) Math.ceil( mTicksSize ), bottom - top );
			mTickBitmap = makeTickerBitmap( (int) Math.ceil( mTicksSize ), bottom - top );
			mShader3 = new BitmapShader( makeBitmap3( right - left, bottom - top ), Shader.TileMode.CLAMP, Shader.TileMode.REPEAT );

			mMinX = -mMaxX;

			if ( null != mLayoutListener ) {
				mLayoutListener.onLayout( this );
			}
		}

		mInLayout = false;
		mForceLayout = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
	 */
	@Override
	public boolean onDown( MotionEvent event ) {
		mDF = mFast;
		mFlingRunnable.stop( false );
		mIsFirstScroll = true;
		return true;
	}

	/**
	 * On up.
	 */
	void onUp() {
		if ( mFlingRunnable.isFinished() ) {
			scrollIntoSlots();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onFling( MotionEvent event0, MotionEvent event1, float velocityX, float velocityY ) {

		boolean toleft = velocityX < 0;

		if ( !toleft ) {
			if ( mOriginalDeltaX > mMaxX ) {
				mFlingRunnable.startUsingDistance( mOriginalDeltaX, mMaxX - mOriginalDeltaX );
				return true;
			}
		} else {
			if ( mOriginalDeltaX < mMinX ) {
				mFlingRunnable.startUsingDistance( mOriginalDeltaX, mMinX - mOriginalDeltaX );
				return true;
			}
		}

		mFlingRunnable.startUsingVelocity( mOriginalDeltaX, (int) velocityX / 2 );
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
	 */
	@Override
	public void onLongPress( MotionEvent arg0 ) {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onScroll( MotionEvent event0, MotionEvent event1, float distanceX, float distanceY ) {
		getParent().requestDisallowInterceptTouchEvent( true );

		if ( mIsFirstScroll ) {
			if ( distanceX > 0 )
				distanceX -= mTouchSlop;
			else
				distanceX += mTouchSlop;
			scrollStarted();
		}

		mIsFirstScroll = false;

		float delta = -1 * distanceX;
		mToLeft = delta < 0;

		if ( !mToLeft ) {
			if ( mOriginalDeltaX + delta > mMaxX ) {
				delta /= ( ( (float) mOriginalDeltaX + delta ) - mMaxX ) / 10;
			}
		} else {
			if ( mOriginalDeltaX + delta < mMinX ) {
				delta /= -( ( (float) mOriginalDeltaX + delta ) - mMinX ) / 10;
			}
		}

		trackMotionScroll( (int) ( mOriginalDeltaX + delta ) );
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
	 */
	@Override
	public void onShowPress( MotionEvent arg0 ) {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
	 */
	@Override
	public boolean onSingleTapUp( MotionEvent arg0 ) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent( MotionEvent event ) {
		boolean retValue = mGestureDetector.onTouchEvent( event );

		int action = event.getAction();
		if ( action == MotionEvent.ACTION_UP ) {
			mDF = null;
			onUp();
		} else if ( action == MotionEvent.ACTION_CANCEL ) {}
		return retValue;
	}

	/** The m last motion value. */
	float mLastMotionValue;

	/**
	 * Track motion scroll.
	 * 
	 * @param newX
	 *           the new x
	 */
	@Override
	public void trackMotionScroll( int newX ) {
		mOriginalDeltaX = newX;
		scrollRunning();
		invalidate();
	}

	/**
	 * Gets the limited motion scroll amount.
	 * 
	 * @param motionToLeft
	 *           the motion to left
	 * @param deltaX
	 *           the delta x
	 * @return the limited motion scroll amount
	 */
	int getLimitedMotionScrollAmount( boolean motionToLeft, int deltaX ) {

		if ( motionToLeft ) {} else {
			if ( mMaxX >= mOriginalDeltaX ) {
				// The extreme child is past his boundary point!
				return deltaX;
			}
		}

		int centerDifference = mOriginalDeltaX - mMaxX;
		return motionToLeft ? Math.max( centerDifference, deltaX ) : Math.min( centerDifference, deltaX );
	}

	/**
	 * Scroll into slots.
	 */
	@Override
	public void scrollIntoSlots() {

		if ( !mFlingRunnable.isFinished() ) {
			return;
		}

		if ( mOriginalDeltaX > mMaxX ) {
			mFlingRunnable.startUsingDistance( mOriginalDeltaX, mMaxX - mOriginalDeltaX );
			return;
		} else if ( mOriginalDeltaX < mMinX ) {
			mFlingRunnable.startUsingDistance( mOriginalDeltaX, mMinX - mOriginalDeltaX );
			return;
		}

		int diff = Math.round( mOriginalDeltaX % mTickSpace );
		int diff2 = (int) ( mTickSpace - diff );
		int diff3 = (int) ( mTickSpace + diff );

		if ( diff != 0 && diff2 != 0 && diff3 != 0 ) {
			if ( Math.abs( diff ) < ( mTickSpace / 2 ) ) {
				mFlingRunnable.startUsingDistance( mOriginalDeltaX, -diff );
			} else {
				mFlingRunnable.startUsingDistance( mOriginalDeltaX, (int) ( mToLeft ? -diff3 : diff2 ) );
			}
		} else {
			onFinishedMovement();
		}
	}

	/**
	 * On finished movement.
	 */
	private void onFinishedMovement() {
		scrollCompleted();
	}

	/**
	 * Gets the real width.
	 * 
	 * @return the real width
	 */
	private int getRealWidth() {
		return ( getWidth() - mPaddingLeft - mPaddingRight );
	}

	/** The m scroll selection notifier. */
	ScrollSelectionNotifier mScrollSelectionNotifier;

	/**
	 * The Class ScrollSelectionNotifier.
	 */
	private class ScrollSelectionNotifier implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if ( mShader3 == null ) {
				post( this );
			} else {
				fireOnScrollCompleted();
			}
		}
	}

	/**
	 * Scroll completed.
	 */
	void scrollCompleted() {
		if ( mScrollListener != null ) {
			if ( mInLayout ) {
				if ( mScrollSelectionNotifier == null ) {
					mScrollSelectionNotifier = new ScrollSelectionNotifier();
				}
				post( mScrollSelectionNotifier );
			} else {
				fireOnScrollCompleted();
			}
		}
	}

	/**
	 * Scroll started.
	 */
	void scrollStarted() {
		if ( mScrollListener != null ) {
			if ( mInLayout ) {
				if ( mScrollSelectionNotifier == null ) {
					mScrollSelectionNotifier = new ScrollSelectionNotifier();
				}
				post( mScrollSelectionNotifier );
			} else {
				fireOnScrollStarted();
			}
		}
	}

	/**
	 * Scroll running.
	 */
	void scrollRunning() {
		if ( mScrollListener != null ) {
			if ( mInLayout ) {
				if ( mScrollSelectionNotifier == null ) {
					mScrollSelectionNotifier = new ScrollSelectionNotifier();
				}
				post( mScrollSelectionNotifier );
			} else {
				fireOnScrollRunning();
			}
		}
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public float getValue() {
		int w = getRealWidth();
		int position = mOriginalDeltaX;
		float value = (float) position / ( w * mWheelSizeFactor );
		return value;
	}

	/**
	 * Gets the tick value.
	 * 
	 * @return the tick value
	 */
	int getTickValue() {
		return (int) ( ( getCurrentPage() * mTicksCount ) + ( mOriginalDeltaX % mWidth ) / mTickSpace );
	}

	/**
	 * Return the total number of ticks available for scrolling.
	 * 
	 * @return the ticks count
	 */
	public int getTicksCount() {
		try {
			return (int) ( ( ( mMaxX / mWidth ) * mTicksCount ) + ( mOriginalDeltaX % mWidth ) / mTickSpace ) * 2;
		} catch ( ArithmeticException e ) {
			return 0;
		}
	}

	/**
	 * Gets the ticks.
	 * 
	 * @return the ticks
	 */
	public int getTicks() {
		return mTicksCount;
	}

	/**
	 * Gets the current page.
	 * 
	 * @return the current page
	 */
	int getCurrentPage() {
		return ( mOriginalDeltaX / mWidth );
	}

	/**
	 * Fire on scroll completed.
	 */
	private void fireOnScrollCompleted() {
		mScrollListener.onScrollFinished( this, getValue(), getTickValue() );
	}

	/**
	 * Fire on scroll started.
	 */
	private void fireOnScrollStarted() {
		mScrollListener.onScrollStarted( this, getValue(), getTickValue() );
	}

	/**
	 * Fire on scroll running.
	 */
	private void fireOnScrollRunning() {

		int value = getTickValue();

		if ( value != mLastMotionValue ) {
			if ( mVibrationHandler != null ) {
				mVibrationHandler.sendEmptyMessage( MSG_VIBRATE );
			}
		}

		if ( null != mScrollListener ) {
			mScrollListener.onScroll( this, getValue(), value );
		}
		mLastMotionValue = value;
	}

	@Override
	public int getMinX() {
		return mMinX;
	}

	@Override
	public int getMaxX() {
		return mMaxX;
	}
}
