import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Docs implements Serializable {
    private String name, groupAddr;
    private ConcurrentHashMap<String, Integer> editors;
    private Boolean[] inEditing;
    private ArrayList<Message> history;

    public Docs(String name, int nsect){
        this.name = name;
        this.editors = new ConcurrentHashMap<>();
        this.history = new ArrayList<>();

        inEditing = new Boolean[nsect];
        for(int i=0; i<inEditing.length;i++){
            inEditing[i]=false;
        }

        int tmp = (int)(Math.random()*40);

        while(tmp < 24 || tmp > 40) {
            tmp = (int)(Math.random()*40);
        }

        tmp += 200;
        groupAddr = tmp + "." + (int)(Math.random()*256) + "." + (int)(Math.random()*256) + "." + (int)(Math.random()*256);
    }

    public String getName(){
        return name;
    }

    public InetAddress getGroupAddr() throws UnknownHostException {
        return InetAddress.getByName(groupAddr);
    }

    public void addEditor(String user, int sec){
        editors.put(user, sec);
        inEditing[sec] = true;
    }

    public void removeEditor(String user, int sec){
        editors.remove(user);
        inEditing[sec] = false;
        if(editors.isEmpty())
            history.clear();
    }

    public boolean isLocked(int index){
        return inEditing[index];
    }

    public int getEdSection(String user){
        return editors.get(user);
    }

    public void newMess(Message m){
        history.add(m);
    }

    public ArrayList<Message> getHistory(){
        return history;
    }

}
