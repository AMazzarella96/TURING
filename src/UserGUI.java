import javafx.scene.input.KeyCode;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;


@SuppressWarnings("Duplicates")
public class UserGUI extends JFrame {

    private JFrame frame;
    private JPanel jp;
    private JButton newdocbut,  editbut, showdocbut, logout, invitebut;
    private JLabel username;
    private JTextArea mailbox, mydocs;
    private Authentication stub;
    private String user;
    private Toolkit tk = Toolkit.getDefaultToolkit();
    private Dimension dim = tk.getScreenSize();
    private SocketChannel channel;
    private Font f = new Font("sansserif", Font.PLAIN, 16);
    private Thread t;
    private NotificationHandler notificationHandler;
    private Color c;


    //MAIN FRAME GUI
    public UserGUI(String user, Authentication stub, SocketChannel channel){
        this.user = user;
        this.stub = stub;
        this.channel = channel;

        frame = new JFrame();
        frame.setSize(700, 460);

        int x = (dim.width / 2) - (frame.getWidth() / 2);
        int y = (dim.height / 2) - (frame.getHeight()) / 2;
        frame.setLocation(x, y);
        ListenForWindow win = new ListenForWindow();
        frame.addWindowListener(win);
        frame.setTitle("TURING");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //BACKGROUND
        ImageIcon background = new ImageIcon("src\\Templates\\guiback.jpg");
        Image img = background.getImage();
        background = new ImageIcon(img.getScaledInstance(700,460, Image.SCALE_SMOOTH));
        JLabel b = new JLabel("", background, JLabel.CENTER);
        b.setBounds(0,0,700,460);

        jp = new JPanel();
        jp.setLayout(null);



        //Button Handler
        UserGUI.ListenForButton lis = new UserGUI.ListenForButton();

        c = new Color(49,63,76);

        //new doc Button
        Icon newico = new ImageIcon("src\\Templates\\newico.png");
        newdocbut = new JButton("New", newico);
        jp.add(newdocbut);
        newdocbut.setFont(f);
        newdocbut.setBounds(17,90,165,40);
        newdocbut.setToolTipText("Create new Document");
        newdocbut.setBackground(c);
        newdocbut.setForeground(Color.WHITE);
        newdocbut.addActionListener(lis);


        //edit document button
        Icon editico = new ImageIcon("src\\Templates\\editico.png");
        editbut = new JButton("Edit", editico);
        editbut.setToolTipText("Edit Specified Document/Section");
        jp.add(editbut);
        editbut.setFont(f);
        editbut.setBounds(newdocbut.getX(),newdocbut.getY()+55,165,40);
        editbut.setBackground(c);
        editbut.setForeground(Color.WHITE);
        editbut.addActionListener(lis);

        //Show Section button
        Icon showico = new ImageIcon("src\\Templates\\showico.png");
        showdocbut = new JButton("Show", showico);
        jp.add(showdocbut);
        showdocbut.setFont(f);
        showdocbut.setBounds(newdocbut.getX(),editbut.getY()+55,165,40);
        showdocbut.setBackground(c);
        showdocbut.setForeground(Color.WHITE);
        showdocbut.addActionListener(lis);


        //Invite users
        Icon inviteico = new ImageIcon("src\\Templates\\inviteico.png");
        invitebut = new JButton("Invite", inviteico);
        jp.add(invitebut);
        invitebut.setFont(f);
        invitebut.setBounds(newdocbut.getX(),showdocbut.getY()+55,165,40);
        invitebut.setBackground(c);
        invitebut.setForeground(Color.WHITE);
        invitebut.addActionListener(lis);


        //logout button
        logout = new JButton("Logout");
        logout.setBackground(new Color(115,10,10));
        logout.setForeground(Color.WHITE);
        jp.add(logout);
        logout.setFont(new Font(logout.getFont().getName(), Font.BOLD, 15));
        logout.setBounds(frame.getWidth()-110,8,logout.getPreferredSize().width,logout.getPreferredSize().height);
        logout.addActionListener(lis);

        //username
        Icon logo = new ImageIcon("src\\Templates\\logo.png");
        username = new JLabel(" Hi, "+ user + "!", logo ,SwingConstants.RIGHT);
        username.setForeground(Color.WHITE);
        jp.add(username);
        username.setFont(f);
        username.setBounds(frame.getWidth()-410, 13, 290, username.getPreferredSize().height);


        //MailBox
        mailbox = new JTextArea();
        jp.add(mailbox);
        mailbox.setFont(f);
        mailbox.setBounds(newdocbut.getWidth() + 35, newdocbut.getY(), 315, 280);
        mailbox.setEditable(false);
        mailbox.setWrapStyleWord(true);
        mailbox.setLineWrap(true);
        try {
            ArrayList<Docs> invitations = stub.getMyInvites(user);
            for(Docs s : invitations){
                mailbox.append(" • You've been invited to work on document " + s.getName() + "\n");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        try {
            notificationHandler = new NotificationHandler(mailbox, channel.getLocalAddress());
           t = new Thread(notificationHandler);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        JLabel notif = new JLabel("Notifications", SwingConstants.CENTER);
        notif.setFont(f);
        notif.setBounds(mailbox.getX(), mailbox.getY()-25, mailbox.getWidth(), 25);
        notif.setOpaque(true);
        notif.setBackground(c);
        notif.setForeground(Color.WHITE);
        jp.add(notif);




        //MyDocs
        mydocs = new JTextArea();
        jp.add(mydocs);
        mydocs.setFont(f);
        mydocs.setEditable(false);
        mydocs.setBounds((mailbox.getWidth()+15) + mailbox.getX(), mailbox.getY(), 150 ,280);

        JLabel my = new JLabel("My Documents", SwingConstants.CENTER);
        my.setFont(f);
        my.setBounds(mydocs.getX(), mydocs.getY()-25, 150, 25);
        my.setOpaque(true);
        my.setBackground(c);
        my.setForeground(Color.WHITE);
        jp.add(my);
        try {
            for(Docs d : stub.getMyDocs(user)){
                mydocs.append(d.getName()+"\n");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        frame.add(jp);
        frame.setVisible(true);
        frame.setBackground(Color.blue);
        jp.add(b);


    }



    //INVITE USER GUI
    private class InviteGUI extends JFrame{

        private JFrame f2;
        private JTextField dest;
        private JComboBox<String> list;
        private JButton send;
        private JLabel name;
         private SocketChannel channel;

        public InviteGUI(SocketChannel channel) {
            this.channel = channel;
            f2 = new JFrame();
            f2.setSize(500, 315);
            int x = (dim.width / 2) - (f2.getWidth() / 2);
            int y = (dim.height / 2) - (f2.getHeight()) / 2;
            f2.setLocation(x, y);
            f2.setTitle("Send Invitation");
            f2.setResizable(false);
            JPanel jp = new JPanel();
            jp.setLayout(null);
            f2.add(jp);

            f2.setAlwaysOnTop(true);
            frame.setEnabled(false);

            //Background
            ImageIcon background = new ImageIcon("src\\Templates\\newgui.jpg");
            Image i = background.getImage();
            background = new ImageIcon(i.getScaledInstance(500,315, Image.SCALE_SMOOTH));
            JLabel b = new JLabel("", background, JLabel.CENTER);
            b.setBounds(0,0,f2.getPreferredSize().width,f2.getPreferredSize().height);

            //Receiver
            dest = new JTextField();
            dest.setBounds((f2.getWidth()-230)/2, 45, 230, 35);
            dest.setFont(f);
            name = new JLabel("Receiver:");
            name.setForeground(Color.WHITE);
            name.setFont(f);
            name.setBounds(dest.getX(), dest.getY()-25, name.getPreferredSize().width, name.getPreferredSize().height);
            jp.add(dest);
            jp.add(name);

            //Sender's Document list
            try {

                //Fill combobox with owner's documents
                ArrayList<Docs> tmp = stub.getMyDocs(user);
                String[] s = new String[tmp.size()+1];
                s[0] = "Select Document";
                int j=1;
                for(Docs d : tmp)
                    s[j++] = d.getName();
                list=new JComboBox<>(s);
                list.setFont(f);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
            list.setBounds((f2.getWidth()-230)/2, dest.getY()+75, 230, 35);
            jp.add(list);


            send = new JButton("Invite");
            send.setFont(f);
            send.setBounds((f2.getWidth()-165)/2,list.getY()+75,165, 30);
            jp.add(send);
            f2.getRootPane().setDefaultButton(send);
            InviteGUI.ListenForButton lis = new ListenForButton();
            send.addActionListener(lis);

            f2.setVisible(true);
            jp.setOpaque(false);
            f2.add(b);
        }

        private class ListenForButton implements ActionListener{

            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == send){
                    if(dest.getText().length()==0){
                        JOptionPane.showMessageDialog(f2, "Select an User", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                    else if(user.equals(dest.getText())){
                        JOptionPane.showMessageDialog(f2, "You Can't Invite Yourself!", "Invalid Username", JOptionPane.WARNING_MESSAGE);
                    }
                    else if(list.getSelectedIndex()==0){
                        JOptionPane.showMessageDialog(f2, "Select a Document", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                    else {
                        ArrayList<String> param = new ArrayList<>();
                        param.add(dest.getText());

                        param.add((String) list.getSelectedItem());
                        sendReq(channel, new Request("invite", param));
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        try {
                            channel.read(buffer);
                            buffer.flip();
                            int res = buffer.get();
                            if (res == -1) {
                                JOptionPane.showMessageDialog(f2, "Inexistent User", "Error", JOptionPane.ERROR_MESSAGE);
                            } else if (res == 0) {
                                JOptionPane.showMessageDialog(f2, "Invitation Sent Successfully!", "Success", JOptionPane.PLAIN_MESSAGE);
                                f2.setVisible(false);
                                f2.dispose();
                            } else if(res == 1){
                                JOptionPane.showMessageDialog(f2, "User Already Invited To That Document", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (IOException io) {
                            io.printStackTrace();
                        }
                    }
                }
            }
        }

    }




    //CREATE NEW DOCUMENT GUI
    private class CreateDocGUI extends JFrame{

        private JFrame f3;
        private JTextField filename;
        private JComboBox<String> numSec;
        private JButton create;
        private JLabel name, doc;
        private SocketChannel channel;
        private String[] num = {"N. Section","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20"};

        public CreateDocGUI(SocketChannel channel) {

            this.channel = channel;
            f3 = new JFrame();
            f3.setSize(500, 315);
            int x = (dim.width / 2) - (f3.getWidth() / 2);
            int y = (dim.height / 2) - (f3.getHeight()) / 2;
            f3.setLocation(x, y);
            f3.setTitle("New Document");
            f3.setResizable(false);
            JPanel jp = new JPanel();
            jp.setLayout(null);
            f3.add(jp);

            //Background
            ImageIcon background = new ImageIcon("src\\Templates\\newgui.jpg");
            Image i = background.getImage();
            background = new ImageIcon(i.getScaledInstance(500,315, Image.SCALE_SMOOTH));
            JLabel b = new JLabel("", background, JLabel.CENTER);
            b.setBounds(0,0,f3.getPreferredSize().width,f3.getPreferredSize().height);

           CreateDocGUI.ListenForButton lis = new ListenForButton();

            //FileName
            filename = new JTextField();
            filename.setBounds((f3.getWidth()-230)/2, 45, 230, 35);
            filename.setFont(f);
            name = new JLabel("File Name:");
            name.setForeground(Color.WHITE);
            name.setFont(f);
            name.setBounds(filename.getX(), filename.getY()-25, name.getPreferredSize().width, name.getPreferredSize().height);
            jp.add(filename);
            jp.add(name);

            //Num. Section
            numSec=new JComboBox<>(num);
            numSec.setBounds((f3.getWidth()-120)/2, filename.getY()+75, 120, 35);
            numSec.setFont(f);

            jp.add(numSec);


            create = new JButton("Create");
            create.setFont(f);
            create.setBounds((f3.getWidth()-150)/2,numSec.getY()+75,150, 30);
            jp.add(create);
            create.addActionListener(lis);
            f3.getRootPane().setDefaultButton(create);
            f3.setVisible(true);
            jp.setOpaque(false);
            f3.add(b);
        }



        private class ListenForButton implements ActionListener{

            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == create){
                    try {
                        if(filename.getText().length()==0)
                            JOptionPane.showMessageDialog(f3, "Specify Doc Name", "Warning!", JOptionPane.WARNING_MESSAGE);
                        else
                        if(numSec.getSelectedIndex()!=0) {
                            ArrayList<String> param = new ArrayList<>();
                            param.add(user);
                            param.add(filename.getText());
                            param.add(numSec.getItemAt(numSec.getSelectedIndex()));
                            sendReq(channel, new Request("newdoc", param));
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            channel.read(buffer);
                            buffer.flip();
                            int res = buffer.get();
                            if (res == -1) {
                                JOptionPane.showMessageDialog(f3, "Error during creation of document", "Error", JOptionPane.ERROR_MESSAGE);
                            } else if (res > 0) {

                                //STAMPA LO STESSO ANCHE CON NOME DOCUMENTO VUOTO
                                JOptionPane.showMessageDialog(f3, "Document already exists!\n Select a different Name", "Warning", JOptionPane.WARNING_MESSAGE);
                            } else {
                                mydocs.append(filename.getText()+"\n");
                                JOptionPane.showMessageDialog(f3, "Document Created Successfully!", "Success", JOptionPane.PLAIN_MESSAGE);
                                f3.setVisible(false);
                                f3.dispose();
                            }
                        }
                        else
                            JOptionPane.showMessageDialog(f3, "Select Number of Sections", "Warning!", JOptionPane.WARNING_MESSAGE);
                    } catch (IOException rm){rm.printStackTrace();}
                }
            }
        }

    }



    //SHOW DOCUMENT GUI
    private class ShowDocGUI extends JFrame{

        JFrame f1;
        SocketChannel channel;
        JLabel name;
        JComboBox<String> list, sect;
        String[] sections;
        JButton show;

        public ShowDocGUI(SocketChannel channel){


            this.channel = channel;
            f1 = new JFrame();
            f1.setSize(500, 315);
            int x = (dim.width / 2) - (f1.getWidth() / 2);
            int y = (dim.height / 2) - (f1.getHeight()) / 2;
            f1.setLocation(x, y);
            f1.setTitle("Show Document");
            f1.setResizable(false);
            JPanel jp = new JPanel();
            jp.setLayout(null);
            f1.add(jp);

            //Background
            ImageIcon background = new ImageIcon("src\\Templates\\newgui.jpg");
            Image i = background.getImage();
            background = new ImageIcon(i.getScaledInstance(500,315, Image.SCALE_SMOOTH));
            JLabel b = new JLabel("", background, JLabel.CENTER);
            b.setBounds(0,0,f1.getPreferredSize().width,f1.getPreferredSize().height);

            ShowDocGUI.ListenForButton lis = new ListenForButton();


            File dir = new File("src\\Documents");
            File[] directoryListing = dir.listFiles();
            assert directoryListing!=null;
            int l = directoryListing.length;
            String[] files = new String[l+1];
            files[0] = "Select Document";
            int z=1;
            for (File child : directoryListing) {
                files[z++] = child.getName();
            }



            dir.delete();

            list = new JComboBox<>(files);
            list.setBounds((f1.getWidth()-165)/2,45, 165, 35);
            list.addActionListener(lis);
            list.setFont(f);
            name = new JLabel("File Name:");
            name.setForeground(Color.WHITE);
            name.setFont(f);
            name.setBounds(list.getX(), list.getY()-25, name.getPreferredSize().width, name.getPreferredSize().height);
            jp.add(name);
            jp.add(list);


            sect = new JComboBox<>();
            sect.setBounds((f1.getWidth()-150)/2, list.getY()+60, 150, 35);
            sect.setFont(f);
            jp.add(sect);

            show = new JButton("Show");
            show.setFont(f);
            show.setBounds((f1.getWidth()-150)/2,sect.getY()+75,150, 30);
            jp.add(show);
            show.addActionListener(lis);
            f1.getRootPane().setDefaultButton(show);

            f1.setVisible(true);
            jp.setOpaque(false);
            f1.add(b);



        }

        private class ListenForButton implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                    if(e.getSource()==show){
                        if(list.getSelectedIndex()==0)
                            JOptionPane.showMessageDialog(f1, "Select a Document!", "Warning", JOptionPane.WARNING_MESSAGE);
                        else {
                            ArrayList<String> param = new ArrayList<>();
                            String name = (String) list.getSelectedItem();
                            param.add(name);
                            String req = "showdoc";
                            if (sect.getSelectedIndex() != 0) {
                                req = "showsec";
                                param.add((String) sect.getSelectedItem());
                                name = name + " - " + sect.getSelectedItem();
                                try {
                                    if(stub.inEditing((String) list.getSelectedItem(), sect.getSelectedIndex()-1))
                                        name = name + " [In Editing]";
                                } catch (RemoteException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            sendReq(channel, new Request(req, param));

                            try {
                                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                                serverSocketChannel.socket().bind(new InetSocketAddress("localhost", 2000));
                                SocketChannel reciever = serverSocketChannel.accept();
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                buffer.clear();

                                assert sect.getSelectedItem() != null;
                                File file = new File("src\\Documents", (String) sect.getSelectedItem());
                                if (!file.createNewFile()) System.err.println("Error while creating file");
                                else {
                                    FileChannel fileChannel = FileChannel.open(Paths.get("src\\Documents\\" + sect.getSelectedItem()), StandardOpenOption.WRITE);
                                    while (reciever.read(buffer) > 0) {
                                        buffer.flip();
                                        fileChannel.write(buffer);
                                        buffer.clear();
                                    }

                                    fileChannel.close();
                                    reciever.close();
                                    serverSocketChannel.close();

                                    String line;
                                    FileReader fr = new FileReader(file);
                                    BufferedReader reader = new BufferedReader(fr);


                                    DocVisualizer docFrame = new DocVisualizer(name);
                                    while ((line = reader.readLine()) != null) {
                                        docFrame.page.append(line + "\n");
                                    }
                                    fr.close();
                                    reader.close();

                                    f1.setVisible(false);
                                    f1.dispose();
                                    if (!file.delete()) System.err.println("Error while deleting file");
                                }

                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                }

                if(e.getSource() == list){
                    assert list.getSelectedItem()!=null;
                    if(!list.getSelectedItem().equals("Select Document")) {
                        File subdir = new File("src\\Documents\\" + list.getSelectedItem());
                        File[] sectionListing = subdir.listFiles();
                        assert sectionListing!=null;

                        Arrays.sort(sectionListing, Comparator.comparing(File::getName, new FilenameComparator()));

                        sections = new String[sectionListing.length + 1];
                        sections[0] = "Entire Document";
                        int z = 1;
                        for (File sec : sectionListing) {
                            sections[z++] = sec.getName();
                        }
                         sect.setModel(new DefaultComboBoxModel<>(sections));

                    }
                     else{
                        sect.setModel(new DefaultComboBoxModel<>());
                    }

                 }
            }
        }

        private class DocVisualizer extends JFrame {

            private JTextArea page;


            public DocVisualizer(String name){
                JFrame doc = new JFrame(name);
                doc.setSize(900, 569);
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Dimension size = toolkit.getScreenSize();
                int x = (size.width / 2) - (doc.getWidth() / 2);
                int y = (size.height / 2) - (doc.getHeight() / 2);
                doc.setLocation(x, y);
                JPanel panel = (JPanel) doc.getContentPane();
                panel.setLayout(new BorderLayout());
                page = new JTextArea();
                page.setBounds(0, 0, panel.getWidth(), panel.getHeight());
                page.setEditable(false);
                page.setWrapStyleWord(true);
                page.setLineWrap(true);
                page.setFont(f);
                JScrollPane scrollableTextArea = new JScrollPane(page);
                scrollableTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                panel.add(scrollableTextArea);
                page.setRows(10);
                page.setColumns(10);
                panel.setBorder(new EtchedBorder());
                doc.setResizable(false);
                doc.setVisible(true);
            }



        }



    }




    //EDIT DOCUMENT GUI
    private class EditDocGUI extends JFrame {
        JFrame f1;
        SocketChannel channel;
        JLabel name;
        JComboBox<String> list, sect;
        String[] sections;
        JButton edit;

        public EditDocGUI(SocketChannel channel) {

            this.channel = channel;
            f1 = new JFrame();
            f1.setSize(500, 315);
            int x = (dim.width / 2) - (f1.getWidth() / 2);
            int y = (dim.height / 2) - (f1.getHeight()) / 2;
            f1.setLocation(x, y);
            f1.setTitle("Edit Section");
            f1.setResizable(false);
            JPanel jp = new JPanel();
            jp.setLayout(null);
            f1.add(jp);

            //Background
            ImageIcon background = new ImageIcon("src\\Templates\\newgui.jpg");
            Image i = background.getImage();
            background = new ImageIcon(i.getScaledInstance(500, 315, Image.SCALE_SMOOTH));
            JLabel b = new JLabel("", background, JLabel.CENTER);
            b.setBounds(0, 0, f1.getPreferredSize().width, f1.getPreferredSize().height);

            ListenForButton lis = new ListenForButton();


            ArrayList<String> myDocs = null;
            try {
                myDocs = stub.getDocs(user);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            myDocs.add(0, "Select Document");
            String[] files = myDocs.toArray(new String[0]);


            list = new JComboBox<>(files);
            list.setBounds((f1.getWidth() - 150) / 2, 45, 150, 35);
            list.addActionListener(lis);
            name = new JLabel("File Name:");
            name.setForeground(Color.WHITE);
            name.setFont(f);
            name.setBounds(list.getX(), list.getY() - 25, name.getPreferredSize().width, name.getPreferredSize().height);
            jp.add(name);
            jp.add(list);


            sect = new JComboBox<>();
            sect.setBounds((f1.getWidth() - 150) / 2, list.getY() + 60, 150, 35);
            jp.add(sect);

            edit = new JButton("Edit");
            edit.setFont(f);
            edit.setBounds((f1.getWidth() - 150) / 2, sect.getY() + 75, 150, 30);
            jp.add(edit);
            edit.addActionListener(lis);
            f1.getRootPane().setDefaultButton(edit);

            f1.setVisible(true);
            jp.setOpaque(false);
            f1.add(b);
        }

        private class ListenForButton  implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(e.getSource()==edit){

                    if(list.getSelectedIndex()==0)
                        JOptionPane.showMessageDialog(f1, "Select a Document!", "Warning", JOptionPane.WARNING_MESSAGE);



                    else {
                        try {
                            if(!stub.getinEdit(user))
                                JOptionPane.showMessageDialog(f1, "You can only edit a section per time!", "Warning", JOptionPane.WARNING_MESSAGE);

                            else{
                                ArrayList<String> param = new ArrayList<>();
                                param.add(user);
                                String name = (String) list.getSelectedItem();
                                param.add(name);
                                String sec = (String) sect.getSelectedItem();
                                param.add(sec);
                                param.add(String.valueOf(sect.getSelectedIndex()));
                                name = name + " - " + sec;

                                sendReq(channel, new Request("edit", param));
                                ByteBuffer buffer = ByteBuffer.allocate(1024);

                                    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                                    serverSocketChannel.socket().bind(new InetSocketAddress("localhost", 9000));
                                    SocketChannel receiver = serverSocketChannel.accept();

                                    buffer.clear();
                                    channel.read(buffer);
                                    buffer.flip();
                                    int res = buffer.get();
                                    buffer.clear();

                                    if (res == 1){

                                        assert sec!=null;
                                        File file=new File("src\\Documents", sec);
                                        if (file.createNewFile()) {
                                            FileChannel fileChannel = FileChannel.open(Paths.get("src\\Documents\\" + sec), StandardOpenOption.WRITE);
                                            while (receiver.read(buffer) > 0) {
                                                buffer.flip();
                                                fileChannel.write(buffer);
                                                buffer.clear();
                                            }

                                            fileChannel.close();
                                            EditingGUI space=new EditingGUI(name);
                                            String line;
                                            FileReader fr = new FileReader(file);
                                            BufferedReader reader = new BufferedReader(fr);

                                            while ((line = reader.readLine()) != null) {

                                                space.page.append(line + "\n");
                                            }
                                            fr.close();
                                            reader.close();

                                            if (!file.delete()){
                                                System.err.println("Cannot delete the temporary file");
                                            }
                                        }

                                    } else {
                                        JOptionPane.showMessageDialog(f1, "Section already in Editing!", "Warning", JOptionPane.WARNING_MESSAGE);
                                    }

                                    receiver.close();
                                    serverSocketChannel.close();

                                f1.setVisible(false);
                                f1.dispose();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }


                if(e.getSource()==list){
                    assert list.getSelectedItem()!=null;
                    if(!list.getSelectedItem().equals("Select Document")) {
                        File subdir = new File("src\\Documents\\" + list.getSelectedItem());
                        File[] sectionListing = subdir.listFiles();
                        assert sectionListing!=null;

                        Arrays.sort(sectionListing, Comparator.comparing(File::getName, new FilenameComparator()));

                        sections = new String[sectionListing.length];
                        int z = 0;
                        for (File sec : sectionListing) {
                            sections[z++] = sec.getName();
                        }
                        sect.setModel(new DefaultComboBoxModel<>(sections));

                    }
                    else{
                        sect.setModel(new DefaultComboBoxModel<>());
                    }
                }

            }
        }

        private class EditingGUI extends JFrame{
            private JTextArea page, chat, textchat;
            private JButton end;
            private JFrame doc;
            Thread t;
            ChatHandler handler;

            public EditingGUI(String name){
                doc = new JFrame();
                doc.setSize(1120, 830);

                int x = (dim.width / 2) - (doc.getWidth() / 2);
                int y = (dim.height / 2) - (doc.getHeight()) / 2;
                doc.setLocation(x, y);
                doc.setTitle(name);
                doc.setResizable(false);

                JPanel panel = new JPanel();
                panel.setLayout(null);

                ListenForKey lis = new ListenForKey();
                ListenForButton lbut = new ListenForButton();
                ListenForWindow lwin = new ListenForWindow();

                doc.addWindowListener(lwin);

                //Editing documento
                page = new JTextArea();
                page.setEditable(true);
                page.setWrapStyleWord(true);
                page.setLineWrap(true);
                page.setFont(f);
                JScrollPane scrollableTextArea = new JScrollPane(page);
                scrollableTextArea.setBounds(5,5,700,785);
                scrollableTextArea.getViewport().setOpaque(false);
                scrollableTextArea.setOpaque(false);
                panel.add(scrollableTextArea);


                //Chat
                chat = new JTextArea();
                chat.setEditable(false);
                chat.setWrapStyleWord(true);
                chat.setLineWrap(true);
                chat.setFont(f);

                try {
                    for(Message m : stub.getHistory((String) list.getSelectedItem())){
                        chat.append(m.getUsername() + ": "+ m.getMessage()+"\n");
                    }

                    handler = new ChatHandler(chat, stub.GetAddress((String) list.getSelectedItem()));
                    t = new Thread(handler);
                    t.start();
                } catch (RemoteException | UnknownHostException e) {
                    e.printStackTrace();
                }

                JScrollPane scrollableChat = new JScrollPane(chat);
                scrollableChat.setBounds(scrollableTextArea.getX()+scrollableTextArea.getWidth()+5,scrollableTextArea.getY(),400,677);
                scrollableChat.getViewport().setOpaque(false);
                scrollableChat.setOpaque(false);
                panel.add(scrollableChat);

                textchat = new JTextArea();
                textchat.setEditable(true);
                textchat.setWrapStyleWord(true);
                textchat.setLineWrap(true);
                textchat.setFont(f);
                JScrollPane scrollableText = new JScrollPane(textchat);
                scrollableText.setBounds(scrollableChat.getX(),scrollableChat.getY() + scrollableChat.getHeight()+3,scrollableChat.getWidth(),60);
                scrollableText.getViewport().setOpaque(false);
                scrollableText.setOpaque(false);
                textchat.addKeyListener(lis);
                panel.add(scrollableText);

                end = new JButton("End Edit");
                end.setFont(f);
                end.setBackground(c);
                end.setForeground(Color.WHITE);
                end.setBounds(scrollableChat.getX()+scrollableChat.getWidth()-125, scrollableText.getY()+scrollableText.getHeight()+6, 120, 35);
                end.addActionListener(lbut);
                panel.add(end);


                doc.add(panel);
                doc.setVisible(true);
            }

            private class ListenForButton implements ActionListener{

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(e.getSource()==end){
                        ArrayList<String> param = new ArrayList<>();
                        param.add(user);
                        param.add((String) list.getSelectedItem());
                        param.add((String) sect.getSelectedItem());
                        param.add(String.valueOf(sect.getSelectedIndex()));
                        param.add(page.getText());
                        Request request = new Request("endedit", param);
                        sendReq(channel, request);
                        handler.terminate();
                        t.interrupt();
                        doc.setVisible(false);
                        doc.dispose();
                    }
                }
            }

            private class ListenForKey implements KeyListener{

                @Override
                public void keyTyped(KeyEvent e) {

                }

                @Override
                public void keyPressed(KeyEvent e) {

                    if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                        String message = textchat.getText();
                        message.replaceAll("\n", "");
                        if(message.length()!=0) {
                            ArrayList<String> param = new ArrayList<>();
                            param.add((String) list.getSelectedItem());
                            param.add(textchat.getText());
                            param.add(user);
                            Request request = new Request("send", param);
                            sendReq(channel, request);
                            textchat.setText(null);
                        }


                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if(e.getKeyCode() == KeyEvent.VK_ENTER)
                        textchat.setText("");
                }
            }

            private class ListenForWindow implements WindowListener{

                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    int confirm=JOptionPane.showConfirmDialog(EditDocGUI.this,"Closing without press End Edit will dischard your changes!\nAre you sure you want to exit?","Warning!",JOptionPane.YES_NO_OPTION);
                    if (confirm==JOptionPane.YES_OPTION){
                        ArrayList<String> param = new ArrayList<>();
                        param.add(user);
                        param.add(String.valueOf(list.getSelectedItem()));
                        param.add(String.valueOf(sect.getSelectedIndex()));
                        Request request = new Request("undo", param);
                        sendReq(channel,request);
                        doc.setVisible(false);
                        doc.dispose();
                    }
                    else{
                        doc.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    }
                }

                @Override
                public void windowClosed(WindowEvent e) {

                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            }

        }

    }




    // *------------------------------------------MAIN FRAME LISTENERS--------------------------------------------*


    private class ListenForWindow implements WindowListener {

        @Override
        public void windowClosing(WindowEvent e) {

            int c=JOptionPane.showConfirmDialog(UserGUI.this,"Are you sure you want to exit?","Warning!",JOptionPane.YES_NO_OPTION);
            if(c == JOptionPane.YES_OPTION) {
                ArrayList<String> param = new ArrayList<>();
                param.add(user);
                param.add("exit");
                Request request = new Request("logout", param);
                sendReq(channel, request);
                t.interrupt();
                frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            }
            else frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        }

        @Override
        public void windowOpened(WindowEvent e) {

        }

        @Override
        public void windowClosed(WindowEvent e) {}

        @Override
        public void windowIconified(WindowEvent e) {

        }

        @Override
        public void windowDeiconified(WindowEvent e) {

        }

        @Override
        public void windowActivated(WindowEvent e) {

        }

        @Override
        public void windowDeactivated(WindowEvent e) {}
    }

    private class ListenForButton implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            ArrayList<String> param = new ArrayList<>();

            //Logout Button Handler
            if (e.getSource() == logout){
                try{
                    if(stub.getinEdit(user)){
                        param.add(user);
                        Request request = new Request("logout", param);
                        sendReq(channel, request);
                        frame.setVisible(false);
                        notificationHandler.terminate();
                        //terminate() chiude la datagram socket del Notification Handler per le notifiche
                        t.interrupt();
                        frame.dispose();
                        new GUI(stub, channel);
                    }

                    else JOptionPane.showMessageDialog(frame,"You must press 'End Edit' before logout", "Warning - Section in Editing", JOptionPane.WARNING_MESSAGE);
                }catch (RemoteException r){r.printStackTrace();}

            }

            if (e.getSource()==invitebut){
                new InviteGUI(channel);
            }

            if(e.getSource() == editbut){
                new EditDocGUI(channel);
            }

            if(e.getSource() == newdocbut){
                new CreateDocGUI(channel);
            }

            if(e.getSource() == showdocbut){
                new ShowDocGUI(channel);
            }
        }
    }

    private static void sendReq(SocketChannel ch, Request r){
        try {
            ByteArrayOutputStream byteout = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteout);
            outputStream.writeObject(r);
            outputStream.flush();
            ch.write(ByteBuffer.wrap(byteout.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //Comparatore per ordinare la lista di documenti all'interno dei combobox
    public static final class FilenameComparator implements Comparator<String> {
        private final Pattern NUMBERS =
                Pattern.compile("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        @Override public final int compare(String o1, String o2) {

            if (o1 == null || o2 == null)
                return o1 == null ? o2 == null ? 0 : -1 : 1;

            // Splittare entrambe le stringhe in base al pattern
            String[] split1 = NUMBERS.split(o1);
            String[] split2 = NUMBERS.split(o2);

            for (int i = 0; i < Math.min(split1.length, split2.length); i++) {
                char c1 = split1[i].charAt(0);
                char c2 = split2[i].charAt(0);
                int cmp = 0;

                if (c1 >= '0' && c1 <= '9' && c2 >= 0 && c2 <= '9')
                    cmp = new BigInteger(split1[i]).compareTo(new BigInteger(split2[i]));

                if (cmp == 0)
                    cmp = split1[i].compareTo(split2[i]);

                if (cmp != 0)
                    return cmp;
            }

            return split1.length - split2.length;
        }
    }

}

