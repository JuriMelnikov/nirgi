package ee.jvm.nirgi_java.dto;

public record SalaryRecord(
    Long employeeId,
    String employeeName,
    String employeeSurname,
    Double totalTime,
    Double totalEarnings
) {}
