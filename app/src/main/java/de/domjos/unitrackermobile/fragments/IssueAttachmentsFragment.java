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

package de.domjos.unitrackermobile.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.ListObject;
import de.domjos.unibuggerlibrary.model.issues.Attachment;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.utils.Converter;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.MainActivity;
import de.domjos.unitrackermobile.adapter.ListAdapter;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.helper.Validator;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public final class IssueAttachmentsFragment extends AbstractFragment {
    private ListView lvIssueAttachments;
    private ListAdapter attachmentAdapter;
    private ImageButton cmdIssueAttachmentAdd;
    private IBugService bugService;

    private View root;
    private Issue issue;
    private boolean editMode;

    private final static int PICKFILE_REQUEST_CODE = 99;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.issue_fragment_attachments, container, false);

        this.lvIssueAttachments = this.root.findViewById(R.id.lvIssueAttachments);
        if (this.getActivity() != null) {
            this.attachmentAdapter = new ListAdapter(this.getActivity(), R.drawable.ic_file_upload_black_24dp);
            this.lvIssueAttachments.setAdapter(this.attachmentAdapter);
            this.attachmentAdapter.notifyDataSetChanged();

            this.bugService = Helper.getCurrentBugService(this.getActivity());
        }
        this.cmdIssueAttachmentAdd = this.root.findViewById(R.id.cmdIssueAttachmentAdd);

        this.lvIssueAttachments.setOnItemClickListener((parent, view, position, id) -> {
            try {
                ListObject ls = attachmentAdapter.getItem(position);
                if (ls != null) {
                    if (this.getContext() != null) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(((Attachment) ls.getDescriptionObject()).getDownloadUrl()));
                        startActivity(browserIntent);
                    }
                }
            } catch (Exception ex) {
                MessageHelper.printException(ex, this.getActivity());
            }
        });

        this.lvIssueAttachments.setOnItemLongClickListener((parent, view, position, id) -> {
            if (this.issue != null) {
                for (int i = 0; i <= this.issue.getAttachments().size() - 1; i++) {
                    ListObject lsObj = this.attachmentAdapter.getItem(position);
                    if (lsObj != null) {
                        if (lsObj.getDescriptionObject().getId().equals(((Attachment) this.issue.getAttachments().get(i)).getId())) {
                            this.attachmentAdapter.remove(lsObj);
                            break;
                        }
                    }
                }
            }
            return true;
        });

        this.cmdIssueAttachmentAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICKFILE_REQUEST_CODE);
        });

        this.updateUITrackerSpecific();
        this.initData();
        this.manageControls(this.editMode);
        return this.root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (resultCode == RESULT_OK && requestCode == PICKFILE_REQUEST_CODE) {
                if (data != null) {
                    if (data.getData() != null) {
                        if (this.getContext() != null) {
                            Attachment attachment = new Attachment();
                            attachment.setDownloadUrl(data.getData().getPath());
                            attachment.setFilename(data.getData().getPath());

                            InputStream iStream = this.getContext().getContentResolver().openInputStream(data.getData());
                            if (iStream != null) {
                                byte[] inputData = getBytes(iStream);
                                attachment.setContent(inputData);
                                iStream.close();
                            }

                            String type = this.getContext().getContentResolver().getType(data.getData());
                            attachment.setContentType(type);

                            this.attachmentAdapter.add(new ListObject(this.getActivity(), R.drawable.ic_file_upload_black_24dp, attachment));
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

        int len = 0;
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
    public DescriptionObject getObject(DescriptionObject descriptionObject) {
        Issue issue = (Issue) descriptionObject;

        if (this.root != null) {
            issue.getAttachments().clear();
            for (int i = 0; i <= attachmentAdapter.getCount() - 1; i++) {
                ListObject ls = attachmentAdapter.getItem(i);
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
        }
    }

    @Override
    protected void initData() {
        this.attachmentAdapter.clear();
        for (Object obj : this.issue.getAttachments()) {
            Attachment attachment = (Attachment) obj;

            byte[] content;
            if (attachment.getContentType().contains("image")) {
                content = attachment.getContent();
            } else {
                try {
                    content = Converter.convertDrawableToByteArray(this.getResources().getDrawable(R.drawable.ic_file_upload_black_24dp));
                } catch (Exception ex) {
                    this.attachmentAdapter.add(new ListObject(this.getContext(), R.drawable.ic_file_upload_black_24dp, attachment));
                    continue;
                }
            }
            this.attachmentAdapter.add(new ListObject(this.getContext(), content, attachment));
        }
    }

    @Override
    public Validator initValidator() {
        Validator validator = new Validator(this.getContext());
        if (this.root != null) {

        }
        return validator;
    }

    @Override
    public void updateUITrackerSpecific() {
        Authentication authentication = MainActivity.GLOBALS.getSettings(this.getContext()).getCurrentAuthentication();

        switch (authentication.getTracker()) {
            case MantisBT:

                break;
        }
    }
}