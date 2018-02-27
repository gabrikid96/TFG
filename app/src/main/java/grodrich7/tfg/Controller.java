package grodrich7.tfg;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import grodrich7.tfg.Models.Group;
import grodrich7.tfg.Models.User;

/**
 * Created by gabri on 14/01/2018.
 */

public class Controller {
    private static Controller instance = null;
    private FirebaseDatabase database;
    private DatabaseReference userReference;

    private User currentUser;


    protected Controller() {
        Log.d("CONTROLLER", "Get instance");
        database = FirebaseDatabase.getInstance();
        userReference = database.getReference("users");
        loadUser();
    }

    private void loadUser() {
        userReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("CONTROLLER", "User changes");
                currentUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                Log.e("CONTROLLER",databaseError.getMessage());
            }
        });
    }

    public User getCurrentUser(){
        return currentUser;
    }

    public static Controller getInstance() {
        if(instance == null) {
            instance = new Controller();
        }
        return instance;
    }
}
