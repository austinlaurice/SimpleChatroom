import java.util.Scanner;
import java.util.List;
import java.util.LinkedList;
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

public class UI extends Thread{
	public List<String> readBuf;
	public List<ByteBuffer> writeBuf;
	//public List<String> writeBuf;
	public boolean write;
	public Selector selector;
	public boolean end;

	public UI(Selector selector){
		this.write = false;
		this.readBuf = Collections.synchronizedList(new LinkedList<String>());
		//this.writeBuf = Collections.synchronizedList(new LinkedList<String>());
		this.writeBuf = Collections.synchronizedList(new LinkedList<ByteBuffer>());
		this.selector = selector;
		this.end = false;
	}

	public void receive(String msg){
		System.out.println("receive: " + msg);
		if(msg.startsWith("reg:")){
		}else if(msg.startsWith("login:")){
		}else if(msg.startsWith("knock:")){
		}else if(msg.startsWith("pm:")){
		}else if(msg.startsWith("gm:")){
		}else if(msg.startsWith("pf:")){
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
			synchronized(this.writeBuf){
				this.writeBuf.add(Charset.forName("UTF-8").encode(CharBuffer.wrap(msg)));
				this.write = true;
			}
			this.selector.wakeup();

			RandomAccessFile file = new RandomAccessFile(filename, "r");
			FileChannel fileChannel = file.getChannel();
			ByteBuffer buf = ByteBuffer.allocate(500);
			while(fileChannel.read(buf) != 0){
				buf.flip();
				synchronized(this.writeBuf){
					this.writeBuf.add(buf);
					this.write = true;
				}
				buf = ByteBuffer.allocate(500);
				this.selector.wakeup();
			}
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

	public void run(){
		Scanner scanner = new Scanner(System.in);
		while(true){
			String msg = scanner.nextLine();
			if(msg.equals("exit"))
				break;
			this.send(msg);
		}
		this.end = true;
		this.selector.wakeup();
		System.out.println("goodbye...");
	}
}
