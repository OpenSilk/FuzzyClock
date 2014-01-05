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

import android.content.res.Configuration;
import android.os.Handler;
import android.service.dreams.DreamService;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import hugo.weaving.DebugLog;

public class FuzzyDreams extends DreamService {

    private static final String TAG = FuzzyDreams.class.getSimpleName();
    private static final boolean LOGV = true;

    private View mContentView, mSaverView;
    private IFuzzyClockView mFuzzyClock;

    private final Handler mHandler = new Handler();

    private final ScreenSaverAnimation mMoveSaverRunnable;

    public FuzzyDreams() {
        if (LOGV) Log.v(TAG, "Screensaver allocated");
        mMoveSaverRunnable = new ScreenSaverAnimation(mHandler);
    }

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @DebugLog
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mHandler.removeCallbacks(mMoveSaverRunnable);
        setupLayout();
        mHandler.post(mMoveSaverRunnable);
    }

    @DebugLog
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(false);
        mHandler.removeCallbacks(mMoveSaverRunnable);
        setupLayout();
        mHandler.post(mMoveSaverRunnable);
    }

    @DebugLog
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mMoveSaverRunnable);
    }

    private void setupLayout() {
        setContentView(R.layout.fuzzy_dreams);

        mSaverView = findViewById(R.id.clock_wrapper);
        mSaverView.setAlpha(0);
        mContentView = (View) mSaverView.getParent();

        FuzzyPrefs prefs = new FuzzyPrefs(this);
        switch (prefs.style) {
            case FuzzyPrefs.STYLE_STAGGERED:
                getWindow().getLayoutInflater().inflate(R.layout.fuzzy_clock_staggered, (LinearLayout)mSaverView, true);
                mFuzzyClock = (IFuzzyClockView) findViewById(R.id.fuzzy_clock_staggered);
                break;
            case FuzzyPrefs.STYLE_VERTICAL:
                getWindow().getLayoutInflater().inflate(R.layout.fuzzy_clock_vertical, (LinearLayout)mSaverView, true);
                mFuzzyClock = (IFuzzyClockView) findViewById(R.id.fuzzy_clock_vertical);
                break;
            case FuzzyPrefs.STYLE_HORIZONTAL:
            default:
                getWindow().getLayoutInflater().inflate(R.layout.fuzzy_clock_horizontal, (LinearLayout)mSaverView, true);
                mFuzzyClock = (IFuzzyClockView) findViewById(R.id.fuzzy_clock_horizontal);
                break;
        }

        mFuzzyClock.registerCallback(mListener);
        mFuzzyClock.loadPreferences(prefs);

        mMoveSaverRunnable.registerViews(mContentView, mSaverView);
    }

    final IFuzzyClockView.TimeChangedListener mListener = new IFuzzyClockView.TimeChangedListener() {
        @DebugLog
        @Override
        public void onTimeChanged() {
            // When text has changed we need to recalculate so we don't run off the screen
            mHandler.removeCallbacks(mMoveSaverRunnable);
            mHandler.post(mMoveSaverRunnable);
        }
    };
}
