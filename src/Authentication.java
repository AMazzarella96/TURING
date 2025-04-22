import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface Authentication extends Remote {
    boolean register(String username, String password) throws RemoteException;
    ArrayList<Docs> getMyDocs(String name) throws RemoteException;
    ArrayList<Docs> getMyInvites(String name) throws RemoteException;
    ArrayList<String> getDocs(String name) throws RemoteException;
    ArrayList<Message> getHistory(String name) throws RemoteException;
    InetAddress GetAddress(String name) throws RemoteException, UnknownHostException;
    boolean inEditing(String name, int sec) throws RemoteException;
    boolean getinEdit(String user) throws RemoteException;
}
