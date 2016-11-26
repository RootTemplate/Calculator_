/*
 * Copyright (c) 2016 RootTemplate Group 1.
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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import roottemplate.calculator.MainActivity;
import roottemplate.calculator.R;
import roottemplate.calculator.util.Util;

public class FirstLaunchDialogFragment extends DialogFragment {
    public static final String FRAGMENT_TAG = "FirstLaunchDialogFragment";

    private DelayThread mDelay;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_firstLaunch_title)
                .setMessage(R.string.dialog_firstLaunch_message)
                .setPositiveButton(R.string.dialog_firstLaunch_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        ((MainActivity) getActivity()).showTips();
                    }
                })
                .setNegativeButton(R.string.dialog_firstLaunch_negative, null)
                .create();

        mDelay = new DelayThread(savedInstanceState == null ? 2000L : savedInstanceState.getLong("threadDelay"));
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mDelay.getState() == Thread.State.NEW) {
            setButtonsEnabled(false);
            mDelay.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("threadDelay", mDelay.getTimeLeft());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDelay.interrupt();
    }

    private void setButtonsEnabled(boolean enabled) {
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(enabled);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(enabled);
        dialog.setCanceledOnTouchOutside(enabled);
    }



    private class DelayThread extends Thread {
        private volatile long mDelay;
        private volatile long mStart = -1;

        public DelayThread(long delay) {
            mDelay = delay;
        }

        @Override
        public void run() {
            mStart = Util.getTime();
            try {
                Thread.sleep(mDelay);
            } catch (InterruptedException e) {
                return;
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setButtonsEnabled(true);
                }
            });
        }

        public long getTimeLeft() {
            if(mStart == -1) return mDelay;
            return mDelay - (Util.getTime() - mStart);
        }
    }
}
