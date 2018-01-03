package grodrich7.tfg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView logoImage;
    private LinearLayout loginLayout;

    /*Login Views*/
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
        if (!restarted){
            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        restarted = true;
        Toast.makeText(this, "Restart", Toast.LENGTH_SHORT);
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

    public void register(View v){
        Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.transition_left_in, R.anim.transition_left_out);
    }

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

    private void updateUI(FirebaseUser user) {
        if (loginLayout.getVisibility() == View.VISIBLE && user == null){
            Snackbar.make(logoImage,"Bad Login", Snackbar.LENGTH_SHORT).show();
        }
        else if (user == null){
            animationDelay(2000);
        }else{
            //TODO : App home
            progressBar.setVisibility(View.GONE);
            Snackbar.make(logoImage,"Good Login", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void getViewsByXML() {
        /*TITLE LAYOUT*/
        logoImage = findViewById(R.id.logoImage);
        progressBar = findViewById(R.id.progressBar);
        loginLayout = findViewById(R.id.loginLayout);

        /*LOGIN LAYOUT*/
        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);
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
                            Snackbar.make(logoImage,"Bad Login", Snackbar.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
        timerThread.start();
    }

    private void loginAnimation(){
        if (loginLayout.getVisibility() != View.VISIBLE) {
            logoImage.animate().y(0).setDuration(1500);
            loginLayout.setVisibility(View.VISIBLE);
            loginLayout.setAlpha(0.0f);
            loginLayout.animate().alpha(1.0f).setDuration(2000);
        }
    }

    public boolean isValidPassword(String password){
        return password.length() > 4;
    }

    public boolean isValidEmail(String email){
        return email.contains("@");
    }

}
