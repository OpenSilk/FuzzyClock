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
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Locale;

import hugo.weaving.DebugLog;

public class FuzzyWidgetService extends Service {

    private static final String TAG = FuzzyWidgetService.class.getSimpleName();
    private static final boolean LOGV = BuildConfig.DEBUG;

    private Context mContext;
    private FormatChangeObserver mFormatChangeObserver;
    private AlarmManager mAlarmManager;
    private FuzzyClockView mFuzzyClock;
    private AppWidgetManager mWidgetManager;
    private HashMap<Integer, FuzzyPrefs> mWidgetSettings;
    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @DebugLog
        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.post(mUpdateWidgetsRunnable);
        }
    };

    private final Runnable mUpdateSettingsRunnable = new Runnable() {
        @Override
        public void run() {
            updateSettings();
        }
    };

    private final Runnable mUpdateWidgetsRunnable = new Runnable() {
        @Override
        public void run() {
            updateWidgets();
        }
    };

    private class UpdateWidgetRunnable implements Runnable {
        private final int widgetId;
        public UpdateWidgetRunnable(int widgetId) {
            this.widgetId = widgetId;
        }
        @Override
        public void run() {
            updateWidget(widgetId);
        }
    }

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
            mHandler.post(mUpdateWidgetsRunnable);
        }
    }

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        /* monitor time ticks, time changed, timezone */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mContext.registerReceiver(mIntentReceiver, filter);

        //mFormatChangeObserver = new FormatChangeObserver();
        //mContext.getContentResolver().registerContentObserver(
        //        Settings.System.CONTENT_URI, true, mFormatChangeObserver
        //);

        mWidgetSettings = new HashMap<Integer, FuzzyPrefs>();
        mWidgetManager = AppWidgetManager.getInstance(mContext);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mFuzzyClock = (FuzzyClockView) layoutInflater.inflate(R.layout.fuzzy_clock, null);
    }

    @DebugLog
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() == null) {
            mHandler.post(mUpdateSettingsRunnable);
            mHandler.post(mUpdateWidgetsRunnable);
        } else if (intent.getAction().startsWith("scheduled_update")) {
            final int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            if (!mWidgetSettings.containsKey((Integer) id)) {
                mHandler.post(mUpdateSettingsRunnable);
            }
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                mHandler.post(new UpdateWidgetRunnable(id));
            }
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DELETED)) {
            int[] ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (ids != null) {
                for (int id: ids) {
                    cancelUpdate(id);
                    new FuzzyPrefs(mContext, id).remove();
                    if (mWidgetSettings.containsKey((Integer) id)) {
                        mWidgetSettings.remove((Integer) id);
                    }
                }
            }
            mHandler.post(mUpdateWidgetsRunnable);
        }
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
        //mContext.getContentResolver().unregisterContentObserver(mFormatChangeObserver);
        super.onDestroy();
    }

    @DebugLog
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mHandler.post(mUpdateSettingsRunnable);
        mHandler.post(mUpdateWidgetsRunnable);
    }

    @DebugLog
    private void updateWidgets() {
        int[] widgetIds = mWidgetManager.getAppWidgetIds(new ComponentName(mContext, FuzzyWidget.class));
        if (widgetIds != null && widgetIds.length > 0) {
            for (int id: widgetIds) {
                 updateWidget(id);
            }
        } else {
            Log.i(TAG, "No widgets left to update...");
            stopSelf();
        }
    }

    @DebugLog
    private void updateWidget(int id) {
        FuzzyPrefs settings = mWidgetSettings.get((Integer) id);
        if (settings == null) {
            return; // Once setup is done we will be called again.
        }
        if (LOGV) Log.v(TAG, "Updating widget id=" + id + " " + settings.toString());
        mFuzzyClock.loadPreferences(settings);
        mFuzzyClock.setDateFormat();
        mFuzzyClock.updateTime();
        // Where the magic happens... w & h are 0 without this
        mFuzzyClock.measure(0, 0);
        mFuzzyClock.layout(0, 0, mFuzzyClock.getMeasuredWidth(), mFuzzyClock.getMeasuredHeight());
        if (mFuzzyClock.getMeasuredWidth() == 0 || mFuzzyClock.getMeasuredHeight() == 0) {
            return;
        }
        // Draw view into a bitmap
        Bitmap bitmap = Bitmap.createBitmap(mFuzzyClock.getMeasuredWidth(), mFuzzyClock.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        mFuzzyClock.draw(c);
        // build onClick intent
        Intent intent = new Intent(mContext, FuzzyWidgetSettings.class);
        intent.setAction(String.format(Locale.US, "dummy_%d", id));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // send bitmap to remote view
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.fuzzy_widget);
        views.setImageViewBitmap(R.id.fuzzy_clock_image, bitmap);
        views.setContentDescription(R.id.fuzzy_clock_image, mFuzzyClock.getContentDescription());
        views.setOnClickPendingIntent(R.id.fuzzy_clock_image, pi);
        mWidgetManager.updateAppWidget(id, views);
        scheduleUpdate(mFuzzyClock.getLogic().getCalendar().getTimeInMillis(), mFuzzyClock.getLogic().getNextIntervalMilli(), id);
    }

    @DebugLog
    private void scheduleUpdate(long now, long nextMilli, int id) {
        PendingIntent pendingIntent = createPendingIntent(id);
        cancelUpdate(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAlarmManager.setExact(AlarmManager.RTC, now+nextMilli, pendingIntent);
        } else {
            mAlarmManager.set(AlarmManager.RTC, now+nextMilli, pendingIntent);
        }
        if (LOGV) Log.i(TAG, "Scheduled update for " +
                (nextMilli/1000) + "s from now for widget " + id);
    }

    private void cancelUpdate(int id) {
        cancelUpdate(createPendingIntent(id));
    }

    private void cancelUpdate(PendingIntent pendingIntent) {
        mAlarmManager.cancel(pendingIntent);
    }

    private PendingIntent createPendingIntent(int id) {
        Intent i = new Intent(String.format(Locale.US, "scheduled_update_%d", id),
                null, mContext, FuzzyWidgetService.class);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        return PendingIntent.getService(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
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

}
