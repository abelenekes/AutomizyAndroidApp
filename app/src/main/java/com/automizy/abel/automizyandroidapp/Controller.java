package com.automizy.abel.automizyandroidapp;

import com.automizy.abel.automizyandroidapp.API.ApiManager;

/**
 * Created by Abel on 2016. 04. 24..
 */
public class Controller {
    private HomeActivity activity = null;
    private String client_id = "";
    private String client_secret = "";

    public Controller(HomeActivity activity, String client_id, String client_secret){
        this.activity = activity;
        this.client_id = client_id;
        this.client_secret = client_secret;
    }

    public void login(){
        ApiManager.Token.getAccessToken(client_id, client_secret, new ApiManager.RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                System.out.println(result);
            }
        });
    }
}
