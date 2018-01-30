package grodrich7.tfg.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import grodrich7.tfg.Controller;
import grodrich7.tfg.Models.Group;
import grodrich7.tfg.R;
import grodrich7.tfg.Views.GroupsAdapter;

public class GroupsActivity extends AppCompatActivity {

    private ListView groups_list;
    private Toolbar toolbar;
    private static GroupsAdapter groupsAdapter;
    private Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        controller = Controller.getInstance();
        getViewsByXML();

    }

    private void getViewsByXML() {
        /*Toolbar*/
        /*toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        /*List View*/
        groups_list = (ListView) findViewById(R.id.groups_list);
        final ArrayList<Group> groups = controller.getCurrentUser().getGroups() == null ? new ArrayList<Group>() : controller.getCurrentUser().getGroups();
        groups.add(new Group("Familia"));

        groupsAdapter = new GroupsAdapter(groups,getApplicationContext());
        groups_list.setAdapter(groupsAdapter);
        groups_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Group group= groups.get(position);

                Snackbar.make(view, group.getNameGroup() , Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
            }
        });
    }
}
