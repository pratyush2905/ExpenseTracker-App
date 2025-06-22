package com.expense.expService.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpenseDto {

    private String externalId;
    @NonNull
    private BigDecimal amount;
    private String userId;
    private String merchant;
    private String currency;
    private Timestamp createdAt;

    public ExpenseDto(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            ExpenseDto expense = mapper.readValue(json, ExpenseDto.class);
            this.externalId = expense.externalId;
            this.amount = expense.amount;
            this.userId = expense.userId;
            this.merchant = expense.merchant;
            this.currency = expense.currency;
            this.createdAt = expense.createdAt;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to deserialize ExpenseDTO from json", e);
        }
    }
}
