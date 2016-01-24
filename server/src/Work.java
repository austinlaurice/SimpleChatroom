public class Work{
	static enum WorkType{SEND, RECEIVE};

	public WorkType workType;
	public User user;
	public String msg;

	public Work(WorkType workType, User user, String msg){
		this.workType = workType;
		this.user = user;
		this.msg = msg;
	}
}
