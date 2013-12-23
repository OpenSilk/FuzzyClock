/*
 *  Copyright (C) 2013 OpenSilk Productions LLC
 *
 *  This file is part of Fuzzy Clock
 *
 *  Fuzzy Clock is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  Fuzzy Clock is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Fuzzy Clock.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opensilk.fuzzyclock;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import hugo.weaving.DebugLog;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static org.opensilk.fuzzyclock.FuzzyWidget.ACTION_UPDATE_WIDGET_SETTINGS;
import static org.opensilk.fuzzyclock.FuzzyWidget.ACTION_UPDATE_WIDGET_TIME;

public class FuzzyWidgetService extends Service {

    private static final String TAG = FuzzyWidgetService.class.getSimpleName();
    private static final boolean LOGV = true;

    private Context mContext;
    private FuzzyLogic mFuzzyLogic = new FuzzyLogic();
    private FormatChangeObserver mFormatChangeObserver;

    private AppWidgetManager mWidgetManager;

    private String mTimeZoneId;
    private SharedPreferences mPrefs;

    private HashMap<Integer, FuzzyPrefs> mWidgetSettings = new HashMap<Integer, FuzzyPrefs>();

    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                mFuzzyLogic.setCalendar(Calendar.getInstance());
            }
            mHandler.removeCallbacks(mUpdateTimeRunnable);
            mHandler.post(mUpdateTimeRunnable);
        }
    };

    private final Runnable mUpdateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            updateTime();
        }
    };

    private final Runnable mUpdateSettingsRunnable = new Runnable() {
        @Override
        public void run() {
            updateSettings();
            updateTime();
        }
    };

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
            setDateFormat();
            updateTime();
        }
    }

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mFuzzyLogic.setCalendar(Calendar.getInstance());

        /* monitor time ticks, time changed, timezone */
        IntentFilter filter = new IntentFilter();
        //filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mContext.registerReceiver(mIntentReceiver, filter);

        mFormatChangeObserver = new FormatChangeObserver();
        mContext.getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver
        );
        setDateFormat();

        mWidgetManager = AppWidgetManager.getInstance(mContext);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        updateSettings();
    }

    @DebugLog
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (LOGV) Log.v(TAG, "Received " + intent.getAction());
            if (ACTION_UPDATE_WIDGET_TIME.equals(intent.getAction())) {
                mHandler.removeCallbacks(mUpdateTimeRunnable);
                mHandler.post(mUpdateTimeRunnable);
            } else if (ACTION_UPDATE_WIDGET_SETTINGS.equals(intent.getAction())) {
                mHandler.removeCallbacks(mUpdateSettingsRunnable);
                mHandler.removeCallbacks(mUpdateTimeRunnable);
                mHandler.post(mUpdateSettingsRunnable);
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @DebugLog
    @Override
    public void onDestroy() {
        mContext.unregisterReceiver(mIntentReceiver);
        mContext.getContentResolver().unregisterContentObserver(mFormatChangeObserver);
        super.onDestroy();
    }

    @DebugLog
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateSettings();
        updateTime();
    }

    @DebugLog
    private void updateTime() {
        mFuzzyLogic.getCalendar().setTimeInMillis(System.currentTimeMillis());

        if (mTimeZoneId != null) {
            mFuzzyLogic.getCalendar().setTimeZone(TimeZone.getTimeZone(mTimeZoneId));
        }

        mFuzzyLogic.updateTime();

        FuzzyLogic.FuzzyTime time = mFuzzyLogic.getFuzzyTime();
        CharSequence timeM = (time.minute != -1) ? getResources().getString(time.minute) : "";
        CharSequence timeH = (time.hour != -1) ? getResources().getString(time.hour) : "";
        CharSequence separator = (time.separator != -1) ? getResources().getString(time.separator) : "";

        final StringBuilder fullTimeStr = new StringBuilder();
        fullTimeStr.append(timeM);
        fullTimeStr.append(separator);
        fullTimeStr.append(timeH);

        int[] widgetIds = mWidgetManager.getAppWidgetIds(new ComponentName(mContext, FuzzyWidget.class));

        if (widgetIds != null && widgetIds.length > 0) {
            for (int id: widgetIds) {
                FuzzyPrefs settings = mWidgetSettings.get((Integer) id);
                if (settings == null) {
                    continue; // Once setup is done we will be called again.
                }
                if (LOGV) Log.v(TAG, "Updating widget view id=" + id + " " + settings.toString());
                RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.fuzzy_widget);
                views.setTextViewText(R.id.timeDisplayMinutes, timeM);
                views.setTextViewText(R.id.timeDisplayHours, timeH);
                views.setTextViewText(R.id.timeDisplaySeparator, separator);
                views.setTextColor(R.id.timeDisplayMinutes, getResources().getColor(settings.color.minute));
                views.setTextColor(R.id.timeDisplayHours, getResources().getColor(settings.color.hour));
                views.setTextColor(R.id.timeDisplaySeparator, getResources().getColor(settings.color.separator));
                views.setTextViewTextSize(R.id.timeDisplayMinutes, COMPLEX_UNIT_SP, settings.size);
                views.setTextViewTextSize(R.id.timeDisplayHours, COMPLEX_UNIT_SP, settings.size);
                views.setTextViewTextSize(R.id.timeDisplaySeparator, COMPLEX_UNIT_SP, settings.size);
                views.setContentDescription(R.id.time, fullTimeStr);
                mWidgetManager.updateAppWidget(id, views);
            }
        } else {
            Log.i(TAG, "No widgets left to update...");
            stopSelf();
        }
    }

    @DebugLog
    private void updateSettings() {
        int[] widgetIds = mWidgetManager.getAppWidgetIds(new ComponentName(mContext, FuzzyWidget.class));
        if (widgetIds != null && widgetIds.length > 0) {
            for (int id: widgetIds) {
                if (LOGV) Log.v(TAG, "Updating widget settings id=" + id);
                mWidgetSettings.put((Integer) id, new FuzzyPrefs(mContext, id));
            }
        }
    }

    private void setDateFormat() {
        mFuzzyLogic.setDateFormat(false); //TODO fix support for 24hour
        //mFuzzyLogic.setDateFormat(android.text.format.DateFormat.is24HourFormat(getContext()));
    }

    public void setTimeZone(String id) {
        mTimeZoneId = id;
        updateTime();
    }
}
