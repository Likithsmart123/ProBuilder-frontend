package com.example.probuilder;

import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {
    private static ExpenseRepository instance;
    private List<Expense> expenses;

    private ExpenseRepository() {
        expenses = new ArrayList<>();
        // Mock Data
        expenses.add(new Expense("1", "Materials", 125000, "3/1/2024", "P1", "Residential Villa - Banjara Hills", "Cement and steel purchase", "INV-2024-001"));
        expenses.add(new Expense("2", "Labor", 45000, "2/1/2024", "P1", "Residential Villa - Banjara Hills", "Worker wages - Week 1", "PAY-2024-012"));
        expenses.add(new Expense("3", "Transportation", 8500, "30/12/2023", "P1", "Residential Villa - Banjara Hills", "Material delivery charges", "DEL-2023-089"));
    }

    public static synchronized ExpenseRepository getInstance() {
        if (instance == null) {
            instance = new ExpenseRepository();
        }
        return instance;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void addExpense(Expense expense) {
        expenses.add(0, expense); // Add to top
    }
}
