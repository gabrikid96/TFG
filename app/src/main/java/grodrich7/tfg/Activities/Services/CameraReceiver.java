package grodrich7.tfg.Activities.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import grodrich7.tfg.Activities.DrivingActivity;
import grodrich7.tfg.Controller;

public class CameraReceiver extends BroadcastReceiver {
    private DrivingActivity drivingActivity;
    private ImageView lastImage;

    public CameraReceiver() {
        super();
    }

    public CameraReceiver(DrivingActivity drivingActivity, ImageView lastImage) {
        super();
        this.lastImage = lastImage;
        this.drivingActivity = drivingActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            Controller controller = Controller.getInstance();
            if (controller != null && controller.getDrivingData() != null && controller.getDrivingData().getImages() != null && controller.getDrivingData().getImages().size() > 0){
                String url = controller.getDrivingData().getImages().get(controller.getDrivingData().getImages().size() -1);
                Glide.with(drivingActivity).load(url).into(lastImage);
            }
            Log.d("RECEIVE", "Receive");
        }catch (Exception ex){
            Toast.makeText(context, "RECEIVE" + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
