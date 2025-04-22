import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MainClient {

    public static final int sockport = 2020;

    public static void main(String[] args) {

        Authentication stub = null;
        SocketChannel channel = null;
        try {
            Registry registry = LocateRegistry.getRegistry(1919);
            stub = (Authentication) registry.lookup("Database");
        }catch (RemoteException | NotBoundException rm){rm.printStackTrace();}


       try {
            channel = SocketChannel.open(new InetSocketAddress("localhost", sockport));

        } catch (IOException e) {
            e.printStackTrace();
        }

       new GUI(stub, channel);
    }
}
