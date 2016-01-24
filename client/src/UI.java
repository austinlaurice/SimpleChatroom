import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Collections;
import java.nio.channels.Selector;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public class UI extends JFrame{
	public List<String> readBuf;
	public List<ByteBuffer> writeBuf;
	public boolean write;
	public Selector selector;
	public boolean end;

    private JLabel err_msg;
    private JLabel reg_msg;
    private String current_user;
    private Hashtable<String, ArrayList<Message>> PM_list;
    private ArrayList<Message> current_PM;
    private int notice_height;
    private int pri_height;
    private int pub_height;

    private String[] name;
    private JPanel panel_left;
    private JPanel user_panel;
    private JTextField id_search_input;

    private JPanel panel_middle;
    private JScrollPane pri_msg_scrollpanel;
    private JPanel pri_msg_panel;
    private JTextArea pri_msg_input;
    private Msg_Button pri_msg_btn;
    private Msg_Button pri_file_btn;
    private JLabel title_pri;

    private JPanel panel_right;
    private JTextField invite_user_id;
    private JScrollPane pub_msg_scrollpanel;
    private JPanel pub_msg_panel;
    private JTextArea pub_msg_input;
    private Group_Button pub_msg_btn;
    private Group_Button pub_file_btn;
    private JLabel title_group;

    private JTextField id_reg;
    private JTextField id_log;
    private JPasswordField password_reg;
    private JPasswordField password_log;

    public UI(Selector selector){
		this.write = false;
		this.readBuf = Collections.synchronizedList(new LinkedList<String>());
		this.writeBuf = Collections.synchronizedList(new LinkedList<ByteBuffer>());
		this.selector = selector;
		this.end = false;


        //Initialize the private elements
        current_user = new String("");
        panel_left = new JPanel();
        user_panel = new JPanel();
        id_search_input = new JTextField();
        current_PM = new ArrayList<Message>();

        panel_middle = new JPanel();
        pri_msg_scrollpanel = new JScrollPane();
        pri_msg_panel = new JPanel();
        pri_msg_input = new JTextArea();
        title_pri = new JLabel();

        panel_right = new JPanel();
        invite_user_id = new JTextField();
        pub_msg_scrollpanel = new JScrollPane();
        pub_msg_panel = new JPanel();
        pub_msg_input = new JTextArea();
        title_group = new JLabel();

        id_reg = new JTextField();
        id_log = new JTextField();
        password_reg = new JPasswordField();
        password_log = new JPasswordField();
        reg_msg = new JLabel("");
        err_msg = new JLabel("");
        PM_list = new Hashtable<String, ArrayList<Message>>();

        //Initialze login and register page
        JPanel panel = (JPanel) this.getContentPane();
        String n[] = {
            "Registeration",
            "ID",
            "Password",
            "Confirm Password",
            "Apply",
            "Login"
        };

        this.setSize(600, 400);
        this.setLayout(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel head = new JLabel("Welcome to Team13's messanger~");
        head.setBounds(180, 10, 300, 20);
        panel.add(head);

        JLabel title = new JLabel(n[0]);
        JLabel for_id = new JLabel(n[1]);
        JLabel for_password = new JLabel(n[2]);
        JButton apply = new JButton(n[4]);
        apply.addActionListener(getRegisterListener());

        title.setBounds(220, 60, 200, 40);
        for_id.setBounds(30, 110, 100, 20);
        id_reg.setBounds(130, 110, 300, 20);
        for_password.setBounds(30, 135, 100, 20);
        password_reg.setBounds(130, 135, 300, 20);
        apply.setBounds(400, 160, 100, 30);

        panel.add(title);
        panel.add(for_id);
        panel.add(for_password);
        panel.add(id_reg);
        panel.add(password_reg);
        panel.add(apply);

        JLabel title_2 = new JLabel(n[5]);
        JLabel for_id_2 = new JLabel(n[1]);
        JLabel for_password_2 = new JLabel(n[2]);
        JButton login = new JButton(n[5]);
        login.addActionListener(getLoginListener());

        title_2.setBounds(220, 210, 200, 40);
        for_id_2.setBounds(30, 250, 100, 20);
        id_log.setBounds(130, 250, 300, 20);
        for_password_2.setBounds(30, 275, 100, 20);
        password_log.setBounds(130, 275, 300, 20);
        login.setBounds(400, 300, 100, 30);

        panel.add(title_2);
        panel.add(for_id_2);
        panel.add(for_password_2);
        panel.add(id_log);
        panel.add(password_log);
        panel.add(login);

        panel.revalidate();
        panel.repaint();
        this.setVisible(true);
    }

	public void receive(String msg){
		System.out.println("receive: " + msg);
		if(msg.startsWith("reg:")){
            String[] token = msg.split(":");
            if(token[1].equals("yes")){
                registerResult(true);
            }
            else if(token[1].equals("no")){
                registerResult(false);
            }
		}
        else if(msg.startsWith("login:")){
            String[] token = msg.split(":");
            if(token[1].equals("yes")){
                loginResult(true);
            }
            else if(token[1].equals("no")){
                loginResult(false);
            }
        }
		else if(msg.startsWith("knock:")){
            String[] token = msg.split(":");
            if(token[2].equals("yes")){
                knockResult(1, token[1], current_user);
            }
            else if(token[2].equals("no")){
                knockResult(0, token[1], current_user);
            }
            else if(token[2].equals("x")){
                knockResult(2, token[1], current_user);
            }
		}
        else if(msg.startsWith("pm:")){
            String[] token = msg.split(":");
            showPrivateMessage(token[2], token[3]);
            Message msg_tmp = new Message(token[2], token[3]);
            renderNotice(token[2] + " send a message to you.");
            current_PM = this.PM_list.get(token[2]);
            if(current_PM == null){
                current_PM = new ArrayList<Message>();
                this.PM_list.put(token[2], current_PM);
                current_PM = PM_list.get(token[2]);
            }
            current_PM.add(msg_tmp);
            System.out.print("Size of PM in ShowPRI: " + String.valueOf(current_PM.size()));
		}
        else if(msg.startsWith("gm:")){
            String[] token = msg.split(":");
		    if(!token[1].equals(current_user)){
                showGroupMessage(token[1], token[2]);
            }
        }
        else if(msg.startsWith("pf:")){
			//parsing
			String[] tok = msg.split(":");
			String filename = tok[3];
			String filecontent = tok[4];

			try{
				String path = "file/" + filename;
				System.out.println(path);
				RandomAccessFile file = new RandomAccessFile(path, "rw");
				FileChannel fileChannel = file.getChannel();
				fileChannel.write(Charset.forName("UTF-8").encode(CharBuffer.wrap(filecontent)));
				fileChannel.close();
                Message msg_tmp = new Message(tok[2], "receive a file at " + path);
                renderNotice(tok[2] + " send a file to you.");
                current_PM = this.PM_list.get(tok[2]);
                if(current_PM == null){
                    current_PM = new ArrayList<Message>();
                    this.PM_list.put(tok[2], current_PM);
                    current_PM = PM_list.get(tok[2]);
                }
                current_PM.add(msg_tmp);
                showPrivateMessage(tok[2], "receive a file at "+ path);
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
		}else if(msg.startsWith("gf:")){
			//parsing
			String[] tok = msg.split(":");
			String filename = tok[3];
			String filecontent = tok[4];

			try{
				String path = "file/" + filename;
				System.out.println(path);
				RandomAccessFile file = new RandomAccessFile(path, "rw");
				FileChannel fileChannel = file.getChannel();
				fileChannel.write(Charset.forName("UTF-8").encode(CharBuffer.wrap(filecontent)));
				fileChannel.close();
		        if(!tok[1].equals(current_user)){
                    showGroupMessage(tok[2], "receive a file at " + path);
			    }
            }catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
		}else
			System.out.println("ERROR: receive a unrecognized message");
	}

	/*
	 * pf:[to]:[from]:[filename]  (send this message to the server first, and then send the file)
	 * gf:[to]:[from]:[filename]  (send this message to the server first, and then send the file)
	 */
	public void sendFile(String msg){
		//parsing
		String[] tok = msg.split(":");
		String filename = tok[3];

		try{
			RandomAccessFile file = new RandomAccessFile(filename, "r");
			FileChannel fileChannel = file.getChannel();
			ByteBuffer buf = ByteBuffer.allocate(500);
			String fc = "";
			int n;
			while((n = fileChannel.read(buf)) > 0){
				buf.flip();
				byte[] bytes = buf.array();
				String str = new String(bytes, Charset.forName("UTF-8"));
				fc = fc + str.substring(0, n);
				buf.clear();
			}
			synchronized(this.writeBuf){
				this.writeBuf.add(Charset.forName("UTF-8").encode(CharBuffer.wrap(msg + ":" + fc)));
				this.write = true;
			}
			this.selector.wakeup();
		}catch(FileNotFoundException e){
		}catch(IOException e){
		}
	}

	public void send(String msg){
		System.out.println("send: " + msg);
		if(msg.startsWith("pf:", 0) || msg.startsWith("gf:", 0)){
			this.sendFile(msg);
		}else{
			synchronized(this.writeBuf){
				this.writeBuf.add(Charset.forName("UTF-8").encode(CharBuffer.wrap(msg)));
				//this.writeBuf.add(msg);
				this.write = true;
			}
			this.selector.wakeup();
		}
	}

    public ActionListener getRegisterListener(){
        return new ActionListener(){
            public void actionPerformed(ActionEvent e){
                JPanel panel = (JPanel) (UI.this).getContentPane();

                String user_id = (UI.this).id_reg.getText();
                String user_pwd = new String((UI.this).password_reg.getPassword());
                user_id = user_id.replaceAll("\\s+", "");
                user_pwd = user_pwd.replaceAll("\\s+", "");

                System.out.print(user_id);
                System.out.print(user_pwd);

                if(user_id.equals("") || user_pwd.equals("") ){
                    reg_msg.setText("Need to fill in both ID and password.");
                    reg_msg.setBounds(180, 40, 300, 20);
                    reg_msg.setForeground(Color.red);
                    panel.add(reg_msg);
                    panel.revalidate();
                    panel.repaint();
                }
                else{
                    String tmp = "reg:" + user_id + ":" + user_pwd;
                    /* TODO: Send the msg */
					(UI.this).send(tmp);
                }
            }
        };
    }

    public void registerResult(boolean success){
        JPanel panel = (JPanel) this.getContentPane();
        if(success){
            this.id_reg.setText("");
            this.password_reg.setText("");
            reg_msg.setText("Successfully created the account");
            reg_msg.setBounds(180, 40, 300, 20);
            reg_msg.setForeground(Color.blue);
            panel.add(reg_msg);
            panel.revalidate();
            panel.repaint();
        }
        else{
            reg_msg.setText("There's something wrong.");
            reg_msg.setBounds(180, 40, 300, 20);
            reg_msg.setForeground(Color.red);
            panel.add(reg_msg);
            panel.revalidate();
            panel.repaint();
        }
    }

    public ActionListener getLoginListener(){
        return new ActionListener(){
            public void actionPerformed(ActionEvent e){
                JPanel panel = (JPanel) (UI.this).getContentPane();
                String user_id = (UI.this).id_log.getText();
                String user_pwd = new String((UI.this).password_log.getPassword());
                user_id = user_id.replaceAll("\\s+", "");
                user_pwd = user_pwd.replaceAll("\\s+", "");

                if(user_id.equals("") || user_pwd.equals("") ){
                    err_msg.setText("Need to fill in both ID and password.");
                    err_msg.setBounds(180, 190, 300, 20);
                    err_msg.setForeground(Color.red);
                    panel.add(err_msg);
                    panel.revalidate();
                    panel.repaint();
                }
                else{
                    String tmp = "login:" + user_id + ":" + user_pwd;
                    /* TODO: Send the login message */
					current_user = user_id;
                    (UI.this).send(tmp);
                }
            }
        };
    }

    public void loginResult(boolean success){
        JPanel panel = (JPanel) this.getContentPane();
        if(success){
            this.user_page();
        }
        else{
            current_user = "";
            err_msg.setText("Wrong password or user ID");
            err_msg.setBounds(180, 190, 300, 20);
            err_msg.setForeground(Color.red);
            panel.add(err_msg);
            panel.revalidate();
            panel.repaint();
        }
    }

    public void user_page(){
        this.setSize(1200, 750);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        JPanel panel = (JPanel) this.getContentPane();
        panel.removeAll();
        panel.setLayout(new GridLayout(1, 3));


        /* panel_left */
        panel_left = new JPanel();
        panel_left.setLayout(null);


        /* Search bar in panel_left */
        id_search_input = new JTextField("Search for other users");
        id_search_input.setBounds(10, 10, 250, 30);
        panel_left.add(id_search_input);

        JButton search_btn = new JButton("Search");
        search_btn.addActionListener(getSearchListener());
        search_btn.setBounds(280, 10, 100, 30);
        panel_left.add(search_btn);

        /* online user list panel */

        /* get the user list that are online */
        //String user_id[] = {"Laurice", "Yu-Wen", "PJ", "123", "12222"};
        user_panel = new JPanel(null);
        user_panel.setPreferredSize(new Dimension(320, 620));
        notice_height = 10;
        //renderUserList(user_id);

        JScrollPane user_scrollpanel = new JScrollPane(user_panel);
        user_scrollpanel.setBounds(10, 50, 350, 620);

        panel_left.add(user_scrollpanel);
        panel_left.setBackground(Color.white);

        panel.add(panel_left);

        /* panel_middle -- private chatroom */
        panel_middle = new JPanel();
        panel_middle.setLayout(null);
        panel_middle.setBackground(Color.blue);
        JLabel title_mid = new JLabel("Private message slot");
        title_mid.setBounds(140, 10, 200, 30);
        title_mid.setForeground(Color.white);
        panel_middle.add(title_mid);

        pri_msg_panel = new JPanel();
        pri_msg_panel.setLayout(null);
        title_pri = new JLabel("Start a chat with someone");
        title_pri.setBounds(100, 10, 200, 20);
        pri_height = 30;
        pri_msg_panel.add(title_pri);

        pri_msg_scrollpanel = new JScrollPane(pri_msg_panel);
        pri_msg_scrollpanel.setBounds(10, 40, 370, 450);
        panel_middle.add(pri_msg_scrollpanel);

        pri_msg_input = new JTextArea();
        pri_msg_input.setBounds(10, 491, 370, 120);
        pri_msg_input.setLineWrap(true);
        pri_msg_input.setWrapStyleWord(true);
        panel_middle.add(pri_msg_input);
        pri_msg_btn = new Msg_Button("Send", false, "", "");
        pri_msg_btn.addActionListener(getSendListener(pri_msg_btn.activated, pri_msg_btn.from_user, pri_msg_btn.to_user));
        pri_msg_btn.setBounds(10, 620, 185, 30);
        panel_middle.add(pri_msg_btn);

        pri_file_btn = new Msg_Button("Send File", false, "", "");
        pri_file_btn.addActionListener(getFileSendListener(pri_file_btn.activated, pri_file_btn.from_user, pri_file_btn.to_user));
        pri_file_btn.setBounds(196, 620, 185, 30);
        panel_middle.add(pri_file_btn);

        panel.add(panel_middle);

        /* panel_right -- group chatroom */
        panel_right = new JPanel();
        panel_right.setLayout(null);
        panel_right.setBackground(Color.white);
        JLabel title_right = new JLabel("Group message slot");
        title_right.setBounds(120, 10, 190, 30);
        panel_right.add(title_right);

        /*
        JButton close_group_btn = new JButton("Close recent group");
        close_group_btn.addActionListener(getCloseListener("", -1));
        close_group_btn.setBackground(Color.orange);
        close_group_btn.setBounds(210, 10, 170, 30);
        panel_right.add(close_group_btn);
        */

        /*
        invite_user_id = new JTextField();
        invite_user_id.setBounds(10, 50, 200, 20);
        panel_right.add(invite_user_id);
        JButton invite = new JButton("Invite");
        invite.addActionListener(getInviteListener(-1));
        invite.setBounds(220, 50, 160, 20);
        panel_right.add(invite);
        */

        pub_msg_panel = new JPanel();
        pub_msg_panel.setLayout(null);
        title_group = new JLabel("Chat with all the people online");
        title_group.setBounds(90, 10, 300, 20);
        pub_height = 30;
        pub_msg_panel.add(title_group);

        pub_msg_scrollpanel = new JScrollPane(pub_msg_panel);
        pub_msg_scrollpanel.setBounds(10, 40, 370, 450);
        panel_right.add(pub_msg_scrollpanel);

        pub_msg_input = new JTextArea();
        pub_msg_input.setBounds(10, 500, 370, 150);
        pub_msg_input.setLineWrap(true);
        pub_msg_input.setWrapStyleWord(true);
        pub_msg_input.setBorder(BorderFactory.createLineBorder(Color.black));
        panel_right.add(pub_msg_input);

        pub_msg_btn = new Group_Button("Send", true, current_user, -1);
        pub_msg_btn.addActionListener(getGroupSendListener(pub_msg_btn.activated, pub_msg_btn.from_user, pub_msg_btn.to_group));
        pub_msg_btn.setBounds(10, 650, 185, 30);
        panel_right.add(pub_msg_btn);

        pub_file_btn = new Group_Button("Send File", true, current_user, -1);
        pub_file_btn.addActionListener(getGroupFileSendListener(pub_file_btn.activated, pub_file_btn.from_user, pub_file_btn.to_group));
        pub_file_btn.setBounds(196, 650, 185, 30);
        panel_right.add(pub_file_btn);

        panel.add(panel_right);

        panel.revalidate();
        panel.repaint();
    }

    public void renderNotice(String msg){
        JLabel tmp = new JLabel(msg);
        tmp.setBounds(10, this.notice_height, 320, 25);
        this.user_panel.add(tmp);
        notice_height = notice_height + 25;
        this.user_panel.setPreferredSize(new Dimension(360, Math.max(620, notice_height)));
        this.user_panel.revalidate();
        this.user_panel.repaint();
    }

    /*
    public void renderUserList(String user_id[]){
        for(int i=0; i<user_id.length; i++){
            JButton knock_user = new JButton(user_id[i]);
            knock_user.addActionListener(getKnockListener(user_id[i]));
            knock_user.setBounds(10, 10+25*i, 320, 25);
            this.user_panel.add(knock_user);
        }
    }
    */

    public ActionListener getSearchListener(){
        return new ActionListener(){
            public void actionPerformed(ActionEvent e){
                String user = id_search_input.getText();
                user = user.replaceAll("\\s+", "");
                if(!user.equals("")){
                    String tmp = "knock:" + user;
                    (UI.this).id_search_input.setText("");
                    /* TODO: send message */
					(UI.this).send(tmp);
                }
            }
        };
    }

    public ActionListener getKnockListener(final String user){
        return new ActionListener(){
            public void actionPerformed(ActionEvent e){
                String tmp = "knock:" + user;
                /* TODO: send message */
				(UI.this).send(tmp);
            }
        };
    }

    // knock or search all use this
    public void knockResult(int online, String to_user, String from_user){
        /*
         0: offline
         1: online
         2: user doesn't exist
        */
        JPanel panel = (JPanel) this.getContentPane();
        if(online == 0){
            this.pri_height = 30;
            this.pri_msg_btn.setActivated(true);
            this.pri_msg_btn.setUser(from_user, to_user);
            for(ActionListener al: this.pri_msg_btn.getActionListeners()) {
                this.pri_msg_btn.removeActionListener(al);
            }
            //this.pri_msg_btn.removeActionListener(this.pri_msg_btn.getActionListener);
            this.pri_msg_btn.addActionListener(getSendListener(pri_msg_btn.activated, pri_msg_btn.from_user, pri_msg_btn.to_user));
            this.pri_msg_panel.removeAll();
            this.pri_file_btn.setActivated(true);
            this.pri_file_btn.setUser(from_user, to_user);
            for(ActionListener al: this.pri_file_btn.getActionListeners()) {
                this.pri_file_btn.removeActionListener(al);
            }
            this.pri_file_btn.addActionListener(getFileSendListener(pri_file_btn.activated, pri_file_btn.from_user, pri_file_btn.to_user));
            String title = to_user + " is not online now";
            System.out.print(title);
            this.title_pri.setText(title);
            this.pri_msg_panel.add(title_pri);
            panel.revalidate();
            panel.repaint();
        }
        else if(online == 1){
            this.pri_height = 30;
            this.pri_msg_btn.setActivated(true);
            this.pri_msg_btn.setUser(from_user, to_user);
            for(ActionListener al: this.pri_msg_btn.getActionListeners()) {
                this.pri_msg_btn.removeActionListener(al);
            }
            //this.pri_msg_btn.removeActionListener(this.pri_msg_btn.getActionListener);
            this.pri_msg_btn.addActionListener(getSendListener(pri_msg_btn.activated, pri_msg_btn.from_user, pri_msg_btn.to_user));
            this.pri_msg_panel.removeAll();
            this.pri_file_btn.setActivated(true);
            this.pri_file_btn.setUser(from_user, to_user);
            for(ActionListener al: this.pri_file_btn.getActionListeners()) {
                this.pri_file_btn.removeActionListener(al);
            }
            this.pri_file_btn.addActionListener(getFileSendListener(pri_file_btn.activated, pri_file_btn.from_user, pri_file_btn.to_user));
            String title = "You are chatting with " + to_user;
            this.title_pri.setText(title);
            this.pri_msg_panel.add(title_pri);
            panel.revalidate();
            panel.repaint();
            ArrayList<Message> current_PM = PM_list.get(to_user);
            //System.out.print(current_PM);
            if(current_PM != null){
                System.out.print("\n");
                System.out.print(current_PM.size());
                Iterator it = current_PM.iterator();
                //try{
                    while(it.hasNext()){
                        Message m = (Message)it.next();
                        String tmp = m.msg;
                        String user_tmp = m.sender;
                        showPrivateMessage(user_tmp, tmp);
                    }
                //}
                //catch(ConcurrentModificationException e){
                //}
                /*
                int i=0;
                for(i=0;i<current_PM.size();i++){
                    System.out.print(i);
                    String tmp = current_PM.get(i).msg;
                    String user_tmp = current_PM.get(i).sender;
                    showPrivateMessage(user_tmp, tmp);
                }
                */
            }
            else{
                current_PM = new ArrayList<Message>();
                PM_list.put(to_user, current_PM);
            }
        }
        else if(online == 2){
            JFrame err = new JFrame();
            err_msg.setText("User doesn't exist");
            err.add(err_msg);
            err.setSize(600, 120);
            err.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            err.setVisible(true);
        }
    }

    public ActionListener getSendListener(final boolean activated, final String from_user, final String to_user){
        return new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(activated){
                    String msg = (UI.this).pri_msg_input.getText();
                    if(!msg.equals("")){
                        String tmp = "pm:" + to_user + ":" + from_user + ":" + msg;
                        (UI.this).pri_msg_input.setText("");
                        /* TODO: send message */
					    (UI.this).send(tmp);
                        (UI.this).showPrivateMessage(from_user, msg);
                        current_PM = (UI.this).PM_list.get(to_user);
                        if(current_PM == null){
                            current_PM = new ArrayList<Message>();
                            (UI.this).PM_list.put(to_user, current_PM);
                            current_PM = PM_list.get(to_user);
                        }
                        Message msg_tmp = new Message(from_user, msg);
                        current_PM.add(msg_tmp);
                        System.out.print("size of current_PM in send: " + String.valueOf(current_PM.size()));
                    }
                }
            }
        };
    }

    public ActionListener getFileSendListener(final boolean activated, final String from_user, final String to_user){
        return new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(activated){
                    String msg = (UI.this).pri_msg_input.getText();
                    String tmp = "pf:" + to_user + ":" + from_user + ":" + msg;
                    if(!msg.equals("")){
                        (UI.this).pri_msg_input.setText("");
                        /* TODO: send message */
					    (UI.this).send(tmp);
                        (UI.this).showPrivateMessage(from_user, "I sent a file");
                        current_PM = (UI.this).PM_list.get(to_user);
                        if(current_PM == null){
                            current_PM = new ArrayList<Message>();
                            (UI.this).PM_list.put(to_user, current_PM);
                            current_PM = PM_list.get(to_user);
                        }
                        Message msg_tmp = new Message(from_user, msg);
                        current_PM.add(msg_tmp);
                        System.out.print("size of current_PM in send: " + String.valueOf(current_PM.size()));
                    }
                }

            }
        };
    }

    public void showPrivateMessage(String from_user, String msg){
        //System.out.print(from_user);
        //System.out.print("\n" + msg);
        JPanel panel = (JPanel) this.getContentPane();
        JLabel msg_label = new JLabel();
        msg_label.setText("<html><p style=\"width: 500px\">" + msg + "</p></html>");
        msg_label.setOpaque(true);
        System.out.print("\n");

        if(from_user.equals(current_user)){
            msg_label.setBounds(120, pri_height, 250, 20);
            msg_label.setBackground(Color.white);
        }
        else{
            if(!(this.pri_msg_btn.activated)){
                this.pri_msg_btn.setActivated(true);
                this.pri_msg_btn.setUser(current_user, from_user);
                for(ActionListener al: this.pri_msg_btn.getActionListeners()) {
                    this.pri_msg_btn.removeActionListener(al);
                }
                //this.pri_msg_btn.removeActionListener(this.pri_msg_btn.getActionListener);
                this.pri_msg_btn.addActionListener(getSendListener(pri_msg_btn.activated, pri_msg_btn.from_user, pri_msg_btn.to_user));
                this.pri_msg_panel.removeAll();
                for(ActionListener al: this.pri_file_btn.getActionListeners()) {
                    this.pri_file_btn.removeActionListener(al);
                }
                this.pri_file_btn.setActivated(true);
                this.pri_file_btn.setUser(current_user, from_user);
                this.pri_file_btn.addActionListener(getFileSendListener(pri_file_btn.activated, pri_file_btn.from_user, pri_file_btn.to_user));
                String title = "You are chatting with " + from_user;
                this.title_pri.setText(title);
                this.pri_msg_panel.add(title_pri);
            }
            msg_label.setBounds(0, pri_height, 250, 20);
            msg_label.setBackground(Color.yellow);
        }
        if(from_user.equals(current_user) || from_user.equals(pri_msg_btn.to_user)){
            pri_height = pri_height + 25;
            pri_msg_panel.setPreferredSize(new Dimension(360, Math.max(450, pri_height)));
            pri_msg_panel.add(msg_label);
            pri_msg_panel.revalidate();
            pri_msg_panel.repaint();
            panel.revalidate();
            panel.repaint();
        }
    }

    public ActionListener getInviteListener(final int group){
        return new ActionListener(){
            public void actionPerformed(ActionEvent e){
                String user = (UI.this).invite_user_id.getText();
                /* TODO: Add the user to the group */
				//(UI.this).send(tmp);
            }
        };
    }

    public ActionListener getCloseListener(final String user, final int group){
        return new ActionListener(){
            public void actionPerformed(ActionEvent e){
                String tmp = "close:" + user + ":" + String.valueOf(group);
                /* TODO: send the mesaage */
				(UI.this).send(tmp);
            }
        };
    }

    public void closeResult(int group){
        JPanel panel = (JPanel) this.getContentPane();
        if(group == -1){
            this.pub_msg_panel.removeAll();
            this.title_group.setText("Invite people to join the chat");
            this.pub_msg_panel.add(title_group);
            panel.revalidate();
            panel.repaint();
        }
        else{
            /* TODO: switch to the group */
            this.pub_msg_panel.removeAll();
            /* render the message in the group */
        }
    }

    public ActionListener getGroupSendListener(final boolean activated, final String from_user, final int to_group){
        return new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(activated){
                        String msg = (UI.this).pub_msg_input.getText();
                    if(!msg.equals("")){
                        String tmp = "gm:" + from_user + ":" + msg;
                        (UI.this).pub_msg_input.setText("");
                        /* TODO: send message */
					    (UI.this).send(tmp);
                        showGroupMessage(from_user, msg);
                    }
                }
            }
        };
    }

    public ActionListener getGroupFileSendListener(final boolean activated, final String from_user, final int to_group){
        return new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(activated){
                    String msg = (UI.this).pub_msg_input.getText();
                    if(!msg.equals("")){
                        String tmp = "gf:g:" + from_user + ":" + msg;
                        (UI.this).pub_msg_input.setText("");
                        /* TODO: send message */
					    (UI.this).send(tmp);
                        showGroupMessage(from_user, "send a file: "+ msg);
                    }
                }
            }
        };
    }

    public void showGroupMessage(String from_user, String msg){
        JPanel panel = (JPanel) this.getContentPane();
        JLabel msg_label = new JLabel();
        JLabel sender_label = new JLabel();
        msg_label.setText("<html><p style=\"width: 500px\">" + msg + "</p></html>");
        msg_label.setOpaque(true);
        if(from_user.equals(current_user)){
            msg_label.setBounds(120, pub_height, 250, 20);
            msg_label.setBackground(Color.white);
        }
        else{
            sender_label.setText(from_user + ":");
            sender_label.setBounds(0, pub_height, 250, 20);
            pub_msg_panel.add(sender_label);

            pub_height = pub_height + 20;
            msg_label.setBounds(0, pub_height, 250, 20);
            msg_label.setBackground(Color.yellow);
        }
        pub_height = pub_height + 25;
        pub_msg_panel.setPreferredSize(new Dimension(360, Math.max(450, pub_height)));
        pub_msg_panel.add(msg_label);
        pub_msg_panel.revalidate();
        pub_msg_panel.repaint();
        panel.revalidate();
        panel.repaint();
    }
}

