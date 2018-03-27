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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import grodrich7.tfg.R;

public class DrivingActivity extends HelperActivity {

    private ImageButton drivingToggle;
    private ImageButton helpBtn;
    private boolean isDrivingActivate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void getViewsByXML() {
        setContentView(R.layout.activity_driving);
        enableToolbar(R.string.driving_title);
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
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDrivingActivate = !isDrivingActivate;
                        toggleDrivingIcon();
                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void toggleDrivingIcon(){
        drivingToggle.setBackgroundResource(isDrivingActivate ? R.mipmap.driving_on : R.mipmap.driving_off);
    }
}
