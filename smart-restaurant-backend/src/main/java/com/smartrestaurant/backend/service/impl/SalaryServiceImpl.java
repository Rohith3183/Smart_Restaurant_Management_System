package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.SalaryDto;
import com.smartrestaurant.backend.entity.Activity;
import com.smartrestaurant.backend.service.ActivityService;
import com.smartrestaurant.backend.entity.Employee;
import com.smartrestaurant.backend.entity.Investment;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.entity.SalaryPayment;
import com.smartrestaurant.backend.repository.EmployeeRepository;
import com.smartrestaurant.backend.repository.InvestmentRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.repository.SalaryPaymentRepository;
import com.smartrestaurant.backend.service.SalaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SalaryServiceImpl implements SalaryService {

    private final RestaurantRepository restaurantRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;
    private final InvestmentRepository investmentRepository;
    private final ActivityService activityService;

    public SalaryServiceImpl(RestaurantRepository restaurantRepository,
                            EmployeeRepository employeeRepository,
                            SalaryPaymentRepository salaryPaymentRepository,
                            InvestmentRepository investmentRepository,
                            ActivityService activityService) {
        this.restaurantRepository = restaurantRepository;
        this.employeeRepository = employeeRepository;
        this.salaryPaymentRepository = salaryPaymentRepository;
        this.investmentRepository = investmentRepository;
        this.activityService = activityService;
    }

    @Override
    public List<SalaryDto> getAllSalaries(String restaurantCode) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        return employeeRepository.findByRestaurant(restaurant).stream()
                .filter(Employee::isActive)
                .map(emp -> {
                    SalaryDto dto = new SalaryDto();
                    dto.setEmployeeId(emp.getId());
                    dto.setEmployeeName(emp.getName());
                    dto.setRole(emp.getRole().name());
                    dto.setSalary(BigDecimal.valueOf(emp.getSalary()));

                    Optional<SalaryPayment> payment = salaryPaymentRepository.findByEmployeeAndMonth(emp, currentMonth);
                    dto.setPaidThisMonth(payment.isPresent());
                    
                    if (payment.isPresent()) {
                        dto.setLastPaymentDate(payment.get().getPaymentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public void paySalary(String restaurantCode, Long employeeId) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (!employee.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Employee does not belong to this restaurant");
        }

        String currentMonth = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        // Check if already paid this month
        Optional<SalaryPayment> existingPayment =
                salaryPaymentRepository.findByEmployeeAndMonth(employee, currentMonth);
        if (existingPayment.isPresent()) {
            throw new IllegalArgumentException("Salary already paid for this month");
        }

        // Actual salary amount for this employee
        BigDecimal salaryAmount = BigDecimal.valueOf(employee.getSalary());

        // Create salary payment record
        SalaryPayment payment = SalaryPayment.builder()
                .employee(employee)
                .amount(salaryAmount)
                .paymentDate(LocalDateTime.now())
                .month(currentMonth)
                .paid(true)
                .build();

        salaryPaymentRepository.save(payment);

        // Log salary payment activity
        activityService.log(
            restaurantCode,
            Activity.Type.SALARY_PAID,
            String.format("Salary paid to '%s' – ₹%s for %s",
                          employee.getName(),
                          salaryAmount.toPlainString(),
                          currentMonth),
            "OWNER"
        );

        // Add a single-investment entry for this salary
        Investment investment = Investment.builder()
                .restaurant(restaurant)
                .amount(salaryAmount)
                .description("Salary paid to " + employee.getName() +
                             " (" + employee.getRole() + ") - " + currentMonth)
                .type(Investment.Type.SALARY)
                .createdAt(LocalDateTime.now())
                .build();

        investmentRepository.save(investment);

        // Log that this salary was also recorded as an investment
        activityService.log(
            restaurantCode,
            Activity.Type.INVESTMENT_CREATED,
            String.format("Salary payout added as investment: ₹%s for 1 employee",
                          salaryAmount.toPlainString()),
            "OWNER"
        );
    }
}
