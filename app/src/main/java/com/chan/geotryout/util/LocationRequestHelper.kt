package com.chan.geotryout.util

import android.content.Context
import android.content.SharedPreferences




/**
 * Created by chandra-1765$ on 24/08/21$.
 */
class LocationRequestHelper private constructor(context: Context) {
    protected var mContext: Context
    private val mSharedPreferences: SharedPreferences
    private val mSharedPreferencesEditor: SharedPreferences.Editor?

    /**
     * Stores String value in preference
     */
    fun setValue(key: String?, value: String?) {
        mSharedPreferencesEditor!!.putString(key, value)
        mSharedPreferencesEditor!!.commit()
    }

    /**
     * Stores int value in preference
     */
    fun setValue(key: String?, value: Int) {
        mSharedPreferencesEditor!!.putInt(key, value)
        mSharedPreferencesEditor!!.commit()
    }

    /**
     * Stores Double value in String format in preference
     */
    fun setValue(key: String?, value: Double) {
        setValue(key, java.lang.Double.toString(value))
    }

    /**
     * Stores long value in preference
     */
    fun setValue(key: String?, value: Long) {
        mSharedPreferencesEditor!!.putLong(key, value)
        mSharedPreferencesEditor!!.commit()
    }

    /**
     * Stores boolean value in preference
     */
    fun setValue(key: String?, value: Boolean) {
        mSharedPreferencesEditor!!.putBoolean(key, value)
        mSharedPreferencesEditor!!.commit()
    }

    /**
     * Retrieves String value from preference
     */
    fun getStringValue(key: String?, defaultValue: String?): String? {
        return mSharedPreferences.getString(key, defaultValue)
    }

    /**
     * Retrieves int value from preference
     */
    fun getIntValue(key: String?, defaultValue: Int): Int {
        return mSharedPreferences.getInt(key, defaultValue)
    }

    /**
     * Retrieves long value from preference
     */
    fun getLongValue(key: String?, defaultValue: Long): Long {
        return mSharedPreferences.getLong(key, defaultValue)
    }

    /**
     * Retrieves boolean value from preference
     */
    fun getBoolanValue(keyFlag: String?, defaultValue: Boolean): Boolean {
        return mSharedPreferences.getBoolean(keyFlag, defaultValue)
    }

    /**
     * Removes key from preference
     *
     * @param key key of preference that is to be deleted
     */
    fun removeKey(key: String?) {
        if (mSharedPreferencesEditor != null) {
            mSharedPreferencesEditor.remove(key)
            mSharedPreferencesEditor.commit()
        }
    }

    /**
     * Clears all the preferences stored
     */
    fun clear() {
        mSharedPreferencesEditor!!.clear().commit()
    }

    companion object {
        private var mSharedPreferenceUtils: LocationRequestHelper? = null
        @Synchronized
        fun getInstance(context: Context): LocationRequestHelper? {
            if (mSharedPreferenceUtils == null) {
                mSharedPreferenceUtils = LocationRequestHelper(context.getApplicationContext())
            }
            return mSharedPreferenceUtils
        }
    }

    init {
        mContext = context
        mSharedPreferences = context.getSharedPreferences(
            "com.freakyjolly.demobackgroundlocation",
            Context.MODE_PRIVATE
        )
        mSharedPreferencesEditor = mSharedPreferences.edit()
    }
}