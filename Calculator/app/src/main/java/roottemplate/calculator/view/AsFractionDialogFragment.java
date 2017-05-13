/*
 * Copyright (c) 2017 RootTemplate Group 1.
 * This file is part of Calculator_.
 *
 * Calculator_ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Calculator_ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Calculator_.  If not, see <http://www.gnu.org/licenses/>.
 */

package roottemplate.calculator.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import roottemplate.calculator.R;
import roottemplate.calculator.evaluator.util.Util;

public class AsFractionDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final double IDEAL_ERROR = 1e-12;
    private static final int MAX_Q_VALUE = 1_300_000;
    private static final double[] PARAMETERS = new double[] {
            Math.sqrt(2), Math.sqrt(3), Math.sqrt(5), Math.sqrt(6), Math.sqrt(7), Math.sqrt(10),
            Math.cbrt(2), Math.cbrt(3), Math.PI, Math.E
    };
    private static final String[] PARAMETER_NAMES = new String[] {
            "\u221a2", "\u221a3", "\u221a5", "\u221a6", "\u221a7", "\u221a10",
            "\u221b2", "\u221b3", "\u03c0", "e"
    };

    private AlertDialog mDialog;
    private volatile double mX;
    private volatile int mMaxQ;
    private volatile int mMinQ = 2;
    private volatile Util.Fraction mFract;
    private int mParamI = -1;
    private Thread mThread;

    public AsFractionDialogFragment() {
        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        double x = mX = getArguments().getDouble("number");

        Util.Fraction fraction = Util.toFraction(x, mMaxQ = 31001, mMinQ);
        for(int i = 0; i < PARAMETERS.length; i++) {
            Util.Fraction f = Util.toFraction(x / PARAMETERS[i], 100);
            if(fraction.errorPercentage > f.errorPercentage) {
                fraction = f;
                mParamI = i;
            }
        }
        mFract = fraction;

        return mDialog =  new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_asFraction_title)
                .setMessage(genMessage())
                .setPositiveButton(R.string.ok, null)
                .setNeutralButton("_", null)
                .create();
    }

    private void regenFraction() {
        if(mParamI != -1) throw new IllegalStateException("regenFraction() called; mParamI = -1");
        if(mMaxQ < 45000) {
            regenFraction0();
            onFractionRegenerated();
        } else {
            mDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(false);
            (mThread = new Thread() {
                @Override
                public void run() {
                    regenFraction0();

                    mThread = null;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onFractionRegenerated();
                        }
                    });
                }
            }).start();
        }
    }
    private void regenFraction0() {
        Util.Fraction f = Util.toFraction(mX, mMaxQ, mMinQ);
        if(mFract == null || f.errorPercentage < mFract.errorPercentage)
            mFract = f;
    }

    private String genMessage() {
        String message = "";
        int paramI = mParamI;
        Util.Fraction fraction = mFract;

        if(paramI > -1) {
            if(fraction.denom == 1) {
                if (fraction.num != 1)
                    message = String.valueOf(fraction.num);
            } else
                message = "(" + fraction.num + "/" + fraction.denom + ")";
            message += PARAMETER_NAMES[paramI];
        } else
            message = String.valueOf(fraction.num) + '/' + fraction.denom;

        if(fraction.errorPercentage > IDEAL_ERROR) {
            message += "\n\n" + String.format(getResources().getString(R.string.dialog_asFraction_message_error),
                    Util.doubleToString(fraction.errorPercentage, 8, 3, 3));
            if (mMaxQ > 45000)
                message += "\n\n" + String.format(getResources().getString(R.string.dialog_asFraction_currentMaxQ),
                        String.valueOf(mMaxQ));
        }

        return message;
    }

    private void onFractionRegenerated() {
        mDialog.setMessage(genMessage());
        updateNeutralButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            ((TextView) mDialog.findViewById(android.R.id.message)).setTextIsSelectable(true);
        } catch(Exception e) {
            Log.i(roottemplate.calculator.util.Util.LOG_TAG, "Cannot apply hack to copy from dialog");
        }
        updateNeutralButton();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mThread != null)
            mThread.interrupt();
    }

    private void updateNeutralButton() {
        int textId = 0;
        if(mParamI > -1) {
            mFract = null;
            textId = R.string.dialog_asFraction_toFraction;
        } else if(mFract.errorPercentage > IDEAL_ERROR && mMaxQ < MAX_Q_VALUE)
            textId = R.string.dialog_asFraction_findAccurateValue;

        Button btn = mDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        btn.setEnabled(true);
        if(textId == 0)
            btn.setVisibility(View.GONE);
        else {
            btn.setText(textId);
            btn.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        // On neutral button clicked
        if(mParamI > -1) {
            mParamI = -1;
        } else {
            Util.FRACTION_IDEAL_ERROR = 0;
            mMinQ = mMaxQ;
            mMaxQ *= 5;
            if(mMaxQ > MAX_Q_VALUE) mMaxQ = MAX_Q_VALUE;
        }
        regenFraction();
    }
}