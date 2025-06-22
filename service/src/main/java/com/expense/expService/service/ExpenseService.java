package com.expense.expService.service;

import com.expense.expService.dto.ExpenseDto;
import com.expense.expService.entities.Expense;
import com.expense.expService.repository.ExpenseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ExpenseService
{

    private final ExpenseRepository expenseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ExpenseService(ExpenseRepository expenseRepository){
        this.expenseRepository = expenseRepository;
    }

    public boolean createExpense(ExpenseDto expenseDto){
        setCurrency(expenseDto);
        try{
            Expense expense = new Expense();
            expense.setAmount(expenseDto.getAmount());
            expense.setMerchant(expenseDto.getMerchant());
            expense.setCurrency(expenseDto.getCurrency());
            expense.setUserId(expenseDto.getUserId());
            expense.setCreatedAt(expenseDto.getCreatedAt() != null ? expenseDto.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
            expenseRepository.save(expense);
            return true;
        }catch(Exception ex){
            return false;
        }
    }

    public boolean updateExpense(ExpenseDto expenseDto){
        setCurrency(expenseDto);
        Optional<Expense> expenseFoundOpt = expenseRepository.findByUserIdAndExternalId(expenseDto.getUserId(), expenseDto.getExternalId());
        if(expenseFoundOpt.isEmpty()){
            return false;
        }
        Expense expense = expenseFoundOpt.get();
        expense.setAmount(expenseDto.getAmount());
        expense.setMerchant(Strings.isNotBlank(expenseDto.getMerchant())?expenseDto.getMerchant():expense.getMerchant());
        expense.setCurrency(Strings.isNotBlank(expenseDto.getCurrency())?expenseDto.getMerchant():expense.getCurrency());
        expenseRepository.save(expense);
        return true;
    }

    public List<ExpenseDto> getExpenses(String userId){
            List<Expense> expenses = expenseRepository.findByUserId(userId);
            return expenses.stream().map(exp -> ExpenseDto.builder()
                    .externalId(exp.getExternalId())
                    .amount(exp.getAmount())
                    .userId(exp.getUserId())
                    .merchant(exp.getMerchant())
                    .currency(exp.getCurrency())
                    .createdAt(exp.getCreatedAt())
                    .build()
            ).toList();
    }

    private void setCurrency(ExpenseDto expenseDto){
        if(Objects.isNull(expenseDto.getCurrency())){
            expenseDto.setCurrency("inr");
        }
    }
}
