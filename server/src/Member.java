import java.util.Hashtable;
import java.util.ArrayList;

public class Member{
	public User user;
	public String pwd;
	public Hashtable<String, ArrayList<String>> historyMsg;
	public boolean login;

	public Member(String pwd){
		this.user = null;
		this.pwd = pwd;
		this.historyMsg = new Hashtable<String, ArrayList<String>>();
		this.login = true;
	}

	public void addUser(User user){
		this.user = user;
	}

	public boolean isOnline(){
		return user.channel.isConnected() && this.login;
	}

	public void addMsg(String id, String msg){
		ArrayList<String> msgList = this.historyMsg.get(id);
		if(msgList == null){
			msgList = new ArrayList<String>();
			msgList.add(msg);
			historyMsg.put(id, msgList);
		}else{
			msgList.add(msg);
		}
	}
}
