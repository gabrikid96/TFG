package grodrich7.tfg.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import grodrich7.tfg.BuildConfig;
import grodrich7.tfg.Controller;
import grodrich7.tfg.R;

public class MainActivity extends HelperActivity {
    private ProgressBar progressBar;
    private ImageView logoImage;
    private LinearLayout loginLayout;

    /**Login Views**/
    private AutoCompleteTextView emailInput;
    private EditText passwordInput;

    /**Auth**/
    private FirebaseAuth mAuth;

    private boolean restarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        restarted = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (!restarted)
            updateUI(currentUser);
        else{
            if (currentUser != null)
                launchIntent(HomeActivity.class, TRANSITION_RIGHT);
        }
    }

    protected void getViewsByXML() {
        setContentView(R.layout.activity_main);
        /*TITLE LAYOUT*/
        logoImage = findViewById(R.id.logoImage);
        progressBar = findViewById(R.id.progressBar);
        loginLayout = findViewById(R.id.loginLayout);

        /*LOGIN LAYOUT*/
        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);

        passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    login(v);
                    handled = true;
                }
                return handled;
            }
        });
    }

    public void login(View v){
        if (!checkInputs()) return;

        mAuth.signInWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        updateUI(task.isSuccessful() ? mAuth.getCurrentUser() : null);
                    }
                });
    }


    private void updateUI(FirebaseUser user) {
        if (user == null)animationDelay(2000);
        else{
            progressBar.setVisibility(View.GONE);
            launchIntent(HomeActivity.class, TRANSITION_RIGHT);
        }
    }

    private void animationDelay(final long milis){
        Thread timerThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(milis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loginAnimation();
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }
            }
        });
        timerThread.start();
    }

    private void loginAnimation(){
        if (loginLayout.getVisibility() != View.VISIBLE) {
            logoImage.animate().y(100).setDuration(1500);
            loginLayout.setVisibility(View.VISIBLE);
            loginLayout.setAlpha(0.0f);
            loginLayout.animate().alpha(1.0f).setDuration(2000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_RESULT) {
            if(resultCode == Activity.RESULT_OK)
                updateUI(mAuth.getCurrentUser());

            if (resultCode == Activity.RESULT_CANCELED)
                restarted = true;
        }
    }

    public void register(View v){
        launchIntentForResult(RegisterActivity.class, TRANSITION_RIGHT, LOGIN_RESULT);
    }

    //region INPUT VALIDATIONS
    public boolean checkInputs(){
        boolean isCorrect = true;
        emailInput.setError(null);
        passwordInput.setError(null);

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        if (email.isEmpty()){
            emailInput.setError(getString(R.string.error_field_required));
            emailInput.setFocusableInTouchMode(true);
            emailInput.requestFocus();
            isCorrect = false;
        }else if (!isValidEmail(email)){
            emailInput.setError(getString(R.string.error_invalid_email));
            emailInput.setFocusableInTouchMode(true);
            emailInput.requestFocus();
            isCorrect = false;
        }

        if (password.isEmpty() || !isValidPassword(password)){
            passwordInput.setError(getString(R.string.error_invalid_password));
            passwordInput.setFocusableInTouchMode(true);
            passwordInput.requestFocus();
            isCorrect = false;
        }


        return isCorrect;
    }
    //endregion
}