package grodrich7.tfg.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import grodrich7.tfg.Controller;
import grodrich7.tfg.R;

public class HomeActivity extends AppCompatActivity {
    Controller controller;
    private TextView user_label;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        controller = Controller.getInstance();
        user_label = (TextView) findViewById(R.id.user_label);
        user_label.setText(getResources().getString(R.string.welcome) + " " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    public void handleButtons(View v){
        switch (v.getId()){
            case R.id.drivingModeBtn:
                launchIntent(DrivingActivity.class, true);
                break;
            case R.id.viewBtn:
                launchIntent(ViewUserActiviy.class, true);
                break;
            case R.id.groupsBtn:
                launchIntent(GroupsActivity.class, true);
                break;
            case R.id.settingsBtn:
                FirebaseAuth.getInstance().signOut();
                launchIntent(MainActivity.class, false);
                finish();
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