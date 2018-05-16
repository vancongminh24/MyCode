package com.example.minhvan.mynote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static String API_KEY_WEATHER = "0683ab06f0cf18761c57276ee421d3dc";
    public static String API_LINK_WEATHER = "http://api.openweathermap.org/data/2.5/weather";


    //byte[] to bitmap
    public static Bitmap byteToBitMap(byte[] byteImage){
        Bitmap bmp = BitmapFactory.decodeByteArray(byteImage,0,byteImage.length);
        return bmp;
    }
    //byte[] to bitmap with reduce sized
    public static Bitmap byteToBitMapReduced(byte[] image, double ratio){
        Bitmap bmp = BitmapFactory.decodeByteArray(image,0,image.length);
        Log.d("Before", "" + bmp.getByteCount());
        Log.d("After", "" + bmp.getByteCount()*ratio);
        return Bitmap.createScaledBitmap(bmp,(int)(bmp.getWidth()*ratio), (int)(bmp.getHeight()*ratio), true);

    }
    //imageView(bitmap) to byte[]
    public static byte[] imageViewToByte(ImageView imageView, int qualityLevel){
        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        Log.d("imageViewtoByteBefore", bitmap.getByteCount()+"");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,qualityLevel,stream);
        Log.d("imageViewtoByteAfter", bitmap.getByteCount()+"");
        byte[] byteArray = stream.toByteArray();
        Log.d("Bitmap size compressed",bitmap.getByteCount() + "");
        return byteArray;
    }
    //get current date returning String
    public static String getCurrentDateString(long currentTimeMillis){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy, hh:mm:ss a");
        String dateString = sdf.format(currentTimeMillis);
        return dateString;
    }
    //get current timestamp string
    public static String getCurrentTimeStampString(){
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return ts;
    }
    //api request for weather based on coordinates of latitude and longitude
    public static String apiRequestWeatherString(String link, String key, String lat, String lon){
        StringBuilder sb = new StringBuilder(link);
        sb.append(String.format("?lat=%s&lon=%s&APPID=%s&units=metric",lat,lon,key));
        return sb.toString();
    }
    //get image from openweathermap.org based on icon code
    public static int getIconWeatherDrawable(String iconString){
        Map<String,Integer> map = new HashMap<>();
        map.put("01d",R.drawable.w01d);
        map.put("02d",R.drawable.w02d);
        map.put("03d",R.drawable.w03d);
        map.put("04d",R.drawable.w04d);
        map.put("09d",R.drawable.w09d);
        map.put("10d",R.drawable.w10d);
        map.put("11d",R.drawable.w11d);
        map.put("13d",R.drawable.w13d);
        map.put("50d",R.drawable.w50d);
        map.put("01n",R.drawable.w01n);
        map.put("02n",R.drawable.w02n);
        map.put("03n",R.drawable.w03n);
        map.put("04n",R.drawable.w04n);
        map.put("09n",R.drawable.w09n);
        map.put("10n",R.drawable.w10n);
        map.put("11n",R.drawable.w11n);
        map.put("13n",R.drawable.w13n);
        map.put("50n",R.drawable.w50n);
        return map.get(iconString);
    }

}
