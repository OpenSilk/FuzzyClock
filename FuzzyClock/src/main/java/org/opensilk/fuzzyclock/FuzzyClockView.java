package org.opensilk.fuzzyclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.TimeZone;

public class FuzzyClockView extends LinearLayout {

    private static final String TAG = FuzzyClockView.class.getSimpleName();

    private Calendar mCalendar;
    private boolean m24HourFormat;
    private TextView mTimeDisplayHours, mTimeDisplayMinutes, mTimeDisplaySeparator;
    private ContentObserver mFormatChangeObserver;
    private boolean mLive = true;
    private boolean mAttached;
    private final Typeface mRoboto;
    private String mTimeZoneId;


    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mLive && intent.getAction().equals(
                    Intent.ACTION_TIMEZONE_CHANGED)) {
                mCalendar = Calendar.getInstance();
            }
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

    public FuzzyClockView(Context context) {
        this(context, null);
    }

    public FuzzyClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoboto = Typeface.createFromAsset(context.getAssets(),"fonts/Roboto-Regular.ttf");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTimeDisplayHours = (TextView)findViewById(R.id.timeDisplayHours);
        mTimeDisplayHours.setTypeface(mRoboto);
        mTimeDisplayMinutes = (TextView)findViewById(R.id.timeDisplayMinutes);
        mTimeDisplayMinutes.setTypeface(mRoboto);
        mTimeDisplaySeparator = (TextView)findViewById(R.id.timeDisplaySeparator);
        mTimeDisplaySeparator.setTypeface(mRoboto);
        mCalendar = Calendar.getInstance();

        setDateFormat();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        Log.v(TAG, "onAttachedToWindow");

        if (mAttached) return;
        mAttached = true;

        if (mLive) {
            /* monitor time ticks, time changed, timezone */
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            getContext().registerReceiver(mIntentReceiver, filter);
        }

        /* monitor 12/24-hour display preference */
        mFormatChangeObserver = new FormatChangeObserver();
        getContext().getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        updateTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (!mAttached) return;
        mAttached = false;

        if (mLive) {
            getContext().unregisterReceiver(mIntentReceiver);
        }
        getContext().getContentResolver().unregisterContentObserver(
                mFormatChangeObserver);
    }


    void updateTime(Calendar c) {
        mCalendar = c;
        updateTime();
    }

    public void updateTime(int hour, int minute) {
        // set the alarm text
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        mCalendar = c;
        updateTime();
    }

    int mPrevMinState;
    int mPrevHours;

    private void updateTime() {
        if (mLive) {
            mCalendar.setTimeInMillis(System.currentTimeMillis());
        }
        if (mTimeZoneId != null) {
            mCalendar.setTimeZone(TimeZone.getTimeZone(mTimeZoneId));
        }

        int minState, minutes, hours;
        minutes = mCalendar.get(Calendar.MINUTE);
        hours = mCalendar.get(m24HourFormat ? Calendar.HOUR_OF_DAY : Calendar.HOUR);

        if        (minutes >= 56) { minState = 1;
        } else if (minutes >= 51) { minState = 2;
        } else if (minutes >= 46) { minState = 3;
        } else if (minutes >= 41) { minState = 4;
        } else if (minutes >= 36) { minState = 5;
        } else if (minutes >= 25) { minState = 6;
        } else if (minutes >= 20) { minState = 7;
        } else if (minutes >= 15) { minState = 8;
        } else if (minutes >= 10) { minState = 9;
        } else if (minutes >= 5)  { minState = 10;
        } else                    { minState = 1;
        }

        if (mPrevMinState == minState && mPrevHours == hours) {
            return; // No change;
        }

        mPrevMinState = minState;
        mPrevHours = hours;

        Context context = getContext();
        CharSequence timeH, timeM, separator;
        StringBuilder fullTimeStr = new StringBuilder();

        if        (minutes >= 56) { timeM = ""; // O'CLOCK
        } else if (minutes >= 51) { timeM = context.getString(R.string.fuzzy_five);
        } else if (minutes >= 46) { timeM = context.getString(R.string.fuzzy_ten);
        } else if (minutes >= 41) { timeM = context.getString(R.string.fuzzy_quarter);
        } else if (minutes >= 36) { timeM = context.getString(R.string.fuzzy_twenty);
        } else if (minutes >= 25) { timeM = context.getString(R.string.fuzzy_half);
        } else if (minutes >= 20) { timeM = context.getString(R.string.fuzzy_twenty);
        } else if (minutes >= 15) { timeM = context.getString(R.string.fuzzy_quarter);
        } else if (minutes >= 10) { timeM = context.getString(R.string.fuzzy_ten);
        } else if (minutes >= 5)  { timeM = context.getString(R.string.fuzzy_five);
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
            case 0:  timeH = context.getString(R.string.fuzzy_twelve); break;
            case 1:  timeH = context.getString(R.string.fuzzy_one); break;
            case 2:  timeH = context.getString(R.string.fuzzy_two); break;
            case 3:  timeH = context.getString(R.string.fuzzy_three); break;
            case 4:  timeH = context.getString(R.string.fuzzy_four); break;
            case 5:  timeH = context.getString(R.string.fuzzy_five); break;
            case 6:  timeH = context.getString(R.string.fuzzy_six); break;
            case 7:  timeH = context.getString(R.string.fuzzy_seven); break;
            case 8:  timeH = context.getString(R.string.fuzzy_eight); break;
            case 9:  timeH = context.getString(R.string.fuzzy_nine); break;
            case 10: timeH = context.getString(R.string.fuzzy_ten); break;
            case 11: timeH = context.getString(R.string.fuzzy_eleven); break;
            case 12: timeH = context.getString(R.string.fuzzy_twelve); break;
            case 13: timeH = context.getString(R.string.fuzzy_thirteen); break;
            case 14: timeH = context.getString(R.string.fuzzy_fourteen); break;
            case 15: timeH = context.getString(R.string.fuzzy_fifteen); break;
            case 16: timeH = context.getString(R.string.fuzzy_sixteen); break;
            case 17: timeH = context.getString(R.string.fuzzy_seventeen); break;
            case 18: timeH = context.getString(R.string.fuzzy_eighteen); break;
            case 19: timeH = context.getString(R.string.fuzzy_nineteen); break;
            case 20: timeH = context.getString(R.string.fuzzy_twenty); break;
            case 21: timeH = context.getString(R.string.fuzzy_twenty) + context.getString(R.string.fuzzy_one); break;
            case 22: timeH = context.getString(R.string.fuzzy_twenty) + context.getString(R.string.fuzzy_two); break;
            case 23: timeH = context.getString(R.string.fuzzy_twenty) + context.getString(R.string.fuzzy_three); break;
            case 24: timeH = context.getString(R.string.fuzzy_twenty) + context.getString(R.string.fuzzy_four); break;
            default: timeH = ""; break;
        }

        // Handle Noon and Midnight
        if (m24HourFormat) {
            if (hours == 12) {
                timeH = context.getString(R.string.fuzzy_noon);
            } else if (hours == 0) {
                timeH = context.getString(R.string.fuzzy_midnight);
            }
        } else {
            if (hours == 0) {
                if (minutes >= 36) {
                    timeH = (mCalendar.get(Calendar.AM_PM) == 1) ?
                            context.getString(R.string.fuzzy_midnight) :
                            context.getString(R.string.fuzzy_noon);
                } else {
                    timeH = (mCalendar.get(Calendar.AM_PM) == 0) ?
                            context.getString(R.string.fuzzy_midnight) :
                            context.getString(R.string.fuzzy_noon);
                }
            }
        }

        // Final shuffle before writing to display
        if (minutes >= 56) {
            separator = (hours > 12) ?
                    context.getString(R.string.fuzzy_hundred) :
                    context.getString(R.string.fuzzy_oclock);
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
            separator = context.getString(R.string.fuzzy_to);
        } else if (minutes >= 5) {
            separator = context.getString(R.string.fuzzy_past);
        } else {
            separator = (hours > 12) ?
                    context.getString(R.string.fuzzy_hundred) :
                    context.getString(R.string.fuzzy_oclock);
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
        mTimeDisplayMinutes.setText(timeM);
        mTimeDisplaySeparator.setText(separator);
        mTimeDisplayHours.setText(timeH);

        // Update accessibility string.
        fullTimeStr.append(timeM);
        fullTimeStr.append(separator);
        fullTimeStr.append(timeH);
        setContentDescription(fullTimeStr);
    }

    private void setDateFormat() {
        m24HourFormat = android.text.format.DateFormat.is24HourFormat(getContext());
    }

    void setLive(boolean live) {
        mLive = live;
    }

    public void setTimeZone(String id) {
        mTimeZoneId = id;
        updateTime();
    }
}
