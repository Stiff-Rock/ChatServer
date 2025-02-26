package com.yago.chatServer.dto;

import com.yago.chatServer.model.Message;
import com.yago.chatServer.model.MessageState;
import com.yago.chatServer.model.User;

public class MessageUpdateDto {
    private Long readerUser;
    private Long msgId;
    private MessageState state;

    public MessageUpdateDto() {
    }

    public MessageUpdateDto(User readerUser, Message msgId, MessageState state) {
        this.readerUser = readerUser.getId();
        this.msgId = msgId.getId();
        this.state = state;
    }

    public Long getReaderUser() {
        return readerUser;
    }

    public void setReaderUser(Long readerUser) {
        this.readerUser = readerUser;
    }

    public Long getMsgId() {
        return msgId;
    }

    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }

    public MessageState getState() {
        return state;
    }

    public void setState(MessageState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "MessageUpdateDto{" +
                "readerUser=" + readerUser +
                ", msgId=" + msgId +
                ", state=" + state +
                '}';
    }
}