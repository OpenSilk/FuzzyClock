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

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

abstract class FuzzySettings extends FragmentActivity {

    protected FuzzyPrefs mFuzzyPrefs;

    //View pager
    private StylePagerAdapter mStylePagerAdapter;
    private PrefsPagerAdapter mPrefsPagerAdapter;
    private ViewPager mStyleViewPager;
    private ViewPager mPrefsViewPager;

    /**
     * Style fragments register with this for callbacks from the pref fragments
     */
    final List<PrefChangeListener> mPrefChangeListeners = new ArrayList<>(3);

    /**
     * Interface passing information from the pref fragments to the style fragments
     */
    interface PrefChangeListener {
        void onPrefChanged(FuzzyPrefs prefs);
        void setSelected(String pref);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fuzzy_settings_base);

        // Init style fragments
        mStylePagerAdapter = new StylePagerAdapter(getSupportFragmentManager());
        mStyleViewPager = (ViewPager) findViewById(R.id.style_pager);
        mStyleViewPager.setAdapter(mStylePagerAdapter);
        mStyleViewPager.setOffscreenPageLimit(mStylePagerAdapter.getCount() - 1);
        mStyleViewPager.addOnPageChangeListener(mStylePageChangeListener);

        // Init pref fragments
        mPrefsPagerAdapter = new PrefsPagerAdapter(getSupportFragmentManager());
        mPrefsViewPager = (ViewPager) findViewById(R.id.prefs_pager);
        mPrefsViewPager.setAdapter(mPrefsPagerAdapter);
        mPrefsViewPager.setOffscreenPageLimit(mPrefsPagerAdapter.getCount() - 1);
        mPrefsViewPager.addOnPageChangeListener(mPrefPageChangeListener);

        // Init action bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeAsUpIndicator(R.drawable.ic_done_white_36dp);
        } else {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setIcon(R.drawable.ic_done_white_36dp);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPrefChangeListeners.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPrefsViewPager.setCurrentItem(0);
        mStyleViewPager.setCurrentItem(mFuzzyPrefs.clockStyle);
    }

    @Override
    protected void onPause() {
        mFuzzyPrefs.save();
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Style ViewPager page change listener
     */
    final ViewPager.SimpleOnPageChangeListener mStylePageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        @DebugLog
        public void onPageSelected(int position) {
            mFuzzyPrefs.clockStyle = mStylePagerAdapter.mFragments.get(position).style;
        }
    };

    /**
     * Prefs ViewPager page change listener
     */
    final ViewPager.SimpleOnPageChangeListener mPrefPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        @DebugLog
        public void onPageSelected(int position) {
            for (PrefChangeListener l : mPrefChangeListeners) {
                l.setSelected(mPrefsPagerAdapter.mFragments.get(position).pref);
            }
        }
    };

    /**
     * Called from pref fragments
     */
    protected void notifyPrefChanged() {
        for (PrefChangeListener l : mPrefChangeListeners) {
            l.onPrefChanged(mFuzzyPrefs);
        }
    }

    /**
     * Adapter for Style ViewPager
     */
    class StylePagerAdapter extends FragmentPagerAdapter {
        List<Holder> mFragments = new ArrayList<>(3);
        // DO NOT REORDER
        final CharSequence[] mTitles = new CharSequence[] {
                getString(R.string.horizontal),
                getString(R.string.vertical),
                getString(R.string.staggered)
        };

        StylePagerAdapter(FragmentManager fm) {
            super(fm);
            // DO NOT REORDER
            mFragments.add(new Holder(FuzzySettingsStylePage.class.getName(), FuzzyPrefs.CLOCK_STYLE_HORIZONTAL));
            mFragments.add(new Holder(FuzzySettingsStylePage.class.getName(), FuzzyPrefs.CLOCK_STYLE_VERTICAL));
            mFragments.add(new Holder(FuzzySettingsStylePage.class.getName(), FuzzyPrefs.CLOCK_STYLE_STAGGERED));
        }

        @Override
        public Fragment getItem(int pos) {
            return Fragment.instantiate(FuzzySettings.this, mFragments.get(pos).className, mFragments.get(pos).getArguments());
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        class Holder {
            String className;
            int style;

            Holder(String className, int style) {
                this.className = className;
                this.style = style;
            }

            Bundle getArguments() {
                Bundle b = new Bundle();
                b.putInt("style", style);
                return b;
            }
        }

    }

    /**
     * Adapter for Prefs ViewPager
     */
    class PrefsPagerAdapter extends FragmentPagerAdapter {
        List<Holder> mFragments = new ArrayList<Holder>();
        // DO NOT REORDER
        final CharSequence[] mTitles = new CharSequence[] {
                getString(R.string.common),
                getString(R.string.minutes),
                getString(R.string.separator),
                getString(R.string.hours)
        };

        PrefsPagerAdapter(FragmentManager fm) {
            super(fm);
            // DO NOT REORDER
            mFragments.add(new Holder(FuzzySettingsPrefsCommonPage.class.getName(), "common"));
            mFragments.add(new Holder(FuzzySettingsPrefsPage.class.getName(), "minute"));
            mFragments.add(new Holder(FuzzySettingsPrefsPage.class.getName(), "separator"));
            mFragments.add(new Holder(FuzzySettingsPrefsPage.class.getName(), "hour"));
        }

        @Override
        public Fragment getItem(int pos) {
            return Fragment.instantiate(FuzzySettings.this, mFragments.get(pos).className, mFragments.get(pos).getArguments());
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        class Holder {
            String className;
            String pref;

            Holder(String className, String pref) {
                this.className = className;
                this.pref = pref;
            }

            Bundle getArguments() {
                Bundle b = new Bundle();
                b.putString("pref", pref);
                return b;
            }
        }
    }

}
