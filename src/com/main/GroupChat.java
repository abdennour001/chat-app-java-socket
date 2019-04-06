package com.main;

public class GroupChat {

    private String groupName;
    private int membersCount=0;
    private ChatServerThread[] members=new ChatServerThread[50];

    public GroupChat() {

    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(int membersCount) {
        this.membersCount = membersCount;
    }

    public ChatServerThread[] getMembers() {
        return members;
    }

    public void addMember(ChatServerThread member) {
        this.members[membersCount++] = member;
    }
}
