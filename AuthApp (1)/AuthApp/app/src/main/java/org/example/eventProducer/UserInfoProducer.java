package org.example.eventProducer;



import org.example.models.UserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class UserInfoProducer
{
    private final KafkaTemplate<String, UserInfoEvent> kafkaTemplate;

    @Value("${spring.kafka.topic-json.name}")
    private String TOPIC_NAME;

    @Autowired
    UserInfoProducer(KafkaTemplate<String,UserInfoEvent>kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(UserInfoEvent eventData){
        Message<UserInfoEvent> message = MessageBuilder.withPayload(eventData).setHeader(KafkaHeaders.TOPIC,TOPIC_NAME).build();
        kafkaTemplate.send(message);
    }
}
