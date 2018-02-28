package grodrich7.tfg.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import grodrich7.tfg.Controller;
import grodrich7.tfg.Models.Group;
import grodrich7.tfg.Models.User;
import grodrich7.tfg.R;
import grodrich7.tfg.Views.GroupsAdapter;

public class GroupsActivity extends AppCompatActivity {

    private ListView groups_list;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private GroupsAdapter groupsAdapter;
    private Controller controller;
    private HashMap<String,User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        controller = Controller.getInstance();
        getViewsByXML();
        loadGroups();
    }


    private void loadGroups() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                while(controller.getCurrentUser() == null);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressBar.setVisibility(View.GONE);
                putGroups();
            }
        }.execute();
    }

    private void getViewsByXML() {
        /*List View*/
        groups_list = (ListView) findViewById(R.id.groups_list);
        progressBar = findViewById(R.id.progressBar);

    }

    private void putGroups() {
        groupsAdapter = new GroupsAdapter(getGroupsArray(), getApplicationContext());
        groups_list.setAdapter(groupsAdapter);
    }

    public ArrayList<Group> getGroupsArray(){
        return controller.getCurrentUser().getGroups() == null ? new ArrayList<Group>() : new ArrayList<>(controller.getCurrentUser().getGroups().values());
    }

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        overridePendingTransition(R.anim.transition_right_in, R.anim.transition_right_out);
        finish();
    }

    /**
     *
     * @param v
     */
    public void createGroup(View v){
//        Group group = new Group("Familiar");
//        group.addUser("gabrikid96@gmail.com");
//        saveGroup(group);
        launchIntent(GroupActivity.class,true);
    }

//    private void refreshGroups(ArrayList<Group> groups) {
//        groupsAdapter.updateData(groups);
//        groupsAdapter.notifyDataSetChanged();
//    }

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
                    groupsAdapter.updateData(group);
                }
            }
        });
    }

    public void deleteGroup(View v)
    {
        /*Group group = groupsAdapter.getItem(groups_list.getPositionForView(v));
        if (controller.getCurrentUser().getGroups().remove(group)){
            saveGroups();
        }*/
    }

    private void launchIntent(Class<?> activity, boolean transitionRight){
        Intent intent = new Intent(GroupsActivity.this,activity);
        startActivity(intent);
        overridePendingTransition(transitionRight ? R.anim.transition_left_in : R.anim.transition_right_in ,
                transitionRight ? R.anim.transition_left_out : R.anim.transition_right_in);
    }
}
