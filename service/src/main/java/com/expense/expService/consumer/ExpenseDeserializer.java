package com.expense.expService.consumer;

import com.expense.expService.dto.ExpenseDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ExpenseDeserializer implements Deserializer<ExpenseDto> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public ExpenseDto deserialize(String topic, byte[] data) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ExpenseDto expense = null;
        try {
            expense = objectMapper.readValue(data, ExpenseDto.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return expense;
    }

    @Override
    public void close() {
        Deserializer.super.close();
    }
}
