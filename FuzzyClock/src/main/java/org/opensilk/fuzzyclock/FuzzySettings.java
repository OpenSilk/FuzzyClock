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

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import hugo.weaving.DebugLog;

import static org.opensilk.fuzzyclock.FuzzyWidget.ACTION_UPDATE_WIDGET;

public class FuzzySettings extends Activity implements View.OnClickListener {

    private SharedPreferences mPrefs;

    private FuzzyClockView mFuzzyClock;
    private View mMinutes;
    private View mHours;
    private View mSeparator;
    private Button mButtonDone;

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent mResult = new Intent();

    private CharSequence[] mColorEntries;
    private static final int[] mColorResources = {
            android.R.color.holo_blue_light,
            android.R.color.holo_red_light,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_purple,
            android.R.color.white,
    };

    private boolean isWidget = false;

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fuzzy_settings);

        mColorEntries = new CharSequence[] {
                getString(R.string.holo_blue),
                getString(R.string.holo_red),
                getString(R.string.holo_green),
                getString(R.string.holo_orange),
                getString(R.string.holo_purple),
                getString(R.string.white)
        };

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                isWidget = true;
                mResult = new Intent();
                mResult.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_CANCELED, mResult);
            }
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mFuzzyClock = (FuzzyClockView) findViewById(R.id.time);
        mFuzzyClock.setLive(false);
        mFuzzyClock.updateTime(4,30);

        mMinutes = findViewById(R.id.timeDisplayMinutes);
        mMinutes.setOnClickListener(this);

        mHours = findViewById(R.id.timeDisplayHours);
        mHours.setOnClickListener(this);

        mSeparator = findViewById(R.id.timeDisplaySeparator);
        mSeparator.setOnClickListener(this);

        mButtonDone = (Button) findViewById(R.id.button_done);
        mButtonDone.setOnClickListener(this);

        if (isWidget) {
            setTitle(R.string.settings_title_widget);
            mFuzzyClock.setMinuteColor(mPrefs.getInt(FuzzyWidget.PREF_COLOR_MINUTE, android.R.color.white));
            mFuzzyClock.setHourColor(mPrefs.getInt(FuzzyWidget.PREF_COLOR_HOUR, android.R.color.white));
            mFuzzyClock.setSeparatorColor(mPrefs.getInt(FuzzyWidget.PREF_COLOR_SEPARATOR, android.R.color.holo_blue_light));
            mFuzzyClock.updateColors();
        }

    }

    @DebugLog
    @Override
    public void onClick(View v) {
        if (v == mMinutes) {
            chooseColor(isWidget ? FuzzyWidget.PREF_COLOR_MINUTE : FuzzyDreams.PREF_COLOR_MINUTE, 5);
        } else if (v == mHours) {
            chooseColor(isWidget ? FuzzyWidget.PREF_COLOR_HOUR : FuzzyDreams.PREF_COLOR_HOUR, 5);
        } else if (v == mSeparator) {
            chooseColor(isWidget ? FuzzyWidget.PREF_COLOR_SEPARATOR : FuzzyDreams.PREF_COLOR_SEPARATOR, 0);
        } else if (v == mButtonDone) {
            if (isWidget) {
                // Force refresh;
                sendBroadcast(new Intent(ACTION_UPDATE_WIDGET));
                setResult(RESULT_OK, mResult);
            }
            finish();
        }
    }

    @DebugLog
    private int getColorEntry(int color) {
        for (int ii=0;ii< mColorResources.length;ii++) {
            if (mColorResources[ii] == color) {
                return ii;
            }
        }
        return mColorResources[0];
    }

    @DebugLog
    private void chooseColor(final String pref, int def) {
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(mColorEntries,
                        getColorEntry(mPrefs.getInt(pref, def)),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPrefs.edit().putInt(pref, mColorResources[which]).commit();
                                updateClockView(pref, mColorResources[which]);
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    @DebugLog
    private void updateClockView(String pref, int color) {
        if (pref.equals(FuzzyDreams.PREF_COLOR_MINUTE) ||
                pref.equals(FuzzyWidget.PREF_COLOR_MINUTE)) {
            mFuzzyClock.setMinuteColor(color);
        } else if (pref.equals(FuzzyDreams.PREF_COLOR_HOUR) ||
                pref.equals(FuzzyWidget.PREF_COLOR_HOUR)) {
            mFuzzyClock.setHourColor(color);
        } else if (pref.equals(FuzzyDreams.PREF_COLOR_SEPARATOR) ||
                pref.equals(FuzzyWidget.PREF_COLOR_SEPARATOR)) {
            mFuzzyClock.setSeparatorColor(color);
        }
        mFuzzyClock.updateColors();
    }

}
