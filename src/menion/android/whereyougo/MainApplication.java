/*
 * This file is part of WhereYouGo.
 * 
 * WhereYouGo is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * WhereYouGo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with WhereYouGo. If not,
 * see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2012 Menion <whereyougo@asamm.cz>
 */

package menion.android.whereyougo;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import menion.android.whereyougo.geo.location.LocationState;
import menion.android.whereyougo.gui.activity.MainActivity;
import menion.android.whereyougo.preferences.PreferenceValues;
import menion.android.whereyougo.preferences.Preferences;
import menion.android.whereyougo.utils.ExceptionHandler;
import menion.android.whereyougo.utils.FileSystem;
import menion.android.whereyougo.utils.Logger;
import menion.android.whereyougo.utils.StringToken;
import menion.android.whereyougo.utils.Utils;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;
import cz.matejcik.openwig.Engine;
import menion.android.whereyougo.R;

public class MainApplication extends Application {

  public interface OnAppVisibilityChange {

    public void onAppMinimized();

    public void onAppRestored();

  }

  private class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
        // Logger.v(TAG, "ACTION_SCREEN_OFF");
        mScreenOff = true;
      } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
        // Logger.v(TAG, "ACTION_SCREEN_ON");
        LocationState.onScreenOn(false);
        mScreenOff = false;
      }
    }
  }

  private static final String TAG = "MainApplication";
  // application name
  public static String APP_NAME = "WhereYouGo";

  public static void appRestored() {
    onAppRestored();
    if (onAppVisibilityChange != null)
      onAppVisibilityChange.onAppRestored();
  }

  private Locale locale = null;

  // screen ON/OFF receiver
  private ScreenReceiver mScreenReceiver;

  private boolean mScreenOff = false;

  private static Timer mTimer;

  private static OnAppVisibilityChange onAppVisibilityChange;

  public static void onActivityPause() {
    // Logger.i(TAG, "onActivityPause()");
    if (mTimer != null) {
      mTimer.cancel();
    }

    mTimer = new Timer();
    mTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (!PreferenceValues.existCurrentActivity())
          onAppMinimized();
        LocationState.onActivityPauseInstant(PreferenceValues.getCurrentActivity());
        mTimer = null;
      }
    }, 2000);
  }

  private static void onAppMinimized() {
    Logger.w(TAG, "onAppMinimized()");
    if (onAppVisibilityChange != null)
      onAppVisibilityChange.onAppMinimized();
  }

  private static void onAppRestored() {
    Logger.w(TAG, "onAppRestored()");
  }

  public static void registerVisibilityHandler(OnAppVisibilityChange handler) {
    MainApplication.onAppVisibilityChange = handler;
  }

  public void destroy() {
    try {
      unregisterReceiver(mScreenReceiver);
    } catch (Exception e) {
      Logger.w(TAG, "destroy(), e:" + e);
    }
    if (mTimer != null) {
      mTimer.cancel();
      mTimer = null;
    }
    onAppVisibilityChange = null;
  }

  private void initCore() {
    // register screen on/off receiver
    IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
    filter.addAction(Intent.ACTION_SCREEN_OFF);
    mScreenReceiver = new ScreenReceiver();
    registerReceiver(mScreenReceiver, filter);   
    
    // try{Log.i("FS", getCacheDir().getAbsolutePath());}catch(Exception e){Log.i("FS", "-");}
    // try{Log.i("FS", getExternalCacheDir().getAbsolutePath());}catch(Exception e){Log.i("FS",
    // "-");}
    // try{Log.i("FS", getFilesDir().getAbsolutePath());}catch(Exception e){Log.i("FS", "-");}
    // try{Log.i("FS", getExternalFilesDir(null).getAbsolutePath());}catch(Exception e){Log.i("FS",
    // "-");}
    // try{Log.i("FS", Environment.getDataDirectory().getAbsolutePath());}catch(Exception
    // e){Log.i("FS", "-");}
    // try{Log.i("FS", Environment.getDownloadCacheDirectory().getAbsolutePath());}catch(Exception
    // e){Log.i("FS", "-");}
    // try{Log.i("FS", Environment.getExternalStorageDirectory().getAbsolutePath());}catch(Exception
    // e){Log.i("FS", "-");}
    // try{Log.i("FS", Environment.getRootDirectory().getAbsolutePath());}catch(Exception
    // e){Log.i("FS", "-");}
    // initialize root directory
    if (Preferences.GLOBAL_ROOT.equals(PreferenceValues.DEFAULT_ROOT)
        || !FileSystem.setRootDirectory(null, Preferences.GLOBAL_ROOT)) {
      if (!FileSystem.createRoot(APP_NAME)) {
        try {
          FileSystem.setRootDirectory(null, getExternalFilesDir(null).getAbsolutePath());
        } catch (Exception e1) {
          try {
            FileSystem.setRootDirectory(null, getFilesDir().getAbsolutePath());
          } catch (Exception e2) {
          }
        }
      }
    }
    try {
      FileSystem.CACHE = getExternalCacheDir().getAbsolutePath();
    } catch (Exception e1) {
      try {
        FileSystem.CACHE = getCacheDir().getAbsolutePath();
      } catch (Exception e2) {
        FileSystem.CACHE = FileSystem.ROOT + "cache/";
      }
    }
    if (!FileSystem.CACHE.endsWith("/"))
      FileSystem.CACHE += "/";
    FileSystem.CACHE_AUDIO = FileSystem.CACHE + "audio/";

    // set location state
    LocationState.init(this);
    // initialize DPI
    Utils.getDpPixels(this, 1.0f);

    // set DeviceID for OpenWig
    try {
      PackageManager pm = getPackageManager();
      PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
      String name =
          String.format("%s, app:%s", pm.getApplicationLabel(pi.applicationInfo), pi.versionName);
      String platform = String.format("Android %s", android.os.Build.VERSION.RELEASE);
      cz.matejcik.openwig.WherigoLib.env.put(cz.matejcik.openwig.WherigoLib.DEVICE_ID, name);
      cz.matejcik.openwig.WherigoLib.env.put(cz.matejcik.openwig.WherigoLib.PLATFORM, platform);
    } catch (Exception e) {
      // not really important
    }
  }

  public boolean isScreenOff() {
    return mScreenOff;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (locale != null) {
      newConfig.locale = locale;
      Locale.setDefault(locale);
      getBaseContext().getResources().updateConfiguration(newConfig,
          getBaseContext().getResources().getDisplayMetrics());
    }
  }

  /* LEGECY SUPPORT - less v0.8.14
   * Converts preference - comes from a former version (less 0.8.14) 
   * which are not stored as string into string.
   */
  private void legecySupport4PreferencesFloat( int prefId ) {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
	String key = getString( prefId );
	
	try {
		  String vlaue = sharedPref.getString( key, "" );
		} catch( Exception e ) {
			try { 
				  Log.d( TAG, "legecySupport4PreferencesFloat() - LEGECY SUPPORT: convert float to string" );
			  	  Float value = sharedPref.getFloat( key, 0.0f );
			  	  sharedPref.edit().remove( key ).commit();
			  	  sharedPref.edit().putString(key, String.valueOf( value ) ).commit();  
				} catch( Exception ee ) {
			    	Log.e( TAG, "legecySupport4PreferencesFloat() - panic remove", ee );
			    	sharedPref.edit().remove( key ).commit();
			    }			
		}
  }
  
  private void legecySupport4PreferencesInt( int prefId ) {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    String key = getString( prefId );

    try {
    	  String value = sharedPref.getString( key, "" );
    } catch( Exception e ) {
	    try {  
	    	Log.d( TAG, "legecySupport4PreferencesInt() - LEGECY SUPPORT: convert int to string" );	
	      int value = sharedPref.getInt( key, 0 );
	      sharedPref.edit().remove( key ).commit();
	      sharedPref.edit().putString(key, String.valueOf(value) ).commit();  
	    } catch( Exception ee ) {
	      Log.e( TAG, "legecySupportFloat2Int() - panic remove", ee );
	      sharedPref.edit().remove( key ).commit();
	    }	
    }
  }  
  /* LEGECY SUPPORT -- END */
  
  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "onCreate()");
    Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

    /* LEGECY SUPPORT - less v0.8.14
     * Converts preference - comes from a former version (less 0.8.14) 
     * which are not stored as string into string.
     */
    try {
	   	// legecySupport4PreferencesFloat( R.string.pref_KEY_S_GPS_ALTITUDE_MANUAL_CORRECTION );
	   	legecySupport4PreferencesFloat( R.string.pref_KEY_F_LAST_KNOWN_LOCATION_LATITUDE );
	   	legecySupport4PreferencesFloat( R.string.pref_KEY_F_LAST_KNOWN_LOCATION_LONGITUDE );
	   	legecySupport4PreferencesFloat( R.string.pref_KEY_F_LAST_KNOWN_LOCATION_ALTITUDE );
	   	legecySupport4PreferencesInt( R.string.pref_KEY_S_APPLICATION_VERSION_LAST );
    } catch( Exception e ) {
    	Log.e( TAG, "onCreate() - PANIC! Wipe out preferences", e );
    	PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
    }
    /* LEGECY SUPPORT -- END */
    
    // set basic settings values
    PreferenceManager.setDefaultValues(this, R.xml.whereyougo_preferences, false);
    Preferences.setContext( this );
    Preferences.init(this);    

    // get language 
    Configuration config = getBaseContext().getResources().getConfiguration();
    String lang = Preferences.getStringPreference( R.string.pref_KEY_S_LANGUAGE );
    
    /* LEGECY SUPPORT - less v0.8.14
     * This block is a workaround to switch from 'cs' to 'cz' 
     * remove this block after one year (2014-09)
     */
    if ( lang.equals( "cs" ) ) {
    	lang = this.getString( R.string.pref_language_cz_shortcut );
    	Preferences.setStringPreference( R.string.pref_KEY_S_LANGUAGE, lang );
    }
    /* LEGECY SUPPORT -- END */
    
    // set language
    if (!lang.equals( getString( R.string.pref_language_default_value ) )
        && !config.locale.getLanguage().equals(lang)) {
      ArrayList<String> loc = StringToken.parse(lang, "_");
      if (loc.size() == 1) {
        locale = new Locale(lang);
      } else {
        locale = new Locale(loc.get(0), loc.get(1));
      }
      Locale.setDefault(locale);
      config.locale = locale;
      getBaseContext().getResources().updateConfiguration(config,
          getBaseContext().getResources().getDisplayMetrics());
    }

    // initialize core
    initCore();
  }

  public void onLowMemory() {
    super.onLowMemory();
    Log.d(TAG, "onLowMemory()");
  }

  public void onTerminate() {
    super.onTerminate();
    Log.d(TAG, "onTerminate()");
  }

  @Override
  public void onTrimMemory(int level) {
    // TODO Auto-generated method stub
    super.onTrimMemory(level);
    Logger.i(TAG, String.format("onTrimMemory(%d)", level));
    if (Preferences.GLOBAL_SAVEGAME_AUTO
        && level == android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
        && MainActivity.selectedFile != null && Engine.instance != null) {
      
      // backup
      try {
        FileSystem.backupFile(MainActivity.getSaveFile());
      } catch (Exception e) {
      }
      
      Engine.requestSync();
      Toast.makeText(this, R.string.save_game_auto, Toast.LENGTH_SHORT).show();
    }
  }
}
