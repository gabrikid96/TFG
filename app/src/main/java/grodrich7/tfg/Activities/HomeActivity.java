package grodrich7.tfg.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import grodrich7.tfg.R;

public class HomeActivity extends HelperActivity {
    private TextView user_label;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void getViewsByXML() {
        setContentView(R.layout.activity_home);
        user_label = findViewById(R.id.user_label);
        user_label.setText(getResources().getString(R.string.welcome) + " " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    public void handleButtons(View v){
        switch (v.getId()){
            case R.id.drivingModeBtn:
                launchIntent(DrivingActivity.class, TRANSITION_RIGHT);
                break;
            case R.id.viewBtn:
                launchIntent(ViewUsersActivity.class, TRANSITION_RIGHT);
                break;
            case R.id.groupsBtn:
                launchIntent(GroupsActivity.class, TRANSITION_RIGHT);
                break;
            case R.id.settingsBtn:
                launchIntent(SettingsActivity.class, TRANSITION_RIGHT);
                break;
        }
    }
}