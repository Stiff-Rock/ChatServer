package com.yago.chatServer.repository;

import com.yago.chatServer.model.BaseChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BaseChatRepository extends JpaRepository<BaseChat, Long> {
    List<BaseChat> findByParticipants_Id(Long userId);

//    @Modifying
//    @Query("DELETE FROM BaseChat c WHERE c.id = :chatId")
//    @Transactional
//    void deleteWithCascade(@Param("chatId") Long chatId);
}
