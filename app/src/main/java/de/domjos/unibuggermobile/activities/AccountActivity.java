/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniBuggerMobile <https://github.com/domjos1994/UniBuggerMobile>.
 *
 * UniBuggerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniBuggerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniBuggerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggermobile.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.BottomNavigationView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.utils.Converter;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.adapter.ListAdapter;
import de.domjos.unibuggermobile.adapter.ListObject;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.IntentHelper;
import de.domjos.unibuggermobile.helper.Validator;

public final class AccountActivity extends AbstractActivity {
    private ListView lvAccounts;
    private ListAdapter listAdapter;
    private Spinner cmbAccountTracker;
    private ArrayAdapter<Authentication.Tracker> trackerAdapter;
    private EditText txtAccountTitle, txtAccountServer, txtAccountUserName, txtAccountPassword,
            txtAccountAPI, txtAccountImageURL, txtAccountDescription;
    private ImageButton cmdAccountImageGallery;

    private BottomNavigationView navigationView;

    private Authentication currentAccount;
    private Validator accountValidator;

    public AccountActivity() {
        super(R.layout.account_activity);
    }

    @Override
    protected void initActions() {

        this.lvAccounts.setOnItemClickListener((parent, view, position, id) -> {
            ListObject listObject = this.listAdapter.getItem(position);
            if (listObject != null) {
                this.currentAccount = (Authentication) listObject.getDescriptionObject();
                this.accountValidator.addDuplicatedEntry(this.txtAccountTitle, "accounts", "title", this.currentAccount.getId());
            }
            this.objectToControls();
            this.manageControls(false, false, true);
        });

        this.cmbAccountTracker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (trackerAdapter.getItem(position) == Authentication.Tracker.Local) {
                    if (txtAccountServer.getText().toString().isEmpty()) {
                        txtAccountServer.setText(Authentication.Tracker.Local.name());
                    }
                } else if (trackerAdapter.getItem(position) == Authentication.Tracker.Github) {
                    txtAccountServer.setText(getString(R.string.accounts_github_server));
                } else {
                    if (txtAccountServer.getText().toString().equals(Authentication.Tracker.Local.name())) {
                        txtAccountServer.setText("");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.cmdAccountImageGallery.setOnClickListener(v -> IntentHelper.openGalleryIntent(AccountActivity.this));

        this.txtAccountImageURL.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                new Thread(() -> {
                    try {
                        Drawable drawable = Converter.convertStringToImage(s.toString());
                        if (drawable != null) {
                            runOnUiThread(() -> cmdAccountImageGallery.setImageDrawable(drawable));
                        }
                    } catch (Exception ignored) {
                    }
                }).run();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            Bitmap bitmap = IntentHelper.getImageFromGallery(requestCode, resultCode, data, this.getApplicationContext());
            this.cmdAccountImageGallery.setImageBitmap(bitmap);
        } catch (Exception ex) {
            MessageHelper.printException(ex, AccountActivity.this);
        }
    }

    @Override
    protected void initControls() {
        this.navigationView = this.findViewById(R.id.nav_view);
        this.navigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navAdd:
                    this.manageControls(true, true, false);
                    break;
                case R.id.navEdit:
                    this.manageControls(true, false, false);
                    break;
                case R.id.navDelete:
                    MainActivity.GLOBALS.getSqLiteGeneral().delete("accounts", "ID", this.currentAccount.getId());
                    this.manageControls(false, true, false);
                    this.reload();
                    break;
                case R.id.navCancel:
                    this.manageControls(false, true, false);
                    break;
                case R.id.navSave:
                    try {
                        if (this.accountValidator.getState()) {
                            this.controlsToObject();

                            new Thread(() -> {
                                try {
                                    IBugService bugService = Helper.getCurrentBugService(this.currentAccount, this.getApplicationContext());
                                    if (bugService.testConnection()) {
                                        AccountActivity.this.runOnUiThread(() -> {
                                            MessageHelper.printMessage(this.getString(R.string.accounts_connection_successfully), AccountActivity.this);
                                            MainActivity.GLOBALS.getSqLiteGeneral().insertOrUpdateAccount(this.currentAccount);
                                            this.manageControls(false, true, false);
                                            this.reload();
                                        });
                                    } else {
                                        AccountActivity.this.runOnUiThread(() -> MessageHelper.printMessage(this.getString(R.string.accounts_connection_not_successfully), AccountActivity.this));
                                    }
                                } catch (Exception ex) {
                                    AccountActivity.this.runOnUiThread(() -> MessageHelper.printException(ex, AccountActivity.this));
                                }
                            }).start();
                        }
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, AccountActivity.this);
                    }
                    break;
            }
            return false;
        });

        this.lvAccounts = this.findViewById(R.id.lvAccounts);
        this.listAdapter = new ListAdapter(this.getApplicationContext(), R.drawable.ic_account_circle_black_24dp);
        this.lvAccounts.setAdapter(this.listAdapter);
        this.listAdapter.notifyDataSetChanged();

        this.cmbAccountTracker = this.findViewById(R.id.cmbAccountTracker);
        this.trackerAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, Authentication.Tracker.values());
        this.cmbAccountTracker.setAdapter(this.trackerAdapter);
        this.trackerAdapter.notifyDataSetChanged();

        this.txtAccountTitle = this.findViewById(R.id.txtAccountTitle);
        this.txtAccountServer = this.findViewById(R.id.txtAccountServer);
        this.txtAccountUserName = this.findViewById(R.id.txtAccountUserName);
        this.txtAccountPassword = this.findViewById(R.id.txtAccountPassword);
        this.txtAccountAPI = this.findViewById(R.id.txtAccountAPI);
        this.txtAccountImageURL = this.findViewById(R.id.txtAccountImageURL);
        this.txtAccountDescription = this.findViewById(R.id.txtAccountDescription);
        this.cmdAccountImageGallery = this.findViewById(R.id.cmdAccountImageGallery);

        this.txtAccountServer.setText(Authentication.Tracker.Local.name());
    }

    @Override
    protected void initValidators() {
        this.accountValidator = new Validator(this.getApplicationContext());
        this.accountValidator.addEmptyValidator(this.txtAccountTitle);
        this.accountValidator.addEmptyValidator(this.txtAccountServer);
        this.accountValidator.addDuplicatedEntry(this.txtAccountTitle, "accounts", "title", 0);
    }

    @Override
    protected void reload() {
        this.listAdapter.clear();
        for (Authentication authentication : MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("")) {
            ListObject listObject = new ListObject(this.getApplicationContext(), authentication.getCover(), authentication);
            this.listAdapter.add(listObject);
        }
    }

    @Override
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {
        this.navigationView.getMenu().getItem(0).setEnabled(!editMode);
        this.navigationView.getMenu().getItem(1).setEnabled(!editMode && selected);
        this.navigationView.getMenu().getItem(2).setEnabled(!editMode && selected);
        this.navigationView.getMenu().getItem(3).setEnabled(editMode);
        this.navigationView.getMenu().getItem(4).setEnabled(editMode);

        this.lvAccounts.setEnabled(!editMode);
        this.txtAccountTitle.setEnabled(editMode);
        this.txtAccountServer.setEnabled(editMode);
        this.txtAccountUserName.setEnabled(editMode);
        this.txtAccountPassword.setEnabled(editMode);
        this.txtAccountAPI.setEnabled(editMode);
        this.txtAccountImageURL.setEnabled(editMode);
        this.txtAccountDescription.setEnabled(editMode);
        this.cmdAccountImageGallery.setEnabled(editMode);
        this.cmbAccountTracker.setEnabled(editMode);

        if (reset) {
            this.currentAccount = new Authentication();
            this.objectToControls();
        }
    }

    private void objectToControls() {
        if (this.currentAccount != null) {
            this.txtAccountTitle.setText(this.currentAccount.getTitle());
            this.txtAccountServer.setText(this.currentAccount.getServer());
            this.txtAccountUserName.setText(this.currentAccount.getUserName());
            this.txtAccountPassword.setText(this.currentAccount.getPassword());
            this.txtAccountAPI.setText(this.currentAccount.getAPIKey());
            this.txtAccountDescription.setText(this.currentAccount.getDescription());
            if (this.currentAccount.getTracker() != null) {
                this.cmbAccountTracker.setSelection(this.trackerAdapter.getPosition(this.currentAccount.getTracker()));
            } else {
                this.cmbAccountTracker.setSelection(this.trackerAdapter.getPosition(Authentication.Tracker.Local));
                this.txtAccountServer.setText(Authentication.Tracker.Local.name());
            }
            if (this.currentAccount.getCover() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(this.currentAccount.getCover(), 0, this.currentAccount.getCover().length);
                this.cmdAccountImageGallery.setImageBitmap(bitmap);
            } else {
                this.cmdAccountImageGallery.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_account_circle_black_24dp));
            }
        }
    }

    private void controlsToObject() {
        if (this.currentAccount != null) {
            this.currentAccount.setTitle(this.txtAccountTitle.getText().toString());
            this.currentAccount.setServer(this.txtAccountServer.getText().toString());
            this.currentAccount.setUserName(this.txtAccountUserName.getText().toString());
            this.currentAccount.setPassword(this.txtAccountPassword.getText().toString());
            this.currentAccount.setAPIKey(this.txtAccountAPI.getText().toString());
            this.currentAccount.setDescription(this.txtAccountDescription.getText().toString());
            this.currentAccount.setTracker(this.trackerAdapter.getItem(this.cmbAccountTracker.getSelectedItemPosition()));
            if (this.cmdAccountImageGallery.getDrawable() instanceof BitmapDrawable) {
                this.currentAccount.setCover(Converter.convertDrawableToByteArray(this.cmdAccountImageGallery.getDrawable()));
            } else {
                this.currentAccount.setCover(null);
            }
        }
    }
}
