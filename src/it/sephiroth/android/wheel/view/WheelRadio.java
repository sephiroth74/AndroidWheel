package it.sephiroth.android.wheel.view;

import it.sephiroth.android.wheel.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

// TODO: Auto-generated Javadoc
/**
 * The Class WheelRadio.
 */
public class WheelRadio extends View {

	static final String LOG_TAG = "wheel-radio";

	Bitmap mIndicatorBig, mIndicatorSmall;
	Shader mShader;
	Shader mShader1;
	Bitmap mIndicator;
	Paint mPaint;
	DrawFilter mFast;
	int mPaddingLeft = 10;
	int mPaddingRight = 10;
	int mLineTickSize = 1;
	int mLineBigSize = 3;
	int mSmallTicksCount = 10;
	int mBigTicksCount = 1;
	float mCorrectionX = 0;
	Rect mRealRect;
	boolean mForceLayout;
	float mValue = 0;
	int mValueIndicatorColor, mSmallIndicatorColor, mBigIndicatorColor;

	/**
	 * Instantiates a new wheel radio.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 * @param defStyle
	 *           the def style
	 */
	public WheelRadio( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );
		init( context, attrs, defStyle );
	}

	/**
	 * Instantiates a new wheel radio.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public WheelRadio( Context context, AttributeSet attrs ) {
		this( context, attrs, -1 );
	}

	/**
	 * Instantiates a new wheel radio.
	 * 
	 * @param context
	 *           the context
	 */
	public WheelRadio( Context context ) {
		this( context, null );
	}

	/**
	 * Inits the.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 * @param defStyle
	 *           the def style
	 */
	private void init( Context context, AttributeSet attrs, int defStyle ) {
		mFast = new PaintFlagsDrawFilter( Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG, 0 );
		mPaint = new Paint( Paint.FILTER_BITMAP_FLAG );

		TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.WheelRadio, defStyle, 0 );
		mSmallTicksCount = a.getInteger( R.styleable.WheelRadio_smallTicks, 25 ) - 1;
		mBigTicksCount = a.getInteger( R.styleable.WheelRadio_bigTicks, 5 ) - 1;
		mValueIndicatorColor = a.getColor( R.styleable.WheelRadio_valueIndicatorColor, 0xFFFFFFFF );
		mSmallIndicatorColor = a.getColor( R.styleable.WheelRadio_smallIndicatorColor, 0x33FFFFFF );
		mBigIndicatorColor = a.getColor( R.styleable.WheelRadio_bigIndicatorColor, 0x66FFFFFF );
		a.recycle();
	}

	/**
	 * Sets the total ticks count.
	 * 
	 * @param value
	 *           the value
	 * @param value2
	 *           the value2
	 */
	public void setTicksNumber( int value, int value2 ) {
		mSmallTicksCount = value;
		mBigTicksCount = value2;
		mForceLayout = true;
		requestLayout();
		postInvalidate();
	}

	@Override
	protected void onLayout( boolean changed, int left, int top, int right, int bottom ) {
		super.onLayout( changed, left, top, right, bottom );

		int w = right - left;

		if ( w > 0 && changed || mForceLayout ) {
			mRealRect = new Rect( mPaddingLeft, top, w - mPaddingRight, bottom );
			mIndicatorSmall = makeBitmap2( w / mSmallTicksCount, bottom - top, mLineTickSize );
			mShader = new BitmapShader( mIndicatorSmall, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP );
			mIndicatorBig = makeBitmap3( mRealRect.width() / mBigTicksCount, bottom - top, mLineBigSize );
			mShader1 = new BitmapShader( mIndicatorBig, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP );
			mIndicator = makeIndicator( bottom - top, mLineBigSize );
			mCorrectionX = (((float)mRealRect.width() / mBigTicksCount) % 1)*mBigTicksCount;
			mForceLayout = false;
		}
	}

	@Override
	protected void onDraw( Canvas canvas ) {
		super.onDraw( canvas );
		

		if ( mShader != null ) {
			canvas.setDrawFilter( mFast );

			int saveCount = canvas.save();

			mPaint.setShader( mShader );
			canvas.drawRect( mRealRect, mPaint );
			
			canvas.translate( mPaddingLeft - mLineBigSize / 2, 0 );
			mPaint.setShader( mShader1 );
			canvas.drawPaint( mPaint );

			mPaint.setShader( null );

			float rw = ((float) mRealRect.width() - (mCorrectionX)) / 2;
			canvas.drawBitmap( mIndicator, ( rw + ( rw * mValue ) ), 0, mPaint );
			canvas.restoreToCount( saveCount );
		}
	}

	

	/**
	 * Sets the current value.
	 * 
	 * @param value
	 *           the new value
	 */
	public void setValue( float value ) {
		mValue = Math.min( Math.max( value, -1 ), 1 );
		postInvalidate();
	}

	/**
	 * Gets the current value.
	 * 
	 * @return the value
	 */
	public float getValue() {
		return mValue;
	}

	/**
	 * Make small indicator bitmap.
	 * 
	 * @param width
	 *           the width
	 * @param height
	 *           the height
	 * @param line_size
	 *           the line_size
	 * @return the bitmap
	 */
	private Bitmap makeBitmap2( int width, int height, int line_size ) {
		Bitmap bm = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
		Canvas c = new Canvas( bm );

		int center_h = height * 2 / 3;

		Paint p = new Paint( Paint.ANTI_ALIAS_FLAG );
		p.setDither( true );

		p.setColor( mSmallIndicatorColor );
		RectF rect = new RectF( 0, height - center_h, line_size, height - ( height - center_h ) );
		c.drawRect( rect, p );
		return bm;
	}

	/**
	 * Make the big indicator bitmap.
	 * 
	 * @param width
	 *           the width
	 * @param height
	 *           the height
	 * @param line_size
	 *           the line_size
	 * @return the bitmap
	 */
	private Bitmap makeBitmap3( int width, int height, int line_size ) {

		Bitmap bm = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
		Canvas c = new Canvas( bm );

		int center_h = height * 4 / 5;

		Paint p = new Paint( Paint.ANTI_ALIAS_FLAG );
		p.setDither( true );

		p.setColor( mBigIndicatorColor );
		RectF rect = new RectF( 0, height - center_h, line_size, height - ( height - center_h ) );
		c.drawRect( rect, p );
		return bm;
	}

	/**
	 * Make current value indicator bitmap.
	 * 
	 * @param height
	 *           the height
	 * @param line_size
	 *           the line_size
	 * @return the bitmap
	 */
	private Bitmap makeIndicator( int height, int line_size ) {

		Bitmap bm = Bitmap.createBitmap( line_size, height, Bitmap.Config.ARGB_8888 );
		Canvas c = new Canvas( bm );

		int center_h = height;

		Paint p = new Paint( Paint.ANTI_ALIAS_FLAG );
		p.setDither( true );

		p.setColor( mValueIndicatorColor );
		RectF rect = new RectF( 0, height - center_h, line_size, height - ( height - center_h ) );
		c.drawRect( rect, p );
		return bm;
	}
}
