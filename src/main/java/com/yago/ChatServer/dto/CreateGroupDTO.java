package com.yago.ChatServer.dto;

import com.yago.ChatServer.model.User;

import java.util.List;
import java.util.Set;

public class CreateGroupDTO {
    private String groupName;
    private Set<User> participants;

    public CreateGroupDTO(String groupName, Set<User> participants) {
        this.groupName = groupName;
        this.participants = participants;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }
}
