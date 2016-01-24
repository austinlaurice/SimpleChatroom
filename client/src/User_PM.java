import messanger.Message;

public User_PM{
    public List<Message> PM_list;
    private String with_user;

    public User_PM(String user){
        this.PM_list = new ArrayList<Message>;
        this.with_user = user;
    }
}
