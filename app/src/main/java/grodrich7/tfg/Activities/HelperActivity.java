package grodrich7.tfg.Activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import grodrich7.tfg.Activities.Services.NotificationService;
import grodrich7.tfg.Controller.Controller;
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
    private static final int PLAY_SERVICES_RES_REQUEST = 7172;

    protected Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isServiceRunning(NotificationService.class)) stopService(new Intent(this, NotificationService.class));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
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
        return password.length() >= 6;
    }
//endregion

    public String parseString(Boolean bool){
        return bool != null && bool ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void showErrorMessage(@NonNull Exception e, View v){
        String message = e.getLocalizedMessage();
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            switch (((FirebaseAuthInvalidCredentialsException) e).getErrorCode()){
                case "ERROR_WRONG_PASSWORD":
                    message = getResources().getString(R.string.error_message_password);
                    break;
            }

        } else if (e instanceof FirebaseAuthInvalidUserException) {
            switch (((FirebaseAuthInvalidUserException) e).getErrorCode()){
                case "ERROR_USER_NOT_FOUND":
                    message = getResources().getString(R.string.fui_error_email_does_not_exist);
                    break;
                case "ERROR_USER_DISABLED":
                    message = getResources().getString(R.string.error_message_disabled);
                    break;
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    message = getResources().getString(R.string.error_message_email_in_use);
                    break;
            }
        }else if (e instanceof FirebaseNetworkException){
            message = getResources().getString(R.string.no_connection);
        }

        Snackbar.make(v, message,Snackbar.LENGTH_SHORT).show();
    }


    protected void requestFocus(View v){
        v.setFocusableInTouchMode(true);
        v.requestFocus();
    }

    protected void hideKeyboard(){
        try{
            InputMethodManager inputMethodManager = (InputMethodManager) HelperActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(HelperActivity.this.getCurrentFocus().getWindowToken(), 0);
        }catch (Exception e){
            Log.e("KEYBOARD", e.getMessage());
        }

    }

    protected boolean checkPlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RES_REQUEST).show();
            }else{

            }
            return false;
        }
        return true;
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void pressEffect(View button){
        AlphaAnimation buttonAnimation = new AlphaAnimation(0.2f, 1.0f);
        buttonAnimation.setDuration(250);
        button.startAnimation(buttonAnimation);
    }



    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.transition_right_in, R.anim.transition_right_out);
        finish();
    }

    protected abstract void getViewsByXML();
}
