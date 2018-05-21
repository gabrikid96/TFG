package grodrich7.tfg.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;


import grodrich7.tfg.R;

public class MainActivity extends HelperActivity {
    private ProgressBar progressBar;
    private ProgressBar loginProgressBar;
    private ImageView logoImage;
    private LinearLayout loginLayout;
    private Button loginButton;

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
        loginProgressBar = findViewById(R.id.loginProgressBar);
        loginLayout = findViewById(R.id.loginLayout);
        loginButton = findViewById(R.id.btn_login);

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

    public void login(final View v){
        if (!checkInputs()) return;
        switchViews(false);
        hideKeyboard();
        mAuth.signInWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        updateUI(task.isSuccessful() ? mAuth.getCurrentUser() : null);
                        switchViews(true);
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        switchViews(true);
                        showErrorMessage(e, emailInput);
                    }
                });
    }

    private void switchViews(boolean action){
        if (!action){
            loginProgressBar.bringToFront();
            loginProgressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(action);
            loginButton.setText("");
        }else{
            loginProgressBar.setVisibility(View.GONE);
            loginButton.setEnabled(action);
            loginButton.setText(R.string.action_login);
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user == null)animationDelay(2000);
        else{
            progressBar.setVisibility(View.GONE);
            launchIntent(HomeActivity.class, TRANSITION_RIGHT);
            finish();
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
            int [] location = new int[2];
            loginLayout.getLocationOnScreen(location);
            logoImage.animate().y(100).setDuration(1500);
            loginLayout.setVisibility(View.VISIBLE);
            loginLayout.setAlpha(0.0f);
            loginLayout.animate().alpha(1.0f).setDuration(2000);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_RESULT) {
            //IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                updateUI(mAuth.getCurrentUser());
            } else if (resultCode == RESULT_CANCELED)
                restarted = true;
            else {

            }
        }
    }

    public void register(View v){
        launchIntentForResult(RegisterActivity.class, TRANSITION_RIGHT, LOGIN_RESULT);
    }

    /**
     * Metodo encargado de gestionar el reestablecimiento de contraseña
     *
     * @param v
     */
    public void forgotPassword(View v){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.recover_password_title);
        final EditText email = new EditText(this);
        email.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        email.setHint(R.string.prompt_email);
        alert.setView(email);


        alert.setPositiveButton(R.string.send_email, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(email.getText().toString().isEmpty()) {
                    email.setError(getString(R.string.error_field_required));
                    requestFocus(email);
                }else if (!isValidEmail(email.getText().toString())){
                    email.setError(getString(R.string.error_invalid_email));
                    requestFocus(email);
                }else{
                    sendForgotPasswordEmail(email.getText().toString());
                }
            }
        });
        alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void sendForgotPasswordEmail(final String email){
        hideKeyboard();
        mAuth.fetchProvidersForEmail(email).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                if (task.isSuccessful()) {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Snackbar.make(findViewById(android.R.id.content), R.string.email_sent, Snackbar.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            }).addOnFailureListener(MainActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showErrorMessage(e, emailInput);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                }
            }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showErrorMessage(e, emailInput);
                    progressBar.setVisibility(View.GONE);
                }
            });
    }



    //region INPUT VALIDATIONS
    public boolean checkInputs(){
        boolean isCorrect = true;
        emailInput.setError(null);
        passwordInput.setError(null);

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (password.isEmpty() || !isValidPassword(password)){
            passwordInput.setError(getString(R.string.error_invalid_password));
            requestFocus(passwordInput);
            isCorrect = false;
        }

        if (email.isEmpty()){
            emailInput.setError(getString(R.string.error_field_required));
            requestFocus(emailInput);
            isCorrect = false;
        }else if (!isValidEmail(email)){
            emailInput.setError(getString(R.string.error_invalid_email));
            requestFocus(emailInput);
            isCorrect = false;
        }
        return isCorrect;
    }
    //endregion
    //region GoogleAuth
    /*
    private GoogleApiClient mGoogleApiClient;

            findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
    Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        restarted = false;
    }
private void loginGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {

                            // If sign in fails, display a message to the user.
                            Snackbar.make(emailInput, "Authentication failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                loginGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }

        public void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }*/
    //endregion
 }