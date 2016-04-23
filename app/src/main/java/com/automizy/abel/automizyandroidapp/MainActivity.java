package com.automizy.abel.automizyandroidapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Button login;
    private UserManager userManager;
    private Intent authIntent;
    private Intent loginIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        authIntent = new Intent(MainActivity.this, AuthActivity.class);
        loginIntent = new Intent(MainActivity.this, HomeActivity.class);

        login = (Button) findViewById(R.id.loginButton);
        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startActivityForResult(authIntent, 1);
            }
        });

        userManager=new UserManager();


    }

    private void tryLogin(){
        JSONObject userData = userManager.getUserCredentials();
        if(userData != null){
            Log.d("LOGIN DATA", userData.toString());
            startActivity(loginIntent);
        }
        else{
            //If no user data was saved yet start AuthActivity
            Log.d("LOGIN ERROR", "No user data found");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MainActivity", "onActivityResult");

        //On return from AuthActivity
        if (requestCode == 1) {

            //If auth was successful
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra("result");
                Log.d("Result", result);
                try {
                    //Saving user data to shared preferences
                    JSONObject userData = new JSONObject(result);
                    userManager.setUserCredentials(userData.getString("client_id"),userData.getString("client_secret"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //If auth was unsuccessful
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                Log.d("Result", "RESULT_CANCELED");
            }
        }
    }

    @Override
    public void onResume() {

        Log.d("MainActivity", "onResume");
        super.onResume();
        tryLogin();
    }


    //Used for getting and setting user credentials
    private class UserManager{
        public static final String PREFERENCE_NAME = "USER_DATA";
        private SharedPreferences userData;

        public UserManager(){
            userData = getPreferences(MODE_PRIVATE);
        }

        //Setting user credentials
        //Only the last logged in user's credentials are saved
        public void setUserCredentials(String client_id, String client_secret){
            SharedPreferences.Editor prefsEditor = userData.edit();

            String data = null;
            try {
                JSONObject json=new JSONObject();
                json.put("client_id",client_id);
                json.put("client_secret",client_secret);

                data = json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Deleting last login data
            prefsEditor.clear();

            //Saving new data
            prefsEditor.putString(PREFERENCE_NAME,data);
            prefsEditor.commit();

        }


        //Getting the user credentials
        // If there are no users saved returns null
        public JSONObject getUserCredentials(){
            JSONObject data = null;
            try{
                data = new JSONObject(userData.getString(PREFERENCE_NAME, null));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            finally {
                return data;
            }
        }
    }

}
