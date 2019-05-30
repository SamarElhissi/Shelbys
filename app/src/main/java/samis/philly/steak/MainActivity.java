package samis.philly.steak;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ProgressBar progressBar;
    boolean isLoading = false;
    private TextView errorText;
    private RelativeLayout layout, listLayout;
    // Resgistration Id from GCM
    private static final String PREF_GCM_REG_ID = "PREF_GCM_REG_ID";
    private SharedPreferences prefs;
    // Your project number and web server url. Please change below.
    private static final String GCM_SENDER_ID = "999992737868";
    private static final String WEB_SERVER_URL = "http://globalfoodsystem.com/Api/gcm/register_user";

    GoogleCloudMessaging gcm;
    private static final int ACTION_PLAY_SERVICES_DIALOG = 100;
    protected static final int MSG_REGISTER_WITH_GCM = 101;
    protected static final int MSG_REGISTER_WEB_SERVER = 102;
    protected static final int MSG_REGISTER_WEB_SERVER_SUCCESS = 103;
    protected static final int MSG_REGISTER_WEB_SERVER_FAILURE = 104;
    private String gcmRegId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.mainprogressBar);
        errorText = (TextView) findViewById(R.id.errorText);
        layout = (RelativeLayout) findViewById(R.id.mainrelativeL) ;

        new RetrieveFeedTask().execute();

        if (isGoogelPlayInstalled()) {
            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            // Read saved registration id from shared preferences.
            gcmRegId = getSharedPreferences().getString(PREF_GCM_REG_ID, "");

            if (TextUtils.isEmpty(gcmRegId)) {
                GCMhandler.sendEmptyMessage(MSG_REGISTER_WITH_GCM);
            }
            else{
                //   regIdView.setText(gcmRegId);
                //   Toast.makeText(getApplicationContext(), "Already registered with GCM", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {
            //   progressBar.setVisibility(View.VISIBLE);
            //  progressBar = new ProgressBar(
            //    MainActivity.this);
            progressBar.setVisibility(View.VISIBLE);



            //   progressBar.setMessage("Please wait..");
            //    progressBar.setIndeterminate(true);
            //    progressBar.setCancelable(false);
            //    progressBar.show();
            super.onPreExecute();
            this.exception = null;
            isLoading = true;

        }

        protected String  doInBackground(Void... urls) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Do some validation here
         //    String API_URL = "http://globalfoodsystem.com/Api/RestaurantsDirectory/Restaurants/?restaurant=0&latitude=42.9797824&longitude=-81.2443568&limit="+ 0;
           String API_URL = "http://globalfoodsystem.com/Api/RestaurantsDirectory/Restaurants/?restaurant=3&latitude="+0+"&longitude="+0+"&limit="+ 0;
            JSONObject object = null;
            InputStream inStream = null;
            HttpURLConnection urlConnection = null;
            ArrayList<String> cont = new ArrayList<>();
            try {


                URL url = new URL(API_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.connect();
                inStream = urlConnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }


                return stringBuilder.toString();
            }catch (Exception e) {

                this.exception = e;


            } finally {
                if (inStream != null) {
                    try {
                        // this will close the bReader as well
                        inStream.close();
                    } catch (IOException ignored) {
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        protected void onPostExecute(String  response) {
            if(response == null) {
                setErrorText("Connection Error!");
                layout.setBackgroundResource(0);
            }else {


                try{

                    JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                    JSONArray resturants = object.getJSONArray("restaurants");

                    for(int i=0;i<resturants.length();i++){

                        JSONObject jsonobject= (JSONObject) resturants.get(i);


                        Intent intent = new Intent(MainActivity.this, ResturantTabs.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        // intent.putExtra("url", "https://www.restaurantlogin.com/mobile/menu?company_uid=cd6801cb-200c-495d-9b31-66637e177162");//+IdsList.get(position));
                        intent.putExtra("url", "https://www.restaurantlogin.com/mobile/menu?company_uid="+jsonobject.optString("branch_orderlink"));
                        intent.putExtra("dealurl", "http://globalfoodsystem.com/Deals/"+jsonobject.optString("id"));

                        intent.putExtra("location",jsonobject.optString("province_name") + ", " + jsonobject.optString("branch_address")+ ", " + jsonobject.optString("city_name")+", "+jsonobject.optString("branch_postalcode"));
                        intent.putExtra("info",jsonobject.optString("branch_text"));
                        intent.putExtra("workHours",jsonobject.optString("branch_workhours"));
                        intent.putExtra("social",jsonobject.optString("branch_facebook")+","+jsonobject.optString("branch_twitter")+","+
                                jsonobject.optString("branch_google")+","+jsonobject.optString("branch_youtube")+","+
                                jsonobject.optString("branch_linkedin")+","+jsonobject.optString("branch_instagram"));
                        intent.putExtra("contacts",jsonobject.optString("branch_phone")+"%"+jsonobject.optString("branch_fax")+"%"+
                                jsonobject.optString("branch_mobile")+"%"+jsonobject.optString("branch_email"));
                        intent.putExtra("x_coor",jsonobject.optString("branch_x_coordinate"));
                        intent.putExtra("y_coor",jsonobject.optString("branch_y_coordinate"));
                        MainActivity.this.startActivity(intent);
                    }
                    if(resturants.length() == 0 ){
                        //   setErrorText("Sorry, no restaurants are available near your location!");
                        layout.setBackgroundResource(R.drawable.error_page);
                    }else {
                        errorText.setVisibility(View.GONE);

                        layout.setBackgroundResource(0);
                    }


                }catch(Exception e){

                }

            }


            progressBar.setVisibility(View.GONE);
            // Log.i("INFO", response.toString());
            isLoading= false;

        }
    }

    void setErrorText(String msg){
        errorText.setText(msg);
        errorText.setVisibility(View.VISIBLE);
    }

    private boolean isGoogelPlayInstalled() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        ACTION_PLAY_SERVICES_DIALOG).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Google Play Service is not installed",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;

    }

    private SharedPreferences getSharedPreferences() {
        if (prefs == null) {
            prefs = getApplicationContext().getSharedPreferences(
                    "AndroidSRCDemo", Context.MODE_PRIVATE);
        }
        return prefs;
    }

    public void saveInSharedPref(String result) {
        // TODO Auto-generated method stub
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREF_GCM_REG_ID, result);
        editor.commit();
    }

    Handler GCMhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_WITH_GCM:
                    new GCMRegistrationTask().execute();
                    break;
                case MSG_REGISTER_WEB_SERVER:
                    new WebServerRegistrationTask().execute();
                    break;
                case MSG_REGISTER_WEB_SERVER_SUCCESS:
                    //   Toast.makeText(getApplicationContext(),
                    //         "registered with web server", Toast.LENGTH_LONG).show();
                    break;
                case MSG_REGISTER_WEB_SERVER_FAILURE:
                    Toast.makeText(getApplicationContext(),
                            "registration with web server failed",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        };
    };

    private class GCMRegistrationTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub
            if (gcm == null && isGoogelPlayInstalled()) { gcm = GoogleCloudMessaging.getInstance(getApplicationContext());

            }
            try {
                gcmRegId = gcm.register(GCM_SENDER_ID);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return gcmRegId;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
//                Toast.makeText(getApplicationContext(), "registered with GCM",
//                        Toast.LENGTH_LONG).show();
                //   regIdView.setText(result);
                saveInSharedPref(result);
                GCMhandler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER);
            }
        }

    }

    private class WebServerRegistrationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            URL url = null;
            try {
                url = new URL(WEB_SERVER_URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                GCMhandler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_FAILURE);
            }
            Map<String, String> dataMap = new HashMap<String, String>();
            dataMap.put("regId", gcmRegId);
            dataMap.put("os", "android");
            dataMap.put("restaurant_id", "3");
            dataMap.put("x_coordinate", "0");
            dataMap.put("y_coordinate", "0");

            StringBuilder postBody = new StringBuilder();
            Iterator iterator = dataMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry param = (Map.Entry) iterator.next();
                postBody.append(param.getKey()).append('=')
                        .append(param.getValue());
                if (iterator.hasNext()) {
                    postBody.append('&');
                }
            }
            String body = postBody.toString();
            byte[] bytes = body.getBytes();

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded;charset=UTF-8");

                OutputStream out = conn.getOutputStream();
                out.write(bytes);
                out.close();

                int status = conn.getResponseCode();
                if (status == 200) {
                    // Request success
                    GCMhandler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_SUCCESS);
                } else {
                    InputStream error = conn.getErrorStream();
                    throw new IOException("Request failed with error code "
                            + status);
                }
            } catch (ProtocolException pe) {
                pe.printStackTrace();
                GCMhandler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_FAILURE);
            } catch (IOException io) {
                io.printStackTrace();
                GCMhandler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_FAILURE);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }
}

