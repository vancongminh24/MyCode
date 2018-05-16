package com.example.minhvan.mynote;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListViewAdapter extends BaseAdapter implements Filterable{
    private static ArrayList<Notes> notesArrayList;
    private LayoutInflater mInflater;
    private CustomFilter filter;
    private ArrayList<Notes> filterList;

    //constructor require context and ArrayList<Notes> parameters
    public CustomListViewAdapter(Context context, ArrayList<Notes> results){
        notesArrayList = results;
        filterList = results;
        mInflater = LayoutInflater.from(context);
    }

    //class hold reference of each view
    static class ViewHolder {
        TextView title;
        TextView text;
        ImageView icon;
        ImageView iconWeatherList;
        CheckBox checkBox;
    }

    @Override
    public int getCount() {
        return notesArrayList.size();
    }

    @Override
    public Notes getItem(int position) {
        return notesArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            convertView = mInflater.inflate(R.layout.row_listview,parent,false);
            viewHolder = new CustomListViewAdapter.ViewHolder();

            viewHolder.title = (TextView) convertView.findViewById(R.id.rowTitle);
            viewHolder.text = (TextView) convertView.findViewById(R.id.rowText);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.rowIcon);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
            viewHolder.iconWeatherList = (ImageView) convertView.findViewById(R.id.iconWeatherList);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //set content of each view by load the data from note instance into each view
        viewHolder.title.setText(getItem(position).getTitle());
        viewHolder.text.setText(getItem(position).getText());
        viewHolder.icon.setImageBitmap(Utils.byteToBitMapReduced(getItem(position).getImage(),0.1));

        if(!getItem(position).getIconStringCode().equals("")){
            viewHolder.iconWeatherList.setImageResource(Utils.getIconWeatherDrawable(getItem(position).getIconStringCode()));
            viewHolder.iconWeatherList.setVisibility(View.VISIBLE);
        }

        //if user performs delete operation, the checkbox will be visible
        if(MainActivity.isDelete){
            viewHolder.iconWeatherList.setVisibility(View.INVISIBLE);
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        }
        //if the getSelected of the note is true, set the checkbox is checked
        //otherwise it is unchecked
        //by using updateRecords, the checkbox will be changed according to new note updated with new setSelected boolean
        if(getItem(position).getSelected()){
            viewHolder.checkBox.setChecked(true);
        }else{
            viewHolder.checkBox.setChecked(false);
        }
        return convertView;
    }
    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new CustomFilter();
        }
        return filter;
    }
    //inner class for CustomFilter
    class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if(constraint != null && constraint.length()>0){
                //Constraint to upper
                constraint = constraint.toString().toUpperCase();
                ArrayList<Notes> filters = new ArrayList<Notes>();
                //get specific items
                for(int i = 0 ; i< filterList.size();i++){
                    //the title and text will be searched
                    if(filterList.get(i).getText().toUpperCase().contains(constraint) ||
                            filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                        Notes note = new Notes();
                        note.setTitle(filterList.get(i).getTitle());
                        note.setText(filterList.get(i).getText());
                        note.set_id(filterList.get(i).get_id());
                        note.setImage(filterList.get(i).getImage());
                        filters.add(note);
                    }
                }
                results.count = filters.size();
                results.values = filters;
            }else{
                results.count=filterList.size();
                results.values=filterList;
            }
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            notesArrayList =(ArrayList<Notes>) results.values;
            MainActivity.arrayListNotes = notesArrayList;
            notifyDataSetChanged();
        }
    }
}

