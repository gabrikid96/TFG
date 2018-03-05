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
import java.util.Map;
import java.util.Objects;

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
    private static Controller controller;
    private HashMap<String,User> users;
    private static String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static DatabaseReference mDatabase;
    public static final int GROUP_EDIT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        controller = Controller.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
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
        launchIntent(GroupActivity.class,true);
    }

    public static void deleteGroup(Group group)
    {
        if (controller.getCurrentUser().getGroups() != null){
            for (Map.Entry<String, Group> entry : controller.getCurrentUser().getGroups().entrySet()) {
                if (group.equals(entry.getValue())) {
                    mDatabase.child("users").child(userUid).child("groups").child(entry.getKey()).removeValue();
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode ==  GROUP_EDIT) {
            if(resultCode == Activity.RESULT_OK){
                groupsAdapter.updateData(getGroupsArray());
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    private void launchIntent(Class<?> activity, boolean transitionRight){
        Intent intent = new Intent(GroupsActivity.this,activity);
        startActivity(intent);
        overridePendingTransition(transitionRight ? R.anim.transition_left_in : R.anim.transition_right_in ,
                transitionRight ? R.anim.transition_left_out : R.anim.transition_right_in);
    }
}
