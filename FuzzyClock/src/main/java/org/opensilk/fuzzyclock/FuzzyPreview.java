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
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import hugo.weaving.DebugLog;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;

/**
 * Created by drew on 4/4/14.
 */
public class FuzzyPreview extends Activity {

    private View mContentView, mSaverView;
    private FuzzyClockView mFuzzyClock;
    private int mAppId;

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

    public FuzzyPreview() {
        mMoveSaverRunnable = new ScreenSaverAnimation(mHandler);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppId = getIntent().getIntExtra(EXTRA_APPWIDGET_ID, -1);
        setupView();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAppId = savedInstanceState.getInt("APP_ID", -1);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("APP_ID", mAppId);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler.removeCallbacks(mMoveSaverRunnable);
        mHandler.post(mMoveSaverRunnable);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mMoveSaverRunnable);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().getAttributes().screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    private void setupView() {
        setContentView(R.layout.fuzzy_dreams);

        mSaverView = findViewById(R.id.clock_wrapper);
        mSaverView.setAlpha(0);
        mContentView = (View) mSaverView.getParent();

        mFuzzyClock = (FuzzyClockView) findViewById(R.id.fuzzy_clock);
        mFuzzyClock.registerCallback(mListener);
        mFuzzyClock.loadPreferences(new FuzzyPrefs(this, mAppId));

        mMoveSaverRunnable.registerViews(mContentView, mSaverView);
    }

}
