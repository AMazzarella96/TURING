import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("Duplicates")
public class Register extends UnicastRemoteObject implements Authentication {

    private ConcurrentHashMap<String, Info> users;
    private ConcurrentHashMap<String, Docs> documents;

    public Register() throws RemoteException{
        users = new ConcurrentHashMap<>();
        documents = new ConcurrentHashMap<>();
        try {
            File file = new File("users.txt");
            FileInputStream fin = new FileInputStream(file);
            ObjectInputStream o = new ObjectInputStream(fin);
            users = (ConcurrentHashMap<String, Info>) o.readObject();
            File docs = new File("docs.txt");
            fin = new FileInputStream(docs);
            o = new ObjectInputStream(fin);
            documents = (ConcurrentHashMap<String, Docs>) o.readObject();
            o.close();
        } catch (IOException | ClassNotFoundException fn){fn.printStackTrace();}

    }

    public synchronized boolean register(String username, String password) throws RemoteException {

        if(users.containsKey(username)) return false;
        Info info = new Info(password);
        users.put(username,info);
        try {
            File file = new File("users.txt");
            FileOutputStream fout = new FileOutputStream(file);
            ObjectOutputStream o = new ObjectOutputStream(fout);
            o.writeObject(users);
            o.close();
        } catch (IOException nf){nf.printStackTrace();}


        return true;
    }

    public ArrayList<Docs> getMyDocs(String name) throws RemoteException{
        return users.get(name).getOwned();
    }

    public ArrayList<Docs> getMyInvites(String name) throws RemoteException{
        return users.get(name).getInvited();
    }

    public ArrayList<String> getDocs(String name) throws RemoteException{
        return users.get(name).getDocs();
    }


    public ConcurrentHashMap<String, Info> getDB(){
        return users;
    }

    public ConcurrentHashMap<String, Docs> getAllDocs(){
        return documents;
    }

    public InetAddress GetAddress(String name) throws UnknownHostException {
        return documents.get(name).getGroupAddr();
    }

    public ArrayList<Message> getHistory(String name) throws RemoteException {
        return documents.get(name).getHistory();
    }

    public boolean inEditing(String name, int sec) throws RemoteException{
        return documents.get(name).isLocked(sec);
    }

    public boolean getinEdit(String user) throws RemoteException{
        return users.get(user).getInEdit().equals("null");
    }
}
