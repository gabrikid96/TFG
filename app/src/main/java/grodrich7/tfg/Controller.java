package grodrich7.tfg;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import grodrich7.tfg.Models.Constants;
import grodrich7.tfg.Models.User;

/**
 * Created by gabri on 14/01/2018.
 */

public class Controller {
    private static Controller instance = null;
    private FirebaseDatabase database;
    public DatabaseReference usersReference;
    public DatabaseReference userGroupsReference;
    private User currentUser;

    protected Controller() {
        Log.d("CONTROLLER", "Get instance");
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        loadUser();
    }

    private void loadUser() {
        usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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

    public DatabaseReference getUsersReference() {
        return usersReference;
    }


    public DatabaseReference getUserGroupsReference(){
        return userGroupsReference != null ? userGroupsReference : usersReference.child(getUserUid()).child("groups");
    }

    public String getUserUid(){
        try{
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }catch (NullPointerException ex){
            return null;
        }
    }

    public static Controller getInstance() {
        if(instance == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            instance = new Controller();
        }
        return instance;
    }
}
