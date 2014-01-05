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

import android.content.Context;
import android.util.AttributeSet;

public class FuzzyClockViewVertical extends FuzzyClockViewHorizontal {

    public FuzzyClockViewVertical(Context context) {
        this(context, null);
    }

    public FuzzyClockViewVertical(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void updateTextViewPadding() {
        float topPaddingRatio = 0.25f;// 0.328f;
        float bottomPaddingRatio = 0.18f;// 0.25f;
        // Set negative padding to scrunch the lines closer together.
        mTimeDisplayMinutes.setPadding(0, (int) (-topPaddingRatio * mTimeDisplayMinutes.getTextSize()), 0,
                (int) (-bottomPaddingRatio * mTimeDisplayMinutes.getTextSize()));
        mTimeDisplaySeparator.setPadding(0, (int) (-topPaddingRatio * mTimeDisplaySeparator.getTextSize()), 0,
                (int) (-bottomPaddingRatio * mTimeDisplaySeparator.getTextSize()));
        mTimeDisplayHours.setPadding(0, (int) (-topPaddingRatio * mTimeDisplayHours.getTextSize()), 0,
                (int) (-bottomPaddingRatio * mTimeDisplayHours.getTextSize()));
    }

    protected void updateSize() {
        super.updateSize();
        updateTextViewPadding();
    }

}
