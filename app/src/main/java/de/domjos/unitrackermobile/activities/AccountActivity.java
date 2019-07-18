/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
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
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.ListObject;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.utils.Converter;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.custom.AbstractActivity;
import de.domjos.unitrackermobile.custom.SwipeRefreshDeleteList;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.helper.IntentHelper;
import de.domjos.unitrackermobile.helper.Validator;

public final class AccountActivity extends AbstractActivity {
    private SwipeRefreshDeleteList lvAccounts;
    private Spinner cmbAccountTracker;
    private ArrayAdapter<Authentication.Tracker> trackerAdapter;
    private EditText txtAccountServer, txtAccountUserName, txtAccountPassword,
            txtAccountAPI, txtAccountImageURL, txtAccountDescription;
    private AutoCompleteTextView txtAccountTitle;
    private CheckBox chkAccountGuest;
    private ImageButton cmdAccountImageGallery;

    private BottomNavigationView navigationView;

    private Authentication currentAccount;
    private Validator accountValidator;

    static final String ON_BOARDING = "onBoarding";

    public AccountActivity() {
        super(R.layout.account_activity);
    }

    @Override
    protected void initActions() {
        this.lvAccounts.click(new SwipeRefreshDeleteList.ClickListener() {
            @Override
            public void onClick(ListObject listObject) {
                if (listObject != null) {
                    currentAccount = (Authentication) listObject.getDescriptionObject();
                    accountValidator.addDuplicatedEntry(txtAccountTitle, "accounts", "title", currentAccount.getId());
                }
                objectToControls();
                manageControls(false, false, true);
            }
        });

        this.lvAccounts.reload(new SwipeRefreshDeleteList.ReloadListener() {
            @Override
            public void onReload() {
                reload();
            }
        });

        this.lvAccounts.deleteItem(new SwipeRefreshDeleteList.DeleteListener() {
            @Override
            public void onDelete(ListObject listObject) {
                MainActivity.GLOBALS.getSqLiteGeneral().delete("accounts", "ID", listObject.getDescriptionObject().getId());
                manageControls(false, true, false);
                reload();
            }
        });

        this.cmbAccountTracker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (txtAccountServer.getText().toString().equals(Authentication.Tracker.Local.name())) {
                    txtAccountServer.setText("");
                }
                txtAccountAPI.setVisibility(View.VISIBLE);

                Authentication.Tracker item = trackerAdapter.getItem(position);
                if (item != null) {
                    switch (item) {
                        case Local:
                            if (txtAccountServer.getText().toString().isEmpty()) {
                                txtAccountServer.setText(Authentication.Tracker.Local.name());
                            }
                            break;
                        case Github:
                            txtAccountServer.setText(getString(R.string.accounts_github_server));
                            break;
                        case YouTrack:
                            if (chkAccountGuest.isChecked()) {
                                accountValidator.removeValidator(txtAccountAPI);
                            } else {
                                accountValidator.addEmptyValidator(txtAccountAPI);
                            }
                            break;
                        case PivotalTracker:
                            txtAccountServer.setText(R.string.accounts_pivotal_server);
                            if (chkAccountGuest.isChecked()) {
                                accountValidator.removeValidator(txtAccountAPI);
                            } else {
                                accountValidator.addEmptyValidator(txtAccountAPI);
                            }
                            break;
                        case Bugzilla:
                            if (chkAccountGuest.isChecked()) {
                                accountValidator.removeValidator(txtAccountAPI);
                            } else {
                                accountValidator.addEmptyValidator(txtAccountAPI);
                            }
                            break;
                        /*case Tuleap:
                            if (chkAccountGuest.isChecked()) {
                                accountValidator.removeValidator(txtAccountAPI);
                            } else {
                                accountValidator.addEmptyValidator(txtAccountAPI);
                            }
                            break;*/
                        case OpenProject:
                            String user = "apikey";
                            txtAccountUserName.setText(user);
                            txtAccountUserName.setHint(txtAccountAPI.getHint());
                            txtAccountAPI.setVisibility(View.GONE);
                            break;
                        default:
                            accountValidator.removeValidator(txtAccountAPI);
                            break;
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

        this.chkAccountGuest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (trackerAdapter.getItem(this.cmbAccountTracker.getSelectedItemPosition()) == Authentication.Tracker.YouTrack) {
                if (isChecked) {
                    accountValidator.removeValidator(this.txtAccountAPI);
                } else {
                    accountValidator.addEmptyValidator(this.txtAccountAPI);
                }
            } else {
                accountValidator.removeValidator(txtAccountAPI);
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
                                    if (chkAccountGuest.isChecked() || bugService.testConnection()) {
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
                                    if (ex.getMessage().contains("PHP SOAP")) {
                                        AccountActivity.this.runOnUiThread(() -> MessageHelper.printMessage(this.getString(R.string.messages_no_soap), AccountActivity.this));
                                    } else {
                                        AccountActivity.this.runOnUiThread(() -> MessageHelper.printException(ex, AccountActivity.this));
                                    }
                                }
                            }).start();
                        } else {
                            MessageHelper.printMessage(this.getString(R.string.validator_no_success), this.getApplicationContext());
                        }
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, AccountActivity.this);
                    }
                    break;
            }
            return false;
        });

        this.lvAccounts = this.findViewById(R.id.lvAccounts);
        this.cmbAccountTracker = this.findViewById(R.id.cmbAccountTracker);
        this.trackerAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item, Authentication.Tracker.values());
        this.cmbAccountTracker.setAdapter(this.trackerAdapter);
        this.trackerAdapter.notifyDataSetChanged();

        this.chkAccountGuest = this.findViewById(R.id.chkAccountGuest);
        this.txtAccountTitle = this.findViewById(R.id.txtAccountTitle);
        List<Authentication.Tracker> ls = Arrays.asList(Authentication.Tracker.values());
        this.txtAccountTitle.setAdapter(new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_list_item_1, ls));
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
        this.lvAccounts.getAdapter().clear();
        for (Authentication authentication : MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("")) {
            ListObject listObject = new ListObject(this.getApplicationContext(), authentication.getCover(), authentication);
            this.lvAccounts.getAdapter().add(listObject);
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
        this.chkAccountGuest.setEnabled(editMode);

        if (reset) {
            this.currentAccount = new Authentication();
            this.chkAccountGuest.setChecked(false);
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
            this.chkAccountGuest.setChecked(this.currentAccount.isGuest());
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
            this.currentAccount.setGuest(this.chkAccountGuest.isChecked());
            if (this.cmdAccountImageGallery.getDrawable() instanceof BitmapDrawable) {
                this.currentAccount.setCover(Converter.convertDrawableToByteArray(this.cmdAccountImageGallery.getDrawable()));
            } else {
                this.currentAccount.setCover(null);
            }
        }
    }
}
