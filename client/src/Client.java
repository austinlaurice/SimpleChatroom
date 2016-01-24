import java.net.InetSocketAddress;
import java.io.*;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Scanner;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.nio.channels.FileChannel;

public class Client{
	//set ip address and port of the server here
	private static String server_addr = "127.0.0.1";
	private static int server_port = 8787;

	public int client_port;
	public InetSocketAddress server_isa;
	public SocketChannel channel;
	public Selector selector;
	public UI userInterface;

	public Client(int port){
		this.client_port = port;
		this.server_isa = new InetSocketAddress(this.server_addr, this.server_port);
		try{
			this.channel = SocketChannel.open();
			this.channel.configureBlocking(false);
		}catch(IOException e){
			System.out.printf("ERROR: channel\n");
		}
		try{
			this.selector = Selector.open();
			//this.userInterface = new UI(this.selector);
		}catch(IOException e){
			System.out.printf("ERROR: selector\n");
		}
	}

	private void connect(){
		try{
			this.channel.register(this.selector, SelectionKey.OP_CONNECT);
			this.channel.connect(this.server_isa);
		}catch(IOException e){
			System.out.println("ERROR: Connection fails.");
		}
	}

	private void finishConnect(SelectionKey key){
		try{
			this.channel.finishConnect();
		}catch(IOException e){
			System.out.println("ERROR");
		}
		key.interestOps(SelectionKey.OP_WRITE);
		System.out.println("Connected");
	}
	
	private void read(SelectionKey key){
		ByteBuffer recv = ByteBuffer.allocate(500);
		try{
			int n;
			String readString = new String();
			while((n = this.channel.read(recv)) > 0){
				byte[] bytes = recv.array();
				String str = new String(bytes, Charset.forName("UTF-8"));
				readString = readString + str.substring(0, n);
				recv.clear();
			}
			if(n == -1){
				System.out.println("Disconnect to server.");
				this.channel.close();
			}else
				this.userInterface.receive(readString);
		}catch(IOException e){
			System.out.println("ERROR");
		}
	}

	private void write(SelectionKey key){
		try{
			synchronized(this.userInterface.writeBuf){
				Iterator it = this.userInterface.writeBuf.iterator();
				while(it.hasNext()){
					ByteBuffer msg = (ByteBuffer)it.next();
					this.channel.write(msg);
					it.remove();
					//this.channel.write(Charset.forName("UTF-8").encode(CharBuffer.wrap(msg)));
				}
				this.channel.keyFor(this.selector).interestOps(SelectionKey.OP_READ);
				this.userInterface.write = false;
			}
		}catch(IOException e){
			System.out.println("ERROR: write channel error");
		}
	}

	public void listen(){
		Iterator it = null;
		while(!this.userInterface.end){
			if(this.userInterface.write)
				this.channel.keyFor(this.selector).interestOps(SelectionKey.OP_WRITE);

			try{
				this.selector.select();
				it = this.selector.selectedKeys().iterator();
			}catch(IOException e){
				System.out.println("ERROR: selector IO error");
			}
			while(it != null && it.hasNext()){
				SelectionKey key = (SelectionKey)it.next();
				it.remove();
				if(key.isReadable())
					this.read(key);
				else if(key.isWritable())
					this.write(key);
				else if(key.isConnectable())
					this.finishConnect(key);
			}
		}
	}

	public static void main(String[] args){
		if(args.length < 1)
			System.out.println("ERROR: Expact client port number argument.");
		Client client = new Client(Integer.parseInt(args[0]));
		client.userInterface  = new UI(client.selector);
		client.connect();
		client.listen();
	}
}
