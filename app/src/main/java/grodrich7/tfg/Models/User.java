package grodrich7.tfg.Models;

import java.util.ArrayList;

public class User {

    private String name;
    private String email;
    private ArrayList<Group> groups;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.groups = new ArrayList<Group>();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<Group> groups) {
        this.groups = groups;
    }
}
