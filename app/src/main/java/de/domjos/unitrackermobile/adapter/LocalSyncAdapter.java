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

package de.domjos.unitrackermobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.unitrackerlibrary.tasks.LocalSyncTask;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.provider.FileProvider;

public class LocalSyncAdapter extends BaseExpandableListAdapter {
    private List<Map.Entry<String, List<String>>> content;
    private Context context;
    private String path;
    private String search;

    public LocalSyncAdapter(String path, Context context, String search) {
        super();
        this.context = context;
        this.path = path;
        this.search = search;
        this.reload();
    }

    @Override
    public int getGroupCount() {
        return this.content.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.content.get(groupPosition).getValue().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.content.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.content.get(groupPosition).getValue().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = Helper.getRowView(this.context, parent, R.layout.local_sync_group);
        }

        ((CheckedTextView) convertView).setText(LocalSyncTask.pathPartToContent(this.content.get(groupPosition).getKey()));
        ((CheckedTextView) convertView).setChecked(isExpanded);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = Helper.getRowView(this.context, parent, R.layout.local_sync_child);
        }

        String item = this.content.get(groupPosition).getValue().get(childPosition);
        convertView.setOnClickListener(v -> {
            Uri uri;
            if (item.contains("issue")) {
                File file = new File(this.path + File.separatorChar + this.content.get(groupPosition).getKey() + File.separatorChar + item);
                uri = FileProvider.getUriForFile(this.context, "de.domjos.unitrackermobile.provider.FileProvider", file);
            } else {
                File file = new File(this.path + File.separatorChar + this.content.get(groupPosition).getKey() + File.separatorChar + "attachments" + File.separatorChar + item);
                uri = FileProvider.getUriForFile(this.context, "de.domjos.unitrackermobile.provider.FileProvider", file);
            }

            String mimeType = this.context.getContentResolver().getType(uri);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.context.startActivity(intent);
        });

        TextView lblChild = convertView.findViewById(R.id.lblChild);
        ImageView ivChild = convertView.findViewById(R.id.ivChild);

        if (lblChild != null) {
            lblChild.setText(LocalSyncTask.pathPartToContent(item));
        }
        if (ivChild != null) {
            if (item.toLowerCase().trim().contains("issue.pdf")) {
                ivChild.setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_bug_report_black_24dp));
            } else {
                ivChild.setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_file_download_black_24dp));
            }
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private void reload() {
        this.content = new LinkedList<>();
        File tmp = new File(this.path);
        if (tmp.exists()) {
            File[] files = tmp.listFiles();
            if(files!=null) {
                for (File content : files) {
                    if (content.getName().toLowerCase().contains(this.search.trim().toLowerCase())) {
                        if (content.isDirectory()) {
                            List<String> children = new LinkedList<>();
                            File[] contents = content.listFiles();
                            if(contents!=null) {
                                for (File child : contents) {
                                    if (child.isFile()) {
                                        if (child.getAbsolutePath().endsWith(".pdf")) {
                                            children.add(child.getName());
                                            break;
                                        }
                                    }
                                }

                                for (File child : contents) {
                                    if (child.isDirectory()) {
                                        if (child.getAbsolutePath().contains("attachments")) {
                                            File[] childs = child.listFiles();
                                            if(childs!=null) {
                                                for (File attachment : childs) {
                                                    children.add(attachment.getName());
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                            }

                            this.content.add(new AbstractMap.SimpleEntry<>(content.getName(), children));
                        }
                    }
                }
            }
        }
    }
}
