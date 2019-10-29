package de.domjos.unitrackermobile.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;

public class SuggestionAdapter extends ArrayAdapter<DescriptionObject> {
    private Context context;
    private List<DescriptionObject> values;

    public SuggestionAdapter(@NonNull Context context, List<DescriptionObject> values) {
        super(context, android.R.layout.simple_list_item_1);
        this.context = context;
        this.values = values;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        try {
            if(convertView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            DescriptionObject descriptionObject = this.getItem(position);
            if(descriptionObject != null) {
                TextView name = view.findViewById(android.R.id.text1);
                name.setText(descriptionObject.getDescription());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }


    private Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((DescriptionObject) resultValue).getTitle();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<DescriptionObject> descriptionObjects = new LinkedList<>();
            for(int i = 0; i<=SuggestionAdapter.this.values.size()-1; i++) {
                DescriptionObject descriptionObject = SuggestionAdapter.this.values.get(i);
                if(descriptionObject!=null) {
                    if(constraint != null && descriptionObject.getTitle()!=null) {
                        if(descriptionObject.getTitle().contains(constraint)) {
                            descriptionObjects.add(descriptionObject);
                        }
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = descriptionObjects;
            filterResults.count = descriptionObjects.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<DescriptionObject> filterList = (LinkedList) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (DescriptionObject descriptionObject : filterList) {
                    add(descriptionObject);
                    notifyDataSetChanged();
                }
            }
        }
    };
}
