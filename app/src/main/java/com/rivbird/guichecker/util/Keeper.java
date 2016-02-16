package com.rivbird.guichecker.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

public class Keeper {
    private static final String KEEP_XML = "keeper";
    private static final String TAG = "Keeper";
    private static SharedPreferences sSharedPref = null;

    public static SharedPreferences getSharedPref() {
        assertInit();

        return sSharedPref;
    }

    protected static String getKeepXML() {
        return KEEP_XML;
    }

    public static void init(final Context context) {
        if (context == null)
            throw new IllegalArgumentException("Context should not be null");

        if (sSharedPref == null) {
            synchronized (Keeper.class) {
                if (sSharedPref == null)
                    sSharedPref = context.getSharedPreferences(getKeepXML(), Context.MODE_PRIVATE);
            }
        }
    }

    private static void assertInit() {
        if (sSharedPref == null)
            throw new IllegalArgumentException("SharedPreferences hasn't been initialized");
    }

    private static void assertKey(final String key) {
        if (TextUtils.isEmpty(key))
            throw new IllegalArgumentException("Key should not be null");
    }

    public static void keepInt(final String key, int value) {
        assertKey(key);
        assertInit();

        Editor editor = sSharedPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int readInt(final String key) {
        return readInt(key, -1);
    }

    public static int readInt(final String key, int defaultValue) {
        assertKey(key);
        assertInit();

        return sSharedPref.getInt(key, defaultValue);
    }

    public static void keepLong(final String key, long value) {
        assertKey(key);
        assertInit();

        Editor editor = sSharedPref.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long readLong(final String key) {
        return readLong(key, -1);
    }

    public static long readLong(final String key, long defaultValue) {
        assertKey(key);
        assertInit();

        return sSharedPref.getLong(key, defaultValue);
    }

    public static void keepString(final String key, final String value) {
        assertKey(key);
        assertInit();

        Editor editor = sSharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String readString(final String key) {
        return readString(key, null);
    }

    public static String readString(final String key, String defaultValue) {
        assertKey(key);
        assertInit();

        return sSharedPref.getString(key, defaultValue);
    }

    public static void keepBoolean(final String key, boolean value) {
        assertKey(key);
        assertInit();

        Editor editor = sSharedPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean readBoolean(final String key) {
        return readBoolean(key, false);
    }

    public static boolean readBoolean(final String key, boolean defaultValue) {
        assertKey(key);
        assertInit();

        return sSharedPref.getBoolean(key, defaultValue);
    }

    public static void keepFloat(final String key, float value) {
        assertKey(key);
        assertInit();

        Editor editor = sSharedPref.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static float readFloat(final String key) {
        return readFloat(key, -1f);
    }

    public static float readFloat(final String key, float defaultValue) {
        assertKey(key);
        assertInit();

        return sSharedPref.getFloat(key, defaultValue);
    }
}
