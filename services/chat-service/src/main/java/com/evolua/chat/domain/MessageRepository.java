package com.evolua.chat.domain; import java.util.List; public interface MessageRepository { Message save(Message item); List<Message> findAllByUserId(String userId); }
