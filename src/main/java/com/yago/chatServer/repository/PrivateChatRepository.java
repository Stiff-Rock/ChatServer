package com.yago.chatServer.repository;

import com.yago.chatServer.model.PrivateChat;
import com.yago.chatServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrivateChatRepository extends JpaRepository<PrivateChat, Long> {
    PrivateChat findByUniqueHash(String uniqueHash);

    @Query("SELECT DISTINCT u " +
            "FROM PrivateChat pc " +
            "JOIN pc.participants u " +
            "WHERE :user MEMBER OF pc.participants " +
            "AND pc.class = PrivateChat " +
            "AND u <> :user")
    List<User> findUserContacts(@Param("user") User user);
}
