package grodrich7.tfg.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import grodrich7.tfg.Controller;
import grodrich7.tfg.R;

public class HomeActivity extends AppCompatActivity {
    Controller controller;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        controller = Controller.getInstance();
    }

    public void handleButtons(View v){
        switch (v.getId()){
            case R.id.viewBtn:
                FirebaseAuth.getInstance().signOut();
                launchIntent(MainActivity.class, false);
                finish();
                break;
            case R.id.groupsBtn:
                launchIntent(GroupsActivity.class, true);
                break;
        }
    }


    private void launchIntent(Class<?> activity, boolean transitionRight){
        Intent intent = new Intent(HomeActivity.this,activity);
        startActivity(intent);
        overridePendingTransition(transitionRight ? R.anim.transition_left_in : R.anim.transition_right_in ,
                                  transitionRight ? R.anim.transition_left_out : R.anim.transition_right_in);
    }

}