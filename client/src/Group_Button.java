
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class Group_Button extends JButton{
	public boolean activated;
	public String from_user;
	public int to_group;

	public Group_Button(String str, boolean tmp, String user, int group){
		super(str);
		this.activated = tmp;
		this.from_user = user;
		this.to_group = group;
	}
	public void setActivated(boolean tmp){
		this.activated = tmp;
	}

	public void setUser(String user, int group){
		this.from_user = user;
		this.to_group = group;
	}
}
