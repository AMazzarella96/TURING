import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class MainServer {

    public static final int rmiport = 1919;
    public static final int sockport = 2020;
    public static void main(String[] args) {

        try {

            //RMI
            Register reg = new Register();
            LocateRegistry.createRegistry(rmiport);
            Registry registry =LocateRegistry.getRegistry(rmiport);
            registry.rebind("Database", reg);

            //Classe UserDB contiene i metodi che il Server chiama in seguito alle richieste dei client
            //Come parametro prende la ConcurrentHashMap contenente tutti gli user registrati restituita dal metodo getDB in Register
            //e la ConcurrentHashMap di tipo Docs contenente tutte le informazioni sui documenti esistenti all'interno del sistema
            UserDB op = new UserDB(reg.getDB(), reg.getAllDocs());

            try {
                ServerSocketChannel channel = ServerSocketChannel.open();
                channel.bind(new InetSocketAddress("localhost", sockport));
                System.err.println("Server " + InetAddress.getLocalHost().getHostAddress() + " ready on port " + sockport);
                channel.configureBlocking(false);
                Selector selector = Selector.open();
                channel.register(selector, SelectionKey.OP_ACCEPT);

                while(true){
                    selector.select();
                    Set<SelectionKey> keySet = selector.selectedKeys();
                    Iterator<SelectionKey> it = keySet.iterator();
                    while(it.hasNext()){
                        SelectionKey k = it.next();
                        it.remove();
                        if(k.isValid() && k.isAcceptable()){
                            channel = (ServerSocketChannel) k.channel();
                            SocketChannel client = channel.accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                        }

                        if(k.isValid() && k.isReadable()){
                            Request request = new Request("");
                            SocketChannel client = (SocketChannel) k.channel();
                            try {
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                client.read(buffer);
                                ByteArrayInputStream bytein = new ByteArrayInputStream(buffer.array());
                                ObjectInputStream objin = new ObjectInputStream(bytein);
                                request = (Request) objin.readObject();


                            //Client Crash Handler
                            } catch (IOException io){
                                System.err.println("Client " + client.getRemoteAddress() + " crashed");
                                op.crashed(client.getRemoteAddress());
                                client.close();
                            }


                            if(client.isOpen()) {
                                ArrayList<Object> flags = new ArrayList<>();
                                switch (request.getRequest()) {
                                    case "login":
                                        int check = op.login(request.getParam().get(0), request.getParam().get(1), client.getRemoteAddress());
                                        if (check == 1)
                                            System.err.println(client.getRemoteAddress() + " connected");
                                        flags.add(request.getRequest());        //0 - Request
                                        flags.add(check);                       //1 - Int Check
                                        client.register(selector, SelectionKey.OP_WRITE, flags);
                                        break;
                                    case "showdoc":
                                        flags.add(request.getRequest());        //0 - Request
                                        flags.add(request.getParam().get(0));   //1 - Doc name
                                        client.register(selector, SelectionKey.OP_WRITE, flags);
                                        break;

                                    case "showsec":
                                        flags.add(request.getRequest());        //0 - Request
                                        flags.add(request.getParam().get(0));   //1 - Doc name
                                        flags.add(request.getParam().get(1));   //2 - Section
                                        client.register(selector, SelectionKey.OP_WRITE, flags);
                                        break;

                                    case "invite":
                                        int checkinvite = op.invite(request.getParam().get(0),  request.getParam().get(1));
                                        flags.add(request.getRequest());        //0 - Request
                                        flags.add(checkinvite);                 //1 - Int check
                                        flags.add(request.getParam().get(0));   //2 - Dest
                                        flags.add(request.getParam().get(1));   //3 - Document
                                        client.register(selector, SelectionKey.OP_WRITE, flags);
                                        break;
                                    case "newdoc":
                                        int checkdoc = op.create(request.getParam().get(0), request.getParam().get(1), Integer.parseInt(request.getParam().get(2)));
                                        flags.add(request.getRequest());        //0 - Request
                                        flags.add(checkdoc);                    //1 - Int Check
                                        client.register(selector, SelectionKey.OP_WRITE, flags);
                                        break;
                                    case "edit":
                                        flags.add(request.getRequest());        //0 - Request
                                        flags.add(request.getParam().get(0));   //1 - Username
                                        flags.add(request.getParam().get(1));   //2 - Doc name
                                        flags.add(request.getParam().get(2));   //3 - Section [String name]
                                        flags.add(request.getParam().get(3));   //4 - Section [Int index]
                                        client.register(selector, SelectionKey.OP_WRITE, flags);
                                        break;
                                    case "send":
                                        flags.add(request.getRequest());        //0 - Request
                                        flags.add(request.getParam().get(0));   //1 - Doc name
                                        flags.add(request.getParam().get(1));   //2 - Message
                                        flags.add(request.getParam().get(2));   //3 - Username

                                        op.newMess(request.getParam().get(2), request.getParam().get(0), request.getParam().get(1));
                                        client.register(selector, SelectionKey.OP_WRITE, flags);
                                        break;
                                    case "endedit":
                                        flags.add(request.getRequest());        //0 - Request
                                        flags.add(request.getParam().get(0));   //1 - Username
                                        flags.add(request.getParam().get(1));   //2 - Doc name
                                        flags.add(request.getParam().get(2));   //3 - Section [String name]
                                        flags.add(request.getParam().get(3));   //4 - Section [Int index]
                                        flags.add(request.getParam().get(4));   //5 - New Section
                                        client.register(selector, SelectionKey.OP_WRITE, flags);
                                        break;
                                    case "logout":
                                        op.logout(request.getParam().get(0));
                                        System.err.println(client.getRemoteAddress() + " disconnected");
                                        if (request.getParam().size() == 2)
                                            client.close();
                                        break;

                                    case "undo":
                                        String user = request.getParam().get(0);
                                        String docname = request.getParam().get(1);
                                        int sec = Integer.parseInt(request.getParam().get(2));
                                        reg.getAllDocs().get(docname).removeEditor(user, sec);
                                        break;

                                    case "exit":
                                        op.crashed(client.getRemoteAddress());
                                        client.close();
                                        break;
                                }
                            }
                        }


                        if(k.isValid() && k.isWritable()){
                            SocketChannel client = (SocketChannel) k.channel();
                            ArrayList<Object> flags = (ArrayList<Object>) k.attachment();
                            Integer check;

                            ByteBuffer buffer;
                            buffer = ByteBuffer.allocate(1024);
                            String c = (String) flags.get(0);
                            String name, sect;
                            switch (c){
                                case "login":
                                case "newdoc":
                                    check =(Integer) flags.get(1);
                                    buffer.put(check.byteValue());
                                    break;

                                case "invite":
                                    check =(Integer) flags.get(1);
                                    buffer.put(check.byteValue());

                                   //Utente invitato è online && non è già stato invitato
                                    if(op.isIn((String) flags.get(2)) && (Integer) flags.get(1)!=1 ){
                                        DatagramSocket ds = new DatagramSocket();
                                        String newInvite = "• You've been invited to work on document " + flags.get(3)+"\n";
                                        byte[] msg = newInvite.getBytes();
                                        DatagramPacket packet = new DatagramPacket(msg,msg.length, op.getAddr((String)flags.get(2)));
                                        ds.send(packet);
                                    }
                                    break;

                                case "showdoc":
                                     name = (String) flags.get(1);
                                     op.showDoc(name); //Show(D)
                                    break;

                                case "showsec":
                                    name = (String) flags.get(1);
                                    sect = (String) flags.get(2);
                                    op.showDoc(name, sect); //Show(D, S)
                                    break;

                                case "edit":
                                    String user = (String) flags.get(1);
                                    name = (String) flags.get(2);
                                    sect = (String) flags.get(3);
                                    int index = Integer.parseInt((String) flags.get(4));
                                    check = op.editDoc(name, sect, user, index);
                                    buffer.put(check.byteValue());
                                    break;


                                case "endedit":

                                    user = (String) flags.get(1);
                                    name = (String) flags.get(2);
                                    sect = (String) flags.get(3);
                                    index = Integer.parseInt((String) flags.get(4));
                                    String newSect = (String) flags.get(5);
                                    op.endEdit(name, sect, user, index, newSect);
                                    break;

                                case "send":
                                    name = (String) flags.get(1);
                                    String message = (String) flags.get(2);
                                    user = (String) flags.get(3);
                                    DatagramSocket ds =new DatagramSocket();
                                    InetAddress addr = reg.getAllDocs().get(name).getGroupAddr();
                                    String pack = user + ": " + message;
                                    DatagramPacket datagramPacket = new DatagramPacket(pack.getBytes(),pack.getBytes().length,addr,7880);
                                    ds.send(datagramPacket);
                                    break;
                            }

                            client.register(selector, SelectionKey.OP_READ);
                            buffer.flip();
                            client.write(buffer);
                            buffer.clear();
                            flags.clear();
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {e.printStackTrace(); }


        }catch (RemoteException e){e.printStackTrace();}

    }

}
