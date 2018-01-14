package grodrich7.tfg.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.zzn;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import grodrich7.tfg.BuildConfig;
import grodrich7.tfg.Models.User;
import grodrich7.tfg.R;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private AutoCompleteTextView nameInput;
    private AutoCompleteTextView emailInput;
    private EditText passwordInput;
    private ProgressBar progressBar;
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
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
    }

    public void register(View v){
        if (!checkInputs()){
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Snackbar.make(passwordInput,"Good register", Snackbar.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Snackbar.make(passwordInput,"Bad register", Snackbar.LENGTH_SHORT).show();
                            handleError(task);
                            //updateUI(null);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void handleError(Task<AuthResult> task) {
        String errorMessage = task.getException().getMessage();
    }

    private void updateUI(FirebaseUser user) {
        if (user == null){

        }else{
            writeNewUser(user.getUid(), nameInput.getText().toString(), user.getEmail());
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
    }

    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(userId).setValue(user);
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
            isCorrect = false;
        }

        if (email.isEmpty()){
            emailInput.setError(getString(R.string.error_field_required));
            isCorrect = false;
        }else if (!isValidEmail(email)){
            emailInput.setError(getString(R.string.error_invalid_email));
            isCorrect = false;
        }

        if (name.isEmpty()){
            nameInput.setError(getString(R.string.error_field_required));
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

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        overridePendingTransition(R.anim.transition_right_in, R.anim.transition_right_out);
        finish();
    }
}