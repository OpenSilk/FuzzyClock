package org.opensilk.fuzzyclock;

import android.content.res.Configuration;
import android.os.Handler;
import android.service.dreams.DreamService;
import android.util.Log;
import android.view.View;

public class FuzzyDreams extends DreamService {

    static final String TAG = FuzzyDreams.class.getSimpleName();
    static final boolean LOGV = true;

    private View mContentView, mSaverView, mFuzzyClock;

    private final Handler mHandler = new Handler();

    private final ScreenSaverAnimation mMoveSaverRunnable;

    public FuzzyDreams() {
        if (LOGV) Log.v(TAG, "Screensaver allocated");
        mMoveSaverRunnable = new ScreenSaverAnimation(mHandler);
    }

    @Override
    public void onCreate() {
        if (LOGV) Log.v(TAG, "Screensaver created");
        super.onCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (LOGV) Log.v(TAG, "Screensaver configuration changed");
        super.onConfigurationChanged(newConfig);
        mHandler.removeCallbacks(mMoveSaverRunnable);
        setLayout();
        mHandler.post(mMoveSaverRunnable);
    }

    @Override
    public void onAttachedToWindow() {
        if (LOGV) Log.v(TAG, "Screensaver attached to window");
        super.onAttachedToWindow();

        // We want the screen saver to exit upon user interaction.
        setInteractive(false);

        setFullscreen(true);

        setLayout();

        mHandler.post(mMoveSaverRunnable);
    }

    @Override
    public void onDetachedFromWindow() {
        if (LOGV) Log.v(TAG, "Screensaver detached from window");
        super.onDetachedFromWindow();

        mHandler.removeCallbacks(mMoveSaverRunnable);
    }

    private void setLayout() {
        setContentView(R.layout.fuzzy_dreams);
        mFuzzyClock = findViewById(R.id.fuzzy_clock);
        mSaverView = findViewById(R.id.main_clock);
        setScreenBright(false);
        mContentView = (View) mSaverView.getParent();
        mSaverView.setAlpha(0);

        mMoveSaverRunnable.registerViews(mContentView, mSaverView);
    }
}
