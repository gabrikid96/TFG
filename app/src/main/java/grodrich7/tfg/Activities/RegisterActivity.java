package grodrich7.tfg.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import grodrich7.tfg.BuildConfig;
import grodrich7.tfg.Controller.Controller;
import grodrich7.tfg.Models.User;
import grodrich7.tfg.R;

public class RegisterActivity extends HelperActivity {
    private FirebaseAuth mAuth;
    private AutoCompleteTextView nameInput;
    private AutoCompleteTextView emailInput;
    private EditText passwordInput;
    private ProgressBar progressBar;
    private Button registerBtn;
    private ProgressBar registerProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void getViewsByXML() {
        setContentView(R.layout.activity_register);
        enableToolbar(R.string.action_register);
        nameInput = findViewById(R.id.input_name);
        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);
        progressBar = findViewById(R.id.progressBar);
        registerBtn = findViewById(R.id.btn_register);
        registerBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (BuildConfig.DEBUG) {
                    nameInput.setText("Gabriel");
                    emailInput.setText("admin@admin.com");
                    passwordInput.setText("admin1");
                }
                return false;
            }
        });

        passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    register(v);
                    handled = true;
                }
                return handled;
            }
        });

        registerProgressBar = findViewById(R.id.registerProgressBar);
    }

    public void register(final View v){
        pressEffect(v);
        if (!checkInputs())return;
        if (!isOnline()){
            Snackbar.make(registerBtn,R.string.no_connection, Snackbar.LENGTH_SHORT).show();
            return;
        }
        switchViews(false);
        hideKeyboard();
        mAuth.createUserWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }
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
            registerProgressBar.bringToFront();
            registerProgressBar.setVisibility(View.VISIBLE);
            registerBtn.setEnabled(action);
            registerBtn.setText("");
        }else{
            registerProgressBar.setVisibility(View.GONE);
            registerBtn.setEnabled(action);
            registerBtn.setText(R.string.action_register);
        }

    }


    private void updateUI(FirebaseUser user) {
        if (user != null){
            writeNewUser(user.getUid(), nameInput.getText().toString(), user.getEmail());
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
    }

    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);
        Controller.getInstance().getUsersReference().child(userId).setValue(user);
    }

    public boolean checkInputs(){
        boolean isCorrect = true;
        emailInput.setError(null);
        passwordInput.setError(null);
        nameInput.setError(null);

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        String name = nameInput.getText().toString();

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
        if (name.isEmpty()){
            nameInput.setError(getString(R.string.error_field_required));
            requestFocus(nameInput);
            isCorrect = false;
        }
        return isCorrect;
    }

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        super.onBackPressed();
    }
}