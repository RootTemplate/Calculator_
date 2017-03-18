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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class CustomDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(args.getInt("title"))
                .setView(args.getInt("view"));
        OnDialogResultListener.OnDialogButtonClickListener listener =  new OnDialogResultListener
                .OnDialogButtonClickListener(getActivity(), args.getInt("dialogId"));

        int text = args.getInt("negativeText", 0);
        if(text != 0)
            builder.setNegativeButton(text, listener);
        text = args.getInt("neutralText", 0);
        if(text != 0)
            builder.setNeutralButton(text, listener);
        text = args.getInt("positiveText", 0);
        if(text != 0)
            builder.setPositiveButton(text, listener);

        return builder.create();
    }
}
