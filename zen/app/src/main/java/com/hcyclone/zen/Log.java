package com.hcyclone.zen;

/**
 * Logger.
 */
public class Log {

  public static void i(String tag, String string) {
    android.util.Log.i(tag, string);
  }

  public static void e(String tag, String string) {
    android.util.Log.e(tag, string);
  }

  public static void e(String tag, String string, Throwable e) {
    android.util.Log.e(tag, string, e);
  }

  public static void d(String tag, String string) {
    if (isLog()) {
      android.util.Log.d(tag, string);
    }
  }

  public static void v(String tag, String string) {
    if (isLog()) {
      android.util.Log.v(tag, string);
    }
  }

  public static void w(String tag, String string) {
    android.util.Log.w(tag, string);
  }

  public static void w(String tag, String string, Throwable e) {
    android.util.Log.w(tag, string, e);
  }

  private static boolean isLog() {
    return Utils.isDebug();
  }
}
