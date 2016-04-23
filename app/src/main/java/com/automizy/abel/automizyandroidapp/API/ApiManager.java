package com.automizy.abel.automizyandroidapp.API;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private class ApiRequest extends AsyncTask<ApiRequestParams, Void, Boolean>{

        @Override
        protected Boolean doInBackground(ApiRequestParams... params) {
            ApiRequestParams requestParams = params[0];
            HttpURLConnection con = null;
            try {
                URL url = new URL(requestParams.getUrl());
                con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", requestParams.getContentType());
                con.setRequestProperty("Accept", requestParams.getAccpet());
                con.setRequestMethod(requestParams.getType());

                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(requestParams.getData().toString());
                wr.flush();

                StringBuilder sb = new StringBuilder();
                int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    System.out.println("" + sb.toString());
                } else {
                    System.out.println(con.getResponseMessage());
                }
                return true;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            finally {
                if(con != null){
                    con.disconnect();
                }
            }

            return false;
        }
    }

}
