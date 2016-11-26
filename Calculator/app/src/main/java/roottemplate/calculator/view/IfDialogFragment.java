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

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;

@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class IfDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final int dialogId = args.getInt("id", 0);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Activity activity = getActivity();
                if(activity instanceof OnDialogResultListener) {
                    OnDialogResultListener handler = (OnDialogResultListener) activity;
                    if(which == AlertDialog.BUTTON_POSITIVE)
                        handler.onDialogPositiveClick(dialogId);
                    else if(which == AlertDialog.BUTTON_NEGATIVE)
                        handler.onDialogNegativeClick(dialogId);
                }
            }
        };

        return new AlertDialog.Builder(getActivity())
                .setTitle(args.getInt("title"))
                .setMessage(args.getInt("message"))
                .setPositiveButton(args.getInt("positiveBtn"), listener)
                .setNegativeButton(args.getInt("negativeBtn"), listener)
                .create();
    }

    public interface OnDialogResultListener {
        void onDialogPositiveClick(int dialogId);
        void onDialogNegativeClick(int dialogId);
    }
}
