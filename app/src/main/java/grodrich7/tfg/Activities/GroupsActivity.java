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
import android.view.MenuItem;
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

public class GroupsActivity extends HelperActivity {

    private ListView groups_list;
    private ProgressBar progressBar;
    private GroupsAdapter groupsAdapter;
    private HashMap<String,User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadGroups();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }

        return true;
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

    protected void getViewsByXML() {
        setContentView(R.layout.activity_groups);
        enableToolbar(R.string.groups);
        /*List View*/
        groups_list = (ListView) findViewById(R.id.groups_list);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    private void putGroups() {
        groupsAdapter = new GroupsAdapter(getGroupsArray(), this);
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

    public void createGroup(View v){
        Intent intent = new Intent(this,GroupActivity.class);
        startActivityForResult(intent,GROUP_EDIT);
        overridePendingTransition(R.anim.transition_left_in, R.anim.transition_left_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GROUP_EDIT) {
            if(resultCode == Activity.RESULT_OK)
                groupsAdapter.updateData(getGroupsArray());

            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    public void editGroup(Group group){
        Intent intent = new Intent(this,GroupActivity.class);
        intent.putExtra("group",group);
        startActivityForResult(intent,GROUP_EDIT);
        overridePendingTransition(R.anim.transition_left_in, R.anim.transition_left_out);
    }
}
