package app.cryptotip.cryptotip.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Secure;

import com.gani.lib.GApp;
import com.gani.lib.http.GHttp;
import com.gani.lib.logging.GLog;

import app.cryptotip.cryptotip.app.http.MyHttp;

public class App extends Application {
  // Not yet used.
//  public static final int API_LEVEL = android.os.Build.VERSION.SDK_INT;

  private static Context globalContext;
  private static Resources resources;
  private static String versionName;
  private static String deviceId;
//  private static String codeName;
  private static Handler uiHandler;

  @Override
  public void onCreate(){
    super.onCreate();

    App.globalContext = this; // First thing to initialize because many other initializations depend on this
    App.resources = getResources();
    App.versionName = calculateApplicationVersionName();
    App.deviceId = Secure.getString(App.globalContext.getContentResolver(), Secure.ANDROID_ID);
//    App.codeName = Build.INSTANCE.getAppCodeName();
    App.uiHandler = new Handler();  // NOTE: Make sure the handler is created in UI thread.

    GApp.register(new GApp(globalContext, uiHandler, "", 0) {
    });

    GHttp.register(new MyHttp());

  }

  // A workaround for: http://code.google.com/p/android/issues/detail?id=20915
  private static void loadAsyncTaskClassOnUiThread() {
    try {
      Class.forName("android.os.AsyncTask");
    } catch (ClassNotFoundException e) {
      GLog.e(App.class, "Error when applying workaround for async task's dead handler problem", e);
    }
  }

  private static String calculateApplicationVersionName() {
    String versionName;
    try {
      PackageInfo info = globalContext.getPackageManager().getPackageInfo(globalContext.getPackageName(), 0);
      versionName = info.versionName;
      if (versionName == null) {  // We only get a proper version name if we deploy using ant.
        versionName = "test_mode"; // Version in the metrics DB is limited to 10 chars only. Make sure this isn't longer than that.
      }
    } catch (NameNotFoundException e) {
      versionName = "unknown";
    }
    return versionName;
  }

//  private static void enableHttpResponseCache() {
//    try {
//      if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//        long httpCacheSize = 20 * 1024 * 1024; // 20 MiB
//        File httpCacheDir = new File(globalContext.getExternalCacheDir(), "http");
//        Class.forName("android.net.http.HttpResponseCache").
//        getMethod("install", File.class, long.class).invoke(null, httpCacheDir, httpCacheSize);
//      }
//    }
//    catch (Exception httpResponseCacheNotAvailable) {
//    }
//  }

//  public static Context context() {
//    return globalContext;
//  }

//  @Override
//  protected void attachBaseContext(Context base) {
//    super.attachBaseContext(base);
//    MultiDex.install(this);
//  }

  public static String getVersionName() {
    return versionName;
  }

  public static String getDeviceId() {
    return deviceId;
  }

  /**
   * Does nothing if supplied <code>observer</code> is <code>null</code>. The null check is necessary because in dual panel mode
   * the secondary fragment may be created and destroyed immediately afterwards by the Android system, leaving fragment getInstance variables
   * (including content observers) uninitialized. Attempts to unregister such observers should check for null.
   */
  public static void nullSafeUnregisterContentObserver(ContentObserver observer) {
    if (observer != null) {
      globalContext.getContentResolver().unregisterContentObserver(observer);
    }
  }
}
