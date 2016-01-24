import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

public class User{
	public int no;
	public SocketChannel channel;
	public List<String> readBuf;
	public List<ByteBuffer> writeBuf;

	public User(int no){
		this.no = no;
		this.readBuf = Collections.synchronizedList(new LinkedList<String>());
		this.writeBuf = Collections.synchronizedList(new LinkedList<ByteBuffer>());
	}

	public User(int no, SocketChannel channel){
		this.no = no;
		this.channel = channel;
		this.readBuf = Collections.synchronizedList(new LinkedList<String>());
		this.writeBuf = Collections.synchronizedList(new LinkedList<ByteBuffer>());
	}
}
