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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unitrackerlibrary.model.issues.Attachment;

public class AttachmentDialog extends DialogFragment {
    private static List<Attachment<?>> attachments;

    public static AttachmentDialog newInstance(List<Attachment<?>> attachments) {
        AttachmentDialog.attachments = attachments;

        return new AttachmentDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.attachment_dialog, container, false);
        Objects.requireNonNull(this.getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        Activity activity = this.requireActivity();

        AtomicInteger id = new AtomicInteger();
        final ImageView iv = view.findViewById(R.id.ivCurrentAttachment);
        final ImageButton cmdPrevious = view.findViewById(R.id.cmdPrevious);
        final ImageButton cmdNext = view.findViewById(R.id.cmdNext);

        addAttachmentToImageView(activity, iv, attachments.get(id.get()));
        cmdPrevious.setOnClickListener(v -> {
            if (id.get() != 0) {
                id.getAndDecrement();
                addAttachmentToImageView(activity, iv, attachments.get(id.get()));
            }
        });
        cmdNext.setOnClickListener(v -> {
            if (id.get() != attachments.size() - 1) {
                id.getAndIncrement();
                addAttachmentToImageView(activity, iv, attachments.get(id.get()));
            }
        });

        return view;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void addAttachmentToImageView(Activity activity, ImageView iv, Attachment<?> attachment) {
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
                            saveAttachment(file, attachment.getContent(), activity);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle(R.string.issues_context_attachment_overwrite);
                            builder.setPositiveButton(R.string.issues_context_attachment_overwrite_yes, (dialog, which) -> saveAttachment(file, attachment.getContent(), activity));
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
}
