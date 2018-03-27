package grodrich7.tfg.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import grodrich7.tfg.Controller;
import grodrich7.tfg.R;

/**
 * Created by gabri on 27/03/2018.
 */

public abstract class HelperActivity extends AppCompatActivity {

    public static final int TRANSITION_RIGHT = 1;
    public static final int TRANSITION_LEFT = 2;

    /*RESULTS*/
    public static final int LOGIN_RESULT = 1;
    public static final int GROUP_EDIT = 1;

    protected Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controller = Controller.getInstance();
        getViewsByXML();
    }

    //region INTENTS
    protected void launchIntent(Class<?> activity, int transition){
        Intent intent = new Intent(getApplicationContext(),activity);
        startActivity(intent);
        setTransition(transition);
    }

    private void setTransition(int transition){
        switch (transition){
            case TRANSITION_RIGHT:
                overridePendingTransition(R.anim.transition_left_in, R.anim.transition_left_out);
                break;
            case TRANSITION_LEFT:
                overridePendingTransition(R.anim.transition_right_in, R.anim.transition_right_out);
                break;
        }
    }

    protected void launchIntentForResult(Class<?> activity, int transition, int result){
        Intent intent = new Intent(getApplicationContext(),activity);
        startActivityForResult(intent, result);
        setTransition(transition);
    }
    //endregion

    //region TOOLBAR
    protected void enableToolbar(int resId){
        setToolbar(getResources().getString(resId));
    }

    private void setToolbar(String title){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    protected void enableToolbar(String title){
        setToolbar(title);
    }
    //endregion

    //region INPUT VALIDATIONS
    public boolean isValidEmail(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
    public boolean isValidPassword(String password){
        return password.length() > 4;
    }
//endregion

    public String parseString(boolean bool){
        return bool ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.transition_right_in, R.anim.transition_right_out);
        finish();
    }

    protected abstract void getViewsByXML();
}
