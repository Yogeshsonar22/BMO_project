package com.bmo.service;

import com.bmo.dto.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class SqsPublisherService {

    @Autowired
    private SqsClient sqsClient;

    @Value("${aws.sqs.queue-name:bmo-notification-queue}")
    private String queueName;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void publishNotification(NotificationMessage message) {
        try {
            String queueUrl = sqsClient.getQueueUrl(
                    GetQueueUrlRequest.builder().queueName(queueName).build()
            ).queueUrl();

            String messageBody = objectMapper.writeValueAsString(message);

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build());

            System.out.println("Notification published to SQS: " + message.getSubject());
        } catch (Exception e) {
            // Log but don't fail the payment if notification publishing fails
            System.err.println("Failed to publish notification: " + e.getMessage());
        }
    }
}
