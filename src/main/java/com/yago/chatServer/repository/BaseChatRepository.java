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

    @Modifying
    @Query(value = "DELETE FROM message_read_by WHERE message_id IN " +
            "(SELECT id FROM message WHERE chat_id = :chatId)",
            nativeQuery = true)
    void deleteMessageReadBy(@Param("chatId") Long chatId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.chat.id = :chatId")
    void deleteMessagesByChatId(@Param("chatId") Long chatId);

    @Modifying
    @Query("DELETE FROM BaseChat c WHERE c.id = :chatId")
    void deleteChatById(@Param("chatId") Long chatId);

    @Transactional
    default void deleteChatCascade(Long chatId) {
        deleteMessageReadBy(chatId);
        deleteMessagesByChatId(chatId);
        deleteChatById(chatId);
    }
}
