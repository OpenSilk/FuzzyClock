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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FuzzySettings) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        mPref = b.getString("pref");
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
            chooseTypeface();
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

    /**
     * Checks if given color is current color, used to set radio buttons
     * @param color
     * @return
     */
    protected boolean checkColor(int color) {
        switch (mPref) {
            case "hour":
                return mActivity.mFuzzyPrefs.hour.color == color;
            case "separator":
                return mActivity.mFuzzyPrefs.separator.color == color;
            case "minute":
                return mActivity.mFuzzyPrefs.minute.color == color;
        }
        return false;
    }

    /**
     * Applies new color and notifies activity to update preview
     * @param color
     */
    protected void setNewColor(int color) {
        switch (mPref) {
            case "hour":
                mActivity.mFuzzyPrefs.hour.color = color;
                break;
            case "separator":
                mActivity.mFuzzyPrefs.separator.color = color;
                break;
            case "minute":
                mActivity.mFuzzyPrefs.minute.color = color;
                break;
        }
        mActivity.notifyPrefChanged();
    }

    /**
     * Creates the color preference dialog
     */
    @DebugLog
    protected void chooseColor() {
        final ColorPrefAdapter.ColorChoice[] colorChoices = new ColorPrefAdapter.ColorChoice[] {
                new ColorPrefAdapter.ColorChoice(getString(R.string.holo_blue),
                        getResources().getColor(android.R.color.holo_blue_light)),
                new ColorPrefAdapter.ColorChoice(getString(R.string.holo_red),
                        getResources().getColor(android.R.color.holo_red_light)),
                new ColorPrefAdapter.ColorChoice(getString(R.string.holo_green),
                        getResources().getColor(android.R.color.holo_green_light)),
                new ColorPrefAdapter.ColorChoice(getString(R.string.holo_orange),
                        getResources().getColor(android.R.color.holo_orange_dark)),
                new ColorPrefAdapter.ColorChoice(getString(R.string.holo_purple),
                        getResources().getColor(android.R.color.holo_purple)),
                new ColorPrefAdapter.ColorChoice(getString(R.string.white),
                        getResources().getColor(android.R.color.white)),
                new ColorPrefAdapter.ColorChoice(getString(R.string.custom),
                        // Color is just to set text color, not an available choice
                        getResources().getColor(android.R.color.tertiary_text_dark))
        };
        for (ColorPrefAdapter.ColorChoice c : colorChoices) {
            c.isActive = checkColor(c.value);
        }
        final ColorPrefAdapter adapter = new ColorPrefAdapter(getActivity(), 0, colorChoices);
        new AlertDialog.Builder(mActivity)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == colorChoices.length - 1) {
                            FuzzyColorPickerDialog.newInstance(mPref).show(getFragmentManager(), "color_picker");
                        } else {
                            setNewColor(adapter.getItem(which).value);
                        }
                    }
                })
                .show();
    }

    /**
     * Checks if given style is current font style, used to set radio buttons
     * @param style
     * @return
     */
    protected boolean checkTypeface(int style) {
        switch (mPref) {
            case "hour":
                return mActivity.mFuzzyPrefs.hour.style == style;
            case "separator":
                return mActivity.mFuzzyPrefs.separator.style == style;
            case "minute":
                return mActivity.mFuzzyPrefs.minute.style == style;
        }
        return false;
    }

    /**
     * Applies the new font style and notifies activity to update preview
     * @param style
     */
    protected void setNewTypeface(int style) {
        switch (mPref) {
            case "hour":
                mActivity.mFuzzyPrefs.hour.style = style;
                break;
            case "separator":
                mActivity.mFuzzyPrefs.separator.style = style;
                break;
            case "minute":
                mActivity.mFuzzyPrefs.minute.style = style;
                break;
        }
        mActivity.notifyPrefChanged();
    }

    /**
     * Creates the typeface preference dialog
     */
    protected void chooseTypeface() {
        // DO NOT REORDER
        final TypefacePrefAdapter.TypefaceChoice[] typefaceChoices = new TypefacePrefAdapter.TypefaceChoice[] {
                new TypefacePrefAdapter.TypefaceChoice(getString(R.string.regular)),
                new TypefacePrefAdapter.TypefaceChoice(getString(R.string.regular_bold)),
                new TypefacePrefAdapter.TypefaceChoice(getString(R.string.condensed)),
                new TypefacePrefAdapter.TypefaceChoice(getString(R.string.condensed_bold)),
                new TypefacePrefAdapter.TypefaceChoice(getString(R.string.thin)),
                new TypefacePrefAdapter.TypefaceChoice(getString(R.string.thin_bold))
        };
        for (int ii=0; ii< typefaceChoices.length; ii++) {
            typefaceChoices[ii].isActive = checkTypeface(ii);
        }
        final TypefacePrefAdapter adapter = new TypefacePrefAdapter(getActivity(), 0, typefaceChoices);
        new AlertDialog.Builder(mActivity)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setNewTypeface(which);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Adapter for font color dialog
     */
    protected static class ColorPrefAdapter extends ArrayAdapter<ColorPrefAdapter.ColorChoice> {

        protected static class ColorChoice {
            protected CharSequence name;
            protected int value;
            protected boolean isActive;
            protected ColorChoice(CharSequence name, int value) {
                this.name=name;
                this.value=value;
            }
        }

        public ColorPrefAdapter(Context context, int resource, ColorChoice[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.fuzzy_settings_pref_fancy_list_item, parent, false);
            } else {
                v = convertView;
            }
            if (v != null) {
                TextView tv = (TextView) v.findViewById(R.id.entry_name);
                tv.setText(getItem(position).name);
                tv.setTextColor(getItem(position).value);
                RadioButton r = (RadioButton) v.findViewById(R.id.radio_btn);
                r.setChecked(getItem(position).isActive);
            }
            return v;
        }
    }

    /**
     * Adapter for font style dialog
     */
    protected static class TypefacePrefAdapter extends ArrayAdapter<TypefacePrefAdapter.TypefaceChoice> {

        protected static class TypefaceChoice {
            protected CharSequence name;
            protected boolean isActive;
            protected TypefaceChoice(CharSequence name) {
                this.name=name;
            }
        }

        public TypefacePrefAdapter(Context context, int resource, TypefaceChoice[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.fuzzy_settings_pref_fancy_list_item, parent, false);
            } else {
                v = convertView;
            }
            if (v != null) {
                TextView tv = (TextView) v.findViewById(R.id.entry_name);
                tv.setText(getItem(position).name);
                tv.setTypeface(FuzzyPrefs.createTypeface(position));
                RadioButton r = (RadioButton) v.findViewById(R.id.radio_btn);
                r.setChecked(getItem(position).isActive);
            }
            return v;
        }
    }
}
