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
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

public class IfDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        int dialogId = args.getInt("id", 0);

        DialogInterface.OnClickListener listener = new OnDialogResultListener
                .OnDialogButtonClickListener(getActivity(), dialogId);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(args.getInt("title"))
                .setPositiveButton(args.getInt("positiveBtn"), listener)
                .setNegativeButton(args.getInt("negativeBtn"), listener);
        if(args.containsKey("message"))
            builder.setMessage(args.getInt("message"));
        return builder.create();
    }
}
