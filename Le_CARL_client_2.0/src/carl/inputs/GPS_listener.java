package carl.inputs;

import carl.gui.*;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.SystemClock;

public class GPS_listener implements LocationListener
{
	private static final int ONE_MINUTES = 1000 * 60 * 1;
	Location lastKnownLocation_GPS;
	Main_activity main_acti;
	boolean NO_GPS = true;
	long mLastLocationMillis=0;
	private boolean isGPSFix=false;
	
	public GPS_listener(Main_activity act)
	{
    	super();
    	main_acti = act;
	}
	
	public synchronized Location get_gps_loc()
	{
		long nb = SystemClock.elapsedRealtime() - mLastLocationMillis;
		isGPSFix = nb < 3000;
		
		if(!isGPSFix) return null;
		else if(lastKnownLocation_GPS != null) return lastKnownLocation_GPS;  //return new Location(lastKnownLocation_GPS);
		else return null;
	}
	
	public void onLocationChanged(Location location) 
	{				
		if(isBetterLocation(location,  lastKnownLocation_GPS) == true)
		{
			lastKnownLocation_GPS = location;	
			mLastLocationMillis = SystemClock.elapsedRealtime();
			
//			main_acti.runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					main_acti.GPS_txt.setText("Accuracy: " + lastKnownLocation_GPS.getAccuracy()
//							+ "\n X: " + lastKnownLocation_GPS.getLatitude() + "  Y: " + lastKnownLocation_GPS.getLongitude());  
//				}
//			});
		}				
	}
	public void onStatusChanged(String provider, int status, Bundle extras) {} // stupid function never called !!!
	public void onProviderEnabled(String provider) {}
	public void onProviderDisabled(String provider) {}
	
	/********************************************************************************************************************************************************************/
	/**************************************************************** functions for GPS *********************************************************************************/
	/********************************************************************************************************************************************************************/
	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) 
	{
	    if (currentBestLocation == null) {  return true; } 	        // A new location is always better than no location

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > ONE_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -ONE_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {return true;
	    } else if (isSignificantlyOlder) {return false; } 	    // If the new location is more than two minutes older, it must be worse

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) { return true;
	    } else if (isNewer && !isLessAccurate) { return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {   return true;  }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) 
	{
	    if (provider1 == null) { return provider2 == null;}
	    return provider1.equals(provider2);
	}
}
