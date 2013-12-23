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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import hugo.weaving.DebugLog;

import static android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS;

abstract class FuzzySettings extends Activity implements
        View.OnClickListener,
        NumberPicker.OnValueChangeListener {

    protected FuzzyPrefs mFuzzyPrefs;

    protected FuzzyClockView mFuzzyClock;
    protected View mMinutes, mHours, mSeparator;
    protected Button mButtonDone, mButtonReset;

    private FrameLayout mRoot;
    private View mTipsView;
    private Button mButtonDismiss;
    private NumberPicker mPicker;

    private CharSequence[] mColorEntries;
    private static final int[] mColorResources = {
            android.R.color.holo_blue_light,
            android.R.color.holo_red_light,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_purple,
            android.R.color.white,
    };

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fuzzy_settings_base);
        mRoot = (FrameLayout) findViewById(R.id.base);
        mRoot.addView(getLayoutInflater().inflate(R.layout.fuzzy_settings, mRoot, false));

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean firstRun = prefs.getBoolean("first_run", true);
        if (firstRun) {
            mTipsView = getLayoutInflater().inflate(R.layout.fuzzy_settings_tips, mRoot, false);
            mRoot.addView(mTipsView);
            //prefs.edit().putBoolean("first_run", false).commit();
        }

        mColorEntries = new CharSequence[] {
                getString(R.string.holo_blue),
                getString(R.string.holo_red),
                getString(R.string.holo_green),
                getString(R.string.holo_orange),
                getString(R.string.holo_purple),
                getString(R.string.white)
        };

        mFuzzyClock = (FuzzyClockView) findViewById(R.id.time);
        mFuzzyClock.setLive(false);
        mFuzzyClock.updateTime(11,20);

        mMinutes = findViewById(R.id.timeDisplayMinutes);
        mMinutes.setOnClickListener(this);

        mHours = findViewById(R.id.timeDisplayHours);
        mHours.setOnClickListener(this);

        mSeparator = findViewById(R.id.timeDisplaySeparator);
        mSeparator.setOnClickListener(this);

        mPicker = (NumberPicker) findViewById(R.id.numberPicker);
        mPicker.setOnValueChangedListener(this);
        mPicker.setMinValue(getResources().getInteger(R.integer.fuzzy_font_size_min));
        mPicker.setMaxValue(getResources().getInteger(R.integer.fuzzy_font_size_max));
        mPicker.setWrapSelectorWheel(false);
        mPicker.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);

        mButtonDone = (Button) findViewById(R.id.button_done);
        mButtonDone.setOnClickListener(this);

        mButtonReset = (Button) findViewById(R.id.button_reset);
        mButtonReset.setOnClickListener(this);

        if (firstRun) {
            mMinutes.setEnabled(false);
            mHours.setEnabled(false);
            mSeparator.setEnabled(false);
            mPicker.setEnabled(false);
            mButtonDone.setEnabled(false);
            mButtonReset.setEnabled(false);
            mButtonDismiss = (Button) findViewById(R.id.button_dismiss);
            mButtonDismiss.setOnClickListener(this);
        }

    }

    @DebugLog
    @Override
    protected void onResume() {
        super.onResume();
        mFuzzyClock.setMinuteColor(mFuzzyPrefs.color.minute);
        mFuzzyClock.setHourColor(mFuzzyPrefs.color.hour);
        mFuzzyClock.setSeparatorColor(mFuzzyPrefs.color.separator);
        mFuzzyClock.updateColors();
        mFuzzyClock.setFontSize(mFuzzyPrefs.size);
        mPicker.setValue((int) mFuzzyPrefs.size);
    }

    @DebugLog
    @Override
    protected void onPause() {
        mFuzzyPrefs.save();
        super.onPause();
    }

    @DebugLog
    public void onClick(View v) {
        if (v == mMinutes) {
            chooseMinuteColor();
        } else if (v == mHours) {
            chooseHourColor();
        } else if (v == mSeparator) {
            chooseSeparatorColor();
        } else if (v == mButtonReset) {
            mFuzzyPrefs.reset();
            mFuzzyClock.setMinuteColor(mFuzzyPrefs.color.minute);
            mFuzzyClock.setHourColor(mFuzzyPrefs.color.hour);
            mFuzzyClock.setSeparatorColor(mFuzzyPrefs.color.separator);
            mFuzzyClock.updateColors();
            mFuzzyClock.setFontSize(mFuzzyPrefs.size);
            mPicker.setValue((int) mFuzzyPrefs.size);
        } else if (v == mButtonDone) {
            finish();
        } else if (v == mButtonDismiss) {
            mMinutes.setEnabled(true);
            mHours.setEnabled(true);
            mSeparator.setEnabled(true);
            mPicker.setEnabled(true);
            mButtonDone.setEnabled(true);
            mButtonReset.setEnabled(true);
            mRoot.removeView(mTipsView);
        }
    }

    @DebugLog
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        mFuzzyClock.setFontSize(newVal);
        mFuzzyPrefs.size = (float) newVal;
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
    protected void chooseMinuteColor() {
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(mColorEntries,
                        getColorEntry(mFuzzyPrefs.color.minute),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mFuzzyPrefs.color.minute = mColorResources[which];
                                mFuzzyClock.setMinuteColor(mColorResources[which]);
                                mFuzzyClock.updateColors();
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    @DebugLog
    protected void chooseHourColor() {
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(mColorEntries,
                        getColorEntry(mFuzzyPrefs.color.hour),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mFuzzyPrefs.color.hour = mColorResources[which];
                                mFuzzyClock.setHourColor(mColorResources[which]);
                                mFuzzyClock.updateColors();
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    @DebugLog
    protected void chooseSeparatorColor() {
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(mColorEntries,
                        getColorEntry(mFuzzyPrefs.color.separator),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mFuzzyPrefs.color.separator = mColorResources[which];
                                mFuzzyClock.setSeparatorColor(mColorResources[which]);
                                mFuzzyClock.updateColors();
                                dialog.dismiss();
                            }
                        })
                .show();
    }

}
