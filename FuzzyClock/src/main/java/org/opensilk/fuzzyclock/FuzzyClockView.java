/*
 *  Copyright (C) 2013,2014 OpenSilk Productions LLC
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
import android.graphics.Typeface;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.TimeZone;

import hugo.weaving.DebugLog;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class FuzzyClockView extends ViewGroup {

    private FuzzyLogic mFuzzyLogic;
    TextView mTimeDisplayHours, mTimeDisplayMinutes, mTimeDisplaySeparator;
    private ContentObserver mFormatChangeObserver;
    private boolean mLive = true;
    private boolean mAttached;
    private String mTimeZoneId;
    private int mClockStyle = FuzzyPrefs.CLOCK_STYLE_DEFAULT;

    private TimeChangedListener mCallback;

    public interface TimeChangedListener {
        public void onTimeChanged();
    }

    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mLive) {
                if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                    mFuzzyLogic.setCalendar(Calendar.getInstance());
                }
                if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (updateLogic()) updateTime();
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        public void run() {
                            updateTime();
                        }
                    });
                }
            }
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
        this(context, attrs, 0);
    }

    public FuzzyClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mFuzzyLogic = FuzzyPrefs.createLogic(FuzzyPrefs.CLOCK_LOGIC_DEFAULT);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTimeDisplayHours = (TextView)findViewById(R.id.timeDisplayHours);
        mTimeDisplayMinutes = (TextView)findViewById(R.id.timeDisplayMinutes);
        mTimeDisplaySeparator = (TextView)findViewById(R.id.timeDisplaySeparator);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int maxHeight = 0;
        int maxWidth = 0;

        // Find out how big everyone wants to be
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        switch (mClockStyle) {
            case FuzzyPrefs.CLOCK_STYLE_STAGGERED:
                // minutes + hours
                maxWidth = getChildWidth(mTimeDisplayMinutes) + getChildWidth(mTimeDisplayHours);
                // minutes + separator + hours
                maxHeight = getChildHeight(mTimeDisplayMinutes) + getChildHeight(mTimeDisplaySeparator) + getChildHeight(mTimeDisplayHours);
                break;
            case FuzzyPrefs.CLOCK_STYLE_VERTICAL:
                // largest of minutes, separator, hours
                maxWidth = Math.max(Math.max(getChildWidth(mTimeDisplayMinutes), getChildWidth(mTimeDisplaySeparator)), getChildWidth(mTimeDisplayHours));
                // minutes + separator + hours
                maxHeight = getChildHeight(mTimeDisplayMinutes) + getChildHeight(mTimeDisplaySeparator) + getChildHeight(mTimeDisplayHours);
                break;
            case FuzzyPrefs.CLOCK_STYLE_HORIZONTAL:
            default:
                // minutes + separator + hours
                maxWidth = getChildWidth(mTimeDisplayMinutes) + getChildWidth(mTimeDisplaySeparator) + getChildWidth(mTimeDisplayHours);
                // largest of minutes, separator, hours
                maxHeight = Math.max(Math.max(getChildHeight(mTimeDisplayMinutes), getChildHeight(mTimeDisplaySeparator)), getChildHeight(mTimeDisplayHours));
                break;
        }

        // Account for padding too
        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();

        // Check against minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int mX, mY, sX, sY, hX, hY;
        mX = sX = hX = getPaddingLeft();
        mY = sY = hY = getPaddingTop();
        switch (mClockStyle) {
            case FuzzyPrefs.CLOCK_STYLE_STAGGERED:
                // left
                mX += 0;
                // top
                mY += 0;

                // end of minutes - half length of self
                sX += getChildWidth(mTimeDisplayMinutes) - getChildWidth(mTimeDisplaySeparator) / 2;
                // minutes + height of minutes
                sY += mY + getChildHeight(mTimeDisplayMinutes);

                // end of minutes
                hX += getChildWidth(mTimeDisplayMinutes);
                // separator + height of separator
                hY += sY + getChildHeight(mTimeDisplaySeparator);
                break;
            case FuzzyPrefs.CLOCK_STYLE_VERTICAL:
                // centered
                mX += getMeasuredWidth() / 2 - getChildWidth(mTimeDisplayMinutes) / 2;
                // top
                mY += 0;

                // centered
                sX += getMeasuredWidth() / 2 - getChildWidth(mTimeDisplaySeparator) / 2;
                // minutes + height of minutes
                sY += mY + getChildHeight(mTimeDisplayMinutes);

                // centered
                hX += getMeasuredWidth() / 2 - getChildWidth(mTimeDisplayHours) / 2;
                // separator + height of separator;
                hY += sY + getChildHeight(mTimeDisplaySeparator);
                break;
            case FuzzyPrefs.CLOCK_STYLE_HORIZONTAL:
            default:
                // left
                mX += 0;
                // top
                mY += 0;

                // end of minutes
                sX += getChildWidth(mTimeDisplayMinutes);
                // top
                sY += 0;

                // end of minutes + separator
                hX += sX + getChildWidth(mTimeDisplaySeparator);
                // top
                hY += 0;
                break;
        }
        mTimeDisplayMinutes.layout(mX, mY, mX + getChildWidth(mTimeDisplayMinutes), mY + getChildHeight(mTimeDisplayMinutes));
        mTimeDisplaySeparator.layout(sX, sY, sX + getChildWidth(mTimeDisplaySeparator), sY + getChildHeight(mTimeDisplaySeparator));
        mTimeDisplayHours.layout(hX, hY, hX + getChildWidth(mTimeDisplayHours), hY + getChildHeight(mTimeDisplayHours));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mAttached) return;
        mAttached = true;

        //if (mLive) {
            /* monitor time ticks, time changed, timezone */
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            getContext().registerReceiver(mIntentReceiver, filter);
        //}

        /* monitor 12/24-hour display preference */
        //mFormatChangeObserver = new FormatChangeObserver();
        //getContext().getContentResolver().registerContentObserver(
        //        Settings.System.CONTENT_URI, true, mFormatChangeObserver);
        updateTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (!mAttached) return;
        mAttached = false;

        //if (mLive) {
            getContext().unregisterReceiver(mIntentReceiver);
        //}
        //getContext().getContentResolver().unregisterContentObserver(
        //        mFormatChangeObserver);
    }

    protected boolean updateLogic() {
        if (mLive) {
            mFuzzyLogic.getCalendar().setTimeInMillis(System.currentTimeMillis());
        }
        if (mTimeZoneId != null) {
            mFuzzyLogic.getCalendar().setTimeZone(TimeZone.getTimeZone(mTimeZoneId));
        }
        mFuzzyLogic.updateTime();
        return mFuzzyLogic.hasChanged();
    }

    public void updateTime(Calendar c) {
        mFuzzyLogic.setCalendar(c);
        updateTime();
    }

    public void updateTime(int hour, int minute) {
        // set the alarm text
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        mFuzzyLogic.setCalendar(c);
        updateTime();
    }

    public void updateTime() {
        updateLogic();

        FuzzyLogic.FuzzyTime time = mFuzzyLogic.getFuzzyTime();
        CharSequence timeM = (time.minute != -1) ? getResources().getString(time.minute) : "";
        CharSequence timeH = (time.hour != -1) ? getResources().getString(time.hour) : "";
        CharSequence separator = (time.separator != -1) ? getResources().getString(time.separator) : "";

        // Write time to the display

        if (time.minute == -1 ) {
            mTimeDisplayMinutes.setVisibility(GONE);
        } else {
            mTimeDisplayMinutes.setText(timeM);
            mTimeDisplayMinutes.setVisibility(VISIBLE);
        }

        if (time.separator == -1) {
            mTimeDisplaySeparator.setVisibility(GONE);
        } else {
            mTimeDisplaySeparator.setText(separator);
            mTimeDisplaySeparator.setVisibility(VISIBLE);
        }

        if (time.hour == -1) {
            mTimeDisplayHours.setVisibility(GONE);
        } else {
            mTimeDisplayHours.setText(timeH);
            mTimeDisplayHours.setVisibility(VISIBLE);
        }

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

    public void setTextColor(int resId) {
        setMinuteColor(resId);
        setSeparatorColor(resId);
        setHourColor(resId);
    }

    public void setMinuteColor(int resId) {
        mTimeDisplayMinutes.setTextColor(getResources().getColor(resId));
    }

    public void setSeparatorColor(int resId) {
        mTimeDisplaySeparator.setTextColor(getResources().getColor(resId));
    }

    public void setHourColor(int resId) {
        mTimeDisplayHours.setTextColor(getResources().getColor(resId));
    }

    public void setTextSize(float size) {
        setMinuteSize(size);
        setSeparatorSize(size);
        setHourSize(size);
    }

    public void setMinuteSize(float size) {
        mTimeDisplayMinutes.setTextSize(COMPLEX_UNIT_SP, size);
        updateTextViewPadding();
    }

    public void setSeparatorSize(float size) {
        mTimeDisplaySeparator.setTextSize(COMPLEX_UNIT_SP, size);
        updateTextViewPadding();
    }

    public void setHourSize(float size) {
        mTimeDisplayHours.setTextSize(COMPLEX_UNIT_SP, size);
        updateTextViewPadding();
    }

    public void setTypeface(int style) {
        setMinuteTypeface(style);
        setSeparatorTypeface(style);
        setHourTypeface(style);
    }

    public void setMinuteTypeface(int style) {
        mTimeDisplayMinutes.setTypeface(FuzzyPrefs.createTypeface(style));
    }

    public void setSeparatorTypeface(int style) {
        mTimeDisplaySeparator.setTypeface(FuzzyPrefs.createTypeface(style));
    }

    public void setHourTypeface(int style) {
        mTimeDisplayHours.setTypeface(FuzzyPrefs.createTypeface(style));
    }

    public void setClockStyle(int style) {
        mClockStyle = style;
        requestLayout();
    }

    public FuzzyLogic getLogic() {
        return mFuzzyLogic;
    }

    public void setLogic(int type) {
        mFuzzyLogic = FuzzyPrefs.createLogic(type);
        setDateFormat();
    }

    public void loadPreferences(FuzzyPrefs prefs) {
        setMinuteColor(prefs.minute.color);
        setMinuteSize(prefs.minute.size);
        setMinuteTypeface(prefs.minute.style);
        setSeparatorColor(prefs.separator.color);
        setSeparatorSize(prefs.separator.size);
        setSeparatorTypeface(prefs.separator.style);
        setHourColor(prefs.hour.color);
        setHourSize(prefs.hour.size);
        setHourTypeface(prefs.hour.style);
        setLogic(prefs.clockLogic);
        setClockStyle(prefs.clockStyle);
    }

    public void setDateFormat() {
        mFuzzyLogic.setDateFormat(android.text.format.DateFormat.is24HourFormat(getContext()));
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

    private int getChildWidth(final View v) {
        if (v != null && v.getVisibility() != GONE) {
            return v.getMeasuredWidth();
        }
        return 0;
    }

    private int getChildHeight(final View v) {
        if (v != null && v.getVisibility() != GONE) {
            return v.getMeasuredHeight();
        }
        return 0;
    }

    private void updateTextViewPadding() {
        float topPaddingRatio = 0.25f;// 0.328f;
        float bottomPaddingRatio = 0.18f;// 0.25f;
        // Set negative padding to scrunch the lines closer together.
        mTimeDisplayMinutes.setPadding(0, (int) (-topPaddingRatio * mTimeDisplayMinutes.getTextSize()), 0,
                (int) (-bottomPaddingRatio * mTimeDisplayMinutes.getTextSize()));
        mTimeDisplaySeparator.setPadding(0, (int) (-topPaddingRatio * mTimeDisplaySeparator.getTextSize()), 0,
                (int) (-bottomPaddingRatio * mTimeDisplaySeparator.getTextSize()));
        mTimeDisplayHours.setPadding(0, (int) (-topPaddingRatio * mTimeDisplayHours.getTextSize()), 0,
                (int) (-bottomPaddingRatio * mTimeDisplayHours.getTextSize()));
    }

}
