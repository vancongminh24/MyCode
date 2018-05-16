package com.example.minhvan.mynote;

/**
 * Created by Minh Van on 25/7/2017.
 */

public class Notes {
    private int _id;
    private String title= "";
    private String text= "";
    private String date="";
    private String timestamp="";
    private byte[] image;
    private boolean selected = false;
    private String iconStringCode="";

    public String getIconStringCode() {
        return iconStringCode;
    }
    public void setIconStringCode(String iconStringCode) {
        this.iconStringCode = iconStringCode;
    }
    public boolean getSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public void setTitle (String title){
        this.title = title;
    }
    public void setText (String text){
        this.text = text;
    }
    public void set_id(int id) {
        this._id = id;
    }
    public int get_id() {
        return _id;
    }
    public String getTitle(){
        return title;
    }
    public String getText(){
        return text;
    }
    public byte[] getImage() {
        return image;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
