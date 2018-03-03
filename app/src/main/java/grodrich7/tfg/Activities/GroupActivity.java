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
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import grodrich7.tfg.Models.Group;
import grodrich7.tfg.R;

public class GroupActivity extends AppCompatActivity {
    private Group group;
    private AutoCompleteTextView nameInput;

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
        nameInput =findViewById(R.id.input_name_group);
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
                Snackbar.make(nameInput, "Save", Toast.LENGTH_SHORT)
                        .show();
                break;
            default:
                break;
        }

        return true;
    }

    private void saveGroup(final Group group){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("users").child(userUid).child("groups").push().setValue(group, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            if (databaseError != null) {
                Log.d("GROUPS", "Data could not be saved " + databaseError.getMessage());
            } else {
                Log.d("GROUPS", "Group saved succesfully");
            }
            }
        });
    }

    public void handleButtons(View v){
        switch (v.getId()){
            case R.id.usersButton:
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(GroupActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.users_dialog, null);

                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.setTitle(R.string.users_added);
                dialog.show();
                break;
            case R.id.add_user_button:
                break;
            default:
                break;
        }

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.transition_right_in, R.anim.transition_right_out);
        finish();
    }
}
