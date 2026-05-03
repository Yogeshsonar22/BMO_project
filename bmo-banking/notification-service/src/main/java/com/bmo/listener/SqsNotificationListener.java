package com.bmo.listener;

import com.bmo.config.NotificationMessage;
import com.bmo.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Component
public class SqsNotificationListener {

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private EmailService emailService;

    @Value("${aws.sqs.queue-name:bmo-notification-queue}")
    private String queueName;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    public void pollMessages() {
        try {
            String queueUrl = sqsClient.getQueueUrl(
                    GetQueueUrlRequest.builder().queueName(queueName).build()
            ).queueUrl();

            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(2)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

            for (Message message : messages) {
                processMessage(message, queueUrl);
            }
        } catch (Exception e) {
            System.err.println("SQS polling error: " + e.getMessage());
        }
    }

    private void processMessage(Message message, String queueUrl) {
        try {
            NotificationMessage notification = objectMapper.readValue(
                    message.body(), NotificationMessage.class);

            if ("EMAIL".equalsIgnoreCase(notification.getType())) {
                emailService.sendEmail(notification.getTo(),
                        notification.getSubject(), notification.getBody());
            } else if ("SMS".equalsIgnoreCase(notification.getType())) {
                // SMS integration placeholder (e.g., AWS SNS or Twilio)
                System.out.println("SMS to: " + notification.getTo() + " | " + notification.getBody());
            }

            // Delete message after processing
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());

        } catch (Exception e) {
            System.err.println("Failed to process SQS message: " + e.getMessage());
        }
    }
}
