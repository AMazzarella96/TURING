import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("Duplicates")
public class UserDB {


    private ConcurrentHashMap<String, Info> users;
    private ConcurrentHashMap<String, SocketAddress> onlineset;
    private ConcurrentHashMap<String, Docs> docs;


    public UserDB(ConcurrentHashMap<String, Info> users, ConcurrentHashMap<String, Docs> docs) {
        this.users = users;
        this.docs = docs;
        onlineset = new ConcurrentHashMap<>();
    }


    public synchronized int login(String username, String password, SocketAddress ip) {

        if (users.containsKey(username) && users.get(username).getPassword().equals(password)) {
            if (!onlineset.containsKey(username)) {
                onlineset.put(username, ip);
                return 1;
            }
            return 0;
        }

        return -1;
    }

    public synchronized void logout(String user) {

        //Serializza le modifiche solo al logout, se crasha non salva nulla per evitare memorizzazioni parziali
        try {
            File file = new File("users.txt");
            FileOutputStream fout = new FileOutputStream(file);
            ObjectOutputStream o = new ObjectOutputStream(fout);
            o.writeObject(users);
            File docsout = new File("docs.txt");
            fout = new FileOutputStream(docsout);
            o = new ObjectOutputStream(fout);
            o.writeObject(docs);
            o.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
        if(!users.get(user).getInEdit().equals("null")) {
            docs.get(users.get(user).getInEdit()).removeEditor(user, docs.get(users.get(user).getInEdit()).getEdSection(user));
            users.get(user).setInEdit("null");

        }
        onlineset.remove(user);
    }

    public void crashed(SocketAddress add) {

        String strKey = null;
        for (ConcurrentHashMap.Entry entry : onlineset.entrySet()) {
            if (add.equals(entry.getValue())) {
                strKey = entry.getKey().toString();
                break;
            }
        }
        if (strKey != null && onlineset.containsKey(strKey)) {
            onlineset.remove(strKey);
            if (!users.get(strKey).getInEdit().equals("null")){
                docs.get(users.get(strKey).getInEdit()).removeEditor(strKey,docs.get(users.get(strKey).getInEdit()).getEdSection(strKey));
            }
        }
        try {
            File docsout = new File("docs.txt");
            FileOutputStream fout = new FileOutputStream(docsout);
            ObjectOutputStream o = new ObjectOutputStream(fout);
            o.writeObject(docs);
            o.close();
        }catch (IOException e){e.printStackTrace();}

    }

    public boolean isIn(String name) {
        return onlineset.containsKey(name);
    }

    public SocketAddress getAddr(String name) {
        return onlineset.get(name);
    }

    public int create(String owner, String name, int nsect) {
        //if(name.equals()) return -1;
        File docdir = new File("src\\Documents", name);
        if (!docdir.exists()) {
            if (docdir.mkdirs()) {
                for (int j = 1; j <= nsect; j++) {
                    File sect = new File(docdir.getPath(), "Section" + j + ".txt");
                    try {
                        if (!sect.createNewFile()) {
                            System.err.println("Error during creation of file");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (users.get(owner)) {
                    Docs d = new Docs(name, nsect);
                    users.get(owner).addOwned(d);
                    docs.put(name, d);
                }
                return 0;
            }
            return -1;
        }
        return 1;
    }

    public void showDoc(String name, String sect) {

        Path path = Paths.get("src\\Documents\\" + name + "\\" + sect);
        try {
            SocketChannel fileSender = SocketChannel.open();
            fileSender.socket().setReuseAddress(true);
            fileSender.connect(new InetSocketAddress("localhost", 2000));
            ByteBuffer buff = ByteBuffer.allocate(1024);
            FileChannel outChannel = FileChannel.open(path, StandardOpenOption.READ);
            buff.clear();
            while (outChannel.read(buff) > 0) {
                buff.flip();
                fileSender.write(buff);
                buff.clear();
            }
            fileSender.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void showDoc(String name) {

        try {
            SocketChannel fileSender = SocketChannel.open();
            fileSender.socket().setReuseAddress(true);
            fileSender.connect(new InetSocketAddress("localhost", 2000));
            ByteBuffer buff = ByteBuffer.allocate(1024);
            buff.clear();

            File subdir = new File("src\\Documents\\" + name);
            File[] sectionListing = subdir.listFiles();

            assert sectionListing != null;
            Arrays.sort(sectionListing, Comparator.comparing(File::getName, new UserGUI.FilenameComparator()));

            int i=0;
            for (File sec : sectionListing) {
                Path path = Paths.get(subdir.getPath() + "\\" + sec.getName());
                FileChannel outChannel = FileChannel.open(path, StandardOpenOption.READ);

                //Separatore delle sezioni per indicare quali eventualmente sono in editing
                String s = sec.getName();
                if(i==0)
                    s = "|---------------" + s +"---------------|";
                else
                    s = "\n\n\n|---------------" + s +"---------------|";
                if(docs.get(name).isLocked(i++))
                    s = s+"   [In Editing]";
                s = s + "\n\n";
                byte[] secname = s.getBytes();
                fileSender.write(ByteBuffer.wrap(secname));


                while (outChannel.read(buff) > 0) {
                    buff.flip();
                    fileSender.write(buff);
                    buff.clear();
                }

            }

            fileSender.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int invite(String user, String doc) {
        if (!users.containsKey(user)) {
            System.out.println("User " + user + " Inesistente");
            return -1;
        } else if (users.get(user).getInvited().contains(docs.get(doc))) {
            return 1;
        } else {
            users.get(user).addInvited(docs.get(doc));
            return 0;
        }
    }

    public synchronized int editDoc(String name, String sec, String user, int index) {

        try {
            SocketChannel fileSender = SocketChannel.open();
            fileSender.socket().setReuseAddress(true);
            fileSender.connect(new InetSocketAddress("localhost", 9000));

            if (!docs.get(name).isLocked(index)) {
                Path path = Paths.get("src\\Documents\\" + name + "\\" + sec);
                ByteBuffer buff = ByteBuffer.allocate(1024);
                FileChannel outChannel = FileChannel.open(path, StandardOpenOption.READ);
                buff.clear();
                while (outChannel.read(buff) > 0) {
                    buff.flip();
                    fileSender.write(buff);
                    buff.clear();
                }
                fileSender.close();
                docs.get(name).addEditor(user, index);
                users.get(user).setInEdit(name);
                return 1;
            }

            else{
                fileSender.close();
                return -1;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Unreachable Statement
        return -1;
    }

    public synchronized void endEdit(String name, String sec, String user, int index, String newSect) {
        docs.get(name).removeEditor(user,index);
        users.get(user).setInEdit("null");
        try{
            FileChannel fileChannel=FileChannel.open(Paths.get("src\\Documents\\"+ name+"\\"+sec),StandardOpenOption.WRITE,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            fileChannel.write(ByteBuffer.wrap(newSect.getBytes()));
            fileChannel.close();

        } catch (IOException e) {
            System.err.println("Unable to overwrite the file!");
        }
    }

    public void newMess(String user, String docname, String message){
        Message m = new Message(user, message);
        docs.get(docname).newMess(m);
    }


}


