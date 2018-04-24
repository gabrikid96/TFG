package grodrich7.tfg;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

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

    public void uploadImage(Bitmap bitmap){
        // Get the data from an ImageView as bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = drivingImages.child("image1.jpg").putBytes(data);
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
                Controller.getInstance().registerNewImage(downloadUrl);
            }
        });
    }

    public static StorageController getInstance() {
        if(instance == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            instance = new StorageController();
        }
        return instance;
    }
}
