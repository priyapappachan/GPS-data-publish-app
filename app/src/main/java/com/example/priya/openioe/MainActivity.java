package com.example.priya.openioe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView longitudeValueNetwork, latitudeValueNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        longitudeValueGPS = (TextView) findViewById(R.id.longitudeValueGPS);
        latitudeValueGPS = (TextView) findViewById(R.id.latitudeValueGPS);

        

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new     StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

AsyncTask<String, String, String> result = new MyDownloadTask().execute();
    }


    public void toggleGPSUpdates(View view) {

        Button button = (Button) view;
        if (button.getText().equals(getResources().getString(R.string.pause))) {
            locationManager.removeUpdates(locationListenerGPS);
            button.setText(R.string.resume);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
            button.setText(R.string.pause);
        }
    }

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Long tsLong = System.currentTimeMillis()/1000;
                    String ts = tsLong.toString();
                    String lat =  Double.toString(longitudeGPS);
                    String lon = Double.toString(latitudeGPS);

                    longitudeValueGPS.setText(longitudeGPS + "");
                    latitudeValueGPS.setText(latitudeGPS + "");

                    Toast.makeText(MainActivity.this, "GPS Provider update", Toast.LENGTH_SHORT).show();

                    AsyncTask<String, String, String> result;
                  //  result = new MyDownloadTask().execute(longitudeGPS,latitudeGPS);

                    URL url;
                    HttpURLConnection connection = null;
                    try {
                        //Create connection
                        url = new URL("http://192.168.0.6:8080/api/sensors/{id}");
                        connection = (HttpURLConnection)url.openConnection();
                        connection.setRequestMethod("POST");
                        //  connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                        connection.setRequestProperty("Content-Type","application/json");
                        connection.setRequestProperty("Accept", "application/json");
                        connection.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTUwMTgyNzYzNX0.byt0ICNUxsUnPo5UZV7CXS0leLp7nqcAS3FfkOoF-PB9Ce8GiR4XRZ2yomO2eG8fnrnPdQQFdHitNBH1VbC-mg");

                        //  connection.setRequestProperty("Content-Length", "" +Integer.toString(urlParameters.getBytes().length));
                        connection.setRequestProperty("Content-Language", "en-US");

                        connection.setUseCaches (false);
                        connection.setDoInput(true);
                        connection.setDoOutput(true);

                        JSONObject jsonParam = new JSONObject();
                        jsonParam.put("data", lat+" "+lon);
                        jsonParam.put("description", "lat, long");
                        jsonParam.put("sensorId", 1);
                        jsonParam.put("timestamp", ts);
                        jsonParam.put("topic", "gps");

                        //Send request
                        DataOutputStream wr = new DataOutputStream (
                                connection.getOutputStream ());
                        wr.writeBytes(jsonParam.toString());
                        wr.flush ();
                        wr.close ();

                        //Get Response
                        InputStream is = connection.getInputStream();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                        String line;
                        StringBuffer response = new StringBuffer();
                        while((line = rd.readLine()) != null) {
                            response.append(line);
                            response.append('\r');
                        }
                        rd.close();
                      //  return response.toString();

                    } catch (Exception e) {

                        e.printStackTrace();
                        //return null;

                    } finally {

                        if(connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}



class MyDownloadTask extends AsyncTask<String,String,String>
{


    protected void onPreExecute() {
        //display progress dialog.

    }


    protected String doInBackground(String... urls) {

        String result = "PP";

        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL("http://192.168.0.6:8080/api/sensors/{id}");
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
          //  connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTUwMTgyNzYzNX0.byt0ICNUxsUnPo5UZV7CXS0leLp7nqcAS3FfkOoF-PB9Ce8GiR4XRZ2yomO2eG8fnrnPdQQFdHitNBH1VbC-mg");

            //  connection.setRequestProperty("Content-Length", "" +Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            JSONObject jsonParam = new JSONObject();

            jsonParam.put("data", "999");
            jsonParam.put("description", "Real");
            jsonParam.put("sensorId", 1);
            jsonParam.put("timestamp", "2018-03-19T09:01:01.526Z");
            jsonParam.put("topic", "s");

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream ());
            wr.writeBytes(jsonParam.toString());
          //  wr.writeBytes ("Hello");
            wr.flush ();
            wr.close ();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
     //   return "T";

    }

    protected void onPostExecute(Void result) {
        // dismiss progress dialog and update ui
    }
}
