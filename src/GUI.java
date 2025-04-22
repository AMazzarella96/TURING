import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("Duplicates")

public class GUI extends JFrame{

    private static Color c = new Color(94,135,57);
    private JPasswordField psw;
    private JTextField usr;
    private JButton logbut;
    private JButton regbut;
    private Authentication stub;
    private SocketChannel channel;
    private JPanel jp;
    private JFrame frame;
    private Font f = new Font("sansserif", Font.BOLD, 15);
    private Font f2 = new Font("sansserif", Font.PLAIN, 16);


    public GUI(Authentication stub, SocketChannel channel) {

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        frame = new JFrame();

        this.channel = channel;

        this.stub=stub;

        frame.setSize(700, 479);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension dim = tk.getScreenSize();
        int x = (dim.width / 2) - (frame.getWidth() / 2);
        int y = (dim.height / 2) - (frame.getHeight()) / 2;
        frame.setLocation(x, y);
        frame.setTitle("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ListenForWindow win = new ListenForWindow();
        frame.addWindowListener(win);

        //BACKGROUND
        ImageIcon background = new ImageIcon("src\\Templates\\background.jpg");
        Image img = background.getImage();
        background = new ImageIcon(img.getScaledInstance(700,479, Image.SCALE_SMOOTH));
        JLabel b = new JLabel("", background, JLabel.CENTER);
        b.setBounds(0,0,700,479);

        jp = (JPanel) frame.getContentPane();
        jp.setLayout(null);

        ListenForButton lis = new ListenForButton();

        //Username
        usr  = new JTextField();
        usr.setFont(f2);
        usr.setBounds(225, 184, 250, 35);
        jp.add(usr);

        //Password
        psw = new JPasswordField();
        psw.setFont(f2);
        psw.setEchoChar('•');
        psw.setBounds(225, usr.getY()+75, 250, 35);
        jp.add(psw);


        //ShowPSW CheckBox
        JCheckBox cb = new JCheckBox("Show Password");
        cb.setFont(f);
        cb.setHorizontalTextPosition(SwingConstants.LEFT);
        cb.setBounds(350, psw.getY()+40, cb.getPreferredSize().width, cb.getPreferredSize().height);
        jp.add(cb);
        ListenForItem check = new ListenForItem();
        cb.addItemListener(check);

        //Login Button
        logbut = new JButton("Login");
        logbut.setFont(f);
        logbut.setBounds(245, cb.getY()+60, 85, 30);

        jp.add(logbut);
        frame.getRootPane().setDefaultButton(logbut);
        logbut.setBackground(Color.DARK_GRAY);
        logbut.setForeground(Color.WHITE);
        logbut.addActionListener(lis);



        JLabel l = new JLabel("or");
        l.setFont(f);
        l.setBounds(345, logbut.getY()+2, l.getPreferredSize().width, l.getPreferredSize().height);
        jp.add(l);




        //Register Button
        regbut = new JButton("Register");
        regbut.setFont(f);
        regbut.setBounds(369, logbut.getY(), 90, 30);
        regbut.setBackground(c);
        regbut.setForeground(Color.WHITE);
        jp.add(regbut);
        regbut.addActionListener(lis);



        frame.setResizable(false);
        frame.setVisible(true);
        jp.setOpaque(false);
        frame.add(b);



    }

    private class ListenForWindow implements WindowListener {

        @Override
        public void windowClosing(WindowEvent e) {
            if(channel.isOpen()) {
                Request request = new Request("exit");
                ByteArrayOutputStream byteout = new ByteArrayOutputStream();
                try {
                    ObjectOutputStream outputStream = new ObjectOutputStream(byteout);
                    outputStream.writeObject(request);
                    outputStream.flush();
                    channel.write(ByteBuffer.wrap(byteout.toByteArray()));
                    channel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
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
            String username = usr.getText();
            String password = Arrays.toString(psw.getPassword());

            //Login Button Handler
            if (e.getSource() == logbut) {
                try {
                    param.add(username);
                    param.add(password);
                    Request request = new Request("login", param);

                    ByteArrayOutputStream byteout = new ByteArrayOutputStream();
                    ObjectOutputStream outputStream = new ObjectOutputStream(byteout);
                    outputStream.writeObject(request);
                    outputStream.flush();
                    channel.write(ByteBuffer.wrap(byteout.toByteArray()));
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    channel.read(buffer);
                    buffer.flip();
                    int res = buffer.get();

                    if (res==-1) {
                        JOptionPane.showMessageDialog(frame, "Wrong Username or Password", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else if(res==0){
                        JOptionPane.showMessageDialog(frame, "User already logged in", "Warning", JOptionPane.WARNING_MESSAGE);
                    }

                    else{
                        new UserGUI(username, stub, channel);
                        frame.setVisible(false);
                        frame.dispose();
                    }

                } catch (IOException rm) {
                    rm.printStackTrace();
                }
            }

            //Register Button Handler
            if(e.getSource() == regbut){
                if (usr.getText().length() < 4 || usr.getText().length()>16) {
                    JOptionPane.showMessageDialog(frame, "Invalid Username\nIt must contains 4-16 characters", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else if(psw.getPassword().length<4)
                    JOptionPane.showMessageDialog(frame, "Invalid Password - At least 4 characters", "Error", JOptionPane.ERROR_MESSAGE);
                else{
                    try{
                        if(!stub.register(username,password)){
                            JOptionPane.showMessageDialog(frame, "Unavailable Username\n Choose a different one", "Account already exists", JOptionPane.WARNING_MESSAGE);
                        }
                        else JOptionPane.showMessageDialog(frame, "Account created successfully!", "Success!", JOptionPane.PLAIN_MESSAGE);
                    }catch (RemoteException rm){rm.printStackTrace();}
                }
            }
        }
    }



    private class ListenForItem implements ItemListener {

        //Show Password CheckBoxk
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                psw.setEchoChar((char) 0);
            } else {
                psw.setEchoChar('•');
            }
        }
    }



}

