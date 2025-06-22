package com.expense.expService.consumer;

import com.expense.expService.dto.ExpenseDto;
import com.expense.expService.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ExpenseConsumer {
    private ExpenseService expenseService;

    @Autowired
    ExpenseConsumer(ExpenseService expenseService){
        this.expenseService=expenseService;
    }

    @KafkaListener(topics = "${spring.kafka.topic-json.name}",groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ExpenseDto eventData) {
        try {
            expenseService.createExpense(eventData);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception is thrown while consuming kafka event");
        }
    }
}
