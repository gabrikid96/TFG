package grodrich7.tfg.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

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
}
