package com.example.wechat.Models;

import java.util.HashMap;

public class Group {
    private String groupId;
    private String groupName;
    private String groupImage;
    private String createdBy;
    private HashMap<String, Object> members;
    private HashMap<String, Object> invitedMembers;

    public Group() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public HashMap<String, Object> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, Object> members) {
        this.members = members;
    }

    public HashMap<String, Object> getInvitedMembers() {
        return invitedMembers;
    }

    public void setInvitedMembers(HashMap<String, Object> invitedMembers) {
        this.invitedMembers = invitedMembers;
    }
}
