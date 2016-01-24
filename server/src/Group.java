import java.util.ArrayList;

public class Group{
	ArrayList<Member> groupMember;

	public Group(Member maker, Member added){
		this.groupMember = new ArrayList<Member>();
		this.groupMember.add(maker);
		this.groupMember.add(added);
	}
	
	public void addGroupMember(Member m){
		this.groupMember.add(m);
	}
}
