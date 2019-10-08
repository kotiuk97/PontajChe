package com.example.pontajche;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainPage extends BaseActivity { //implements LocationListener{


    private static final String PROPERTIES_FOLDER_NAME = "pontaj_properties.txt";

    private FirebaseFirestore db;
    private Spinner spinner;
    private Button eventsCheckButton;
    private ImageView refreshImageView;
    private WebView eventsWebView;
    private PontajAPI pontajAPI;
    private Map<String, String> items;
    private String uid;

//    @Override
//    public void onLocationChanged(Location location) {
//
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }


    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//            finishCheck();
            eventsCheckButton.setText("insert check");
            refreshEventsView();
            eventsCheckButton.setEnabled(true);
            refreshImageView.setEnabled(true);
//            pontajAPI.setWebChromeClient(null);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
//            action = buttonCheck.getText().toString();
            eventsCheckButton.setText("Wait");
            eventsCheckButton.setEnabled(false);
            refreshImageView.setEnabled(false);
//            buttonCheck.setEnabled(false);
        }

    }

//    protected LocationManager locationManager;
//    protected LocationListener locationListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
        showProgressDialog("Fetching data from the server");

        uid = getIntent().getExtras().getString("uid");
        initFields();
        db = FirebaseFirestore.getInstance();
        items = new HashMap<>();

        pontajAPI = new PontajAPI(this);
        pontajAPI.setWebChromeClient(new MyWebViewClient());

        downloadEventsUsers();

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


    }


    private void downloadEventsUsers() {
        db.collection("employees")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getData().get("name").toString();
                                String value = document.getData().get("value").toString();
                                items.put(name, value);
                            }
                            addItemsToSpinner(new ArrayList<String>(items.keySet()));
                            selectLastSavedUser();
                            refreshEventsView();
                            eventsCheckButton.setText("Insert check");
                            eventsCheckButton.setEnabled(true);
                            refreshImageView.setEnabled(true);
                            hideProgressDialog();
                        }
                    }
                });
    }


    boolean firstItemSelection = true;
    private void initFields(){

        spinner = findViewById(R.id.employees_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstItemSelection){
                    firstItemSelection = false;
                    return;
                }
                Map<String, String> properties = new HashMap<>();
                properties.put("defaultUser", spinner.getSelectedItem().toString());
                writeProperties(properties);
                refreshEventsView();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        eventsCheckButton = findViewById(R.id.event_check_btn);
        eventsCheckButton.setText("Initializing");
        eventsCheckButton.setEnabled(false);
        refreshImageView = findViewById(R.id.events_refresh_btn);
        refreshImageView.setEnabled(false);
        eventsWebView = findViewById(R.id.events_webview);

    }

    private void addItemsToSpinner(List<String> list){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void selectLastSavedUser(){
        Map<String, String> properties = readProperties();
        if (properties != null){
            String selectedUser = properties.get("defaultUser");
            if (items.containsKey(selectedUser)){
                int pos = ((ArrayAdapter) spinner.getAdapter()).getPosition(selectedUser);
                spinner.setSelection(pos);
            }
        }
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.event_check_btn:
                String key = spinner.getSelectedItem().toString();
                String username = items.get(key);
                if (username != null && !username.isEmpty()){
                    insertCheck(username);
                }else{
                    Toast.makeText(this, "Error 134", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.events_refresh_btn:
                refreshEventsView();
                break;

             default:
                 break;
        }
    }

    private void refreshEventsView(){
        String summary = "<html><body>Loading...</body></html>";
        eventsWebView.loadDataWithBaseURL(null, summary, "text/html", "utf-8", null);
        String htmlReply;
        try {
            String username = items.get(spinner.getSelectedItem().toString());
            htmlReply = pontajAPI.getEventsHTMLTable(username);
        } catch (Exception e) {
            e.printStackTrace();
            htmlReply = "<html><body>Error...</body></html>";
        }
//
//        final String finalHtmlReply = htmlReply;
//        TimerTask tt = new TimerTask() {
//            @Override
//            public void run() {
//                eventsWebView.loadDataWithBaseURL(null, finalHtmlReply, "text/html", "utf-8", null);
//            }
//        };
//        Timer timer = new Timer();
//        timer.schedule(tt, 2000);
        eventsWebView.loadDataWithBaseURL(null, htmlReply, "text/html", "utf-8", null);
    }

    private void insertCheck(String username) {
        try {
            pontajAPI.insertCheck(username);
        } catch (IOException e) {
            Toast.makeText(this, "Error..", Toast.LENGTH_LONG).show();
        }
    }

    private boolean writeProperties(Map<String, String> properties){
        if (properties.isEmpty())
            return false;
        StringBuffer text = null;
        boolean isFirstElement = true;
        for (String key : properties.keySet()){
            if (isFirstElement){
                text = new StringBuffer(key + ":" + properties.get(key));
                isFirstElement = false;
            }else{
                text.append("\n" + key + ":" + properties.get(key));
            }
        }
        FileOutputStream fos = null;
        try{
            fos = openFileOutput(PROPERTIES_FOLDER_NAME, MODE_PRIVATE);
            fos.write(text.toString().getBytes());
            fos.close();
        }catch (Exception e){
            return false;
        }

        return true;
    }

    private Map<String, String> readProperties(){
        Map<String, String> result = null;
        FileInputStream fis = null;
        try{
            fis = openFileInput(PROPERTIES_FOLDER_NAME);
            if (fis != null){
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                result = new HashMap<>();
                while ((line = br.readLine()) != null){
                    String[] splitted = line.split(":");
                    result.put(splitted[0], splitted[1]);
                }
                br.close();
                isr.close();
                fis.close();
            }

            return result;
        }catch (Exception e){
            return null;
        }
    }



}
