package com.example.minhvan.mynote;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;


public class NoteDetail extends AppCompatActivity implements View.OnFocusChangeListener, LocationListener {
    private EditText title;
    private EditText text;
    private Intent intentNote;
    private Intent intentToMainActivity;
    RelativeLayout relativeLayout;
    ImageView imageView, iconWeather;
    TextView dateCreateNote;
    final int RESULT_CROP = 222;
    DbBackgroundAsyncTask dbBackgroundAsyncTask;
    LocationManager mLocationManager;
    int MY_PERMISSION = 0;
    Location location;
    double lat, lon;
    String provider;
    String iconCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        initializeViews();

        //set original tag if no click on the imageView
        imageView.setTag("0");
        //load data if necessary: in case of viewing Note
        loadDataIntoViews();
        //Initialize views
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set new tag if having click on the imageView to indicate the image have changed
                imageView.setTag("1");
                //request permission for read external storage
                ActivityCompat.requestPermissions(
                        NoteDetail.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        RESULT_CROP);
            }
        });
        //handle events when title and text has focused
        title.setOnFocusChangeListener(this);
        text.setOnFocusChangeListener(this);
        //run weather API for non-view purpose, it is for creating new note
        if(!MainActivity.isView){
           setUpLocationAndRequestAPI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //run weather API for non-view purpose, it is for creating new note
        if(!MainActivity.isView){
            //ask for permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NoteDetail.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET,

                }, MY_PERMISSION);
            }
            Log.d("onResume", "On resume");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!MainActivity.isView){
            mLocationManager.removeUpdates(this);
        }
        MainActivity.isView = false;
    }

    //handle back button on phone
    @Override
    public void onBackPressed() {
        intentToMainActivity = new Intent(getApplicationContext(), MainActivity.class);
        //If either title and text is focused and imageView was clicked, the dialog will be shown to inform that the note have not been saved yet
        //If the note is saved (either title or text is not focused), the dialog will NOT be shown
        //Note: We can not use an other flag by listening the Save button,
        //because the user after pressing Save button can re-edit the note again
        if (text.hasFocus() || title.hasFocus() || imageView.getTag().equals("1")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your note has not been saved yet. Are you sure?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(intentToMainActivity);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            startActivity(intentToMainActivity);
            finish();
        }
        //activity transition
        overridePendingTransition(R.anim.right_in, R.anim.right_out);

    }

    //request permission for crop action
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RESULT_CROP) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    Intent intentCrop = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intentCrop.putExtra("crop", "true");
                    intentCrop.putExtra("aspectX", 16);
                    intentCrop.putExtra("aspectY", 9);
                    intentCrop.putExtra("outputX", 320);
                    intentCrop.putExtra("outputY", 180);
                    //cropped image will be save into mSaveUri
                    //intentCrop.putExtra("output",mSavedUri);
                    // retrieve data on return
                    intentCrop.putExtra("return-data", true);
                    startActivityForResult(intentCrop, RESULT_CROP);
                }
                // respond to users whose devices do not support the crop action
                catch (ActivityNotFoundException e) {
                    // display an error message
                    String errorMessage = "your device doesn't support the crop action!";
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access file location", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //receive the result from other app and display the result in type of imageView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CROP && resultCode == RESULT_OK && data != null) {
            // get the returned data
            Bundle extras = data.getExtras();
            // get the cropped bitmap
            Bitmap selectedBitmap = extras.getParcelable("data");
            //set image
            imageView.setImageBitmap(selectedBitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_note_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //handle item menu clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //do not save note, just clear keyboard
            case R.id.saveNote:
                imageView.setTag("1");
                //hide keyboard and move focus to linearlayout
                View view = this.getCurrentFocus();
                if (view != null) {
                    //hide keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    //focus on LinearLayout
                    relativeLayout = (RelativeLayout) findViewById(R.id.layoutNoteDetail);
                    relativeLayout.requestFocus();
                }
                saveNote();
                Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show();
                return true;
            //handle home button (back button in actionbar)
            //it will save note and perform onBackPressed()
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //the dateCreateNote will disappear when title and text has focused.
    //the Title and Text need to register at onStart()
    //Reason: it will prevent the soft keyboard pushing the dateCreateNote up
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            dateCreateNote.setVisibility(View.INVISIBLE);
        } else {
            dateCreateNote.setVisibility(View.VISIBLE);
        }
    }

    //save note into database
    public void saveNote() {
        dbBackgroundAsyncTask = new DbBackgroundAsyncTask(this);
        //for updating existing note
        if (MainActivity.isView) {
            //only update when title or text or image changed
            if (!(intentNote.getStringExtra("Title").equals(title.getText().toString())
                    && intentNote.getStringExtra("Text").equals(text.getText().toString())
                    && imageView.getTag().equals("0"))) {
                dbBackgroundAsyncTask.execute("updateNote", intentNote.getIntExtra("Id", -1), title.getText().toString(),
                        text.getText().toString(), Utils.imageViewToByte(imageView,100),
                        Utils.getCurrentDateString(System.currentTimeMillis()), Utils.getCurrentTimeStampString());
            }
        }
        //for creating new note
        if (!MainActivity.isView) {
            if (!(title.getText().toString().equals("") && text.getText().toString().equals("") && imageView.getTag().equals("0"))) {
                dbBackgroundAsyncTask.execute("addNote", title.getText().toString(), text.getText().toString(),
                        Utils.imageViewToByte(imageView,30), Utils.getCurrentDateString(System.currentTimeMillis()),
                        Utils.getCurrentTimeStampString(), iconCode);
            }
        }
        //set imageView tag back to 0
        imageView.setTag("0");
    }

    //receive data from MainActivity to load UI if It is a existing note
    private void loadDataIntoViews() {
        if (MainActivity.isView) {
            intentNote = getIntent();
            title.setText(intentNote.getStringExtra("Title"));
            text.setText(intentNote.getStringExtra("Text"));

            //only set image if the IconStringCode is not ""
            if(!intentNote.getStringExtra("IconStringCode").equals("")){
                iconWeather.setImageResource(Utils.getIconWeatherDrawable(intentNote.getStringExtra("IconStringCode")));
                iconWeather.setVisibility(View.VISIBLE);
            }
            //collect byteImage and then convert into bitmap and then setImageBitmap
            byte[] byteImage = intentNote.getByteArrayExtra("Image");
            Log.d("detail_byteImage",byteImage.length + "");
            Bitmap bmp = Utils.byteToBitMap(byteImage);
            Log.d("BitmapSizeAfter",""+bmp.getByteCount());
            imageView.setImageBitmap(bmp);
            dateCreateNote.setText(intentNote.getStringExtra("Date"));
        }
    }

    //associate views into variables
    private void initializeViews() {
        //initialize views
        title = (EditText) findViewById(R.id.editTitleNote);
        text = (EditText) findViewById(R.id.editTextNote);
        imageView = (ImageView) findViewById(R.id.imageView);
        dateCreateNote = (TextView) findViewById(R.id.dateCreateNote);
        iconWeather = (ImageView) findViewById(R.id.iconWeather);
    }

    //set up location and make a API request using AsyncTask
    private void setUpLocationAndRequestAPI() {
        //ask for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.INTERNET,

            }, MY_PERMISSION);
        }
        //handle GPS set-up
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1000, this);
        provider = mLocationManager.getBestProvider(new Criteria(), true);
        location = mLocationManager.getLastKnownLocation(provider);

        if (location != null) {
            Log.d("getLatLon",""+location.getLatitude()+ " "+location.getLongitude());
            lat = location.getLatitude();
            lon = location.getLongitude();
        }else{
            Log.d("LatLonIsNull","null");
        }
        //stop track GPS
        mLocationManager.removeUpdates(this);
        //handle API request
        new WeatherAsyncTask().execute(Utils.apiRequestWeatherString(Utils.API_LINK_WEATHER, Utils.API_KEY_WEATHER, String.valueOf(lat), String.valueOf(lon)));
    }

    //handle the GPS: only focus on onLocationChanged
    //the whole location manager set is from onLocationChanged to onProviderDisabled
    @Override
    public void onLocationChanged(Location location) {
        if(location!=null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            Log.d("getLatLon 2"," " + lat + lon);
            //handle API request, lat and lon and key will be added into API link. See Utils class for more info
            new WeatherAsyncTask().execute(Utils.apiRequestWeatherString(Utils.API_LINK_WEATHER, Utils.API_KEY_WEATHER, String.valueOf(lat), String.valueOf(lon)));

        }
        mLocationManager.removeUpdates(this);
        Log.d("nLocationChanged", ""+ location.getLatitude() + " and " + location.getLongitude());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //handle API request using AsyncTask
    //4 steps are indicated
    private class WeatherAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String iconStringCode) {
            super.onPostExecute(iconStringCode);
            //Step 4: set weather icon image from UI thread
            if(iconStringCode != null){
                //copy data from method variable to class variable. So the saveNote() can save this icon code to database
                iconCode = iconStringCode;
                Log.d("iconStringCode", iconStringCode);
                //get associate drawableIcon for each iconStringCode
                int drawableIcon = Utils.getIconWeatherDrawable(iconStringCode);
                //then set image from drawable, also set it visible
                iconWeather.setImageResource(drawableIcon);
                iconWeather.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(String... params) {
            String stream = null;
            String iconStringCode = null;
            //receive params[0]: the first arg
            String urlStrings = params[0];
            HttpHelper httpHelper = new HttpHelper();
            //Step 1: receive string of data after requesting API via urlStrings
            stream = httpHelper.getHttpData(urlStrings);
            Log.d("streamAPI", ""+ stream);
            //Step 2: then parse json string to get needed value
            if(stream != null){
                try {
                    //Getting JSON Object
                    JSONObject reader = new JSONObject(stream);
                    // Getting JSON Array node
                    JSONArray weatherArray = reader.getJSONArray("weather");
                    //Json string received with "weather" is parent
                    //iterate weatherArray to getJSONObject(i) and then extracts value (getString) from key (icon)
                    for (int i = 0; i<weatherArray.length(); i++){
                        //the result for icon code
                        iconStringCode = weatherArray.getJSONObject(i).getString("icon");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.d("iconStringCode", ""+iconStringCode);
            //Step 3: return needed value: icon code
            return iconStringCode;
        }
    }
}
