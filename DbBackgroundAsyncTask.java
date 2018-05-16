package com.example.minhvan.mynote;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

public class DbBackgroundAsyncTask extends AsyncTask<Object, Void, Boolean> {
    Context ctx;
    static boolean isFinishedDb;

    public DbBackgroundAsyncTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        isFinishedDb = false;
    }

    @Override
    protected void onPostExecute(Boolean results) {
        super.onPostExecute(results);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Boolean doInBackground(Object... params) {

        DBOpenHelper dbOpenHelper = new DBOpenHelper(ctx);
        String method = (String) params[0];
        if(method.equals("updateNote")){
            int id = (int) params[1];
            String title = (String) params[2];
            String text = (String) params[3];
            byte[] imageByte = (byte[]) params[4];
            String currentDate = (String) params[5];
            String timeStamp = (String) params[6];
            dbOpenHelper.updateNote(id,title,text,imageByte,currentDate,timeStamp);

        }
        if(method.equals("addNote")){
            String title = (String) params[1];
            String text = (String) params[2];
            byte[] imageByte = (byte[]) params[3];
            String currentDate = (String) params[4];
            String timeStamp = (String) params[5];
            String iconStringCode = (String) params[6];
            dbOpenHelper.addNote(title,text,imageByte,currentDate,timeStamp,iconStringCode);
        }
        if(method.equals("deleteNote")){
            //traverse arrayListNotes, if any note with selected=true, let delete it from database
            for (int i = 0; i < MainActivity.arrayListNotes.size() ; i++){
                if(MainActivity.arrayListNotes.get(i).getSelected()){
                    //delete a row in database
                    dbOpenHelper.deleteNote(MainActivity.arrayListNotes.get(i).get_id());
                }
            }
        }
        isFinishedDb = true;
        return true;
    }
}
