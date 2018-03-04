package grodrich7.tfg.Models;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

    private String name;
    private String email;
    private HashMap<String,Group> groups;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.groups = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public HashMap<String,Group> getGroups() {

        return groups;
    }

    public void setGroups(HashMap<String,Group> groups) {
        this.groups = groups;
    }


}
