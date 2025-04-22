import java.io.Serializable;
import java.util.ArrayList;

public class Info implements Serializable {
    private String password;
    private ArrayList<Docs> owned;
    private ArrayList<Docs> invited;
    private String inEdit = "null";

    public Info(String password){
        this.password=password;
        owned = new ArrayList<>();
        invited = new ArrayList<>();
    }

    public String getInEdit() {
        return inEdit;
    }

    public void setInEdit(String inEdit) {
        this.inEdit = inEdit;
    }

    public String getPassword(){
        return password;
    }

    public void addOwned(Docs doc){
        owned.add(doc);
    }

    public ArrayList<Docs> getOwned(){
        return owned;
    }

    public ArrayList<Docs> getInvited(){
        return invited;
    }

    public ArrayList<String> getDocs() {
        ArrayList<String> myDocs = new ArrayList<>();
        for(Docs d : getInvited())
            myDocs.add(d.getName());
        for (Docs d : getOwned()){
            myDocs.add(d.getName());
        }
        return myDocs;
    }

    public void addInvited(Docs doc){
        invited.add(doc);
    }
}
