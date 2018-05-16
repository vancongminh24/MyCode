package com.example.minhvan.mynote;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpHelper {
    static String stream = null;
    HttpURLConnection httpURLConnection;
    URL url;
    public HttpHelper(){}
    public String getHttpData(String urlString){
        try {
            url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();

            Log.d("httpGetResponseCode", " " + httpURLConnection.getResponseCode());
            if(httpURLConnection.getResponseCode() == 200)// OK = 200
            {
                BufferedReader r = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null){
                    sb.append(line);
                }
                stream = sb.toString();
                httpURLConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream;
    }
}
