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
import android.view.View;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.SQLiteGeneral;
import de.domjos.unitrackerlibrary.tools.Notifications;

public class PasswordDialog extends AbstractDialog {
    private final Boolean firstLogin;
    private final Boolean changePassword;
    private final Runnable successRunnable;
    private TextInputLayout password1;
    private TextInputLayout password2;

    public PasswordDialog(Activity activity, boolean firstLogin, boolean changePassword, Runnable successRunnable) {
        super(activity, R.layout.password_dialog);

        this.firstLogin = firstLogin;
        this.changePassword = changePassword;
        this.successRunnable = successRunnable;

        this.setTitle(R.string.pwd_title);
        this.setCancelable(false);


        this.setOnSubmit(R.string.pwd_submit, (i,l) -> {
            try {
                if (!firstLogin || changePassword) {
                    if (Objects.requireNonNull(password1.getEditText()).getText().toString().equals(Objects.requireNonNull(password2.getEditText()).getText().toString())) {
                        if (password1.getEditText().getText().toString().length() >= 4) {
                            password1.getEditText().setTextColor(Color.GREEN);
                            password2.getEditText().setTextColor(Color.GREEN);
                            MainActivity.GLOBALS.getSettings(activity).isFirstLogin(true);

                            new Thread(() -> activity.runOnUiThread(() -> {
                                try {
                                    if (changePassword) {
                                        MainActivity.GLOBALS.getSqLiteGeneral().changePassword(password1.getEditText().getText().toString());
                                    }
                                    MainActivity.GLOBALS.setPassword(password1.getEditText().getText().toString());
                                    MainActivity.GLOBALS.setSqLiteGeneral(new SQLiteGeneral(activity, MainActivity.GLOBALS.getPassword()));
                                    if (Helper.checkDatabase()) {
                                        successRunnable.run();
                                        super.alertDialog.cancel();
                                    }
                                } catch (Exception ex) {
                                    Notifications.printException(activity,  ex, R.mipmap.ic_launcher_round);
                                }
                            })).start();
                        } else {
                            password2.setError(activity.getString(R.string.messages_passwords_too_small));
                        }
                    } else {
                        password2.setError(activity.getString(R.string.messages_passwords_dont_fit));
                    }
                } else {
                    MainActivity.GLOBALS.setPassword(Objects.requireNonNull(password1.getEditText()).getText().toString());
                    MainActivity.GLOBALS.setSqLiteGeneral(new SQLiteGeneral(activity, MainActivity.GLOBALS.getPassword()));
                    if(Helper.checkDatabase()) {
                        password1.getEditText().setTextColor(Color.GREEN);
                        new Thread(() -> activity.runOnUiThread(() -> {
                            successRunnable.run();
                            super.alertDialog.cancel();
                        })).start();
                    } else {
                        password1.setError(activity.getString(R.string.messages_wrong_password));
                    }
                }
            } catch (Exception ex) {
                Notifications.printException(activity,  ex, R.mipmap.ic_launcher_round);
            }
        });
    }

    @Override
    public void show() {
        try {
            if (MainActivity.GLOBALS.getPassword().isEmpty() || changePassword) {
                if(!MainActivity.GLOBALS.getSettings(activity).isEncryptionEnabled()) {
                    MainActivity.GLOBALS.setPassword(SQLiteGeneral.NO_PASS);
                    MainActivity.GLOBALS.setSqLiteGeneral(new SQLiteGeneral(activity, MainActivity.GLOBALS.getPassword()));
                    successRunnable.run();
                } else {
                    super.show();
                }
            } else {
                if(MainActivity.GLOBALS.getSqLiteGeneral() == null) {
                    super.show();
                } else {
                    successRunnable.run();
                }
            }
        } catch (Exception ex) {
            Notifications.printException(activity, ex, R.mipmap.ic_launcher_round);
        }
    }

    @Override
    protected void init(View view) {
        password1 = view.findViewById(R.id.txtPassword1);
        password2 = view.findViewById(R.id.txtPassword2);


        if (!this.firstLogin || this.changePassword) {
            super.setTitle(R.string.pwd_title);
        } else {
            password2.setVisibility(View.GONE);
            super.setTitle(R.string.pwd_title_pwd);
        }
    }
}
