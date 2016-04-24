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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Abel on 2016. 04. 15..
 */
public class ApiManager {
    private static final String API_BASE_URL = "https://api.automizy.com/";
    private static String ACCES_TOKEN = "";

    private static ApiManager instance = null;

    private ApiManager(){}

    public static ApiManager getInstance(){
        if(instance == null){
            instance=new ApiManager();
        }
        return instance;
    }



    private static class ApiRequestParams {
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


    private static class ApiRequest{

        private RestTaskCallback restTaskCallback;
        private ApiRequestParams params;
        private AsyncTask asyncTask = null;

        public ApiRequest(ApiRequestParams params, RestTaskCallback restTaskCallback){
            this.restTaskCallback = restTaskCallback;
            this.params = params;

            asyncTask = new AsyncTask<Object, Void, String>() {
                @Override
                protected String doInBackground(Object... params) {

                    //The result json string
                    String result = null;

                    ApiRequestParams requestParams = ApiRequest.this.params;
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
                    ApiRequest.this.restTaskCallback.onTaskComplete(result);
                    super.onPostExecute(result);
                }

            };
        }

        public void execute(){
            asyncTask.execute();
        }

    }

    public abstract static class RestTaskCallback{
        public abstract void onTaskComplete (String result);
    }

    public static class Token{

        public static void getAccessToken(String client_id, String client_secret, RestTaskCallback callback){
            JSONObject data = new JSONObject();
            try {
                data.put("client_id",client_id);
                data.put("client_secret",client_secret);
                data.put("grant_type","client_credentials");
                System.out.println(data.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            ApiRequestParams params = new ApiRequestParams("POST",API_BASE_URL+"oauth",data);
            ApiRequest apiRequest = new ApiRequest(params,callback);
            apiRequest.execute();
        }
    }

}
