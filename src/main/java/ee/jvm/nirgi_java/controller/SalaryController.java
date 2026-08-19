package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.dto.SalaryRecord;
import ee.jvm.nirgi_java.repository.WorkResultRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salary")
@CrossOrigin(origins = "*")
public class SalaryController {

    private final WorkResultRepository workResultRepository;

    public SalaryController(WorkResultRepository workResultRepository) {
        this.workResultRepository = workResultRepository;
    }

    @GetMapping
    public ResponseEntity<List<SalaryRecord>> getSalaryRecords(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        List<SalaryRecord> records = workResultRepository.findSalaryRecordsByMonthAndYear(year, month);
        return ResponseEntity.ok(records);
    }
}
