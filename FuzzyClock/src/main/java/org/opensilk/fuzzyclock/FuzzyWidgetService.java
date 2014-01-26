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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import hugo.weaving.DebugLog;

public class FuzzyWidgetService extends Service {

    private static final String TAG = FuzzyWidgetService.class.getSimpleName();
    private static final boolean LOGV = BuildConfig.DEBUG;

    private Context mContext;
    private FormatChangeObserver mFormatChangeObserver;

    private PendingIntent mRestartIntent;
    private AlarmManager mAlarmManager;
    private LayoutInflater mLayoutInflater;

    private AppWidgetManager mWidgetManager;
    private String mTimeZoneId;

    private int startCount;

    private final FuzzyLogic mFuzzyLogic = new FuzzyLogicWarped();
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
            mHandler.post(mUpdateTimeRunnable);
        }
    };

    private final Runnable mUpdateSettingsRunnable = new Runnable() {
        @Override
        public void run() {
            updateSettings();
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
            mHandler.post(mUpdateTimeRunnable);
        }
    }

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();
        startCount = 0;
        mContext = this;
        mFuzzyLogic.setCalendar(Calendar.getInstance());

        /* monitor time ticks, time changed, timezone */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mContext.registerReceiver(mIntentReceiver, filter);

        mFormatChangeObserver = new FormatChangeObserver();
        mContext.getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver
        );
        setDateFormat();

        mWidgetManager = AppWidgetManager.getInstance(mContext);
        mRestartIntent = PendingIntent.getService(mContext, 0,
                new Intent(mContext, FuzzyWidgetService.class),
                PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @DebugLog
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (startCount++ == 0 || intent.getAction() != null) {
            mHandler.post(mUpdateSettingsRunnable);
        }
        mHandler.post(mUpdateTimeRunnable);
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
        super.onDestroy();
    }

    @DebugLog
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mHandler.post(mUpdateSettingsRunnable);
        mHandler.post(mUpdateTimeRunnable);
    }

    @DebugLog
    private void updateTime() {
        mFuzzyLogic.getCalendar().setTimeInMillis(System.currentTimeMillis());

        if (mTimeZoneId != null) {
            mFuzzyLogic.getCalendar().setTimeZone(TimeZone.getTimeZone(mTimeZoneId));
        }

        mFuzzyLogic.updateTime();

        FuzzyClockView fuzzyClock = (FuzzyClockView) mLayoutInflater.inflate(R.layout.fuzzy_clock, null);
        fuzzyClock.updateTime();

        int[] widgetIds = mWidgetManager.getAppWidgetIds(new ComponentName(mContext, FuzzyWidget.class));
        if (widgetIds != null && widgetIds.length > 0) {
            for (int id: widgetIds) {
                FuzzyPrefs settings = mWidgetSettings.get((Integer) id);
                if (settings == null) {
                    continue; // Once setup is done we will be called again.
                }
                if (LOGV) Log.v(TAG, "Updating widget view id=" + id + " " + settings.toString());
                fuzzyClock.loadPreferences(settings);
                // Where the magic happens... w & h are 0 without this
                fuzzyClock.measure(0, 0);
                fuzzyClock.layout(0, 0, fuzzyClock.getMeasuredWidth(), fuzzyClock.getMeasuredHeight());
                if (fuzzyClock.getMeasuredWidth() == 0 || fuzzyClock.getMeasuredHeight() == 0) {
                    Log.e(TAG, "WARN: w or h was zero! skipping draw");
                    continue;
                }
                // Draw view into a bitmap
                Bitmap bitmap = Bitmap.createBitmap(fuzzyClock.getMeasuredWidth(), fuzzyClock.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(bitmap);
                fuzzyClock.draw(c);
                // send bitmap to remote view
                RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.fuzzy_widget);
                Intent intent = new Intent(mContext, FuzzyWidgetSettings.class);
                intent.setAction(String.format("dummy_%d", id));
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
                PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                views.setImageViewBitmap(R.id.fuzzy_clock_image, bitmap);
                views.setContentDescription(R.id.fuzzy_clock_image, fuzzyClock.getContentDescription());
                views.setOnClickPendingIntent(R.id.fuzzy_clock_image, pi);
                mWidgetManager.updateAppWidget(id, views);
            }
            scheduleUpdate();
        } else {
            Log.i(TAG, "No widgets left to update...");
            cancelUpdate();
            stopSelf();
        }
    }

    private void scheduleUpdate() {
        cancelUpdate();
        long now = mFuzzyLogic.getCalendar().getTimeInMillis();
        long nextMilli = mFuzzyLogic.getNextIntervalMilli();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAlarmManager.setExact(AlarmManager.RTC, now+nextMilli, mRestartIntent);
        } else {
            mAlarmManager.set(AlarmManager.RTC, now+nextMilli, mRestartIntent);
        }
        if (LOGV) Log.i(TAG, "Scheduled clock update for " + (nextMilli/1000) + "s from now");
    }

    private void cancelUpdate() {
        mAlarmManager.cancel(mRestartIntent);
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
        mFuzzyLogic.setDateFormat(android.text.format.DateFormat.is24HourFormat(mContext));
    }

    public void setTimeZone(String id) {
        mTimeZoneId = id;
        updateTime();
    }

}
