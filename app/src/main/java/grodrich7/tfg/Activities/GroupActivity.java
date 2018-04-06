package grodrich7.tfg.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import grodrich7.tfg.Models.Constants;
import grodrich7.tfg.Models.Group;
import grodrich7.tfg.R;


public class GroupActivity extends HelperActivity {
    private Group group;
    private String key;
    private AutoCompleteTextView nameInput;
    private AutoCompleteTextView userAddInput;
    private CheckBox destination;
    private CheckBox location;
    private CheckBox driving;
    private CheckBox startTime;
    private CheckBox frontalImages;
    private CheckBox acceptCalls;
    private CheckBox parking;
    private boolean isUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void fillGroupInfo() {
        nameInput.setText(group.getNameGroup());
        location.setChecked(group.getPermissions().get(Constants.Data.LOCATION.toString()));
        destination.setChecked(group.getPermissions().get(Constants.Data.DESTINATION.toString()));
        driving.setChecked(group.getPermissions().get(Constants.Data.DRIVING.toString()));
        startTime.setChecked(group.getPermissions().get(Constants.Data.TRIP_TIME_START.toString()));
        frontalImages.setChecked(group.getPermissions().get(Constants.Data.IMAGES.toString()));
        acceptCalls.setChecked(group.getPermissions().get(Constants.Data.ACCEPT_CALLS.toString()));
        parking.setChecked(group.getPermissions().get(Constants.Data.SEARCHING_PARKING.toString()));
    }

    private void groupSettings(){
        isUpdate = group != null;
        int titleId = isUpdate ? R.string.edit_title : R.string.create_group;
        enableToolbar(titleId);

        if (isUpdate) fillGroupInfo();
        else group = new Group("");
    }

    protected void getViewsByXML() {
        setContentView(R.layout.activity_group);
        nameInput = findViewById(R.id.input_name_group);
        userAddInput = findViewById(R.id.input_add_user);
        destination = findViewById(R.id.destinationToggle);
        location = findViewById(R.id.locationToggle);
        driving = findViewById(R.id.drivingToggle);
        startTime = findViewById(R.id.startTimeToggle);
        frontalImages = findViewById(R.id.imagesToggle);
        acceptCalls = findViewById(R.id.callToggle);
        parking = findViewById(R.id.parkingToggle);
        group = (Group) getIntent().getSerializableExtra("group");
        key = (String) getIntent().getSerializableExtra("key");
        groupSettings();
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
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }

        return true;
    }

    private boolean checkInputs(){
        boolean result = true;
        if (nameInput.getText().toString().isEmpty()){
            nameInput.setError(getResources().getText(R.string.error_field_required));
            nameInput.setFocusableInTouchMode(true);
            nameInput.requestFocus();
            result = false;
        }
        if (group.getUsers() == null || group.getUsers().size() < 1){
            userAddInput.setError(getResources().getString(R.string.user_required_error));
            userAddInput.setFocusableInTouchMode(true);
            userAddInput.requestFocus();
            result = false;
        }
        return result;
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

        if (!isUpdate) createGroup();
        else updateGroup();
    }

    private void updateGroup() {
        controller.getUserGroupsReference().child(key).setValue(group, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Snackbar.make(nameInput, "Update fails", Toast.LENGTH_SHORT)
                            .show();
                    Log.d("GROUPS", "Data could not be update " + databaseError.getMessage());
                } else {
                    Snackbar.make(nameInput, "Update success", Toast.LENGTH_SHORT)
                            .show();
                    Log.d("GROUPS", "Group update succesfully");
                    goodEdit();
                }
            }
        });
    }

    public void createGroup(){
       controller.getUserGroupsReference().push().setValue(group, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Snackbar.make(nameInput, "Save fails", Toast.LENGTH_SHORT)
                            .show();
                    Log.d("GROUPS", "Data could not be saved " + databaseError.getMessage());
                } else {
                    Snackbar.make(nameInput, "Save good", Toast.LENGTH_SHORT)
                            .show();
                    Log.d("GROUPS", "Group saved succesfully");
                    goodEdit();
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
                    userAddInput.setFocusableInTouchMode(true);
                    userAddInput.requestFocus();
                }else if (!isValidEmail(userAddInput.getText().toString())){
                    userAddInput.setError(getResources().getText(R.string.error_invalid_email));
                    userAddInput.setFocusableInTouchMode(true);
                    userAddInput.requestFocus();
                }else{
                    if (group.getUsers() == null) {
                        group.setUsers(new ArrayList<String>());
                        group.addUser(userAddInput.getText().toString());
                        Snackbar.make(userAddInput, getResources().getText(R.string.user_added), Snackbar.LENGTH_SHORT).show();
                    }else{
                        if (group.getUsers().contains(userAddInput.getText().toString())){
                            userAddInput.setError(getResources().getText(R.string.email_added));
                        }else{
                            group.addUser(userAddInput.getText().toString());
                            Snackbar.make(userAddInput, getResources().getText(R.string.user_added), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            default:
                break;
        }

    }

    private void showUsersDialog(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(GroupActivity.this);
        builderSingle.setTitle(R.string.users_added);
        builderSingle.setCancelable(false);
        builderSingle.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final boolean[] selected;
        String[] usersArray;
        if (group.getUsers() != null){
            selected = new boolean[group.getUsers().size()];
            usersArray = new String[group.getUsers().size()];
            usersArray = group.getUsers().toArray(usersArray);
        }else{
            selected = new boolean[0];
            usersArray = new String[0];
        }
        builderSingle.setMultiChoiceItems(usersArray, selected, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selected[which] = isChecked;
            }
        });
        if (selected.length > 0){
            builderSingle.setPositiveButton(getResources().getString(R.string.remove), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int offset = 0;
                    for (int j = 0; j < selected.length; j++){
                        if (selected[j]){
                            group.getUsers().remove(j-offset);
                            offset+=1;
                        }
                    }
                }
            });
        }

        builderSingle.show();
    }

    public void goodEdit(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("group", group);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    public void badEdit(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();
    }

    @Override
    public void onBackPressed(){
        badEdit();
        super.onBackPressed();
    }
}
