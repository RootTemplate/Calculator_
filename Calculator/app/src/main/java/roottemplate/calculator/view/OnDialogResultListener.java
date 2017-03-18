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

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

public interface OnDialogResultListener {
    void onDialogPositiveClick(int dialogId);
    void onDialogNegativeClick(int dialogId);
    void onDialogNeutralClick(int dialogId);

    class OnDialogButtonClickListener implements DialogInterface.OnClickListener {
        private final Activity mActivity;
        private final int mDialogId;

        public OnDialogButtonClickListener(Activity activity, int dialogId) {
            mActivity = activity;
            mDialogId = dialogId;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(mActivity instanceof OnDialogResultListener) {
                OnDialogResultListener handler = (OnDialogResultListener) mActivity;
                if(which == AlertDialog.BUTTON_POSITIVE)
                    handler.onDialogPositiveClick(mDialogId);
                else if(which == AlertDialog.BUTTON_NEGATIVE)
                    handler.onDialogNegativeClick(mDialogId);
            }
        }
    }
}
