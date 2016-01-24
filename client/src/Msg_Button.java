
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class Msg_Button extends JButton{
	public boolean activated;
	public String from_user;
	public String to_user;
	public Msg_Button(String str, boolean tmp, String user1, String user2){
		super(str);
		this.activated = tmp;
		this.from_user = user1;
		this.to_user = user2;
	}
	public void setActivated(boolean tmp){
		this.activated = tmp;
	}

	public void setUser(String user1, String user2){
		this.from_user = user1;
		this.to_user = user2;
	}
}
