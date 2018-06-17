package grodrich7.tfg.Controller;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import grodrich7.tfg.Activities.Services.CameraHandler;

/**
 * Created by grodrich on 24/04/2018.
 */

public class StorageController {
    private static StorageController instance = null;
    private StorageReference mStorageRef;
    private StorageReference drivingImages;

    public StorageController(){
        mStorageRef = FirebaseStorage.getInstance().getReference();
        drivingImages = mStorageRef.child("driving-images").child(FirebaseAuth.getInstance().getUid());
    }

    public void uploadImage(final Service service, Bitmap bitmap){
        try{
            // Get the data from an ImageView as bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap = RotateBitmap(bitmap, 90);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final byte[] data = baos.toByteArray();
            Date now = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd-HH:mm:ss");
            String name = "image"+ sdf.format(now) +".jpg";
            UploadTask uploadTask = drivingImages.child(name).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Controller.getInstance().updateImages(downloadUrl.toString());
//                    LocalBroadcastManager.getInstance(service).sendBroadcast(intent);
                    /*try{
                        Intent intent = new Intent("grodrich7.tfg.CAMERA_ACTION");
                        // You can also include some extra data.
                        intent.putExtra("image",data);
                        service.sendBroadcast(intent);
                    }catch (Exception ex){
                        Toast.makeText(service.getApplicationContext(), "RECEIVE" + ex.getMessage(), Toast.LENGTH_LONG).show();
                    }*/
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(service, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception ex){
            Toast.makeText(service, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static StorageController getInstance() {
        if(instance == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            instance = new StorageController();
        }
        return instance;
    }
}