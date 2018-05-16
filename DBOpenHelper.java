package com.example.minhvan.mynote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DBOpenHelper extends SQLiteOpenHelper {

    //constants for db name and version
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    //constants for identifying table and columns
    public static final String TABLE_NOTES = "notes";
    public static final String NOTE_ID = "_id";
    public static final String NOTE_TITLE = "noteTitle";
    public static final String NOTE_TEXT = "noteText";
    public static final String NOTE_IMAGE = "noteImage";
    public static final String NOTE_DATE = "noteDate";
    public static final String NOTE_TIMESTAMP = "noteTimeStamp";
    public static final String NOTE_ICONSTRINGCODE = "noteIconStringCode";

    //SQL to create table
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NOTES + " (" +
            NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NOTE_TITLE + " TEXT, " +
            NOTE_TEXT + " TEXT, " +
            NOTE_IMAGE + " BLOB, " +
            NOTE_DATE + " TEXT, " +
            NOTE_TIMESTAMP + " TEXT, " +
            NOTE_ICONSTRINGCODE + " TEXT" +
            ")";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    //add notes into database, return false if fail to insert
    public boolean addNote(String title, String text, byte[] image, String date, String timeStamp, String iconStringCode) {
        ContentValues values = new ContentValues();
        values.put(NOTE_TITLE, title);
        values.put(NOTE_TEXT, text);
        values.put(NOTE_IMAGE, image);
        values.put(NOTE_DATE, date);
        values.put(NOTE_TIMESTAMP, timeStamp);
        values.put(NOTE_ICONSTRINGCODE, iconStringCode);
        SQLiteDatabase db = getWritableDatabase();
        long result = db.insert(TABLE_NOTES, null, values);
        db.close();
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    //delete row from database
    public boolean deleteNote(int id) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + TABLE_NOTES + " WHERE " + NOTE_ID + "=" + id + ";");
            db.close();
            return true;
        } catch (SQLException e) {
            return false;
        }

    }

    //update row from database
    public boolean updateNote(int id, String title, String text, byte[] image, String date, String timeStamp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(NOTE_TITLE, title);
        cv.put(NOTE_TEXT, text);
        cv.put(NOTE_IMAGE, image);
        cv.put(NOTE_DATE, date);
        cv.put(NOTE_TIMESTAMP, timeStamp);
        long result = db.update(TABLE_NOTES, cv, "_id=" + id, null);
        db.close();
        if (result >= 0) {
            return true;
        } else {
            return false;
        }
    }

    //load all data from database into cursor and convert them into ArrayList of Notes
    public ArrayList<Notes> getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Notes> resultNote = new ArrayList<>();
        Notes note;
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTES, null);
        while (cursor.moveToNext()) {
            note = new Notes();
            note.set_id(cursor.getInt(0));
            note.setTitle(cursor.getString(1));
            note.setText(cursor.getString(2));
            note.setImage(cursor.getBlob(3));
            note.setDate(cursor.getString(4));
            note.setTimestamp(cursor.getString(5));
            note.setIconStringCode(cursor.getString(6));
            resultNote.add(note);
        }
        return resultNote;
    }
}
