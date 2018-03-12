package grodrich7.tfg.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import grodrich7.tfg.R;

public class DrivingActivity extends AppCompatActivity {

    private ImageButton drivingToggle;
    private ImageButton helpBtn;
    private boolean isDrivingActivate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.driving_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getViewsByXML();
    }

    private void getViewsByXML() {
        isDrivingActivate = false;
        drivingToggle = findViewById(R.id.drivingToggle);
        helpBtn = findViewById(R.id.helpBtn);
        drivingToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDrivingDialog();
            }
        });
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(DrivingActivity.this, R.style.FullHeightDialog);
                dialog.setContentView(R.layout.help_actions);
                dialog.setTitle("");
                dialog.setCancelable(true);
//                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                dialog.show();
            }
        });
    }

    private void showDrivingDialog(){
        int message = !isDrivingActivate ? R.string.driving_mode_on_attempt : R.string.driving_mode_off_attempt;

        new AlertDialog.Builder(DrivingActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.driving_title)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDrivingActivate = !isDrivingActivate;
                        toggleDrivingIcon();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void toggleDrivingIcon(){
        drivingToggle.setBackgroundResource(isDrivingActivate ? R.mipmap.driving_on : R.mipmap.driving_off);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.transition_right_in, R.anim.transition_right_out);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
