package grodrich7.tfg.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import grodrich7.tfg.BuildConfig;
import grodrich7.tfg.Controller;
import grodrich7.tfg.R;

public class MainActivity extends AppCompatActivity {

    public static final int LOGIN_RESULT = 1;
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
        setContentView(R.layout.activity_main);
        getViewsByXML();
        mAuth = FirebaseAuth.getInstance();
        restarted = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (!restarted){
            // Check if user is signed in (non-null) and update UI accordingly.
            updateUI(currentUser);
        }else{
            if (currentUser != null){
                goHome();
            }
        }
    }

    private void getViewsByXML() {
        /*TITLE LAYOUT*/
        logoImage = (ImageView) findViewById(R.id.logoImage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        loginLayout = (LinearLayout) findViewById(R.id.loginLayout);

        logoImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (BuildConfig.DEBUG && mAuth.getCurrentUser() != null) {
                    Snackbar.make(logoImage,"Log Out " + mAuth.getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();
                    mAuth.signOut();
                    updateUI(null);
                }

                return false;
            }
        });

        /*LOGIN LAYOUT*/
        emailInput = (AutoCompleteTextView) findViewById(R.id.input_email);
        passwordInput = (EditText) findViewById(R.id.input_password);
    }

    public void login(View v){
        if (!checkInputs()){
            return;
        }
        mAuth.signInWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        updateUI(task.isSuccessful() ? mAuth.getCurrentUser() : null);
                    }
                });
    }


    private void updateUI(FirebaseUser user) {
        if (loginLayout.getVisibility() == View.VISIBLE && user == null){
            Snackbar.make(logoImage,"Bad Login", Snackbar.LENGTH_SHORT).show();
        }
        else if (user == null){
            animationDelay(2000);
        }else{
            progressBar.setVisibility(View.GONE);
            goHome();
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

    /**Activities**/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode ==  LOGIN_RESULT) {
            if(resultCode == Activity.RESULT_OK){
                setContentView(R.layout.activity_main);
                updateUI(mAuth.getCurrentUser());
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                restarted = true;
            }
        }
    }

    public void register(View v){
        Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
        startActivityForResult(intent, LOGIN_RESULT);
        overridePendingTransition(R.anim.transition_left_in, R.anim.transition_left_out);
    }


    public void goHome(){
        Intent intent = new Intent(MainActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.transition_left_in, R.anim.transition_left_out);
    }

    /**Validations**/

    public boolean checkInputs(){
        boolean isCorrect = true;
        emailInput.setError(null);
        passwordInput.setError(null);

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (password.isEmpty() || !isValidPassword(password)){
            passwordInput.setError(getString(R.string.error_invalid_password));
            isCorrect = false;
        }

        if (email.isEmpty()){
            emailInput.setError(getString(R.string.error_field_required));
            isCorrect = false;
        }else if (!isValidEmail(email)){
            emailInput.setError(getString(R.string.error_invalid_email));
            isCorrect = false;
        }
        return isCorrect;
    }

    public boolean isValidPassword(String password){
        return password.length() > 4;
    }

    public boolean isValidEmail(String email){
        return email.contains("@");
    }


}