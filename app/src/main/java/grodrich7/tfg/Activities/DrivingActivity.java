package grodrich7.tfg.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import grodrich7.tfg.Manifest;
import grodrich7.tfg.Models.Services.CameraService;
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

    public void onClick(View v){
        switch (v.getId()){
            case R.id.image_camera:
                takePicture();
                break;
        }
    }

    private void takePicture(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        100);
            }else{
                startService(new Intent(DrivingActivity.this, CameraService.class));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void putLastPicture(){
        FirebaseDatabase.getInstance().getReference("driving-images").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String>  images = new ArrayList<>();
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                    images.add(imageSnapshot.getValue(String.class));
                }
                downloadPicture(images.get(images.size()-1));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void downloadPicture(String uri){

    }
}
