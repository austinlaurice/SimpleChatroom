import java.io.*;
import java.util.Scanner;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;

public class Postman extends Thread{
	public ArrayList<User> users;
	public Hashtable<String, Member> memberTable;

	public List<User> writeReqQueue;
	public List<Work> workQueue;
	private Selector selector;

	public Postman(Selector selector){
		this.users = new ArrayList<User>();
		this.memberTable = new Hashtable<String, Member>();
		this.writeReqQueue = Collections.synchronizedList(new LinkedList<User>());
		this.workQueue = Collections.synchronizedList(new LinkedList<Work>());
		this.selector = selector;
	}

	public void processWrite(User receiver, String msg){
		synchronized(receiver.writeBuf){
			receiver.writeBuf.add(Charset.forName("UTF-8").encode(CharBuffer.wrap(msg)));
		}
		synchronized(writeReqQueue){
			this.writeReqQueue.add(receiver);
		}
		this.selector.wakeup();
	}

	public void processRead(User sender, String msg){
		System.out.println(msg);
		//reg:[id]:[pwd]
		if(msg.startsWith("reg:")){
			String[] tok = msg.split(":");
			String id = tok[1];
			String pwd = tok[2];
			if(this.memberTable.containsKey(id)){
				this.serverMsgToUser(sender, "reg:no");
			}else{
				Member m = new Member(pwd);
				this.memberTable.put(id, m);
				this.serverMsgToUser(sender, "reg:yes");
				m.addUser(sender);
			}
		//login:[id]:[pwd]
		}else if(msg.startsWith("login:")){
			String[] tok = msg.split(":");
			String id = tok[1];
			String pwd = tok[2];
			Member m = this.memberTable.get(id);
			if(m != null && m.pwd.equals(pwd)){
				this.serverMsgToUser(sender, "login:yes");
				m.addUser(sender);
				m.login = true;
			}else
				this.serverMsgToUser(sender, "login:no");
		//knock:[id]
		}else if(msg.startsWith("knock:")){
			String[] tok = msg.split(":");
			String id = tok[1];
			Member m = this.memberTable.get(id);
			if(m != null && m.isOnline())
				this.serverMsgToUser(sender, "knock:" + id + ":yes");
			else if(m == null)
				this.serverMsgToUser(sender, "knock:" + id + ":x");
			else
				this.serverMsgToUser(sender, "knock:" + id + ":no");
		//pm:[to]:[from]:[msg content]
		}else if(msg.startsWith("pm:")){
			synchronized(sender.readBuf){
				sender.readBuf.add(msg);
			}
			synchronized(this.workQueue){
				this.workQueue.add(new Work(Work.WorkType.SEND, sender, msg));
				this.workQueue.notify();
			}
			this.addHistoryMsg(msg);
		//gm:[from]:[msg content]
		}else if(msg.startsWith("gm:")){
			synchronized(sender.readBuf){
				sender.readBuf.add(msg);
			}
			synchronized(this.workQueue){
				this.workQueue.add(new Work(Work.WorkType.SEND, sender, msg));
				this.workQueue.notify();
			}
		}else if(msg.startsWith("pf:")){
			synchronized(sender.readBuf){
				sender.readBuf.add(msg);
			}
			synchronized(this.workQueue){
				this.workQueue.add(new Work(Work.WorkType.SEND, sender, msg));
				this.workQueue.notify();
			}
			this.addHistoryMsg(msg);
		}else if(msg.startsWith("gf:")){
			synchronized(sender.readBuf){
				sender.readBuf.add(msg);
			}
			synchronized(this.workQueue){
				this.workQueue.add(new Work(Work.WorkType.SEND, sender, msg));
				this.workQueue.notify();
			}
		}else
			System.out.println("unrecognized message");
	}

	public void serverMsgToUser(User user, String msg){
		synchronized(this.workQueue){
			this.workQueue.add(new Work(Work.WorkType.RECEIVE, user, msg));
			this.workQueue.notify();
		}
	}

	public void processFile(String fileReceiver, String fileSender, String filename, boolean isPrivateFile){
		//tmp
		//int id = Integer.parseInt(fileReceiver);
		//Member sender_m = this.memberTable.get(fileSender);
		try{
			String msg;
			if(isPrivateFile){
				Member receiver_m = this.memberTable.get(fileReceiver);
				User receiver = null;
				if(receiver_m != null)
					receiver = receiver_m.user;
				//pf:[to]:[from]:[filename]:
				msg = "pf:" + fileReceiver + ":" + fileSender + ":" + filename + ":";
				//msg = "pf:" + fileReceiver + ":" + fileSender + ":" + filename;
				//this.processWrite(receiver, msg);
				/*
				synchronized(receiver.writeBuf){
					receiver.writeBuf.add(Charset.forName("UTF-8").encode(CharBuffer.wrap(msg)));
				}
				synchronized(writeReqQueue){
					this.writeReqQueue.add(receiver);
				}
				*/
				//this.selector.wakeup();
				/*
				synchronized(receiver.writeBuf){
					receiver.writeBuf.add(Charset.forName("UTF-8").encode(CharBuffer.wrap(msg)));
				}
				this.selector.wakeup();
				Thread.sleep(1000);
				*/

				//file
				RandomAccessFile file = new RandomAccessFile("file/" + filename, "r");
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
				this.processWrite(receiver, msg + fc);
			}else{
				//gf:g:[from]:[filename]:
				msg = "gf:g" + ":" + fileSender + ":" + filename + ":";
				//msg = "gf:" + fileReceiver + ":" + fileSender + ":" + filename;

				Enumeration<String> k = this.memberTable.keys();
				while(k.hasMoreElements()){
					String name = k.nextElement();
					if(!name.equals(fileSender)){
						Member m = this.memberTable.get(name);
						if(m != null && m.isOnline()){ 
							this.processWrite(m.user, msg);
							//file
							RandomAccessFile file = new RandomAccessFile("file/" + filename, "r");
							System.out.println(msg + " file/" + filename);
							FileChannel fileChannel = file.getChannel();
							ByteBuffer buf = ByteBuffer.allocate(500);
							int n;
							while((n = fileChannel.read(buf)) > 0){
								buf.flip();
								synchronized(m.user.writeBuf){
									m.user.writeBuf.add(buf);
								}
								synchronized(writeReqQueue){
									this.writeReqQueue.add(m.user);
								}
								buf = ByteBuffer.allocate(500);
							}
							this.selector.wakeup();
						}else
							return;
					}
				}


			}
		}catch(FileNotFoundException e){
		}catch(IOException e){
		//}catch(InterruptedException e){
		}
	}

	public void addHistoryMsg(String msg){
		//pm:[to]:[from]:[msg content]
		String[] tok = msg.split(":");
		String sender = tok[1];
		String receiver = tok[2];
		String content = tok[3];
		Member sender_m = this.memberTable.get(sender);
		Member receiver_m = this.memberTable.get(receiver);
		if(sender_m != null && receiver_m != null){
			sender_m.addMsg(receiver, content);
			receiver_m.addMsg(sender, content);
		}
	}

	public void run(){
		Work work;
		while(true){
			synchronized(this.workQueue){
				while(this.workQueue.isEmpty()){
					try{
						this.workQueue.wait();
					}catch(InterruptedException e){
					}
				}
				work = (Work)this.workQueue.remove(0);
			}
			if(work.workType == Work.WorkType.SEND){
				if(work.msg.startsWith("gm:") || work.msg.startsWith("gf:")){
					String[] tok = work.msg.split(":");
					String sender = tok[1];
					Enumeration<String> k = this.memberTable.keys();
					while(k.hasMoreElements()){
						String name = k.nextElement();
						if(!name.equals(sender)){
							Member m = this.memberTable.get(name);
							if(m != null && m.isOnline()) 
								this.processWrite(m.user, work.msg);
							else
								return;
						}
					}
				}else{
					//just for testing now
					String[] tok = work.msg.split(":");
					String recv = tok[1];
					Member receiver_m = this.memberTable.get(recv);
					User receiver = null;
					if(receiver_m != null)
						receiver = receiver_m.user;
					else
						return;
					this.processWrite(receiver, work.msg);
				}
			}else if(work.workType == Work.WorkType.RECEIVE){
				this.processWrite(work.user, work.msg);
			}
		}
	}
}
