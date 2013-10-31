package org.opensilk.fuzzyclock;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class FuzzyWidgetService extends Service {

    private static final String TAG = FuzzyWidgetService.class.getSimpleName();
    private static final boolean LOGV = true;

    private Context mContext;
    private Calendar mCalendar;
    private ContentObserver mFormatChangeObserver;

    private boolean m24HourFormat;
    private String mTimeZoneId;
    List<Integer> mWidgetIds = new ArrayList<Integer>();

    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    Intent.ACTION_TIMEZONE_CHANGED)) {
                mCalendar = Calendar.getInstance();
            }
            if (LOGV) Log.v(TAG, "Received: " + intent.getAction());
            // Post a runnable to avoid blocking the broadcast.
            mHandler.post(new Runnable() {
                public void run() {
                    updateTime();
                }
            });
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

    @Override
    public void onCreate() {
        if (LOGV) Log.v(TAG, "onCreate");
        super.onCreate();
        mContext = this;
        mCalendar = Calendar.getInstance();
        /* monitor time ticks, time changed, timezone */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mContext.registerReceiver(mIntentReceiver, filter);

        /* monitor 12/24-hour display preference */
        mFormatChangeObserver = new FormatChangeObserver();
        mContext.getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);
        setDateFormat();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (LOGV) Log.v(TAG, "onStartCommand: action=" + action);

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            int[] ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            for (int id : ids) {
                if (!mWidgetIds.contains(id)) {
                    if (LOGV) Log.v(TAG, "Adding widget id=" + id);
                    mWidgetIds.add(id);
                }
            }
        } else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            int[] ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            for (int id : ids) {
                if (mWidgetIds.contains((Integer)id)) {
                    if (LOGV) Log.v(TAG, "Removing widget id=" + id);
                    mWidgetIds.remove((Integer)id);
                }
            }
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateTime();
            }
        });

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (LOGV) Log.v(TAG, "onDestroy");
        super.onDestroy();
        mContext.unregisterReceiver(mIntentReceiver);
        mContext.getContentResolver().unregisterContentObserver(
                mFormatChangeObserver);
    }

    private void updateTime() {
        if (mWidgetIds.isEmpty()) {
            stopSelf();
        }

        mCalendar.setTimeInMillis(System.currentTimeMillis());

        if (mTimeZoneId != null) {
            mCalendar.setTimeZone(TimeZone.getTimeZone(mTimeZoneId));
        }

        int minutes, hours;
        CharSequence timeH, timeM, separator;

        minutes = mCalendar.get(Calendar.MINUTE);
        hours = mCalendar.get(m24HourFormat ? Calendar.HOUR_OF_DAY : Calendar.HOUR);

        if        (minutes >= 56) { timeM = ""; // O'CLOCK
        } else if (minutes >= 51) { timeM = mContext.getString(R.string.fuzzy_five);
        } else if (minutes >= 46) { timeM = mContext.getString(R.string.fuzzy_ten);
        } else if (minutes >= 41) { timeM = mContext.getString(R.string.fuzzy_quarter);
        } else if (minutes >= 36) { timeM = mContext.getString(R.string.fuzzy_twenty);
        } else if (minutes >= 25) { timeM = mContext.getString(R.string.fuzzy_half);
        } else if (minutes >= 20) { timeM = mContext.getString(R.string.fuzzy_twenty);
        } else if (minutes >= 15) { timeM = mContext.getString(R.string.fuzzy_quarter);
        } else if (minutes >= 10) { timeM = mContext.getString(R.string.fuzzy_ten);
        } else if (minutes >= 5)  { timeM = mContext.getString(R.string.fuzzy_five);
        } else                    { timeM = ""; // O'CLOCK
        }

        // Adjust for next hour
        if (minutes >= 36) {
            if (m24HourFormat) {
                hours = (hours + 1) % 24;
            } else {
                hours = (hours + 1) % 12;
            }
        }

        switch (hours) {
            case 0:  timeH = mContext.getString(R.string.fuzzy_twelve); break;
            case 1:  timeH = mContext.getString(R.string.fuzzy_one); break;
            case 2:  timeH = mContext.getString(R.string.fuzzy_two); break;
            case 3:  timeH = mContext.getString(R.string.fuzzy_three); break;
            case 4:  timeH = mContext.getString(R.string.fuzzy_four); break;
            case 5:  timeH = mContext.getString(R.string.fuzzy_five); break;
            case 6:  timeH = mContext.getString(R.string.fuzzy_six); break;
            case 7:  timeH = mContext.getString(R.string.fuzzy_seven); break;
            case 8:  timeH = mContext.getString(R.string.fuzzy_eight); break;
            case 9:  timeH = mContext.getString(R.string.fuzzy_nine); break;
            case 10: timeH = mContext.getString(R.string.fuzzy_ten); break;
            case 11: timeH = mContext.getString(R.string.fuzzy_eleven); break;
            case 12: timeH = mContext.getString(R.string.fuzzy_twelve); break;
            case 13: timeH = mContext.getString(R.string.fuzzy_thirteen); break;
            case 14: timeH = mContext.getString(R.string.fuzzy_fourteen); break;
            case 15: timeH = mContext.getString(R.string.fuzzy_fifteen); break;
            case 16: timeH = mContext.getString(R.string.fuzzy_sixteen); break;
            case 17: timeH = mContext.getString(R.string.fuzzy_seventeen); break;
            case 18: timeH = mContext.getString(R.string.fuzzy_eighteen); break;
            case 19: timeH = mContext.getString(R.string.fuzzy_nineteen); break;
            case 20: timeH = mContext.getString(R.string.fuzzy_twenty); break;
            case 21: timeH = mContext.getString(R.string.fuzzy_twenty) + mContext.getString(R.string.fuzzy_one); break;
            case 22: timeH = mContext.getString(R.string.fuzzy_twenty) + mContext.getString(R.string.fuzzy_two); break;
            case 23: timeH = mContext.getString(R.string.fuzzy_twenty) + mContext.getString(R.string.fuzzy_three); break;
            case 24: timeH = mContext.getString(R.string.fuzzy_twenty) + mContext.getString(R.string.fuzzy_four); break;
            default: timeH = ""; break;
        }

        // Handle Noon and Midnight
        if (m24HourFormat) {
            if (hours == 12) {
                timeH = mContext.getString(R.string.fuzzy_noon);
            } else if (hours == 0) {
                timeH = mContext.getString(R.string.fuzzy_midnight);
            }
        } else {
            if (hours == 0) {
                if (minutes >= 36) {
                    timeH = (mCalendar.get(Calendar.AM_PM) == 1) ?
                            mContext.getString(R.string.fuzzy_midnight) :
                            mContext.getString(R.string.fuzzy_noon);
                } else {
                    timeH = (mCalendar.get(Calendar.AM_PM) == 0) ?
                            mContext.getString(R.string.fuzzy_midnight) :
                            mContext.getString(R.string.fuzzy_noon);
                }
            }
        }

        // Final shuffle before writing to display
        if (minutes >= 56) {
            separator = (hours > 12) ?
                    mContext.getString(R.string.fuzzy_hundred) :
                    mContext.getString(R.string.fuzzy_oclock);
            if (hours == 0 || (m24HourFormat && (hours == 12))) {
//                separator = (mCalendar.get(Calendar.AM_PM) == 1) ?
//                        context.getString(R.string.fuzzy_midnight) :
//                        context.getString(R.string.fuzzy_noon);
                separator = timeH;
                timeM = "";
            } else {
                timeM = timeH;
            }
            timeH = "";
        } else if (minutes >= 36) {
            separator = mContext.getString(R.string.fuzzy_to);
        } else if (minutes >= 5) {
            separator = mContext.getString(R.string.fuzzy_past);
        } else {
            separator = (hours > 12) ?
                    mContext.getString(R.string.fuzzy_hundred) :
                    mContext.getString(R.string.fuzzy_oclock);
            if (hours == 0 || (m24HourFormat && (hours == 12))) {
//                separator = (mCalendar.get(Calendar.AM_PM) == 0) ?
//                        context.getString(R.string.fuzzy_midnight) :
//                        context.getString(R.string.fuzzy_noon);
                separator = timeH;
                timeM = "";
            } else {
                timeM = timeH;
            }
            timeH = "";
        }

        // Write time to the display
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.fuzzy_widget);

        views.setTextViewText(R.id.timeDisplayMinutes, timeM);
        views.setTextViewText(R.id.timeDisplaySeparator, separator);
        views.setTextViewText(R.id.timeDisplayHours, timeH);

        AppWidgetManager appManager = AppWidgetManager.getInstance(mContext);

        int[] ids = new int[mWidgetIds.size()];
        for (int ii = 0; ii< ids.length; ii++) {
            ids[ii] = mWidgetIds.get(ii);
            if (LOGV) Log.v(TAG, "Updating widget id=" + ids[ii]);
        }

        appManager.updateAppWidget(ids, views);
    }

    private void setDateFormat() {
        m24HourFormat = android.text.format.DateFormat.is24HourFormat(mContext);
    }

    public void setTimeZone(String id) {
        mTimeZoneId = id;
        updateTime();
    }
}
