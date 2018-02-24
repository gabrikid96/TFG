package grodrich7.tfg.Models;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Created by gabri on 14/01/2018.
 */

public class Group {
    private String nameGroup;

    //private Map<Constants.Data, Boolean> permissions;
    private ArrayList<String> users;


    public Group(){}

    public Group (String nameGroup){
        this.nameGroup = nameGroup;
        initPermissions();
    }

    private void initPermissions() {
       // permissions = new HashMap<>();
        users = new ArrayList<>();
        /*for (Constants.Data data : Constants.Data.values() ){
            permissions.put(data,false);
        }*/
    }

    private void changePermission(Constants.Data data, boolean value){
      //  permissions.put(data,value);
    }

    public String getNameGroup() {
        return nameGroup;
    }

    public void setNameGroup(String nameGroup) {
        this.nameGroup = nameGroup;
    }

   /* public Map<Constants.Data, Boolean> getPermissions() {
        return permissions;
    }*/

  /*  public void setPermissions(Map<Constants.Data, Boolean> permissions) {
        this.permissions = permissions;
    }*/

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }
}
