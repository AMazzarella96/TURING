import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ChatHandler implements Runnable {
    private JTextArea chat;
    private byte[] buffer;
    private MulticastSocket multicastSocket;

    public ChatHandler(JTextArea chat, InetAddress inetAddress) {
        this.chat = chat;
        this.buffer=new byte[1024];
        try {
            this.multicastSocket=new MulticastSocket(7880);
            this.multicastSocket.joinGroup(inetAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(true){
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                if (!multicastSocket.isClosed()) {
                    multicastSocket.receive(packet);
                }
                String newMessage=new String(buffer,0,buffer.length);
                chat.append(newMessage + "\n");
                buffer = new byte[1024];
            } catch (IOException e) {
                multicastSocket.close();
                multicastSocket.disconnect();
            }
        }
    }

    public void terminate(){
        multicastSocket.close();
        multicastSocket.disconnect();
    }

}
