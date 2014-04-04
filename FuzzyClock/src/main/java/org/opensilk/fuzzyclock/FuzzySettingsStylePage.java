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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by drew on 4/3/14.
 */
public class FuzzySettingsStylePage extends Fragment implements
        FuzzySettings.PrefChangeListener {

    private FuzzySettings mActivity;

    private int mStyle;
    private FuzzyClockView mFuzzyClock;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FuzzySettings) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        mStyle = b.getInt("style");
        mActivity.mPrefChangeListeners.add(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity.mPrefChangeListeners.remove(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fuzzy_settings_style_page, container, false);
        mFuzzyClock = (FuzzyClockView) v.findViewById(R.id.fuzzy_clock);
        mFuzzyClock.setLive(false);
        mFuzzyClock.updateTime(0,26);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mActivity.mFuzzyPrefs != null) {
            initFuzzyClock(mActivity.mFuzzyPrefs);
        }
    }

    public void initFuzzyClock(FuzzyPrefs prefs) {
        mFuzzyClock.loadPreferences(prefs);
        mFuzzyClock.updateTime(0,26);
        mFuzzyClock.setClockStyle(mStyle);
    }

    @Override
    public void onPrefChanged(FuzzyPrefs prefs) {
        initFuzzyClock(prefs);
    }

    @Override
    public void setSelected(String pref) {
        switch (pref) {
            case "minute":
                mFuzzyClock.mTimeDisplayMinutes.setBackground(getResources().getDrawable(R.drawable.text_border));
                mFuzzyClock.mTimeDisplayHours.setBackground(null);
                mFuzzyClock.mTimeDisplaySeparator.setBackground(null);
                break;
            case "hour":
                mFuzzyClock.mTimeDisplayMinutes.setBackground(null);
                mFuzzyClock.mTimeDisplayHours.setBackground(getResources().getDrawable(R.drawable.text_border));
                mFuzzyClock.mTimeDisplaySeparator.setBackground(null);
                break;
            case "separator":
                mFuzzyClock.mTimeDisplayMinutes.setBackground(null);
                mFuzzyClock.mTimeDisplayHours.setBackground(null);
                mFuzzyClock.mTimeDisplaySeparator.setBackground(getResources().getDrawable(R.drawable.text_border));
                break;
            default:
                mFuzzyClock.mTimeDisplayMinutes.setBackground(null);
                mFuzzyClock.mTimeDisplayHours.setBackground(null);
                mFuzzyClock.mTimeDisplaySeparator.setBackground(null);
        }
    }
}
