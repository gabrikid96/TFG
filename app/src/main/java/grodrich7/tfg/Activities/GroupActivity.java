package grodrich7.tfg.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import grodrich7.tfg.Models.Constants;
import grodrich7.tfg.Models.Group;
import grodrich7.tfg.R;

public class GroupActivity extends AppCompatActivity {
    private Group group;
    private AutoCompleteTextView nameInput;
    private AutoCompleteTextView userAddInput;
    private CheckBox destination;
    private CheckBox location;
    private CheckBox driving;
    private CheckBox startTime;
    private CheckBox frontalImages;
    private CheckBox acceptCalls;
    private CheckBox parking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getViewsByXML();
        group = (Group)  getIntent().getSerializableExtra("group");
        if (group != null){
            nameInput.setText(group.getNameGroup());
        }else{
            group = new Group("");
        }
    }

    private void getViewsByXML() {
        nameInput = findViewById(R.id.input_name_group);
        userAddInput = findViewById(R.id.input_add_user);
        destination = findViewById(R.id.destinationToggle);
        location = findViewById(R.id.destinationToggle);
        driving = findViewById(R.id.destinationToggle);
        startTime= findViewById(R.id.destinationToggle);
        frontalImages = findViewById(R.id.destinationToggle);
        acceptCalls = findViewById(R.id.destinationToggle);
        parking = findViewById(R.id.destinationToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.save_action:
                saveGroup();

                break;
            default:
                break;
        }

        return true;
    }

    private boolean checkInputs(){
        if (nameInput.getText().toString().isEmpty()){
            nameInput.setError(getResources().getText(R.string.error_field_required));
            return false;
        }
        return true;
    }

    private void setPermissions(){
        group.changePermission(Constants.Data.DESTINATION, destination.isChecked());
        group.changePermission(Constants.Data.LOCATION, location.isChecked());
        group.changePermission(Constants.Data.DRIVING, driving.isChecked());
        group.changePermission(Constants.Data.IMAGES, frontalImages.isChecked());
        group.changePermission(Constants.Data.TRIP_TIME_START, startTime.isChecked());
        group.changePermission(Constants.Data.ACCEPT_CALLS, acceptCalls.isChecked());
        group.changePermission(Constants.Data.SEARCHING_PARKING, parking.isChecked());
    }

    private void saveGroup(){
        if (!checkInputs()) return;
        group.setNameGroup(nameInput.getText().toString());
        setPermissions();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("users").child(userUid).child("groups").push().setValue(group, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            if (databaseError != null) {
                Snackbar.make(nameInput, "Save bad", Toast.LENGTH_SHORT)
                        .show();
                Log.d("GROUPS", "Data could not be saved " + databaseError.getMessage());
            } else {
                Snackbar.make(nameInput, "Save good", Toast.LENGTH_SHORT)
                        .show();
                Log.d("GROUPS", "Group saved succesfully");
            }
            }
        });
    }

    public void handleButtons(View v){
        switch (v.getId()){
            case R.id.usersButton:
                showUsersDialog();
                break;
            case R.id.add_user_button:
                if (userAddInput.getText().toString().isEmpty()){
                    userAddInput.setError(getResources().getText(R.string.error_field_required));
                }else if (!isValidEmail(userAddInput.getText().toString())){
                    userAddInput.setError(getResources().getText(R.string.error_invalid_email));
                }else{
                    group.addUser(userAddInput.getText().toString());
                    Snackbar.make(userAddInput, getResources().getText(R.string.user_added), Snackbar.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

    }

    private void showUsersDialog(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(GroupActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.users_dialog, null);
        ListView userList = mView.findViewById(R.id.list_users);
        userList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, group.getUsers()));
        userList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();

        dialog.setTitle(R.string.users_added);
        dialog.show();
        dialog.getWindow().setLayout(600, 400);
    }

    public boolean isValidEmail(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.transition_right_in, R.anim.transition_right_out);
        finish();
    }
}
