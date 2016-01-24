import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.net.InetSocketAddress;
import java.io.*;
import java.util.Scanner;
import java.util.Iterator;
import java.nio.charset.Charset;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.io.File;

public class Server{
	private int listen_port;
	private ServerSocketChannel channel;
	private Selector selector;
	private Postman postman;

	private String fileReceiver;
	private String fileSender;
	private String filename;
	private boolean nextFile;
	private boolean isPrivateFile;

	public Server(int port){
		this.listen_port = port;
		try{
			this.channel = ServerSocketChannel.open();
			this.channel.configureBlocking(false);
			this.channel.socket().bind(new InetSocketAddress(this.listen_port));
			this.selector = Selector.open();
			this.postman = new Postman(this.selector);
			this.channel.register(this.selector, SelectionKey.OP_ACCEPT);
		}catch(IOException e){
			System.out.printf("ERROR: Could not listen to port %d.\n", this.listen_port);
		}
		this.nextFile = false;
	}

	private void accept(){
		try{
			SocketChannel clientChannel = this.channel.accept();
			System.out.println("Connect to client " + clientChannel.socket().getRemoteSocketAddress());
			clientChannel.configureBlocking(false);
			User newUser = this.addNewUser(clientChannel);
			SelectionKey key = clientChannel.register(this.selector, SelectionKey.OP_READ);
			key.attach(newUser);
			this.postman.serverMsgToUser(newUser, "Your user number is " + newUser.no + ".");
		}catch(IOException e){
			System.out.println("ERROR: cannot accept the connection");
		}
	}

	private void read(SelectionKey key){
		SocketChannel clientChannel = (SocketChannel) key.channel();
		User user = (User)key.attachment();
		ByteBuffer recv = ByteBuffer.allocate(500);
		//transfer file
		if(this.nextFile){
			try{
				String path = "file/" + this.filename;
				System.out.println(path);
				RandomAccessFile file = new RandomAccessFile(path, "rw");
				FileChannel fileChannel = file.getChannel();
				ByteBuffer recv_file = ByteBuffer.allocate(500);
				int n;
				while((n = clientChannel.read(recv)) > 0){
					recv.flip();
					fileChannel.write(recv);
					recv.clear();
				}
				fileChannel.close();
				this.postman.processFile(this.fileReceiver, this.fileSender, this.filename, this.isPrivateFile);
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
			this.nextFile = false;
		}else{
			try{
				int n;
				String readString = new String();
				while((n = clientChannel.read(recv)) > 0){
					byte[] bytes = recv.array();
					String str = new String(bytes, Charset.forName("UTF-8"));
					readString = readString + str.substring(0, n);
					recv.clear();
				}
				if(n == -1){
					System.out.println("Disconnect to client " + clientChannel.socket().getRemoteSocketAddress());
					clientChannel.close();
				/*
				//transfer file msg
				//pf:[to]:[from]:[filename]
				}else if(readString.startsWith("pf:")){
					//parsing
					String[] tok = readString.split(":");
					this.fileSender = tok[2];
					this.fileReceiver = tok[1];
					this.filename = tok[3];
					this.nextFile = true;
					this.isPrivateFile = true;
				}else if(readString.startsWith("gf:")){
					//parsing
					String[] tok = readString.split(":");
					this.fileSender = tok[2];
					this.fileReceiver = tok[1];
					this.filename = tok[3];
					this.nextFile = true;
					this.isPrivateFile = false;
				//msg
				//[2 digits]:[msg]
				*/
				}else{
					this.postman.processRead(user, readString);
				}
			}catch(IOException e){
				System.out.println("ERROR");
			}
		}
	}

	private void write(SelectionKey key){
		User user = (User)key.attachment();
		try{
			synchronized(user.writeBuf){
				Iterator it = user.writeBuf.iterator();
				while(it.hasNext()){
					ByteBuffer msg = (ByteBuffer)it.next();
					it.remove();
					user.channel.write(msg);
				}
			}
			key.interestOps(SelectionKey.OP_READ);
		}catch(IOException e){
			System.out.println("ERROR: write channel error");
		}
	}

	public void listen(){
		System.out.printf("Server is listening to port %d...\n", this.listen_port);
		Iterator it = null;
		while(true){
			synchronized(this.postman.writeReqQueue){
				Iterator writeReq_it = this.postman.writeReqQueue.iterator();
				while(writeReq_it.hasNext()){
					User req = (User)writeReq_it.next();
					writeReq_it.remove();
					req.channel.keyFor(this.selector).interestOps(SelectionKey.OP_WRITE);
				}
			}

			try{
				this.selector.select();
				it = this.selector.selectedKeys().iterator();
			}catch(IOException e){
				System.out.println("ERROR: selector IO error");
			}
			while(it != null && it.hasNext()){
				SelectionKey key = (SelectionKey) it.next();
				it.remove();
				if(key.isAcceptable())
					this.accept();
				else if(key.isReadable())
					this.read(key);
				else if(key.isWritable())
					this.write(key);
			}
		}
	}

	/////////////////////////////////////////////////////////////
	
	private User addNewUser(SocketChannel channel){
		User newUser = new User(this.postman.users.size(), channel);
		this.postman.users.add(newUser);
		return newUser;
	}

	/////////////////////////////////////////////////////////////

	public static void main(String args[]){
		if(args.length < 1)
			System.out.println("ERROR: Expact server listening port number argument.");
		Server server = new Server(Integer.parseInt(args[0]));
		server.postman.start();
		server.listen();
	}
}
