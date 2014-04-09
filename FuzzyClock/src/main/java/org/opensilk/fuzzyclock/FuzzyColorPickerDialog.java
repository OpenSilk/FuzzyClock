package org.opensilk.fuzzyclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

/**
 * Created by drew on 4/8/14.
 */
public class FuzzyColorPickerDialog extends DialogFragment implements ColorPicker.OnColorChangedListener {

    protected FuzzySettings mActivity;
    protected String mPref;

    protected ColorPicker mPicker;
    protected TextView mColorText;

    public static FuzzyColorPickerDialog newInstance(String pref) {
        Bundle b = new Bundle(1);
        b.putString("pref", pref);
        FuzzyColorPickerDialog d = new FuzzyColorPickerDialog();
        d.setArguments(b);
        return d;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FuzzySettings) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPref = getArguments().getString("pref");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.color_picker, null);
        mPicker = (ColorPicker) v.findViewById(R.id.color_picker);
        mColorText = (TextView) v.findViewById(R.id.color_text);
        OpacityBar opacityBar = (OpacityBar) v.findViewById(R.id.opacity_bar);
        mPicker.addOpacityBar(opacityBar);
//        SVBar svBar = (SVBar) v.findViewById(R.id.svbar);
//        mPicker.addSVBar(svBar);
        SaturationBar saturationBar = (SaturationBar) v.findViewById(R.id.saturationbar);
        mPicker.addSaturationBar(saturationBar);
        ValueBar valueBar = (ValueBar) v.findViewById(R.id.valuebar);
        mPicker.addValueBar(valueBar);
        mPicker.setOnColorChangedListener(this);
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setNewColor(mPicker.getColor());
                    }
                })
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        mColorText.setText("#"+Integer.toHexString(getOldColor()));
        mPicker.setColor(getOldColor());
        mPicker.setOldCenterColor(getOldColor());
    }

    @Override
    public void onColorChanged(int i) {
        mColorText.setText("#"+Integer.toHexString(i));
    }

    private int getOldColor() {
        switch (mPref) {
            case "minute":
                return mActivity.mFuzzyPrefs.minute.color;
            case "separator":
                return mActivity.mFuzzyPrefs.separator.color;
            case "hour":
                return mActivity.mFuzzyPrefs.hour.color;
        }
        return -1;
    }

    private void setNewColor(int newColor) {
        switch (mPref) {
            case "minute":
                mActivity.mFuzzyPrefs.minute.color = newColor;
                break;
            case "separator":
                mActivity.mFuzzyPrefs.separator.color = newColor;
                break;
            case "hour":
                mActivity.mFuzzyPrefs.hour.color = newColor;
                break;
        }
        mActivity.notifyPrefChanged();
    }

}
