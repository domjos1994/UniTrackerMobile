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

package de.domjos.unibuggermobile.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Attachment;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.issues.Note;
import de.domjos.unitrackerlibrary.model.issues.Tag;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.tracker.AzureDevOps;
import de.domjos.unitrackerlibrary.services.tracker.Backlog;
import de.domjos.unitrackerlibrary.services.tracker.Bugzilla;
import de.domjos.unitrackerlibrary.services.tracker.Github;
import de.domjos.unitrackerlibrary.services.tracker.Jira;
import de.domjos.unitrackerlibrary.services.tracker.MantisBT;
import de.domjos.unitrackerlibrary.services.tracker.OpenProject;
import de.domjos.unitrackerlibrary.services.tracker.PivotalTracker;
import de.domjos.unitrackerlibrary.services.tracker.Redmine;
import de.domjos.unitrackerlibrary.services.tracker.SQLite;
import de.domjos.unitrackerlibrary.services.tracker.Tuleap;
import de.domjos.unitrackerlibrary.services.tracker.YouTrack;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tasks.LoaderTask;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.customwidgets.tokenizer.CommaTokenizer;
import de.domjos.unibuggermobile.settings.Settings;

public class Helper {
    public static final List<Authentication.Tracker> disabledBugTrackers =
            Arrays.asList(
                Authentication.Tracker.Github,
                Authentication.Tracker.TuLeap,
                Authentication.Tracker.AzureDevOps
            );

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String readStringFromRaw(int rawID, Context context) throws Exception {
        Resources res = context.getResources();
        InputStream in_s = res.openRawResource(rawID);

        byte[] b = new byte[in_s.available()];
        in_s.read(b);
        return new String(b);
    }

    static int getVersionCode(Context context) throws Exception {
        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return info.versionCode;
    }

    public static View getRowView(Context context, ViewGroup parent, int layout) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return inflater.inflate(layout, parent, false);
        }
        return new View(context);
    }

    public static IBugService getCurrentBugService(Context context) {
        return Helper.getCurrentBugService(MainActivity.GLOBALS.getSettings(context).getCurrentAuthentication(), context);
    }

    public static IBugService getCurrentBugService(Authentication authentication, Context context) {
        IBugService bugService = null;
        try {
            if (authentication != null) {
                switch (authentication.getTracker()) {
                    case MantisBT:
                        Settings settings = MainActivity.GLOBALS.getSettings(context);
                        bugService = new MantisBT(
                            authentication,
                            settings.isShowMantisBugsOfSubProjects(),
                            settings.isShowMantisFilter()
                        );
                        break;
                    case Bugzilla:
                        bugService = new Bugzilla(authentication);
                        break;
                    case YouTrack:
                        bugService = new YouTrack(authentication);
                        break;
                    case RedMine:
                        bugService = new Redmine(authentication);
                        break;
                    case Github:
                        bugService = new Github(authentication);
                        break;
                    case Jira:
                        bugService = new Jira(authentication);
                        break;
                    case PivotalTracker:
                        bugService = new PivotalTracker(authentication);
                        break;
                    case OpenProject:
                        bugService = new OpenProject(authentication);
                        break;
                    case Backlog:
                        bugService = new Backlog(authentication);
                        break;
                    case AzureDevOps:
                        bugService = new AzureDevOps(authentication);
                        break;
                    case TuLeap:
                        bugService = new Tuleap(authentication);
                        break;
                    default:
                        bugService = new SQLite(context, Helper.getVersionCode(context), authentication);
                        break;
                }
            } else {
                bugService = new SQLite(context, Helper.getVersionCode(context), new Authentication());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, context);
        }
        return bugService;
    }

    public static ArrayAdapter<String> setAdapter(Context context, String key) {
        if (context != null) {
            int spItem = R.layout.spinner_item;
            return new ArrayAdapter<>(context, spItem, ArrayHelper.getValues(context, key));
        }
        return null;
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (MainActivity.GLOBALS.getSettings(activity).isBlockMobile()) {
                return false;
            } else {
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }

    public static boolean isInWLan(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if(activeNetworkInfo!=null) {
                return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            }
        }
        return false;
    }

    public static void isStoragePermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("dd", "Permission is granted");
            } else {

                Log.v("dd", "Permission is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("dd", "Permission is granted");
        }
    }

    public static String showTagDialog(Activity activity, IBugService bugService, boolean show, Object pid) {
        String str_result = "";
        try {
            @SuppressLint("HandlerLeak")
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message mesg) {
                    throw new RuntimeException();
                }
            };

            AlertDialog.Builder tagDialogBuilder = new AlertDialog.Builder(activity);
            MultiAutoCompleteTextView txtTags = new MultiAutoCompleteTextView(activity);
            txtTags.setLayoutParams(new ViewGroup.LayoutParams(200, ViewGroup.LayoutParams.WRAP_CONTENT));
            ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1);
            txtTags.setAdapter(tagAdapter);
            txtTags.setTokenizer(new CommaTokenizer());
            tagAdapter.notifyDataSetChanged();

            LoaderTask loaderTask = new LoaderTask(activity, bugService, show, LoaderTask.Type.Tags);
            Object result = loaderTask.execute(pid).get();
            if (result instanceof List) {
                List lst = (List) result;
                for (Object item : lst) {
                    if (item instanceof Tag) {
                        tagAdapter.add(((Tag) item).getTitle());
                    }
                }
            }
            tagDialogBuilder.setView(txtTags);
            tagDialogBuilder.setNegativeButton(R.string.sys_cancel, (dialog, which) -> dialog.cancel());
            tagDialogBuilder.setPositiveButton(R.string.sys_save, ((dialog, which) -> handler.sendMessage(handler.obtainMessage())));
            tagDialogBuilder.create().show();

            try{ Looper.loop(); }
            catch(RuntimeException ignored){}
            str_result = txtTags.getText().toString();
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, activity);
        }
        return str_result;
    }

    public static void showPasswordDialog(Activity activity, boolean firstLogin, boolean changePassword, Runnable successRunnable) {
        try {
            Dialog pwdDialog = new Dialog(activity);
            pwdDialog.setContentView(R.layout.password_dialog);
            final TextView lblTitle = pwdDialog.findViewById(R.id.lblTitle);
            final EditText password1 = pwdDialog.findViewById(R.id.txtPassword1);
            final EditText password2 = pwdDialog.findViewById(R.id.txtPassword2);
            final Button cmdSubmit = pwdDialog.findViewById(R.id.cmdSubmit);
            pwdDialog.setCancelable(false);
            pwdDialog.setCanceledOnTouchOutside(false);
            if (!firstLogin || changePassword) {
                lblTitle.setText(R.string.pwd_title);
            } else {
                password2.setVisibility(View.GONE);
                lblTitle.setText(R.string.pwd_title_pwd);
            }
            if (MainActivity.GLOBALS.getPassword().isEmpty() || changePassword) {
                if(MainActivity.GLOBALS.getSettings(activity).isEncryptionEnabled()) {
                    pwdDialog.show();
                    new Thread(() -> {
                        while (pwdDialog.isShowing()) {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception ignored) {
                            }
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
            cmdSubmit.setOnClickListener(v -> {
                try {
                    if (!firstLogin || changePassword) {
                        if (password1.getText().toString().equals(password2.getText().toString())) {
                            if (password1.getText().toString().length() >= 4) {
                                password1.setTextColor(Color.GREEN);
                                password2.setTextColor(Color.GREEN);
                                MainActivity.GLOBALS.getSettings(activity).isFirstLogin(true);

                                new Thread(() -> activity.runOnUiThread(() -> {
                                    try {
                                        if (changePassword) {
                                            MainActivity.GLOBALS.getSqLiteGeneral().changePassword(password1.getText().toString());
                                        }
                                        MainActivity.GLOBALS.setPassword(password1.getText().toString());
                                        MainActivity.GLOBALS.setSqLiteGeneral(new SQLiteGeneral(activity, MainActivity.GLOBALS.getPassword()));
                                        if (Helper.checkDatabase()) {
                                            successRunnable.run();
                                            pwdDialog.cancel();
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
                        if(Helper.checkDatabase()) {
                            password1.setTextColor(Color.GREEN);
                            new Thread(() -> activity.runOnUiThread(() -> {
                                successRunnable.run();
                                pwdDialog.cancel();
                            })).start();
                        } else {
                            password1.setError(activity.getString(R.string.messages_wrong_password));
                        }
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, activity);
                }
            });
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, activity);
        }
    }

    @SuppressWarnings("unchecked")
    public static void showResolveDialog(Activity activity, String array, int position, Issue issue, IBugService bugService, Object pid, boolean show, Runnable runnable) {
        try {
            Dialog resolveDialog = new Dialog(activity);
            resolveDialog.setContentView(R.layout.resolve_dialog);
            final Spinner cmbState = resolveDialog.findViewById(R.id.cmbStatus);
            cmbState.setAdapter(Helper.setAdapter(activity, array));
            try {
                if (position != -1) {
                    cmbState.setSelection(position);
                }
            } catch (Exception ignored) {
            }
            final EditText txtDescription = resolveDialog.findViewById(R.id.txtComment);
            final ImageButton cmdSave = resolveDialog.findViewById(R.id.cmdResolve);

            cmdSave.setOnClickListener(v -> {
                try {
                    String noteContent = txtDescription.getText().toString();
                    if (!noteContent.isEmpty()) {
                        Note note = new Note();
                        note.setDescription(noteContent);
                        note.setTitle(noteContent);
                        note.setState(10, "öffentlich");
                        issue.getNotes().add(note);
                    }
                    issue.setStatus(ArrayHelper.getIdOfEnum(activity, cmbState, array), cmbState.getSelectedItem().toString());

                    IssueTask issueTask = new IssueTask(activity, bugService, pid, false, false, show, R.drawable.ic_bug_report_black_24dp);
                    issueTask.execute(issue).get();
                    resolveDialog.dismiss();
                    runnable.run();
                } catch (Exception ex) {
                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, activity);
                }
            });
            resolveDialog.setCancelable(true);
            resolveDialog.show();
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, activity);
        }
    }

    public static void showAttachmentDialog(Activity activity, List<Attachment> attachments) {
        try {
            AtomicInteger id = new AtomicInteger();
            Dialog attachmentDialog = new Dialog(activity);
            attachmentDialog.setContentView(R.layout.attachment_dialog);
            final ImageView iv = attachmentDialog.findViewById(R.id.ivCurrentAttachment);
            final ImageButton cmdPrevious = attachmentDialog.findViewById(R.id.cmdPrevious);
            final ImageButton cmdNext = attachmentDialog.findViewById(R.id.cmdNext);

            Helper.addAttachmentToImageView(activity, iv, attachments.get(id.get()));
            cmdPrevious.setOnClickListener(v -> {
                if (id.get() != 0) {
                    id.getAndDecrement();
                    Helper.addAttachmentToImageView(activity, iv, attachments.get(id.get()));
                }
            });
            cmdNext.setOnClickListener(v -> {
                if (id.get() != attachments.size() - 1) {
                    id.getAndIncrement();
                    Helper.addAttachmentToImageView(activity, iv, attachments.get(id.get()));
                }
            });
            attachmentDialog.show();
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, activity);
        }
    }

    public static void showWhatsNewDialog(Activity activity) {
        try {
            Dialog whatsNewDialog = new Dialog(activity);
            whatsNewDialog.setContentView(R.layout.whats_new_dialog);
            TextView lblTitle = whatsNewDialog.findViewById(R.id.lblWhatsNewTitle);
            TextView lblContent = whatsNewDialog.findViewById(R.id.lblWhatsNewContent);
            String version = Helper.getVersion(activity);

            lblTitle.setText(version);
            lblContent.setText(Html.fromHtml(Helper.getStringResourceByName(activity, "whats_new_" + version)));

            Settings settings = MainActivity.GLOBALS.getSettings(activity);

            if(!settings.getWhatsNewVersion().isEmpty()) {
                if(!settings.getWhatsNewVersion().equals(version)) {
                    whatsNewDialog.show();
                    settings.setWhatsNewVersion();
                }
            } else {
                whatsNewDialog.show();
                settings.setWhatsNewVersion();
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, activity);
        }
    }

    public static FilePickerDialog initFilePickerDialog(Activity activity, boolean selectDir, String[] extensions, String title) {
        DialogProperties dialogProperties = new DialogProperties();
        dialogProperties.selection_mode = DialogConfigs.SINGLE_MODE;
        dialogProperties.root = new File(DialogConfigs.DEFAULT_DIR);
        dialogProperties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        dialogProperties.offset = new File(DialogConfigs.DEFAULT_DIR);
        if(selectDir) {
            dialogProperties.selection_type = DialogConfigs.DIR_SELECT;
            dialogProperties.extensions = extensions;
        } else {
            dialogProperties.selection_type = DialogConfigs.FILE_SELECT;
            dialogProperties.extensions = extensions;
        }

        FilePickerDialog dialog = new FilePickerDialog(activity, dialogProperties);
        dialog.setCancelable(true);
        dialog.setTitle(title);
        return dialog;
    }

    private static String getStringResourceByName(Activity activity, String aString) {
        try {
            String packageName = activity.getPackageName();
            int resId = activity.getResources().getIdentifier(aString, "string", packageName);
            return activity.getString(resId);
        } catch (Exception ex) {
            return "";
        }
    }

    public static String getVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, context);
        }
        return "";
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void addAttachmentToImageView(Activity activity, ImageView iv, Attachment attachment) {
        if (attachment.getContentType().toLowerCase().contains("image") ||
                attachment.getFilename().toLowerCase().endsWith("png") ||
                attachment.getFilename().toLowerCase().endsWith("jpg") ||
                attachment.getFilename().toLowerCase().endsWith("jpeg") ||
                attachment.getFilename().toLowerCase().endsWith("bmp") ||
                attachment.getFilename().toLowerCase().endsWith("gif")) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(attachment.getContent(), 0, attachment.getContent().length);
            iv.setImageBitmap(bitmap);
        } else {
            iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_file_download_black_24dp));
        }

        iv.setOnClickListener(v -> {
            DialogProperties dialogProperties = new DialogProperties();
            dialogProperties.selection_mode = DialogConfigs.SINGLE_MODE;
            dialogProperties.root = new File(DialogConfigs.DEFAULT_DIR);
            dialogProperties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
            dialogProperties.offset = new File(DialogConfigs.DEFAULT_DIR);
            dialogProperties.selection_type = DialogConfigs.DIR_SELECT;
            dialogProperties.extensions = null;

            FilePickerDialog filePickerDialog = new FilePickerDialog(activity, dialogProperties);
            filePickerDialog.setCancelable(true);
            filePickerDialog.setDialogSelectionListener(files -> {
                try {
                    if (files != null) {
                        String path = files[0];
                        File file = new File(path + File.separatorChar + attachment.getFilename());
                        file.mkdirs();
                        if (file.createNewFile()) {
                            Helper.saveAttachment(file, attachment.getContent(), activity);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle(R.string.issues_context_attachment_overwrite);
                            builder.setPositiveButton(R.string.issues_context_attachment_overwrite_yes, (dialog, which) -> Helper.saveAttachment(file, attachment.getContent(), activity));
                            builder.setNegativeButton(R.string.issues_context_attachment_overwrite_no, (dialog, which) -> dialog.dismiss());
                            builder.create().show();
                        }
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, activity);
                }
            });
            filePickerDialog.show();
        });
    }

    private static void saveAttachment(File file, byte[] content, Activity activity) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(content);
            bos.flush();
            bos.close();
            MessageHelper.printMessage(activity.getString(R.string.issues_context_attachment_saved), R.mipmap.ic_launcher_round, activity);
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, activity);
        }
    }

    private static boolean checkDatabase() {
        try {
            List<Authentication> authenticationList = MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("", true);
            return authenticationList != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
