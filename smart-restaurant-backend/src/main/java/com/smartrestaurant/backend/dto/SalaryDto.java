package com.smartrestaurant.backend.dto;

import java.math.BigDecimal;

public class SalaryDto {
    private Long employeeId;
    private String employeeName;
    private String role;
    private BigDecimal salary;
    private boolean paidThisMonth;
    private String lastPaymentDate;

    // Getters and setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public boolean isPaidThisMonth() { return paidThisMonth; }
    public void setPaidThisMonth(boolean paidThisMonth) { this.paidThisMonth = paidThisMonth; }
    public String getLastPaymentDate() { return lastPaymentDate; }
    public void setLastPaymentDate(String lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }
}
