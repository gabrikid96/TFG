package grodrich7.tfg.Models;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Created by gabri on 14/01/2018.
 */

public class Group {
    private String nameGroup;
    private Map<Constants.Data, Boolean> permissions;

    public Group (String nameGroup){
        this.nameGroup = nameGroup;
        initPermissions();
    }

    private void initPermissions() {
        permissions = new HashMap<>();
        for (Constants.Data data : Constants.Data.values() ){
            permissions.put(data,false);
        }
    }

    private void changePermission(Constants.Data data, boolean value){
        permissions.put(data,value);
    }


}
