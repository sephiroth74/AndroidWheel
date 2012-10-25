package it.sephiroth.android.wheel.view;

public interface VibrationWidget {

	/**
	 * Enable the vibration feedback
	 * 
	 * @param value
	 */
	public void setVibrationEnabled( boolean value );

	/**
	 * Get the vibration feedback enabled status
	 * 
	 * @return
	 */
	public boolean getVibrationEnabled();
}
