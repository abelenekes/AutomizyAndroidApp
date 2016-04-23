package com.automizy.abel.automizyandroidapp.API;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Abel on 2016. 04. 15..
 */
public class ApiManager {
    private String client_secret;
    private String client_id;
    private URL authUrl;
    private String redirectUri;

    public ApiManager(){
        redirectUri = new String("http://localhost");
    }


    public void getAccesToken() throws IOException, JSONException {

        HttpURLConnection con = (HttpURLConnection) authUrl.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");

        JSONObject data = new JSONObject();

        data.put("app_id", client_id);
        data.put("app_secret", client_secret);
        data.put("redirect_uri",redirectUri);

        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
        wr.write(data.toString());
        wr.flush();
        wr.close();

    }


    private class ApiRequestParams {
        private String type;
        private String url;
        private String accpet;
        private String content_type;
        private JSONObject data;

        public ApiRequestParams(String type, String url, JSONObject data){
            this.type = type;
            this.url = url;
            this.data = data;
            this.content_type = "application/json";
            this.accpet = "application/json";
        }


        public ApiRequestParams(String type, String url, String accpet, String content_type, JSONObject data){
            this.type = type;
            this.url = url;
            this.accpet = accpet;
            this.content_type = content_type;
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }

        public String getAccpet() {
            return accpet;
        }

        public String getContentType() {
            return content_type;
        }

        public JSONObject getData() {
            return data;
        }
    }

    private class ApiRequest extends AsyncTask<ApiRequestParams, Void, String>{

        @Override
        protected String doInBackground(ApiRequestParams... params) {

            //The result json string
            String result = null;

            ApiRequestParams requestParams = params[0];
            HttpURLConnection urlConnection = null;
            try {

                URL url = null;
                try {
                    url = new URL(requestParams.getUrl());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", requestParams.getContentType());
                    urlConnection.setRequestProperty("Accept", requestParams.getAccpet());
                    urlConnection.setRequestMethod(requestParams.getType());
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


                //Sending the POST request
                OutputStream os = null;

                try {
                    os = urlConnection.getOutputStream();
                    os.write(requestParams.getData().toString().getBytes());
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
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
        }
    }

}
