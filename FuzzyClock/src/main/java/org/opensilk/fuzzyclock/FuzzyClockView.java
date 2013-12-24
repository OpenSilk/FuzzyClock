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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.TimeZone;

import hugo.weaving.DebugLog;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class FuzzyClockView extends LinearLayout {

    private static final String TAG = FuzzyClockView.class.getSimpleName();

    private FuzzyLogic mFuzzyLogic = new FuzzyLogic();
    private TextView mTimeDisplayHours, mTimeDisplayMinutes, mTimeDisplaySeparator;
    private ContentObserver mFormatChangeObserver;
    private boolean mLive = true;
    private boolean mAttached;
    private String mTimeZoneId;

    private int mMinuteColorRes = android.R.color.white;
    private int mHourColorRes = android.R.color.white;
    private int mSeparatorColorRes = android.R.color.holo_blue_light;
    private float mFontSize;

    private TimeChangedListener mCallback;

    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mLive && intent.getAction().equals(
                    Intent.ACTION_TIMEZONE_CHANGED)) {
                mFuzzyLogic.setCalendar(Calendar.getInstance());
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

    public interface TimeChangedListener {
        public void onTimeChanged();
    }

    public FuzzyClockView(Context context) {
        this(context, null);
    }

    public FuzzyClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTimeDisplayHours = (TextView)findViewById(R.id.timeDisplayHours);
        mTimeDisplayMinutes = (TextView)findViewById(R.id.timeDisplayMinutes);
        mTimeDisplaySeparator = (TextView)findViewById(R.id.timeDisplaySeparator);
        mFuzzyLogic.setCalendar(Calendar.getInstance());
        setDateFormat();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

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

    @DebugLog
    public void updateTime(Calendar c) {
        mFuzzyLogic.setCalendar(c);
        updateTime();
    }

    @DebugLog
    public void updateTime(int hour, int minute) {
        // set the alarm text
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        mFuzzyLogic.setCalendar(c);
        updateTime();
    }

    @DebugLog
    private void updateTime() {
        if (mLive) {
            mFuzzyLogic.getCalendar().setTimeInMillis(System.currentTimeMillis());
        }
        if (mTimeZoneId != null) {
            mFuzzyLogic.getCalendar().setTimeZone(TimeZone.getTimeZone(mTimeZoneId));
        }

        mFuzzyLogic.updateTime();

        if (!mFuzzyLogic.hasChanged()) {
            return;
        }

        FuzzyLogic.FuzzyTime time = mFuzzyLogic.getFuzzyTime();
        CharSequence timeM = (time.minute != -1) ? getResources().getString(time.minute) : "";
        CharSequence timeH = (time.hour != -1) ? getResources().getString(time.hour) : "";
        CharSequence separator = (time.separator != -1) ? getResources().getString(time.separator) : "";

        // Write time to the display
        mTimeDisplayMinutes.setText(timeM);
        mTimeDisplayHours.setText(timeH);
        mTimeDisplaySeparator.setText(separator);

        // Update accessibility string.
        StringBuilder fullTimeStr = new StringBuilder();
        fullTimeStr.append(timeM);
        fullTimeStr.append(separator);
        fullTimeStr.append(timeH);
        setContentDescription(fullTimeStr);

        if (mCallback != null) {
            mCallback.onTimeChanged();
        }
    }

    public void updateColors() {
        mTimeDisplayMinutes.setTextColor(getResources().getColor(mMinuteColorRes));
        mTimeDisplayHours.setTextColor(getResources().getColor(mHourColorRes));
        mTimeDisplaySeparator.setTextColor(getResources().getColor(mSeparatorColorRes));
    }

    public void setMinuteColor(int res) {
        mMinuteColorRes = res;
    }

    public void setHourColor(int res) {
        mHourColorRes = res;
    }

    public void setSeparatorColor(int res) {
        mSeparatorColorRes = res;
    }

    private void updateSize() {
        mTimeDisplayMinutes.setTextSize(COMPLEX_UNIT_SP, mFontSize);
        mTimeDisplayHours.setTextSize(COMPLEX_UNIT_SP, mFontSize);
        mTimeDisplaySeparator.setTextSize(COMPLEX_UNIT_SP, mFontSize);
    }

    public void setFontSize(float size) {
        mFontSize = size;
        updateSize();
    }

    public void loadPreferences(FuzzyPrefs prefs) {
        mMinuteColorRes = prefs.color.minute;
        mHourColorRes = prefs.color.hour;
        mSeparatorColorRes = prefs.color.separator;
        mFontSize = prefs.size;
        updateColors();
        updateSize();
    }

    private void setDateFormat() {
        mFuzzyLogic.setDateFormat(false); //TODO fix support for 24hour
        //mFuzzyLogic.setDateFormat(android.text.format.DateFormat.is24HourFormat(getContext()));
    }

    public void setLive(boolean live) {
        mLive = live;
    }

    public void setTimeZone(String id) {
        mTimeZoneId = id;
        updateTime();
    }

    public void registerCallback(TimeChangedListener l) {
        mCallback = l;
    }

}
