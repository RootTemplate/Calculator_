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
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import roottemplate.calculator.R;
import roottemplate.calculator.evaluator.util.Util;

public class AsFractionDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        double x = getArguments().getDouble("number");
        Util.Fraction fraction = Util.toFraction(x, 1000);
        String message = String.valueOf(fraction.num) + '/' + fraction.denom;
        if(fraction.errorPercentage != 0)
            message += ". " + String.format(getResources().getString(R.string.dialog_asFraction_message_error),
                    Util.doubleToString(fraction.errorPercentage, 8, 3, 3));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_asFraction_title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create();
    }
}
