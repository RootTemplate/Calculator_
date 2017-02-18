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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import roottemplate.calculator.PreferencesManager;
import roottemplate.calculator.R;
import roottemplate.calculator.util.Util;

public class UpdateDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    public static final String FRAGMENT_TAG = "UpdateDialogFragment";
    private static final String UPDATE_URL = "http://roottemplate.pe.hu/project/calculator/checkversion/";
    private static final long UPDATE_DELAY_LONG = 1000L * 60L * 60L * 24L * 21L; // 3 weeks
    private static final long UPDATE_DELAY_LATER = 1000L * 60L * 60L * 24L * 2L; // 2 days

    public static boolean shouldAskForUpdate(PreferencesManager prefs) {
        return System.currentTimeMillis() >= prefs.nextUpdateCheckTime();
    }
    public static void onAppUpdated(PreferencesManager prefs) {
        prefs.nextUpdateCheckTime(System.currentTimeMillis() + UPDATE_DELAY_LONG);
    }
    private static void checkForUpdates(Context context) {
        String lang = context.getResources().getConfiguration().locale.getLanguage();
        int version = Util.getAppVersion(context);
        String url = UPDATE_URL + lang + "/" + version;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }
    public static void checkForUpdates(Context context, PreferencesManager prefs) {
        checkForUpdates(context);
        prefs.nextUpdateCheckTime(System.currentTimeMillis() + UPDATE_DELAY_LONG);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_update_title)
                .setMessage(R.string.dialog_update_message)
                .setPositiveButton(R.string.dialog_update_positive, this)
                .setNeutralButton(R.string.dialog_update_neutral, this)
                .setNegativeButton(R.string.dialog_update_negative, this)
                .create();
        dialog.setCanceledOnTouchOutside(false); // To prevent user from closing dialog accidentally
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Context context = getContext();
        long delay = 0;

        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                checkForUpdates(context);
                delay = UPDATE_DELAY_LONG;
                break;
            case Dialog.BUTTON_NEUTRAL:
                delay = UPDATE_DELAY_LATER;
                break;
            case Dialog.BUTTON_NEGATIVE:
                delay = UPDATE_DELAY_LONG;
                break;
        }

        PreferencesManager prefs = new PreferencesManager(context);
        prefs.nextUpdateCheckTime(System.currentTimeMillis() + delay);
        dismiss();
    }
}
