package com.example.notificationservice.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    @Autowired
    SimpMessagingTemplate template;

    @MessageMapping("/application") // Client gửi dữ liệu đến "/app/application"
    @SendTo("/all/messages") //Gửi phản hồi đến "/all/messages" cho tất cả client
    public Message message(final Message message) throws Exception {
        return message;
    }

    @MessageMapping("/private") // Client gửi đến "app/private"
    public void sendSpecificUser(@Payload Message message) throws Exception {
        template.convertAndSend("/specific/message", message);
    }



}
