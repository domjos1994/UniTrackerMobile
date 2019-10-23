/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
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

package de.domjos.unitrackermobile.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.ListObject;
import de.domjos.unitrackerlibrary.model.issues.Attachment;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.utils.Converter;
import de.domjos.unitrackerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.custom.SwipeRefreshDeleteList;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.helper.IntentHelper;
import de.domjos.unitrackermobile.helper.Validator;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public final class IssueAttachmentsFragment extends AbstractFragment {
    private SwipeRefreshDeleteList lvIssueAttachments;
    private ImageButton cmdIssueAttachmentAdd;
    private ImageButton cmdIssueAttachmentPhoto;
    private IBugService bugService;

    private View root;
    private Issue issue;
    private boolean editMode;

    private final static int PICKFILE_REQUEST_CODE = 99, PHOTO_GALLERY = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        this.lvIssueAttachments.click(new SwipeRefreshDeleteList.ClickListener() {
            @Override
            public void onClick(ListObject listObject) {
                try {
                    if (listObject != null) {
                        if (getContext() != null) {
                            if(listObject.getDescriptionObject() instanceof Attachment) {
                                Attachment attachment = (Attachment) listObject.getDescriptionObject();
                                IntentHelper.saveAndOpenFile(attachment.getContent(), getActivity());
                            }
                        }
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, getActivity());
                }
            }
        });

        this.lvIssueAttachments.delete(new SwipeRefreshDeleteList.DeleteListener() {
            @Override
            public void onDelete(ListObject listObject) {
                for (int i = 0; i <= lvIssueAttachments.getAdapter().getItemCount() - 1; i++) {
                    ListObject current = lvIssueAttachments.getAdapter().getItem(i);
                    if (current.getDescriptionObject().getId() == listObject.getDescriptionObject().getId()) {
                        lvIssueAttachments.getAdapter().deleteItem(i);
                    }
                }
            }
        });

        this.cmdIssueAttachmentAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICKFILE_REQUEST_CODE);
        });

        this.cmdIssueAttachmentPhoto.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, PHOTO_GALLERY);
        });

        this.updateUITrackerSpecific();
        this.initData();
        this.manageControls(this.editMode);
        return this.root;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        try {
            if(getActivity()!=null) {
                ListObject listObject = this.lvIssueAttachments.getAdapter().getObject();
                Object id = listObject.getDescriptionObject().getId();
                new Thread(()->{
                    try {
                        bugService.deleteAttachment(id, null, null);
                        getActivity().runOnUiThread(()->lvIssueAttachments.getAdapter().deleteItem(lvIssueAttachments.getAdapter().getItemPosition(listObject)));
                    } catch (Exception ex) {
                        getActivity().runOnUiThread(()->MessageHelper.printException(ex, getActivity()));
                    }
                }).start();
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, getActivity());
        }
        return true;
    }

    private String getFileName(Uri uri) {
        if(getActivity()!=null) {
            String scheme = uri.getScheme();
            if(scheme!=null) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PHOTO_GALLERY && resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    if (getContext() != null) {
                        InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                        if (imageStream != null) {
                            Attachment attachment = new Attachment();
                            attachment.setDownloadUrl(imageUri.getPath());
                            attachment.setFilename(this.getFileName(imageUri));
                            attachment.setContentType(data.getType());
                            attachment.setContent(Converter.convertStreamToByteArray(imageStream));
                            this.lvIssueAttachments.getAdapter().add(new ListObject(this.getActivity(), R.drawable.ic_file_upload_black_24dp, attachment));
                        }
                    }
                }
            }
            if (resultCode == RESULT_OK && requestCode == PICKFILE_REQUEST_CODE) {
                if (data != null) {
                    if (data.getData() != null) {
                        if (this.getContext() != null) {
                            Attachment attachment = new Attachment();
                            attachment.setDownloadUrl(data.getData().getPath());
                            attachment.setFilename(this.getFileName(data.getData()));

                            InputStream iStream = this.getContext().getContentResolver().openInputStream(data.getData());
                            if (iStream != null) {
                                byte[] inputData = getBytes(iStream);
                                attachment.setContent(inputData);
                                iStream.close();
                            }

                            String type = this.getContext().getContentResolver().getType(data.getData());
                            attachment.setContentType(type);

                            this.lvIssueAttachments.getAdapter().add(new ListObject(this.getActivity(), R.drawable.ic_file_upload_black_24dp, attachment));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.getActivity());
        }
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

    @Override
    public void setObject(DescriptionObject descriptionObject) {
        this.issue = (Issue) descriptionObject;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DescriptionObject getObject(DescriptionObject descriptionObject) {
        Issue issue = (Issue) descriptionObject;

        if (this.root != null) {
            issue.getAttachments().clear();
            for (int i = 0; i <= lvIssueAttachments.getAdapter().getItemCount() - 1; i++) {
                ListObject ls = lvIssueAttachments.getAdapter().getItem(i);
                if (ls != null) {
                    issue.getAttachments().add(ls.getDescriptionObject());
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
    protected void initData() {
        this.lvIssueAttachments.getAdapter().clear();
        for (Object obj : this.issue.getAttachments()) {
            Attachment attachment = (Attachment) obj;

            byte[] content;
            if (attachment.getContentType().contains("image")) {
                content = attachment.getContent();
            } else {
                try {
                    content = Converter.convertDrawableToByteArray(this.getResources().getDrawable(R.drawable.ic_file_upload_black_24dp));
                } catch (Exception ex) {
                    this.lvIssueAttachments.getAdapter().add(new ListObject(this.getContext(), R.drawable.ic_file_upload_black_24dp, attachment));
                    continue;
                }
            }
            this.lvIssueAttachments.getAdapter().add(new ListObject(this.getContext(), content, attachment));
        }
    }

    @Override
    public Validator initValidator() {
        return new Validator(this.getContext());
    }

    @Override
    public void updateUITrackerSpecific() {}
}