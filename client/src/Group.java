import messanger.Message;

class Group{
    public List<Message> Msg_list;
    private int id;
    private int num_people;
    public List<String> User_list;

    public Group(String user1, String user2, int num){
        this.id = num;
        this.num_people = 2;
        Msg_list = new ArrayList<Message>;
        User_list = new ArrayList<String>;
        User_list.add(user1);
        User_list.add(user2);
    }
}
