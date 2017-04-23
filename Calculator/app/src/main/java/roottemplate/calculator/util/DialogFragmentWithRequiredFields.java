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

package roottemplate.calculator.util;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;

public abstract class DialogFragmentWithRequiredFields extends DialogFragment {
    protected final TextWatcher mTextListener = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            updatePositiveButton();
        }
    };

    protected abstract boolean areTextFilled();

    protected void updatePositiveButton() {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null)
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(areTextFilled());
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePositiveButton();
    }

}
