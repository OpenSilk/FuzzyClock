/*
 *  Copyright (C) 2014 OpenSilk Productions LLC
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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import hugo.weaving.DebugLog;

/**
 * Created by drew on 4/3/14.
 */
public class FuzzySettingsPrefsPage extends Fragment implements
        View.OnClickListener,
        NumberPicker.OnValueChangeListener {

    private FuzzySettings mActivity;
    private String mPref;

    private NumberPicker mPicker;
    private Button mColorButton;
    private Button mFontStyle;

    private CharSequence[] mColorEntries;
    private static final int[] sColorResources = {
            android.R.color.holo_blue_light,
            android.R.color.holo_red_light,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_purple,
            android.R.color.white,
    };

    private CharSequence[] mFontStyleEntries;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle b = getArguments();
        mPref = b.getString("pref");
        mActivity = (FuzzySettings) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColorEntries = new CharSequence[] {
                getString(R.string.holo_blue),
                getString(R.string.holo_red),
                getString(R.string.holo_green),
                getString(R.string.holo_orange),
                getString(R.string.holo_purple),
                getString(R.string.white)
        };
        // DO NOT REORDER
        mFontStyleEntries = new CharSequence[] {
                getString(R.string.regular),
                getString(R.string.regular_bold),
                getString(R.string.condensed),
                getString(R.string.condensed_bold),
                getString(R.string.thin),
                getString(R.string.this_bold)
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fuzzy_settings_prefs_page, container, false);
        mPicker = (NumberPicker) v.findViewById(R.id.numberPicker);
        mPicker.setOnValueChangedListener(this);
        mPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mColorButton = (Button) v.findViewById(R.id.button_color);
        mColorButton.setOnClickListener(this);
        mFontStyle = (Button) v.findViewById(R.id.button_font);
        mFontStyle.setOnClickListener(this);
        return v;
    }

    @Override
    @DebugLog
    public void onResume() {
        super.onResume();
        if (mActivity.mFuzzyPrefs != null) {
            if (mActivity.mFuzzyPrefs.isPortrait) {
                mPicker.setMinValue(getResources().getInteger(R.integer.fuzzy_font_size_min_port));
                mPicker.setMaxValue(getResources().getInteger(R.integer.fuzzy_font_size_max_port));
            } else {
                mPicker.setMinValue(getResources().getInteger(R.integer.fuzzy_font_size_min_land));
                mPicker.setMaxValue(getResources().getInteger(R.integer.fuzzy_font_size_max_land));
            }
            mPicker.setWrapSelectorWheel(false);
            switch (mPref) {
                case "hour":
                    mPicker.setValue((int)mActivity.mFuzzyPrefs.hour.size);
                    break;
                case "separator":
                    mPicker.setValue((int)mActivity.mFuzzyPrefs.separator.size);
                    break;
                case "minute":
                    mPicker.setValue((int)mActivity.mFuzzyPrefs.minute.size);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mColorButton) {
            chooseColor();
        } else if (v == mFontStyle) {
            chooseFontStyle();
        }
    }

    @Override
    @DebugLog
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        switch (mPref) {
            case "hour":
                mActivity.mFuzzyPrefs.hour.size = (float) newVal;
                break;
            case "separator":
                mActivity.mFuzzyPrefs.separator.size = (float) newVal;
                break;
            case "minute":
                mActivity.mFuzzyPrefs.minute.size = (float) newVal;
                break;
        }
        mActivity.notifyPrefChanged();
    }

    protected int getColorEntry() {
        int color = -1;
        switch (mPref) {
            case "hour":
                color = mActivity.mFuzzyPrefs.hour.color;
                break;
            case "separator":
                color = mActivity.mFuzzyPrefs.separator.color;
                break;
            case "minute":
                color = mActivity.mFuzzyPrefs.minute.color;
                break;
        }
        for (int ii=0;ii< sColorResources.length;ii++) {
            if (sColorResources[ii] == color) {
                return ii;
            }
        }
        return sColorResources[0];
    }

    protected void setNewColor(int which) {
        switch (mPref) {
            case "hour":
                mActivity.mFuzzyPrefs.hour.color = sColorResources[which];
                break;
            case "separator":
                mActivity.mFuzzyPrefs.separator.color = sColorResources[which];
                break;
            case "minute":
                mActivity.mFuzzyPrefs.minute.color = sColorResources[which];
                break;
        }
        mActivity.notifyPrefChanged();
    }

    @DebugLog
    protected void chooseColor() {
        new AlertDialog.Builder(mActivity)
                .setSingleChoiceItems(mColorEntries,
                        getColorEntry(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setNewColor(which);
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    protected int getFontEntry() {
        int style = -1;
        switch (mPref) {
            case "hour":
                style = mActivity.mFuzzyPrefs.hour.style;
                break;
            case "separator":
                style = mActivity.mFuzzyPrefs.separator.style;
                break;
            case "minute":
                style = mActivity.mFuzzyPrefs.minute.style;
                break;
        }
        return style;
    }

    protected void setNewFontStyle(int which) {
        switch (mPref) {
            case "hour":
                mActivity.mFuzzyPrefs.hour.style = which;
                break;
            case "separator":
                mActivity.mFuzzyPrefs.separator.style = which;
                break;
            case "minute":
                mActivity.mFuzzyPrefs.minute.style = which;
                break;
        }
        mActivity.notifyPrefChanged();
    }

    protected void chooseFontStyle() {
        new AlertDialog.Builder(mActivity)
                .setSingleChoiceItems(mFontStyleEntries,
                        getFontEntry(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setNewFontStyle(which);
                                dialog.dismiss();
                            }
                        })
                .show();
    }
}
