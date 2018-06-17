package grodrich7.tfg.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

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
    private Menu mMenu;

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
        location.setChecked(true);
        driving = findViewById(R.id.drivingToggle);
        driving.setChecked(true);
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
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.save_action:
                //item.setVisible(false);
                saveGroup();
                hideKeyboard();
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
        switchViews(false);
        if (!isUpdate) createGroup();
        else updateGroup();
    }

    private void updateGroup() {
        controller.updateGroup(key, group).addOnCompleteListener(getOnCompleteListener())
                                          .addOnFailureListener(this, getOnFailureListener());
    }

    public void createGroup(){
        controller.createGroup(group).addOnCompleteListener(getOnCompleteListener())
                                     .addOnFailureListener(this, getOnFailureListener());
    }

    private void switchViews(boolean action){
        mMenu.findItem(R.id.save_action).setVisible(action);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(!action ? View.VISIBLE : View.INVISIBLE);
    }

    public void handleButtons(View v){
        pressEffect(v);
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
                            userAddInput.setText("");
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

    private OnCompleteListener<Void> getOnCompleteListener(){
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                goodEdit();
                switchViews(true);
            }
        };
    }

    private OnFailureListener getOnFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showErrorMessage(e, nameInput);
                switchViews(true);
            }
        };
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
    public void onBackPressed() {
        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.exit_title)
        .setMessage(getString(R.string.exit_message))
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                badEdit();
                GroupActivity.super.onBackPressed();
            }

        })
        .setNegativeButton(R.string.no, null)
        .show();
    }
}
