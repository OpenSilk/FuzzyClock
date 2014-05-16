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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RemoteViews;

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
    private static final ArrayMap<Integer, FuzzyPrefs> sWidgetSettings = new ArrayMap<>(4);
    private final Handler mHandler = new UpdateHandler();

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
            mHandler.sendMessage(mHandler.obtainMessage(UPDATE_ALL_WIDGETS, -1));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        //mFormatChangeObserver = new FormatChangeObserver();
        //mContext.getContentResolver().registerContentObserver(
        //        Settings.System.CONTENT_URI, true, mFormatChangeObserver
        //);

        mWidgetManager = AppWidgetManager.getInstance(mContext);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mFuzzyClock = (FuzzyClockView) layoutInflater.inflate(R.layout.fuzzy_clock, null);
    }

    @DebugLog
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // No action means we want to reinit everything
        if (intent == null || intent.getAction() == null) {
            mHandler.sendEmptyMessage(UPDATE_SETTINGS);
            mHandler.sendMessage(mHandler.obtainMessage(UPDATE_ALL_WIDGETS, startId));
        // Update the specified widget
        } else if (intent.getAction().startsWith("scheduled_update")) {
            final int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            if (!sWidgetSettings.containsKey((Integer) id)) {
                mHandler.sendEmptyMessage(UPDATE_SETTINGS);
            }
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                mHandler.sendMessage(mHandler.obtainMessage(UPDATE_SINGLE_WIDGET, id, startId));
            }
        // A widget was deleted, remove it from our settings map
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DELETED)) {
            int[] ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (ids != null) {
                for (int id: ids) {
                    cancelUpdate(id);
                    new FuzzyPrefs(mContext, id).remove();
                    if (sWidgetSettings.containsKey((Integer) id)) {
                        sWidgetSettings.remove((Integer) id);
                    }
                }
            }
            mHandler.sendMessage(mHandler.obtainMessage(UPDATE_ALL_WIDGETS, startId));
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
        //mContext.getContentResolver().unregisterContentObserver(mFormatChangeObserver);
        super.onDestroy();
    }

    /**
     * Updates all appwidgets
     */
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

    /**
     * Updates single appwidget, will schedule next update based on current logic
     * @param id
     */
    @DebugLog
    private void updateWidget(int id) {
        FuzzyPrefs settings = sWidgetSettings.get((Integer) id);
        if (settings == null) {
            return; // Once setup is done we will be called again.
        }
        if (LOGV) Log.v(TAG, "Updating widget id=" + id + " " + settings.toString());
        mFuzzyClock.loadPreferences(settings);
        mFuzzyClock.setDateFormat();
        mFuzzyClock.updateTime();
        Bitmap bitmap = mFuzzyClock.createBitmap();
        if (bitmap == null) {
            return;
        }
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

    /**
     * Schedules next update with alarmManager
     * @param now
     * @param nextMilli
     * @param id
     */
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

    /**
     * Cancels pending updates for appwidget id
     * @param id
     */
    private void cancelUpdate(int id) {
        cancelUpdate(createPendingIntent(id));
    }

    /**
     * Cancels pending updates for pending intent
     * @param pendingIntent
     */
    private void cancelUpdate(PendingIntent pendingIntent) {
        mAlarmManager.cancel(pendingIntent);
    }

    /**
     * Creates unique pending intent for given appwidget id
     * @param id
     * @return
     */
    private PendingIntent createPendingIntent(int id) {
        Intent i = new Intent(String.format(Locale.US, "scheduled_update_%d", id),
                null, mContext, FuzzyWidgetService.class);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        return PendingIntent.getService(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Reinitializes appwidget settings
     */
    @DebugLog
    private void updateSettings() {
        sWidgetSettings.clear();
        int[] widgetIds = mWidgetManager.getAppWidgetIds(new ComponentName(mContext, FuzzyWidget.class));
        if (widgetIds != null && widgetIds.length > 0) {
            for (int id: widgetIds) {
                if (LOGV) Log.v(TAG, "Updating widget settings id=" + id);
                sWidgetSettings.put((Integer) id, new FuzzyPrefs(mContext, id));
            }
        }
    }

    static final int UPDATE_SETTINGS = 0;
    static final int UPDATE_ALL_WIDGETS = 1;
    static final int UPDATE_SINGLE_WIDGET = 2;

    class UpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_SETTINGS:
                    updateSettings();
                    break;
                case UPDATE_ALL_WIDGETS:
                    updateWidgets();
                    if (msg.arg1 != -1) {
                        stopSelf(msg.arg1);
                    }
                    break;
                case UPDATE_SINGLE_WIDGET:
                    updateWidget(msg.arg1);
                    if (msg.arg2 != -1) {
                        stopSelf(msg.arg2);
                    }
                    break;
            }
        }
    }

}
