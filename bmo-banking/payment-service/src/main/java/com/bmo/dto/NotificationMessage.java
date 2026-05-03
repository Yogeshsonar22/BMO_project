package com.bmo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String to;           // email or phone
    private String subject;
    private String body;
    private String type;         // EMAIL or SMS
}
