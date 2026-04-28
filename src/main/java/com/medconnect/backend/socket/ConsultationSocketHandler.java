package com.medconnect.backend.socket;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import org.springframework.stereotype.Component;

@Component
public class ConsultationSocketHandler {

    private final SocketIOServer server;

    public ConsultationSocketHandler(SocketIOServer server) {
        this.server = server;
    }

    @OnConnect
    public void onConnect(SocketIOClient client) {
        System.out.println("Socket.IO client connected: " + client.getSessionId());
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        System.out.println("Socket.IO client disconnected: " + client.getSessionId());
    }

    @OnEvent("join-room")
    public void onJoinRoom(SocketIOClient client, String roomId) {
        System.out.println("User joined room: " + roomId);
        client.joinRoom(roomId);
        // Broadcast to others in the room
        server.getRoomOperations(roomId).sendEvent("participant-joined", client.getSessionId());
    }

    @OnEvent("join-personal-room")
    public void onJoinPersonalRoom(SocketIOClient client, String userId) {
        System.out.println("User joined personal room: user-" + userId);
        client.joinRoom("user-" + userId);
    }

    @OnEvent("leave-room")
    public void onLeaveRoom(SocketIOClient client, String roomId) {
        System.out.println("User left room: " + roomId);
        client.leaveRoom(roomId);
        server.getRoomOperations(roomId).sendEvent("participant-left", client.getSessionId());
    }

    @OnEvent("offer")
    public void onOffer(SocketIOClient client, Object payload) {
        // Forward the offer to the rest of the room
        for (String room : client.getAllRooms()) {
            if (!room.isEmpty()) {
                server.getRoomOperations(room).getClients().forEach(c -> {
                    if (!c.getSessionId().equals(client.getSessionId())) {
                        c.sendEvent("offer", payload);
                    }
                });
            }
        }
    }

    @OnEvent("answer")
    public void onAnswer(SocketIOClient client, Object payload) {
        for (String room : client.getAllRooms()) {
            if (!room.isEmpty()) {
                server.getRoomOperations(room).getClients().forEach(c -> {
                    if (!c.getSessionId().equals(client.getSessionId())) {
                        c.sendEvent("answer", payload);
                    }
                });
            }
        }
    }

    @OnEvent("ice-candidate")
    public void onIceCandidate(SocketIOClient client, Object payload) {
        for (String room : client.getAllRooms()) {
            if (!room.isEmpty()) {
                server.getRoomOperations(room).getClients().forEach(c -> {
                    if (!c.getSessionId().equals(client.getSessionId())) {
                        c.sendEvent("ice-candidate", payload);
                    }
                });
            }
        }
    }
}
