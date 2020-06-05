package mrtrujis.Rutix;

/**
 * Created by end user on 07/07/2017.
 */
public class Chat {

    private String mName;
    private String mMessage;
    private String mUid;

public Chat() {
    //necesaria esta linea para poder trabajar con firebase
}

    public Chat(String name, String uid, String message){
        mName = name;
        mMessage = message;
           mUid = uid;
    }

    public String getName(){
        return mName;
    }
    public void setName(String name){
        mName = name;
    }

    public String getMessage(){
        return mMessage;
    }
    public void setMessage(String message){
        mMessage = message;
    }
    public String getUid(){
        return mUid;
    }
    public void setUid(String uid){
        mUid = uid;
    }
}


