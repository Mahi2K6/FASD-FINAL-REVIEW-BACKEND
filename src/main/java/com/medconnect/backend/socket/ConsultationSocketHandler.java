package com.medconnect.backend.socket;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ConsultationSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public ConsultationSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/offer/{roomId}")
    public void onOffer(@DestinationVariable String roomId, @Payload Object payload) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/offer", payload);
    }

    @MessageMapping("/answer/{roomId}")
    public void onAnswer(@DestinationVariable String roomId, @Payload Object payload) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/answer", payload);
    }

    @MessageMapping("/ice-candidate/{roomId}")
    public void onIceCandidate(@DestinationVariable String roomId, @Payload Object payload) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/ice-candidate", payload);
    }
}
