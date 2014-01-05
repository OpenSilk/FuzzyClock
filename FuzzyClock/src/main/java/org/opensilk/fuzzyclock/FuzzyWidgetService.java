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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import hugo.weaving.DebugLog;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class FuzzyWidgetService extends Service {

    private static final String TAG = FuzzyWidgetService.class.getSimpleName();
    private static final boolean LOGV = true;

    private Context mContext;
    private FormatChangeObserver mFormatChangeObserver;

    private AppWidgetManager mWidgetManager;
    private String mTimeZoneId;
    private boolean mCanDie;

    private final FuzzyLogic mFuzzyLogic = new FuzzyLogic();
    private final HashMap<Integer, FuzzyPrefs> mWidgetSettings = new HashMap<Integer, FuzzyPrefs>();
    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @DebugLog
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                mFuzzyLogic.setCalendar(Calendar.getInstance());
            }
            mHandler.removeCallbacks(mUpdateTimeRunnable);
            if (!Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                mHandler.post(mUpdateTimeRunnable);
            }
        }
    };

    private final Runnable mUpdateTimeRunnable = new Runnable() {
        @Override
        public void run() {
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
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiver(mIntentReceiver, filter);

        mFormatChangeObserver = new FormatChangeObserver();
        mContext.getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver
        );
        setDateFormat();

        mWidgetManager = AppWidgetManager.getInstance(mContext);
    }

    @DebugLog
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        cancelScheduledRestart();
        updateSettings();
        updateTime();
        return START_STICKY;
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
        mHandler.removeCallbacks(mUpdateTimeRunnable);
        if (!mCanDie) {
            // Oh fuck the system is killing us...
            // Make sure the ui is up to date
            updateTime();
            // Schedule a restart on next time tick in case we're not restarted in time.
            scheduleRestart();
        }
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
                RemoteViews views;
                switch (settings.style) {
                    case FuzzyPrefs.STYLE_STAGGERED:
                        views = new RemoteViews(mContext.getPackageName(), R.layout.fuzzy_widget_staggered);
                        break;
                    case FuzzyPrefs.STYLE_VERTICAL:
                        views = new RemoteViews(mContext.getPackageName(), R.layout.fuzzy_widget_vertical);
                        break;
                    case FuzzyPrefs.STYLE_HORIZONTAL:
                    default:
                        views = new RemoteViews(mContext.getPackageName(), R.layout.fuzzy_widget_horizontal);
                        break;
                }
                if (time.minute == -1) {
                    views.setViewVisibility(R.id.timeDisplayMinutes, View.GONE);
                } else {
                    views.setTextViewText(R.id.timeDisplayMinutes, timeM);
                    views.setTextColor(R.id.timeDisplayMinutes, getResources().getColor(settings.color.minute));
                    views.setTextViewTextSize(R.id.timeDisplayMinutes, COMPLEX_UNIT_SP, settings.size);
                    views.setViewVisibility(R.id.timeDisplayMinutes, View.VISIBLE);
                }
                if (time.separator == -1) {
                    views.setViewVisibility(R.id.timeDisplaySeparator, View.GONE);
                } else {
                    views.setTextViewText(R.id.timeDisplaySeparator, separator);
                    views.setTextColor(R.id.timeDisplaySeparator, getResources().getColor(settings.color.separator));
                    views.setTextViewTextSize(R.id.timeDisplaySeparator, COMPLEX_UNIT_SP, settings.size);
                    views.setViewVisibility(R.id.timeDisplaySeparator, View.VISIBLE);
                }
                if (time.hour == -1) {
                    views.setViewVisibility(R.id.timeDisplayHours, View.GONE);
                } else {
                    views.setTextViewText(R.id.timeDisplayHours, timeH);
                    views.setTextColor(R.id.timeDisplayHours, getResources().getColor(settings.color.hour));
                    views.setTextViewTextSize(R.id.timeDisplayHours, COMPLEX_UNIT_SP, settings.size);
                    views.setViewVisibility(R.id.timeDisplayHours, View.VISIBLE);
                }
                views.setContentDescription(R.id.fuzzy_clock, fullTimeStr);
                mWidgetManager.updateAppWidget(id, views);
            }
            Log.i(TAG, "Scheduling next clock update for "
                    + (mFuzzyLogic.getNextIntervalMilli()/1000) + "s from now");
            mHandler.removeCallbacks(mUpdateTimeRunnable);
            mHandler.postDelayed(mUpdateTimeRunnable, mFuzzyLogic.getNextIntervalMilli());
        } else {
            Log.i(TAG, "No widgets left to update...");
            mCanDie = true;
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

    private void scheduleRestart() {
        mFuzzyLogic.getCalendar().setTimeInMillis(System.currentTimeMillis());
        mFuzzyLogic.updateTime();
        long nextMilli = mFuzzyLogic.getNextIntervalMilli();
        Intent nextUpdate = new Intent(mContext, FuzzyWidgetService.class);
        PendingIntent pi = PendingIntent.getService(mContext, 0, nextUpdate, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC, mFuzzyLogic.getCalendar().getTimeInMillis() + nextMilli, pi);
        } else {
            am.set(AlarmManager.RTC, mFuzzyLogic.getCalendar().getTimeInMillis() + nextMilli, pi);
        }
        Log.i(TAG, "Scheduled service restart for " + (nextMilli/1000) + "s from now");
    }

    private void cancelScheduledRestart() {
        Intent nextUpdate = new Intent(mContext, FuzzyWidgetService.class);
        PendingIntent pi = PendingIntent.getService(mContext, 0, nextUpdate, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }
}
