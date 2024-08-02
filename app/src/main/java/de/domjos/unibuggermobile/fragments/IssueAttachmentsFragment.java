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

package de.domjos.unibuggermobile.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import de.domjos.unitrackerlibrary.tools.Validator;
import de.domjos.unitrackerlibrary.custom.SwipeRefreshDeleteList;
import de.domjos.unitrackerlibrary.model.BaseDescriptionObject;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Attachment;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.IntentHelper;
import de.domjos.unitrackerlibrary.tools.ConvertHelper;
import de.domjos.unitrackerlibrary.tools.Notifications;

/**
 * A placeholder fragment containing a simple view.
 * @noinspection rawtypes
 */
public final class IssueAttachmentsFragment extends AbstractFragment {
    private SwipeRefreshDeleteList lvIssueAttachments;
    private ImageButton cmdIssueAttachmentAdd;
    private ImageButton cmdIssueAttachmentPhoto;
    private IBugService<?> bugService;

    private View root;
    private Issue<?> issue;
    private boolean editMode;

    ActivityResultLauncher<Intent> pick_file = null;
    ActivityResultLauncher<Intent> gallery = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.pick_file = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            assert result.getData() != null;
                            Uri imageUri = result.getData().getData();
                            if (imageUri != null) {
                                if (getContext() != null) {
                                    InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                                    if (imageStream != null) {
                                        Attachment<?> attachment = new Attachment<>();
                                        attachment.setDownloadUrl(imageUri.getPath());
                                        attachment.setFilename(this.getFileName(imageUri));
                                        attachment.setContentType(result.getData().getType());
                                        attachment.setContent(ConvertHelper.convertStreamToByteArray(imageStream));
                                        BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                                        baseDescriptionObject.setObject(attachment);
                                        baseDescriptionObject.setTitle(attachment.getTitle());
                                        baseDescriptionObject.setDescription(attachment.getDescription());
                                        this.lvIssueAttachments.getAdapter().add(baseDescriptionObject);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Log.e("Error", ex.getLocalizedMessage(), ex);
                        }
                    }
                });

        this.gallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            if (result.getData() != null) {
                                if (this.getContext() != null) {
                                    Attachment<?> attachment = new Attachment<>();
                                    attachment.setDownloadUrl(Objects.requireNonNull(result.getData().getData()).getPath());
                                    attachment.setFilename(this.getFileName(result.getData().getData()));

                                    InputStream iStream = this.getContext().getContentResolver().openInputStream(result.getData().getData());
                                    if (iStream != null) {
                                        byte[] inputData = getBytes(iStream);
                                        attachment.setContent(inputData);
                                        iStream.close();
                                    }

                                    String type = this.getContext().getContentResolver().getType(result.getData().getData());
                                    attachment.setContentType(type);

                                    BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                                    baseDescriptionObject.setObject(attachment);
                                    baseDescriptionObject.setTitle(attachment.getTitle());
                                    baseDescriptionObject.setDescription(attachment.getDescription());
                                    this.lvIssueAttachments.getAdapter().add(baseDescriptionObject);
                                }
                            }
                        } catch (Exception ex) {
                            Log.e("Error", ex.getLocalizedMessage(), ex);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.issue_fragment_attachments, container, false);

        this.lvIssueAttachments = this.root.findViewById(R.id.lvIssueAttachments);
        this.lvIssueAttachments.setContextMenu(R.menu.context_attachment);
        if (this.getActivity() != null) {
            this.bugService = Helper.getCurrentBugService(this.getActivity());
        }
        this.cmdIssueAttachmentAdd = this.root.findViewById(R.id.cmdIssueAttachmentAdd);
        this.cmdIssueAttachmentPhoto = this.root.findViewById(R.id.cmdIssueAttachmentPhoto);

        this.lvIssueAttachments.setOnClickListener((SwipeRefreshDeleteList.SingleClickListener) listObject -> {
            try {
                if (listObject != null) {
                    if (getContext() != null) {
                        if(listObject.getObject() instanceof Attachment<?> attachment) {
                            IntentHelper.saveAndOpenFile(attachment.getContent(), getActivity());
                        }
                    }
                }
            } catch (Exception ex) {
                Notifications.printException(getActivity(), ex, R.mipmap.ic_launcher_round);
            }
        });

        this.lvIssueAttachments.setOnDeleteListener(listObject ->  {
            for (int i = 0; i <= lvIssueAttachments.getAdapter().getItemCount() - 1; i++) {
                BaseDescriptionObject current = lvIssueAttachments.getAdapter().getItem(i);
                if (((Attachment<?>)current.getObject()).getId() == ((Attachment<?>)listObject.getObject()).getId()) {
                    lvIssueAttachments.getAdapter().deleteItem(i);
                }
            }
        });

        this.cmdIssueAttachmentAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            this.pick_file.launch(intent);
        });

        this.cmdIssueAttachmentPhoto.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            this.gallery.launch(photoPickerIntent);
        });

        this.updateUITrackerSpecific();
        this.initData();
        this.manageControls(this.editMode);
        return this.root;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        try {
            if(getActivity()!=null) {
                BaseDescriptionObject listObject = this.lvIssueAttachments.getAdapter().getObject();
                Object id = ((Attachment<?>)listObject.getObject()).getId();
                new Thread(()->{
                    try {
                        this.bugService.deleteAttachment(id, null, null);
                        getActivity().runOnUiThread(()->lvIssueAttachments.getAdapter().deleteItem(lvIssueAttachments.getAdapter().getItemPosition(listObject)));
                    } catch (Exception ex) {
                        getActivity().runOnUiThread(()->Notifications.printException(getActivity(), ex, R.mipmap.ic_launcher_round));
                    }
                }).start();
            }
        } catch (Exception ex) {
            Notifications.printException(getActivity(), ex, R.mipmap.ic_launcher_round);
        }
        return true;
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        if (getActivity() != null) {
            String scheme = uri.getScheme();
            if (scheme != null) {
                String result = null;
                if (scheme.equals("content")) {
                    try (Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    }
                }
                if (result == null) {
                    result = uri.getPath();
                    int cut = 0;
                    if (result != null) {
                        cut = result.lastIndexOf('/');
                    }
                    if (cut != -1) {
                        if (result != null) {
                            result = result.substring(cut + 1);
                        }
                    }
                }
                return result;
            }
        }
        return "";
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    /** @noinspection rawtypes*/
    @Override
    public void setObject(DescriptionObject descriptionObject) {
        this.issue = (Issue<?>) descriptionObject;
    }

    /** @noinspection unchecked*/
    @Override
    public DescriptionObject getObject(DescriptionObject descriptionObject) {
        Issue<?> issue = (Issue<?>) descriptionObject;

        if (this.root != null) {
            issue.getAttachments().clear();
            for (int i = 0; i <= lvIssueAttachments.getAdapter().getItemCount() - 1; i++) {
                BaseDescriptionObject ls = lvIssueAttachments.getAdapter().getItem(i);
                if (ls != null) {
                    issue.getAttachments().add((Attachment) ls.getObject());
                }
            }
        }
        return issue;
    }

    @Override
    public void manageControls(boolean editMode) {
        this.editMode = editMode;

        if (this.root != null) {
            this.cmdIssueAttachmentAdd.setEnabled(editMode && this.bugService.getPermissions().addAttachments() || this.bugService.getPermissions().updateAttachments());
            this.lvIssueAttachments.setEnabled(editMode && this.bugService.getPermissions().deleteAttachments());
            this.cmdIssueAttachmentPhoto.setEnabled(editMode && this.bugService.getPermissions().addAttachments() || this.bugService.getPermissions().updateAttachments());
        }
    }

    @Override
    public void initData() {
        this.lvIssueAttachments.getAdapter().clear();
        for (Attachment<?> obj : this.issue.getAttachments()) {

            BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
            baseDescriptionObject.setObject(obj);
            baseDescriptionObject.setTitle(obj.getTitle());
            baseDescriptionObject.setDescription(obj.getDescription());

            byte[] content;
            if (obj.getContentType().contains("image")) {
                content = obj.getContent();
            } else {
                try {
                    content = ConvertHelper.convertDrawableToByteArray(
                        Objects.requireNonNull(
                            ResourcesCompat.getDrawable(
                                this.getResources(),
                                R.drawable.icon_issues_attachments,
                                requireContext().getTheme()
                            )
                        )
                    );

                } catch (Exception ex) {
                    this.lvIssueAttachments.getAdapter().add(baseDescriptionObject);
                    continue;
                }
            }
            baseDescriptionObject.setCover(content);
            this.lvIssueAttachments.getAdapter().add(baseDescriptionObject);
        }
    }

    @Override
    protected Validator initValidator() {
        this.validator =  new Validator(this.getContext(), R.mipmap.ic_launcher_round);
        return this.validator;
    }

    @Override
    public void updateUITrackerSpecific() {}
}