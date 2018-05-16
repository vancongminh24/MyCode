package com.example.minhvan.mynote;


import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;


import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.jar.Manifest;

import android.view.View.OnClickListener;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    static final int VIEW_MODE_LISTVIEW = 0;
    static final int VIEW_MODE_GRIDVIEW = 1;
    static ArrayList<Notes> arrayListNotes;
    static CustomListViewAdapter adapterListview;
    static Boolean isDelete = false;
    static Boolean isView = false;
    DBOpenHelper dbOpenHelper;
    ListView listView;
    CustomGridViewAdapter adapterGrid;
    Intent intentNote;
    int mCountItemSelected = 0;
    SearchView searchView;
    FloatingActionButton fab;
    GridView gridView;
    DbBackgroundAsyncTask dbBackgroundAsyncTask;
    SharedPreferences sp;
    private ActionMode mActionMode;
    private int currentViewMode = 0;

    //create contextual action bar for delete operation
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_main_delete, menu);
            //onCreateActionMode is true, onDestroyActionMode is false
            isDelete = true;
            //refresh listview to see the the changes when isDelete = true;
            //the adapter will add check boxes in to each item of listview
            if (currentViewMode == VIEW_MODE_LISTVIEW) {
                adapterListview.notifyDataSetChanged();
            }
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.deleteItems:
                    //delete note from database using worker thread
                    dbBackgroundAsyncTask = new DbBackgroundAsyncTask(getApplicationContext());
                    dbBackgroundAsyncTask.execute("deleteNote");
                    //wait for the update from the database is finished
                    //it prevents the populateListAndGrid() is called before the update of db is finished.
                    while (!DbBackgroundAsyncTask.isFinishedDb) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //populate listview again to reload database into listview and gridview
                    populateListAndGridView();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //onCreateActionMode is true, onDestroyActionMode is false
            isDelete = false;
            populateListAndGridView();
            mCountItemSelected = 0;
            mActionMode = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        //My supported functions: add fab to create new note
        createFabForAddNote();

        //get currentViewMode
        sp = getSharedPreferences("ViewMode", MODE_PRIVATE);
        currentViewMode = sp.getInt("currentViewMode", 0);

        //My supported functions: populate arraylist into listview and gridview
        populateListAndGridView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(currentViewMode == VIEW_MODE_GRIDVIEW){
            adapterGrid.notifyDataSetChanged();
        }else{
            adapterListview.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //Create main menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        //My supported functions: create search bar on menu
        createSearchBarOnMenu(menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                mActionMode = startActionMode(mActionModeCallback);
                fab.setVisibility(View.INVISIBLE);
                return true;
            default:
                return super.onOptionsItemSelected(item);

            case R.id.switchView:
                if (currentViewMode == VIEW_MODE_LISTVIEW) {
                    currentViewMode = VIEW_MODE_GRIDVIEW;
                    listView.setVisibility(View.GONE);
                } else {
                    currentViewMode = VIEW_MODE_LISTVIEW;
                    gridView.setVisibility(View.GONE);
                }
                //populate the list again
                populateListAndGridView();

                // save currentViewMode into SharePreference
                sp = getSharedPreferences("ViewMode", MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putInt("currentViewMode", currentViewMode);
                edit.apply();
                return true;

        }
    }

    //handle back pressed, close search view
    @Override
    public void onBackPressed() {
        //close the search view if it is shown
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    //handle onItemClick for listview and gridview
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //handle item click if delete button doesn't be pressed
        if (isDelete) {
            //when user clicks, the boolean of note (to determine which note will be deleted)
            //will change from true to false or false to true
            if (arrayListNotes.get(position).getSelected()) {
                arrayListNotes.get(position).setSelected(false);
                //note.setSelected(false);
                mCountItemSelected--;
            } else {
                arrayListNotes.get(position).setSelected(true);
                //note.setSelected(true);
                mCountItemSelected++;
            }

            //refresh adapter
            if (currentViewMode == VIEW_MODE_LISTVIEW) {
                //adapterListview.updateSelectedNote(position,note);
                adapterListview.notifyDataSetChanged();
            } else {
                //adapterGrid.updateSelectedNote(position,note);
                adapterGrid.notifyDataSetChanged();
            }
            //show how many items selected
            mActionMode.setTitle("" + mCountItemSelected + "(s) selected");
            //handle item click if delete button doesn't be pressed
        } else {
            //this set to true to let the NoteDetail.class to update data rather than create new data by using fab
            isView = true;
            //take the selected note
            Notes note = arrayListNotes.get(position);
            //create intent with data
            intentNote = new Intent(getApplicationContext(), NoteDetail.class);
            intentNote.putExtra("Id", note.get_id());
            intentNote.putExtra("Title", note.getTitle());
            intentNote.putExtra("Text", note.getText());
            intentNote.putExtra("Image", note.getImage());
            intentNote.putExtra("Date", note.getDate());
            intentNote.putExtra("TimeStamp", note.getTimestamp());
            intentNote.putExtra("IconStringCode", note.getIconStringCode());
            startActivity(intentNote);
            overridePendingTransition(R.anim.left_in, R.anim.left_out);
            finish();
        }
    }

    /**
     * My supported functions: The collection of needed functions
     */
    //initialize views
    private void initializeViews() {
        listView = (ListView) findViewById(R.id.list);
        gridView = (GridView) findViewById(R.id.gridList);
        fab = (FloatingActionButton) findViewById(R.id.fab);
    }

    //populate or reload database into listview, also set fab is visible
    public void populateListAndGridView() {
        dbOpenHelper = new DBOpenHelper(this);
        arrayListNotes = dbOpenHelper.getAllData();
        //sorting the arrayListNotes
        Collections.sort(arrayListNotes, new MyComparator());
        //and then reverse them following by the latest is the top
        Collections.reverse(arrayListNotes);
        if (VIEW_MODE_LISTVIEW == currentViewMode) {
            adapterListview = new CustomListViewAdapter(this, arrayListNotes);
            listView.setAdapter(adapterListview);
            listView.setVisibility(View.VISIBLE);
            listView.setOnItemClickListener(this);
        } else {
            adapterGrid = new CustomGridViewAdapter(this, arrayListNotes);
            gridView.setAdapter(adapterGrid);
            gridView.setVisibility(View.VISIBLE);
            gridView.setOnItemClickListener(this);
        }
        fab.setVisibility(View.VISIBLE);
    }

    //create bar on Menu, inside onCreateOptionMenu() method
    private void createSearchBarOnMenu(Menu menu) {
        //handle searching
        searchView = (SearchView) menu.findItem(R.id.searchItems).getActionView();
        //set the search bar full width
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Search");
        //handle event when user click search icon
        searchView.setOnSearchClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setVisibility(View.INVISIBLE);
            }
        });
        //handle event when user click x (close) button
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                fab.setVisibility(View.VISIBLE);
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (currentViewMode == VIEW_MODE_LISTVIEW) {
                    adapterListview.getFilter().filter(newText);
                } else {
                    adapterGrid.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    //create fab to create new note
    private void createFabForAddNote() {
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NoteDetail.class);
                startActivity(intent);
                //activity transition
                overridePendingTransition(R.anim.left_in, R.anim.left_out);
                finish();
            }
        });
    }

    /**
     * inner classes
     */
    //using for comparing 2 notes. This is used in Collections.sort(arrayList, new MyComparator());
    private class MyComparator implements Comparator<Notes> {
        @Override
        public int compare(Notes o1, Notes o2) {
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        }
    }
}
