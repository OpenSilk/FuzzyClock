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

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.service.dreams.DreamService;
import android.util.Log;
import android.view.View;

import hugo.weaving.DebugLog;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class FuzzyDreams extends DreamService {

    private static final String TAG = FuzzyDreams.class.getSimpleName();
    private static final boolean LOGV = BuildConfig.DEBUG;

    private View mContentView, mSaverView;
    private FuzzyClockView mFuzzyClock;

    private final Handler mHandler = new Handler();
    private final ScreenSaverAnimation mMoveSaverRunnable;
    private final FuzzyClockView.TimeChangedListener mListener = new FuzzyClockView.TimeChangedListener() {
        @DebugLog
        @Override
        public void onTimeChanged() {
            // When text has changed we need to recalculate so we don't run off the screen
            mHandler.removeCallbacks(mMoveSaverRunnable);
            mHandler.post(mMoveSaverRunnable);
        }
    };

    public FuzzyDreams() {
        if (LOGV) Log.v(TAG, "Daydream allocated");
        mMoveSaverRunnable = new ScreenSaverAnimation(mHandler);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mHandler.removeCallbacks(mMoveSaverRunnable);
        setupView();
        mHandler.post(mMoveSaverRunnable);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(false);
        mHandler.removeCallbacks(mMoveSaverRunnable);
        setupView();
        mHandler.post(mMoveSaverRunnable);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mMoveSaverRunnable);
    }

    private void setupView() {
        setContentView(R.layout.fuzzy_dreams);

        mSaverView = findViewById(R.id.clock_wrapper);
        mSaverView.setAlpha(0);
        mContentView = (View) mSaverView.getParent();

        mFuzzyClock = (FuzzyClockView) findViewById(R.id.fuzzy_clock);
        mFuzzyClock.registerCallback(mListener);
        mFuzzyClock.loadPreferences(new FuzzyPrefs(this));

        mMoveSaverRunnable.registerViews(mContentView, mSaverView);
    }

}
