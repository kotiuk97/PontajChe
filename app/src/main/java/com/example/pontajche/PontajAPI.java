package com.example.pontajche;

import android.content.Context;
import android.os.AsyncTask;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class PontajAPI {


    private WebView webView;
    private Context context;
    private final String DBAdminLoginPass = "chreniuc:68S3PpmGaJfr6";
    private final String DBAdminLoginPassEncoded = "Y2hyZW5pdWM6NjhTM1BwbUdhSmZyNg==";


    public PontajAPI(Context context) {
        this.context = context;
        initWebView();
    }

    private void initWebView(){
        webView = new WebView(context);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
    }

    private String getCurrentDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        return dateFormat.format(date);
    }

    private String getURLCheckRequest(String userName){

        return "http://" + DBAdminLoginPass + "@pontaj.computervoice.ro:5544/files/evidenta_online.php?combo=" + userName + "&checkday=on&data_combo=" + getCurrentDate() + "&insert_check=do+it!";
    }

    public void insertCheck(String userName) throws IOException {
        String url = getURLCheckRequest(userName);
        webView.loadUrl(url);
    }

    public void setWebChromeClient(WebViewClient webViewClient){
        webView.setWebViewClient(webViewClient);
    }

    public int getNumberOfChecks(String userName) throws IOException, ExecutionException, InterruptedException {
        String firstName, lastName;
        lastName = userName.split(" ")[0];
        firstName = userName.split(" ")[1];

        URL url;
        url = new URL("http://" + DBAdminLoginPass + "@pontaj.computervoice.ro:5544/files/evidenta.php?combo=" + lastName + "+" + firstName + "&checkday=on&data_combo=" + getCurrentDate());

        EventsCounter socket = new EventsCounter();
        socket.execute(new URL[]{url});
        return  socket.get();
    }

    public String getEventsHTMLTable(String cryptedName) throws MalformedURLException, ExecutionException, InterruptedException {

        URL url;
        url = new URL("http://" + DBAdminLoginPass + "@pontaj.computervoice.ro:5544/files/evidenta.php?combo=" + cryptedName + "&checkday=on&data_combo=" + getCurrentDate());
        EventsTable events = new EventsTable();
        events.execute(new URL[]{url});
        return events.get();
    }

    class EventsCounter extends AsyncTask<URL, Void, Integer> {

        int result = 0;

        @Override
        protected Integer doInBackground(URL... urls) {
            try {
                URLConnection uc = urls[0].openConnection();
                uc.setRequestProperty("Authorization", String.format("Basic %s", DBAdminLoginPassEncoded));
                BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    if (inputLine.contains("<table id=\"table\"")){
                        while (!(inputLine = br.readLine()).contains("</table>")){
                            if (inputLine.contains("</tr>")){
                                result++;
                            }
                        }
                        break;
                    }
                }
                br.close();
            }catch (Exception e){
                result = -1;
            }
            if (result == 0)
                return 0;
            else
                return result - 1;
        }

    }

    class EventsTable extends AsyncTask<URL, Void, String>{

        boolean isHeaderRow = true;
        boolean eventIn = true;
        int columns = 0;
        String result;
        @Override
        protected String doInBackground(URL... params) {
            try{
                URLConnection uc = params[0].openConnection();
                uc.setRequestProperty("Authorization", String.format("Basic %s", DBAdminLoginPassEncoded));
                BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                String inputLine;

                while ((inputLine = br.readLine()) != null) {
                    if (inputLine.contains("<table id=\"table\"")){
                        result = inputLine;
                        while (!(inputLine = br.readLine()).contains("</table>")){

                            //using it to display only first 2 columns
                            if (inputLine.contains("<th>") || inputLine.contains("<td>")){
                                columns++;

                                // if it is first or second column, add it to result
                                if (columns < 3)
                                    result += inputLine;
                                // at the end of the table row, set column count to 0
                                if (columns == 7)
                                    columns = 0;
                            }else{

                                // if it is table row, we use green color for InEvent, and red color for OutEvent
                                if (inputLine.contains("<tr>") && !isHeaderRow){
                                    if (eventIn){
                                        inputLine = inputLine.replace("<tr","<tr style='color: green'");
                                        eventIn = false;
                                    }else{
                                        inputLine = inputLine.replace("<tr","<tr style='color: red'");
                                        eventIn = true;
                                    }
                                    result += inputLine;
                                }else{
                                    //after we found header row (first row in a table), set flag to false
                                    if (inputLine.contains("<tr") && isHeaderRow)
                                        isHeaderRow = false;
                                    result +=inputLine;
                                }
                            }

                        }

                        // add close tag for the table
                        result +=inputLine;

                        //looking for a total time
                        boolean totalTimeDiv = false;
                        while ((inputLine = br.readLine()) != null){
                            if (inputLine.contains("id = 'total_time'")){
                                totalTimeDiv = true;
                            }
                            if (totalTimeDiv){
                                if (inputLine.contains("</div>")){
                                    result += inputLine;
                                    break;
                                }else{
                                    result += inputLine;
                                }

                            }

                        }
                        break;
                    }
                }
            }catch (Exception e){
                return "Oop...an error occur";
            }

            if (result == null || result.isEmpty())
                return "There is no events today";
            else{
                //               result = result.replaceAll("\\s+","");
                result = result.replace("Ora","Time");
                return result;
            }


        }
    }

}
