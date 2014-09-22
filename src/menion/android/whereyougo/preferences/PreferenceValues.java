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

package menion.android.whereyougo.preferences;

import java.util.Locale;

import menion.android.whereyougo.MainApplication;
import menion.android.whereyougo.R;
import menion.android.whereyougo.geo.location.Location;
import menion.android.whereyougo.geo.location.LocationState;
import menion.android.whereyougo.utils.A;
import menion.android.whereyougo.utils.Logger;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.Window;
import android.view.WindowManager;

public class PreferenceValues {

  private static final String TAG = "Settings";

  /** last application version that run on machine */
  private static final String KEY_S_APPLICATION_VERSION_LAST = "KEY_S_APPLICATION_VERSION_LAST";

  // GLOBAL
  /** root directory */
  public static final String KEY_S_ROOT = "KEY_S_ROOT";
  public static final String DEFAULT_ROOT = "";
  /** map provider */
  public static final String KEY_S_MAP_PROVIDER = "KEY_S_MAP_PROVIDER";
  public static final int VALUE_MAP_PROVIDER_VECTOR = 0;
  public static final int VALUE_MAP_PROVIDER_LOCUS = 1;

  /** enable icon in status bar */
  public static final String KEY_B_STATUSBAR = "KEY_B_STATUSBAR";

  /** enable fullscreen mode on newly created activities */
  public static final String KEY_B_FULLSCREEN = "KEY_B_FULLSCREEN";

  /** screen highlight mode */
  public static final String KEY_S_HIGHLIGHT = "KEY_S_HIGHLIGHT";
  public static final int VALUE_HIGHLIGHT_OFF = 0;
  public static final int VALUE_HIGHLIGHT_ONLY_GPS = 1;
  public static final int VALUE_HIGHLIGHT_ALWAYS = 2;

  /** save game automatically when user switches to another application */
  public static final String KEY_B_SAVEGAME_AUTO = "KEY_B_SAVEGAME_AUTO";

  /** stretch images */
  public static final String KEY_B_IMAGE_STRETCH = "KEY_B_IMAGE_STRETCH";

  /** font size */
  public static final String KEY_S_FONT_SIZE = "KEY_S_FONT_SIZE";
  public static final int VALUE_FONT_SIZE_DEFAULT = 0;
  public static final int VALUE_FONT_SIZE_SMALL = 1;
  public static final int VALUE_FONT_SIZE_MEDIUM = 2;
  public static final int VALUE_FONT_SIZE_LARGE = 3;

  // LOGIN
  /** GC credentials */
  public static final String KEY_S_GC_USERNAME = "KEY_S_GC_USERNAME";
  public static final String DEFAULT_GC_USERNAME = "";
  public static final String KEY_S_GC_PASSWORD = "KEY_S_GC_PASSWORD";
  public static final String DEFAULT_GC_PASSWORD = "";

  // GENERAL
  /** default language */
  public static final String KEY_S_LANGUAGE = "KEY_S_LANGUAGE";
  public static final String VALUE_LANGUAGE_CZ = "cz";
  public static final String VALUE_LANGUAGE_EN = "en";

  /** confirmation on exit */
  public static final String KEY_B_CONFIRM_ON_EXIT = "KEY_B_CONFIRM_ON_EXIT";

  /** last used index of coordinates format */
  public static final String KEY_I_GET_COORDINATES_LAST_INDEX = "KEY_I_GET_COORDINATES_LAST_INDEX";

  // GPS & LOCATION
  /** if GPS should start automatically after application start */
  public static final String KEY_B_START_GPS_AUTOMATICALLY = "KEY_B_START_GPS_AUTOMATICALLY";
  public static final boolean DEFAULT_START_GPS_AUTOMATICALLY = true;
  /** last known latitude */
  protected static final String KEY_F_LAST_KNOWN_LOCATION_LATITUDE =
      "KEY_F_LAST_KNOWN_LOCATION_LATITUDE";
  protected static final float DEFAULT_LAST_KNOWN_LOCATION_LATITUDE = 50.07967f;
  /** last known longitude */
  protected static final String KEY_F_LAST_KNOWN_LOCATION_LONGITUDE =
      "KEY_F_LAST_KNOWN_LOCATION_LONGITUDE";
  protected static final float DEFAULT_LAST_KNOWN_LOCATION_LONGITUDE = 14.42980f;
  /** last known altitude */
  protected static final String KEY_F_LAST_KNOWN_LOCATION_ALTITUDE =
      "KEY_F_LAST_KNOWN_LOCATION_ALTITUDE";
  protected static final float DEFAULT_LAST_KNOWN_LOCATION_ALTITUDE = 0.0f;
  /** add manual correction to altitude */
  public static final String KEY_S_GPS_ALTITUDE_MANUAL_CORRECTION =
      "KEY_S_GPS_ALTITUDE_MANUAL_CORRECTION";

  /** minimum time for notification */
  public static final String KEY_S_GPS_MIN_TIME_NOTIFICATION = "KEY_S_GPS_MIN_TIME_NOTIFICATION";

  /** beep on first gps fix */
  public static final String KEY_B_GPS_BEEP_ON_GPS_FIX = "KEY_B_GPS_BEEP_ON_GPS_FIX";

  /** disable GPS when not needed */
  public static final String KEY_B_GPS_DISABLE_WHEN_HIDE = "KEY_B_GPS_DISABLE_WHEN_HIDE";
  public static final boolean DEFAULT_GPS_DISABLE_WHEN_HIDE = false;

  // SENSORS
  /** is hardware orientation sensor enabled */
  public static final String KEY_B_HARDWARE_COMPASS_SENSOR = "KEY_B_HARDWARE_COMPASS_SENSOR";
  public static final boolean DEFAULT_HARDWARE_COMPASS_SENSOR = true;
  /** is hardware orientation sensor enabled */
  public static final String KEY_B_HARDWARE_COMPASS_AUTO_CHANGE =
      "KEY_B_HARDWARE_COMPASS_AUTO_CHANGE";

  /** is hardware orientation sensor enabled */
  public static final String KEY_S_HARDWARE_COMPASS_AUTO_CHANGE_VALUE =
      "KEY_S_HARDWARE_COMPASS_AUTO_CHANGE_VALUE";
  /** use true or magnetic bearing */
  public static final String KEY_B_SENSORS_BEARING_TRUE = "KEY_B_SENSORS_BEARING_TRUE";
  /** orientation filter */
  public static final String KEY_S_SENSORS_ORIENT_FILTER = "KEY_S_SENSORS_ORIENT_FILTER";
  public static final int VALUE_SENSORS_ORIENT_FILTER_NO = 0;
  public static final int VALUE_SENSORS_ORIENT_FILTER_LIGHT = 1;
  public static final int VALUE_SENSORS_ORIENT_FILTER_MEDIUM = 2;
  public static final int VALUE_SENSORS_ORIENT_FILTER_HEAVY = 3;

  // GUIDING
  /** is guiding sounds enabled on compass screen */
  public static final String KEY_B_GUIDING_COMPASS_SOUNDS = "KEY_B_GUIDING_COMPASS_SOUNDS";
  /** disable gps when screen off during guiding */
  public static final String KEY_B_GUIDING_GPS_REQUIRED = "KEY_B_GUIDING_GPS_REQUIRED";
  /** waypoint sounds */
  public static final String KEY_S_GUIDING_WAYPOINT_SOUND = "KEY_S_GUIDING_WAYPOINT_SOUND";
  public static final int VALUE_GUIDING_WAYPOINT_SOUND_INCREASE_CLOSER = 0;
  public static final int VALUE_GUIDING_WAYPOINT_SOUND_BEEP_ON_DISTANCE = 1;
  public static final int VALUE_GUIDING_WAYPOINT_SOUND_CUSTOM_SOUND = 2;
  public static final String VALUE_GUIDING_WAYPOINT_SOUND_CUSTOM_SOUND_URI = "";
  /** waypoint sounds beep distance */
  public static final String KEY_S_GUIDING_WAYPOINT_SOUND_DISTANCE =
      "KEY_S_GUIDING_WAYPOINT_SOUND_DISTANCE";
  /** navigation point */
  public static final String KEY_S_GUIDING_ZONE_POINT = "KEY_S_GUIDING_ZONE_POINT";
  public static final int VALUE_GUIDING_ZONE_POINT_CENTER = 0;
  public static final int VALUE_GUIDING_ZONE_POINT_NEAREST = 1;
  
  // UNITS PARAMETRES
  /** default latitude/longitude format */
  public static final String KEY_S_UNITS_COO_LATLON = "KEY_S_UNITS_COO_LATLON";
  public static final int VALUE_UNITS_COO_LATLON_DEC = 0;
  public static final int VALUE_UNITS_COO_LATLON_MIN = 1;
  public static final int VALUE_UNITS_COO_LATLON_SEC = 2;
  /** default length format */
  public static final String KEY_S_UNITS_LENGTH = "KEY_S_UNITS_LENGTH";
  public static final int VALUE_UNITS_LENGTH_ME = 0;
  public static final int VALUE_UNITS_LENGTH_IM = 1;
  public static final int VALUE_UNITS_LENGTH_NA = 2;
  /** default height format */
  public static final String KEY_S_UNITS_ALTITUDE = "KEY_S_UNITS_ALTITUDE";
  public static final int VALUE_UNITS_ALTITUDE_METRES = 0;
  public static final int VALUE_UNITS_ALTITUDE_FEET = 1;
  /** default angle format */
  public static final String KEY_S_UNITS_SPEED = "KEY_S_UNITS_SPEED";
  public static final int VALUE_UNITS_SPEED_KMH = 0;
  public static final int VALUE_UNITS_SPEED_MILH = 1;
  public static final int VALUE_UNITS_SPEED_KNOTS = 2;
  /** default angle format */
  public static final String KEY_S_UNITS_ANGLE = "KEY_S_UNITS_ANGLE";
  public static final int VALUE_UNITS_ANGLE_DEGREE = 0;
  public static final int VALUE_UNITS_ANGLE_MIL = 1;
  
  /* LANGUAGE */
  private static String loca = null;

  /* LAST KNOW LOCATION */
  /** last known location */
  public static Location lastKnownLocation;

  // setted from onResume();
  private static Activity currentActivity;

  private static PowerManager.WakeLock wl;

  public static void disableWakeLock() {
    Logger.w(TAG, "disableWakeLock(), wl:" + wl);
    if (wl != null) {
      wl.release();
      wl = null;
    }
  }

  public static void enableWakeLock() {
    try {
      boolean disable = false;
      if (Preferences.APPEARANCE_HIGHLIGHT == VALUE_HIGHLIGHT_OFF) {
        disable = true;
      } else if (Preferences.APPEARANCE_HIGHLIGHT == VALUE_HIGHLIGHT_ONLY_GPS) {
        if (!LocationState.isActuallyHardwareGpsOn()) {
          disable = true;
        }
      }
      Logger.w(TAG, "enableWakeLock(), dis:" + disable + ", wl:" + wl);
      if (disable && wl != null) {
        disableWakeLock();
      } else if (!disable && wl == null) {
        PowerManager pm = (PowerManager) A.getApp().getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
        wl.acquire();
      }
      // Logger.w(TAG, "enableWakeLock(), res:" + wl);
    } catch (Exception e) {
      Logger.e(TAG, "enableWakeLock(), e:" + e.toString());
    }
  }

  public static boolean existCurrentActivity() {
    return currentActivity != null;
  }

  public static int getApplicationVersionActual() {
    try {
      return A.getApp().getPackageManager().getPackageInfo(A.getApp().getPackageName(), 0).versionCode;
    } catch (NameNotFoundException e) {
      Logger.e(TAG, "getApplicationVersionActual()", e);
      return 0;
    }
  }

  public static String getApplicationVersionActualName() {
    try {
      return A.getApp().getPackageManager().getPackageInfo(A.getApp().getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
      Logger.e(TAG, "getApplicationVersionActual()", e);
      return "";
    }
  }

  /* APPLICATION VERSION */
  public static int getApplicationVersionLast() {
    return PreferenceManager.getDefaultSharedPreferences(A.getApp()).getInt(
        KEY_S_APPLICATION_VERSION_LAST, 0);
  }

  public static Activity getCurrentActivity() {
    return currentActivity == null ? A.getMain() : currentActivity;
  }

  public static String getLanguageCode() {
    if (loca == null) {
      // String lang = getPrefString(KEY_S_LANGUAGE,
      // Locale.getDefault().getLanguage());
      String lang = Locale.getDefault().getLanguage();
      Logger.w(TAG, "getLanguageCode() - " + lang);
      if (lang == null)
        return VALUE_LANGUAGE_EN;
      if (lang.equals(VALUE_LANGUAGE_CZ)) {
        loca = VALUE_LANGUAGE_CZ;
      } else {
        loca = VALUE_LANGUAGE_EN;
      }
    }
    return loca;
  }

  public static Location getLastKnownLocation(Context c) {  
    if (lastKnownLocation == null) {
      lastKnownLocation = new Location(TAG);
      lastKnownLocation.setLatitude(getPrefFloat(c, KEY_F_LAST_KNOWN_LOCATION_LATITUDE,
    	  DEFAULT_LAST_KNOWN_LOCATION_LATITUDE));
      lastKnownLocation.setLongitude(getPrefFloat(c, KEY_F_LAST_KNOWN_LOCATION_LONGITUDE,
    	  DEFAULT_LAST_KNOWN_LOCATION_LONGITUDE));
      lastKnownLocation.setAltitude(getPrefFloat(c, KEY_F_LAST_KNOWN_LOCATION_ALTITUDE,
          DEFAULT_LAST_KNOWN_LOCATION_ALTITUDE));
    }
    return lastKnownLocation;
  }
 
  public static boolean getPrefBoolean(Context context, String key, boolean def) {
    // Logger.v(TAG, "getPrefBoolean(" + key + ", " + def + ")");
    return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, def);
  }
  
  public static boolean getPrefBoolean(String key, boolean def) {
    if (A.getApp() == null) {
      return def;
    }
    return PreferenceManager.getDefaultSharedPreferences(A.getApp()).getBoolean(key, def);
  }

  public static float getPrefFloat(Context context, String key, float def) {
    // Logger.v(TAG, "getPrefFloat(" + key + ", " + def + ")");
    return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, def);
  }
  
  @Deprecated  
  public static String getPrefString(Context context, String key, String def) {
    // Logger.v(TAG, "getPrefString(" + key + ", " + def + ")");
    return PreferenceManager.getDefaultSharedPreferences(context).getString(key, def);
  }

  @Deprecated  
  public static String getPrefString(String key, String def) {
    if (A.getApp() == null) {
      return def;
    }
    return PreferenceManager.getDefaultSharedPreferences(A.getApp()).getString(key, def);
  }
    
  @Deprecated
  public static String getPrefString( final int keyId, String def) {
	if (A.getApp() == null) {
	  return def;
	}
	String key = A.getApp().getString( keyId );
	return PreferenceManager.getDefaultSharedPreferences(A.getApp()).getString(key, def);
  }  
  
  public static void setApplicationVersionLast(int lastVersion) {
    PreferenceManager.getDefaultSharedPreferences(A.getApp()).edit()
        .putInt(KEY_S_APPLICATION_VERSION_LAST, lastVersion).commit();
  }

  public static void setCurrentActivity(Activity activity) {
    if (PreferenceValues.currentActivity == null && activity != null)
      MainApplication.appRestored();
    PreferenceValues.currentActivity = activity;
  }

  public static void setLastKnownLocation() {
    try {
      PreferenceManager
          .getDefaultSharedPreferences(A.getApp())
          .edit()
          .putFloat(KEY_F_LAST_KNOWN_LOCATION_LATITUDE,
              (float) LocationState.getLocation().getLatitude())
          .putFloat(KEY_F_LAST_KNOWN_LOCATION_LONGITUDE,
              (float) LocationState.getLocation().getLongitude())
          .putFloat(KEY_F_LAST_KNOWN_LOCATION_ALTITUDE,
              (float) LocationState.getLocation().getAltitude()).commit();
    } catch (Exception e) {
      Logger.e(TAG, "setLastKnownLocation()", e);
    }
  }

  public static void setPrefBoolean(Context context, String key, boolean value) {
    // Logger.v(TAG, "setPrefBoolean(" + key + ", " + value + ")");
    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).commit();
  }

  public static void setPrefBoolean(String key, boolean value) {
    if (A.getApp() == null) {
      return;
    }
    PreferenceManager.getDefaultSharedPreferences(A.getApp()).edit().putBoolean(key, value)
        .commit();
  }

  public static void setPrefFloat(Context context, String key, float value) {
    // Logger.v(TAG, "setPrefFloat(" + key + ", " + value + ")");
    PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat(key, value).commit();
  }

  public static void setPrefFloat(String key, float value) {
    if (A.getApp() == null) {
      return;
    }
    PreferenceManager.getDefaultSharedPreferences(A.getApp()).edit().putFloat(key, value).commit();
  }

  public static void setPrefInt(Context context, String key, int value) {
    // Logger.v(TAG, "setPrefInt(" + key + ", " + value + ")");
    PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).commit();
  }


  public static void setPrefInt(String key, int value) {
    if (A.getApp() == null) {
      return;
    }
    PreferenceManager.getDefaultSharedPreferences(A.getApp()).edit().putInt(key, value).commit();
  }

  public static void setPrefString(Context context, String key, String value) {
    // Logger.v(TAG, "setPrefString(" + key + ", " + value + ")");
    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).commit();
  }

  /**
   * Does some thing in old style.
   *
   * @deprecated use {@link #new()} instead.  
   */

  
  public static void setPrefString( int keyId, String value) {
	if (A.getApp() == null) {
	  return;
	}
	String key = A.getApp().getString( keyId );
	PreferenceManager.getDefaultSharedPreferences(A.getApp()).edit().putString(key, value).commit();
  }  

  public static boolean setScreenBasic(Activity activity) {
    try {
      activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
      return true;
    } catch (Exception e) {
      Logger.e(TAG, "setFullScreen(" + activity + ")", e);
    }
    return false;
  }

  public static void setScreenFullscreen(Activity activity) {
    try {
        if (Preferences.APPEARANCE_FULLSCREEN) {
          activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
              WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
          activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    } catch (Exception e) {
      Logger.e(TAG, "setFullScreen(" + activity + ")", e);
    }
  }

  public static void setStatusbar(Activity activity) {
    try {
         NotificationManager mNotificationManager =
            (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        // set statusbar
        if (Preferences.APPEARANCE_STATUSBAR) {
          Context context = activity.getApplicationContext();
          Intent intent =
              new Intent(context, menion.android.whereyougo.gui.activity.MainActivity.class);
          // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
          intent.addCategory(Intent.CATEGORY_LAUNCHER);
          intent.setAction(Intent.ACTION_MAIN);
          PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
          final int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
          Notification notif = null;
          if (sdkVersion < android.os.Build.VERSION_CODES.HONEYCOMB) {
            notif =
                new Notification(R.drawable.ic_title_logo, "WhereYouGo", System.currentTimeMillis());
            notif.setLatestEventInfo(activity, "WhereYouGo", "", pIntent);
          } else {
            NotificationCompat.Builder builder =
                new NotificationCompat.Builder(activity).setContentTitle("WhereYouGo")
                    .setSmallIcon(R.drawable.ic_title_logo).setContentIntent(pIntent);
            notif = builder.build();
          }
          notif.flags = Notification.FLAG_ONGOING_EVENT;
          mNotificationManager.notify(0, notif);
        } else {
          mNotificationManager.cancel(0);
        }
    } catch (Exception e) {
      Logger.e(TAG, "setStatusbar(" + activity + ")", e);
    }
  }
}
