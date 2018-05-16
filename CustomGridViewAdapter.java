package com.example.minhvan.mynote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
public class CustomGridViewAdapter extends BaseAdapter implements Filterable {
    private static ArrayList<Notes> notesArrayList;
    private LayoutInflater mInflater;
    private ArrayList<Notes> filterList;
    private CustomFilter1 filter;
    private DbBackgroundAsyncTask dbBackgroundAsyncTask;
    private static Context ctx;
    //constructor require context and ArrayList<Notes> parameters
    public CustomGridViewAdapter(Context context, ArrayList<Notes> results){
        notesArrayList = results;
        filterList = results;
        mInflater = LayoutInflater.from(context);
    }

    //class hold reference of each view
    static class ViewHolder {
        TextView title;
        TextView text;
        ImageView icon;
        ImageView selectedIcon;
        ImageView iconWeatherGrid;
        RelativeLayout relativeLayout;
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
            convertView = mInflater.inflate(R.layout.grid_item,parent,false);
            viewHolder = new CustomGridViewAdapter.ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.gridTitle);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.gridImage);
            viewHolder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.gridItemList);
            viewHolder.selectedIcon = (ImageView) convertView.findViewById(R.id.selectedGridView);
            viewHolder.iconWeatherGrid = (ImageView) convertView.findViewById(R.id.iconWeatherGrid);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //set content of each view by load the data from note instance into each view
        viewHolder.title.setText(notesArrayList.get(position).getTitle());
        viewHolder.icon.setImageBitmap(Utils.byteToBitMapReduced(getItem(position).getImage(),0.4));

        if(getItem(position).getSelected()){
            viewHolder.selectedIcon.setVisibility(View.VISIBLE);
        }else{
            viewHolder.selectedIcon.setVisibility(View.INVISIBLE);
        }
        if(!getItem(position).getIconStringCode().equals("")){
            viewHolder.iconWeatherGrid.setImageResource(Utils.getIconWeatherDrawable(getItem(position).getIconStringCode()));
            viewHolder.iconWeatherGrid.setVisibility(View.VISIBLE);
        }

        return convertView;
    }


    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new CustomFilter1();
        }
        return filter;
    }

    //inner class for CustomFilter
    class CustomFilter1 extends Filter {
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
                results.count=filters.size();
                results.values=filters;
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
