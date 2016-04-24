package com.automizy.abel.automizyandroidapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Random;

/**
 * Created by Abel on 2016. 04. 20..
 */
public class AuthActivity extends Activity {

    private final String OAUTH_URL = "https://api.automizy.com/oauth";
    private final String REDIRECT_URI = "automizy.android.scheme://AutomizyAndoidApp";
    private final String APP_ID = PRIVATE_CONSTANTS.app_id;
    private final String APP_SECRET = PRIVATE_CONSTANTS.app_secret;
    private String STATE;

    //WebView that will open when redirecting user to auth url
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity);
        webView = (WebView) findViewById(R.id.auth_web_view);

        STATE = getRandomString(15);
        makeAuthRequest();
    }

    //Making authorization request
    //Server will send a request to the url specified by REDIRECT_URI
    public void makeAuthRequest(){
        webView.loadUrl("about:blank");

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new AuthWebViewClient());

        webView.loadUrl(OAUTH_URL +
                        "/authorize?redirect_uri=" + REDIRECT_URI +
                        "&response_type=code" +
                        "&app_id=" + APP_ID +
                        "&app_secret=" + APP_SECRET +
                        "&state=" + STATE
        );
    }

    //Returns the result to the MainActivity
    private void returnResult(String result){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",result);
        if(result != null) {
            setResult(Activity.RESULT_OK, returnIntent);
        }
        else{
            setResult(Activity.RESULT_CANCELED, returnIntent);
        }
        this.finish();
    }

    //Used for generating random string for STATE
    public String getRandomString(int length) {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        char tempChar;
        for (int i = 0; i < length; i++){
            tempChar = (char) (generator.nextInt(25) + 97);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }


    private class AuthWebViewClient extends WebViewClient {

        //The json that will contain acces token and client credentials & secret
        final JSONObject result = null;

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // Ignore SSL certificate errors
        }

        /*
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("d", "page started");
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("d", "page finished");
        }

        */

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String authorizationUrl) {
            //This method will be called when the Auth proccess redirect to our RedirectUri.
            //We will check the url looking for our RedirectUri.

            //If url starts with our redierct uri
            if(authorizationUrl.startsWith(REDIRECT_URI)){
                Uri uri = Uri.parse(authorizationUrl);


                //Checking if state tokens match
                String stateToken = uri.getQueryParameter("state");
                if(stateToken==null || !stateToken.equals(STATE)){
                    Log.e("Authorize", "State token doesn't match");
                    return true;
                }


                //If the user doesn't allow authorization to our application
                String error = uri.getQueryParameter("error");
                if(error != null){
                    Log.e("Authorize", error);
                    return true;
                }


                //Getting the authorization code
                final String authorizationCode = uri.getQueryParameter("code");
                if(authorizationCode == null){
                    Log.e("Authorize", "No authorization code recieved.");
                    return true;
                }
                else {
                    //We make the request for access token and client credentials in a AsyncTask
                    new AsyncTask<String, Void, String>() {
                        protected String doInBackground(String... args) {

                            //The result json string
                            String result = null;

                            HttpURLConnection urlConnection = null;
                            try {

                                URL url = null;
                                try {
                                    url = new URL(args[0]);
                                    urlConnection = (HttpURLConnection) url.openConnection();
                                    urlConnection.setDoOutput(true);
                                    urlConnection.setDoInput(true);
                                    urlConnection.setRequestProperty("Content-Type", "application/json");
                                    urlConnection.setRequestProperty("Accept", "application/json");
                                    urlConnection.setRequestMethod("POST");
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                    return null;
                                } catch (ProtocolException e) {
                                    e.printStackTrace();
                                    return null;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return null;
                                }


                                //Putting all data in JSON object
                                JSONObject data = new JSONObject();
                                try {
                                    data.put("app_id", APP_ID);
                                    data.put("app_secret", APP_SECRET);
                                    data.put("redirect_uri", REDIRECT_URI);
                                    data.put("code", authorizationCode);
                                    data.put("grant_type", "authorization_code");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    return null;
                                }

                                //Sending the POST request
                                OutputStream os = null;

                                try {
                                    os = urlConnection.getOutputStream();
                                    os.write(data.toString().getBytes());
                                    os.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return null;
                                } finally{
                                    os.close();
                                }

                                //Reading the response
                                InputStream is = null;
                                BufferedReader rd  = null;
                                StringBuilder sb = null;
                                String line = null;

                                try {
                                    //Checking response code
                                    int responseCode = urlConnection.getResponseCode();

                                    //If request was successful reading response
                                    if (responseCode == HttpURLConnection.HTTP_OK) {
                                        is = urlConnection.getInputStream();
                                        rd = new BufferedReader(new InputStreamReader(is));
                                        sb = new StringBuilder();
                                        while ((line = rd.readLine()) != null) {
                                            sb.append(line + '\n');
                                        }
                                    } else {

                                        //IF request wasnt successful logging error
                                        Log.e("ResponseMessage", ((Integer) responseCode).toString() + " - " + urlConnection.getResponseMessage());
                                        is = urlConnection.getErrorStream();
                                        rd = new BufferedReader(new InputStreamReader(is));
                                        sb = new StringBuilder();
                                        while ((line = rd.readLine()) != null) {
                                            sb.append(line + '\n');
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //If everything went well we convert the response to json string
                               result = sb.toString();

                            }catch (Exception e){
                                e.printStackTrace();
                                return null;
                            }
                            finally {
                                urlConnection.disconnect();
                                return result;
                            }
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            super.onPostExecute(result);
                            AuthActivity.this.returnResult(result);
                        }

                    }.execute(OAUTH_URL);
                }
            }else{
                //If there's no redirect uri in the url
                view.loadUrl(authorizationUrl);
            }
            return true;
        }
    }
}
