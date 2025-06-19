package com.project.demo001.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ChatMessage {

	public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
    private String sender;
    private String messageContent;
    @Enumerated(EnumType.STRING)
    private MessageType type;
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
    
    @Transient  // DB에 저장하지 않도록
    private String time;

    @JsonProperty("time")
    public String getTime() {
        return this.timestamp != null
            ? this.timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))
            : "";
    }
    
}
