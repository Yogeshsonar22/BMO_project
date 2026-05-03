package com.bmo.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String to;
    private String subject;
    private String body;
    private String type; // EMAIL or SMS
}
