/*
 * Copyright (C)  2019-2024 Domjos
 * This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniTrackerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggermobile.dialogs;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Objects;

import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unibuggermobile.helper.SQLiteGeneral;
import de.domjos.unitrackerlibrary.services.engine.Authentication;

public class PasswordDialog extends DialogFragment {
    private static boolean firstLogin;
    private static boolean changePassword;
    private static Runnable successRunnable;

    public static PasswordDialog newInstance(boolean firstLogin, boolean changePassword, Runnable successRunnable) {
        PasswordDialog.firstLogin = firstLogin;
        PasswordDialog.changePassword = changePassword;
        PasswordDialog.successRunnable = successRunnable;

        PasswordDialog dialog = new PasswordDialog();
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** @noinspection BusyWait*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.password_dialog, container, false);
        Objects.requireNonNull(this.getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(this.getDialog()).setCanceledOnTouchOutside(false);

        final Activity activity = this.requireActivity();
        final TextView lblTitle = view.findViewById(R.id.lblTitle);
        final EditText password1 = view.findViewById(R.id.txtPassword1);
        final TextInputLayout password2 = view.findViewById(R.id.txtPassword2);
        final Button cmdSubmit = view.findViewById(R.id.cmdSubmit);
        if (!firstLogin || changePassword) {
            lblTitle.setText(R.string.pwd_title);
        } else {
            password2.setVisibility(View.GONE);
            lblTitle.setText(R.string.pwd_title_pwd);
        }

        try {
            if (MainActivity.GLOBALS.getPassword().isEmpty() || changePassword) {
                if(MainActivity.GLOBALS.getSettings(activity).isEncryptionEnabled()) {
                    new Thread(() -> {
                        while (this.getDialog().isShowing()) {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception ignored) {}
                        }
                    }).start();
                } else {
                    MainActivity.GLOBALS.setPassword(SQLiteGeneral.NO_PASS);
                    MainActivity.GLOBALS.setSqLiteGeneral(new SQLiteGeneral(activity, MainActivity.GLOBALS.getPassword()));
                    successRunnable.run();
                }
            } else {
                MainActivity.GLOBALS.setSqLiteGeneral(new SQLiteGeneral(activity, MainActivity.GLOBALS.getPassword()));
                successRunnable.run();
            }
        } catch (Exception ignored) {}

        cmdSubmit.setOnClickListener(v -> {
            try {
                if (!firstLogin || changePassword) {
                    if (password1.getText().toString().equals(Objects.requireNonNull(password2.getEditText()).getText().toString())) {
                        if (password1.getText().toString().length() >= 4) {
                            password1.setTextColor(Color.GREEN);
                            Objects.requireNonNull(password2.getEditText()).setTextColor(Color.GREEN);
                            MainActivity.GLOBALS.getSettings(activity).isFirstLogin(true);

                            new Thread(() -> activity.runOnUiThread(() -> {
                                try {
                                    if (changePassword) {
                                        MainActivity.GLOBALS.getSqLiteGeneral().changePassword(password1.getText().toString());
                                    }
                                    MainActivity.GLOBALS.setPassword(password1.getText().toString());
                                    MainActivity.GLOBALS.setSqLiteGeneral(new SQLiteGeneral(activity, MainActivity.GLOBALS.getPassword()));
                                    if (checkDatabase()) {
                                        successRunnable.run();
                                        getDialog().cancel();
                                    }
                                } catch (Exception ex) {
                                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, activity);
                                }
                            })).start();
                        } else {
                            password2.setError(activity.getString(R.string.messages_passwords_too_small));
                        }
                    } else {
                        password2.setError(activity.getString(R.string.messages_passwords_dont_fit));
                    }
                } else {
                    MainActivity.GLOBALS.setPassword(password1.getText().toString());
                    MainActivity.GLOBALS.setSqLiteGeneral(new SQLiteGeneral(activity, MainActivity.GLOBALS.getPassword()));
                    if(checkDatabase()) {
                        password1.setTextColor(Color.GREEN);
                        new Thread(() -> activity.runOnUiThread(() -> {
                            successRunnable.run();
                            getDialog().cancel();
                        })).start();
                    } else {
                        password1.setError(activity.getString(R.string.messages_wrong_password));
                    }
                }
            } catch (Exception ex) {
                MessageHelper.printException(ex, R.mipmap.ic_launcher_round, activity);
            }
        });

        return view;
    }

    public static boolean checkDatabase() {
        try {
            List<Authentication> authenticationList = MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("", true);
            return authenticationList != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
