package grodrich7.tfg.Models;

/**
 * Created by gabri on 14/04/2018.
 */

public class Friend {
    private String userUid;
    private String name;
    private String groupUid;

    public Friend(String name, String userUid, String groupUid) {
        this.userUid = userUid;
        this.name = name;
        this.groupUid = groupUid;
    }

    public Friend(){

    }


}
