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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;

/**
 * Created by drew on 4/3/14.
 */
public class FuzzySettingsPrefsCommonPage extends Fragment implements
        View.OnClickListener {

    protected FuzzySettings mActivity;

    protected Button mPreviewButton;
    protected Button mLogicButton;
    protected CharSequence[] mLogicEntries;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FuzzySettings) activity;
        // DO NOT REORDER
        mLogicEntries = new CharSequence[] {
                getString(R.string.fast),
                getString(R.string.precise),
                getString(R.string.slow),
                getString(R.string.warped)
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fuzzy_settings_prefs_common_page, container, false);
        mPreviewButton = (Button) v.findViewById(R.id.button_preview);
        mPreviewButton.setOnClickListener(this);
        mLogicButton = (Button) v.findViewById(R.id.button_logic);
        mLogicButton.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        if (v == mLogicButton) {
            chooseLogic();
        } else if (v == mPreviewButton) {
            Intent i = new Intent(mActivity, FuzzyPreview.class);
            if (mActivity instanceof FuzzyWidgetSettings) {
                i.putExtra(EXTRA_APPWIDGET_ID, ((FuzzyWidgetSettings) mActivity).mAppWidgetId);
            }
            mActivity.startActivity(i);
        }
    }

    protected void chooseLogic() {
        new AlertDialog.Builder(mActivity)
                .setSingleChoiceItems(mLogicEntries,
                        mActivity.mFuzzyPrefs.clockLogic,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mActivity.mFuzzyPrefs.clockLogic = which;
                                mActivity.notifyPrefChanged();
                                dialog.dismiss();
                            }
                        })
                .show();
    }
}
