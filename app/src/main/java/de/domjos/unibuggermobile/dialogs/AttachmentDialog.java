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
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.material.button.MaterialButton;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.domjos.unibuggermobile.R;
import de.domjos.unitrackerlibrary.model.issues.Attachment;
import de.domjos.unitrackerlibrary.tools.Notifications;

public class AttachmentDialog extends AbstractDialog {
    private final List<Attachment<?>> attachments;

    public AttachmentDialog(Activity activity, List<Attachment<?>> attachments) {
        super(activity, R.layout.attachment_dialog);
        this.attachments = attachments;

        super.setTitle(R.string.issues_attachments);
    }

    @Override
    public void init(View view) {
        final ImageView iv = view.findViewById(R.id.ivCurrentAttachment);
        final MaterialButton cmdPrevious = view.findViewById(R.id.cmdPrevious);
        final MaterialButton cmdNext = view.findViewById(R.id.cmdNext);
        AtomicInteger id = new AtomicInteger();

        this.addAttachmentToImageView(activity, iv, attachments.get(id.get()));
        cmdPrevious.setOnClickListener(v -> {
            if (id.get() != 0) {
                id.getAndDecrement();
                this.addAttachmentToImageView(activity, iv, attachments.get(id.get()));
            }
        });
        cmdNext.setOnClickListener(v -> {
            if (id.get() != attachments.size() - 1) {
                id.getAndIncrement();
                this.addAttachmentToImageView(activity, iv, attachments.get(id.get()));
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void addAttachmentToImageView(Activity activity, ImageView iv, Attachment<?> attachment) {
        if (attachment.getContentType().toLowerCase().contains("image") ||
                attachment.getFilename().toLowerCase().endsWith("png") ||
                attachment.getFilename().toLowerCase().endsWith("jpg") ||
                attachment.getFilename().toLowerCase().endsWith("jpeg") ||
                attachment.getFilename().toLowerCase().endsWith("bmp") ||
                attachment.getFilename().toLowerCase().endsWith("gif")) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(attachment.getContent(), 0, attachment.getContent().length);
            iv.setImageBitmap(bitmap);
        } else {
            iv.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.icon_download, activity.getTheme()));
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
                            this.saveAttachment(file, attachment.getContent(), activity);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle(R.string.issues_context_attachment_overwrite);
                            builder.setPositiveButton(R.string.issues_context_attachment_overwrite_yes, (dialog, which) -> this.saveAttachment(file, attachment.getContent(), activity));
                            builder.setNegativeButton(R.string.issues_context_attachment_overwrite_no, (dialog, which) -> dialog.dismiss());
                            builder.create().show();
                        }
                    }
                } catch (Exception ex) {
                    Notifications.printException(activity,  ex, R.mipmap.ic_launcher_round);
                }
            });
            filePickerDialog.show();
        });
    }

    private void saveAttachment(File file, byte[] content, Activity activity) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(content);
            bos.flush();
            bos.close();
            Notifications.printMessage(activity, activity.getString(R.string.issues_context_attachment_saved), R.mipmap.ic_launcher_round);
        } catch (Exception ex) {
            Notifications.printException(activity,  ex, R.mipmap.ic_launcher_round);
        }
    }
}
