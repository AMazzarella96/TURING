import javax.swing.*;
import java.io.IOException;
import java.net.*;

public class NotificationHandler implements Runnable {

    private JTextArea mail;
    private DatagramSocket ds;
    private String inv;
    private SocketAddress sock;

    public NotificationHandler(JTextArea mail, SocketAddress remoteAddress){
        this.mail = mail;
        this.sock = remoteAddress;
        try {
            ds = new DatagramSocket(sock);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        while(true){
            byte[] res = new byte[1024];
            DatagramPacket packet = new DatagramPacket(res, res.length);
            try {
                if(!ds.isClosed())
                    ds.receive(packet);
            } catch (IOException e) {
                ds.close();
                ds.disconnect();
            }
            inv = new String(res,0, res.length);
            mail.append( inv + "\n");
       }
    }

    public void terminate(){
        ds.close();
        ds.disconnect();
    }
}
