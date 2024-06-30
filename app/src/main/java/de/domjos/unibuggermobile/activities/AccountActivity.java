/*
 * Copyright (C)  2019-2020 Domjos
 *  This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
 *
 *  UniTrackerMobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UniTrackerMobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggermobile.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableRow;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Arrays;
import java.util.List;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.services.authentication.OAuthHelper;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.customwidgets.utils.ConvertHelper;
import de.domjos.customwidgets.utils.Validator;
import de.domjos.unibuggermobile.R;
import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.customwidgets.widgets.swiperefreshdeletelist.SwipeRefreshDeleteList;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.IntentHelper;

public final class AccountActivity extends AbstractActivity {
    private static Authentication authentication;
    private final static String ACCOUNTS = "accounts";

    private SwipeRefreshDeleteList lvAccounts;
    private Spinner cmbAccountTracker, cmbAccountAuthentication;
    private ArrayAdapter<Authentication.Tracker> trackerAdapter;
    private ArrayAdapter<Authentication.Auth> authAdapter;
    private EditText txtAccountServer, txtAccountUserName, txtAccountPassword,
            txtAccountAPI, txtAccountImageURL, txtAccountDescription, txtAccountExtended;
    private ImageView ivAccountServer;
    private AutoCompleteTextView txtAccountTitle;
    private CheckBox chkAccountGuest;
    private ImageButton cmdAccountImageGallery;

    private BottomNavigationView navigationView;

    private Authentication currentAccount;
    private Validator accountValidator;

    private TableRow rowAuthentication;

    public AccountActivity() {
        super(R.layout.account_activity);
    }

    @Override
    protected void initActions() {

        this.lvAccounts.setOnClickListener((SwipeRefreshDeleteList.SingleClickListener) listObject -> {
            this.txtAccountPassword.setTransformationMethod(new PasswordTransformationMethod());
            if (listObject != null) {
                this.currentAccount = (Authentication) listObject.getObject();
            }
            objectToControls();
            manageControls(false, false, true);
        });

        this.lvAccounts.setOnReloadListener(this::reload);

        this.lvAccounts.setOnDeleteListener(listObject -> {
            MainActivity.GLOBALS.getSqLiteGeneral().delete(AccountActivity.ACCOUNTS, "ID", listObject.getId());
            manageControls(false, true, false);
        });

        this.cmbAccountTracker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                resetFieldsOnChange();

                Authentication.Tracker item = trackerAdapter.getItem(position);
                if (item != null) {
                    fillAuthByTracker(item);
                    switch (item) {
                        case Local:
                            txtAccountServer.setText(Authentication.Tracker.Local.name());
                            break;
                        case Github:
                            txtAccountPassword.setVisibility(View.GONE);
                            txtAccountAPI.setVisibility(View.GONE);
                            txtAccountServer.setText(getString(R.string.accounts_github_server));
                            txtAccountServer.setVisibility(View.GONE);
                            ivAccountServer.setVisibility(View.GONE);
                            txtAccountAPI.setHint(R.string.accounts_github_client_secret);
                            break;
                        case Bugzilla:
                        case AzureDevOps:
                            if (chkAccountGuest.isChecked()) {
                                accountValidator.removeValidator(txtAccountAPI);
                            } else {
                                accountValidator.addEmptyValidator(txtAccountAPI);
                            }
                            break;
                        case YouTrack:
                            txtAccountExtended.setVisibility(View.VISIBLE);
                            txtAccountExtended.setHint(R.string.accounts_youtrack_hub);
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
                        case OpenProject:
                            String user = "apikey";
                            txtAccountUserName.setText(user);
                            txtAccountUserName.setHint(txtAccountAPI.getHint());
                            txtAccountAPI.setVisibility(View.GONE);
                            accountValidator.removeValidator(txtAccountAPI);
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

        this.txtAccountServer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().trim().equals(Authentication.Tracker.Local.name()) || editable.toString().trim().startsWith("https://")) {
                    ivAccountServer.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_lock_close));
                } else {
                    ivAccountServer.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_lock_open));
                }
            }
        });

        this.txtAccountImageURL.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                new Thread(() -> {
                    try {
                        Drawable drawable = ConvertHelper.convertStringToImage(s.toString());
                        if (drawable != null) {
                            runOnUiThread(() -> cmdAccountImageGallery.setImageDrawable(drawable));
                        }
                    } catch (Exception ignored) {
                    }
                }).start();
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

        this.txtAccountTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim().toLowerCase();

                for (int i = 0; i <= trackerAdapter.getCount() - 1; i++) {
                    if (trackerAdapter.getItem(i) != null) {
                        Authentication.Tracker item = trackerAdapter.getItem(i);
                        if (item != null) {
                            String itemString = item.name().trim().toLowerCase();
                            if (text.contains(itemString)) {
                                cmbAccountTracker.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
        });

        this.txtAccountPassword.setOnLongClickListener(view -> {
            if(this.txtAccountPassword.getTransformationMethod()!=null) {
                this.txtAccountPassword.setTransformationMethod(null);
            } else {
                this.txtAccountPassword.setTransformationMethod(new PasswordTransformationMethod());
            }
            return false;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Bitmap bitmap = IntentHelper.getImageFromGallery(requestCode, resultCode, data, this.getApplicationContext());
            this.cmdAccountImageGallery.setImageBitmap(bitmap);
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, AccountActivity.this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(AccountActivity.authentication != null) {
            new Thread(() -> {
                try {
                    OAuthHelper.getAccessToken(getIntent(), AccountActivity.this, AccountActivity.authentication, authentication1 -> {
                        MainActivity.GLOBALS.getSqLiteGeneral().insertOrUpdateAccount(authentication1);
                        AccountActivity.authentication = null;
                    });
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }).start();
        }
    }

    @Override
    protected void initControls() {
        this.navigationView = this.findViewById(R.id.nav_view);
        this.navigationView.setOnNavigationItemSelectedListener(menuItem -> {
            if(menuItem.getItemId() == R.id.navAdd) {
                this.manageControls(true, true, false);
            } else if(menuItem.getItemId() == R.id.navEdit) {
                this.manageControls(true, false, false);
            } else if(menuItem.getItemId() == R.id.navDelete) {
                MainActivity.GLOBALS.getSqLiteGeneral().delete(AccountActivity.ACCOUNTS, "ID", this.currentAccount.getId());
                this.manageControls(false, true, false);
                this.reload();
            } else if(menuItem.getItemId() == R.id.navCancel) {
                this.manageControls(false, true, false);
            } else if(menuItem.getItemId() == R.id.navSave) {
                try {
                    if (this.accountValidator.getState()) {
                        this.controlsToObject();
                        if(this.accountValidator.checkDuplicatedEntry(this.currentAccount.getTitle(), this.currentAccount.getId(), this.lvAccounts.getAdapter().getList())) {
                            if (currentAccount.getTracker() == Authentication.Tracker.Github) {
                                currentAccount.setAuthentication(Authentication.Auth.OAUTH);
                            }

                            new Thread(() -> {
                                try {
                                    if (!chkAccountGuest.isChecked()) {
                                        if (currentAccount.getAuthentication() == Authentication.Auth.OAUTH && this.txtAccountAPI.getText().toString().isEmpty()) {
                                            if(currentAccount.getTracker() == Authentication.Tracker.Github) {
                                                AccountActivity.authentication = currentAccount;
                                                OAuthHelper.startServiceConfig(this, currentAccount);
                                            }
                                        }
                                    }
                                    IBugService<?> bugService = Helper.getCurrentBugService(this.currentAccount, this.getApplicationContext());
                                    if (chkAccountGuest.isChecked() || bugService.testConnection()) {
                                        AccountActivity.this.runOnUiThread(() -> {
                                            MessageHelper.printMessage(this.getString(R.string.accounts_connection_successfully), R.mipmap.ic_launcher_round, AccountActivity.this);
                                            MainActivity.GLOBALS.getSqLiteGeneral().insertOrUpdateAccount(this.currentAccount);
                                            this.manageControls(false, true, false);
                                            this.reload();
                                        });
                                    } else {
                                        AccountActivity.this.runOnUiThread(() -> MessageHelper.printMessage(this.getString(R.string.accounts_connection_not_successfully), R.mipmap.ic_launcher_round, AccountActivity.this));
                                    }
                                } catch (Exception ex) {
                                    Log.v("Exception", ex.toString());
                                    String msg = ex.getMessage();
                                    if (msg != null) {
                                        if (msg.contains("PHP SOAP")) {
                                            AccountActivity.this.runOnUiThread(() -> MessageHelper.printMessage(this.getString(R.string.messages_no_soap), R.mipmap.ic_launcher_round, AccountActivity.this));
                                        } else {
                                            AccountActivity.this.runOnUiThread(() -> MessageHelper.printException(ex, R.mipmap.ic_launcher_round, AccountActivity.this));
                                        }
                                    }
                                }
                            }).start();
                        } else {
                            super.createSnackBar(this.accountValidator.getResult());
                        }
                    } else {
                        super.createSnackBar(this.accountValidator.getResult());
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, AccountActivity.this);
                }
            }
            return false;
        });

        this.lvAccounts = this.findViewById(R.id.lvAccounts);

        this.cmbAccountTracker = this.findViewById(R.id.cmbAccountTracker);
        this.trackerAdapter = new ArrayAdapter<>(this.getApplicationContext(), R.layout.spinner_item);
        this.cmbAccountTracker.setAdapter(this.trackerAdapter);
        this.trackerAdapter.notifyDataSetChanged();

        this.cmbAccountAuthentication = this.findViewById(R.id.cmbAccountAuthentication);
        this.authAdapter = new ArrayAdapter<>(this.getApplicationContext(), R.layout.spinner_item);
        this.cmbAccountAuthentication.setAdapter(this.authAdapter);
        this.authAdapter.notifyDataSetChanged();
        this.rowAuthentication = this.findViewById(R.id.rowAuthentication);

        this.chkAccountGuest = this.findViewById(R.id.chkAccountGuest);
        this.txtAccountTitle = this.findViewById(R.id.txtAccountTitle);
        List<Authentication.Tracker> ls = Arrays.asList(Authentication.Tracker.values());
        this.txtAccountTitle.setAdapter(new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_dropdown_item_1line, ls));
        this.txtAccountServer = this.findViewById(R.id.txtAccountServer);
        this.ivAccountServer = this.findViewById(R.id.ivAccountServer);
        this.txtAccountUserName = this.findViewById(R.id.txtAccountUserName);
        this.txtAccountPassword = this.findViewById(R.id.txtAccountPassword);
        this.txtAccountAPI = this.findViewById(R.id.txtAccountAPI);
        this.txtAccountImageURL = this.findViewById(R.id.txtAccountImageURL);
        this.txtAccountExtended = this.findViewById(R.id.txtAccountExtended);
        this.txtAccountDescription = this.findViewById(R.id.txtAccountDescription);
        this.cmdAccountImageGallery = this.findViewById(R.id.cmdAccountImageGallery);

        this.txtAccountServer.setText(Authentication.Tracker.Local.name());

        IntentHelper.loadAd(this);
    }

    private void fillAuthByTracker(Authentication.Tracker tracker) {
        this.authAdapter.clear();
        this.authAdapter.addAll(Authentication.Auth.values());
        switch (tracker) {
            case MantisBT:
                this.authAdapter.remove(Authentication.Auth.OAUTH);
                this.authAdapter.remove(Authentication.Auth.API_KEY);
                break;
            case YouTrack:
                this.authAdapter.remove(Authentication.Auth.OAUTH);
                this.authAdapter.remove(Authentication.Auth.Basic);
                break;
            case Github:
                this.authAdapter.remove(Authentication.Auth.Basic);
                this.authAdapter.remove(Authentication.Auth.API_KEY);
                break;
        }
    }

    @Override
    protected void initValidator() {
        this.accountValidator = new Validator(this.getApplicationContext(), R.mipmap.ic_launcher_round);
        this.accountValidator.addEmptyValidator(this.txtAccountTitle);
        this.accountValidator.addEmptyValidator(this.txtAccountServer);
    }

    @Override
    protected void reload() {
        this.lvAccounts.getAdapter().clear();
        for (Authentication authentication : MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("")) {
            BaseDescriptionObject listObject = new BaseDescriptionObject();
            listObject.setObject(authentication);
            listObject.setId(Integer.parseInt(String.valueOf(authentication.getId())));
            listObject.setCover(authentication.getCover());
            listObject.setTitle(authentication.getTitle());
            listObject.setDescription(authentication.getDescription());
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
        this.ivAccountServer.setEnabled(editMode);
        this.txtAccountUserName.setEnabled(editMode);
        this.txtAccountPassword.setEnabled(editMode);
        this.txtAccountAPI.setEnabled(editMode);
        this.txtAccountImageURL.setEnabled(editMode);
        this.txtAccountDescription.setEnabled(editMode);
        this.cmdAccountImageGallery.setEnabled(editMode);
        this.cmbAccountTracker.setEnabled(editMode);
        this.cmbAccountAuthentication.setEnabled(editMode);
        this.chkAccountGuest.setEnabled(editMode);
        this.txtAccountExtended.setEnabled(editMode);

        if (reset) {
            this.txtAccountPassword.setTransformationMethod(new PasswordTransformationMethod());
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
            if (this.currentAccount.getAuthentication() !=null) {
                this.cmbAccountAuthentication.setSelection(this.authAdapter.getPosition(this.currentAccount.getAuthentication()));
            } else {
                this.cmbAccountAuthentication.setSelection(this.authAdapter.getPosition(Authentication.Auth.Basic));
            }
            if (this.currentAccount.getCover() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(this.currentAccount.getCover(), 0, this.currentAccount.getCover().length);
                this.cmdAccountImageGallery.setImageBitmap(bitmap);
            } else {
                this.cmdAccountImageGallery.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.icon_accounts, this.getTheme()));
            }

            if(this.currentAccount.getTracker() == Authentication.Tracker.YouTrack) {
                this.txtAccountExtended.setText(this.currentAccount.getHints().get("hub"));
            }
        }
    }

    private void controlsToObject() {
        if (this.currentAccount != null) {
            if(this.currentAccount.getId() == null) {
                this.currentAccount.setId(0L);
            }
            this.currentAccount.setTitle(this.txtAccountTitle.getText().toString());
            this.currentAccount.setServer(this.txtAccountServer.getText().toString());
            this.currentAccount.setUserName(this.txtAccountUserName.getText().toString());
            this.currentAccount.setPassword(this.txtAccountPassword.getText().toString());
            this.currentAccount.setAPIKey(this.txtAccountAPI.getText().toString());
            this.currentAccount.setDescription(this.txtAccountDescription.getText().toString());
            this.currentAccount.setTracker(this.trackerAdapter.getItem(this.cmbAccountTracker.getSelectedItemPosition()));
            this.currentAccount.setGuest(this.chkAccountGuest.isChecked());
            //this.currentAccount.setAuthentication(this.authAdapter.getItem(this.cmbAccountAuthentication.getSelectedItemPosition()));
            try {
                if (this.cmdAccountImageGallery.getDrawable() instanceof BitmapDrawable) {
                    this.currentAccount.setCover(ConvertHelper.convertDrawableToByteArray(this.cmdAccountImageGallery.getDrawable()));
                } else {
                    this.currentAccount.setCover(null);
                }
            } catch (Exception ignored) {}
            if(this.currentAccount.getTracker()== Authentication.Tracker.YouTrack) {
                String hub = this.txtAccountExtended.getText().toString();
                if(!hub.trim().isEmpty()) {
                    this.currentAccount.getHints().put("hub", hub.trim());
                }
            }
        }
    }

    private void resetFieldsOnChange() {
        // reset server
        this.txtAccountServer.setVisibility(View.VISIBLE);
        this.txtAccountServer.setText("");
        this.ivAccountServer.setVisibility(View.VISIBLE);
        this.ivAccountServer.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_lock_open));

        // reset user
        this.txtAccountUserName.setHint(R.string.accounts_user);
        this.txtAccountUserName.setVisibility(View.VISIBLE);

        // reset password
        this.txtAccountPassword.setHint(R.string.accounts_pwd);
        this.txtAccountPassword.setVisibility(View.VISIBLE);

        // reset extended
        this.txtAccountExtended.setHint(R.string.accounts);
        this.txtAccountExtended.setVisibility(View.GONE);

        // reset api
        this.accountValidator.removeValidator(this.txtAccountAPI);
        this.txtAccountAPI.setHint(R.string.accounts_api);
        this.txtAccountAPI.setVisibility(View.VISIBLE);

        if(this.currentAccount!=null) {
            String sever = this.currentAccount.getServer();
            if(sever!=null) {
               if(!sever.trim().isEmpty()) {
                   this.txtAccountServer.setText(sever.trim());
               }
            }
        }
    }

    @Override
    protected void hideExperimentalFeatures() {
        this.rowAuthentication.setVisibility(View.GONE);

        for(Authentication.Tracker tracker : Authentication.Tracker.values()) {
            boolean trackerDisabled = false;
            for(Authentication.Tracker disabledTracker : Helper.disabledBugTrackers) {
                if(tracker == disabledTracker) {
                    trackerDisabled = true;
                    break;
                }
            }
            if(!trackerDisabled) {
                this.trackerAdapter.add(tracker);
            }
        }
    }
}
