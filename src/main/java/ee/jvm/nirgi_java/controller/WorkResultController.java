package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.WorkResult;
import ee.jvm.nirgi_java.service.WorkResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-results")
public class WorkResultController {

    @Autowired
    private WorkResultService workResultService;

    @GetMapping
    public ResponseEntity<List<WorkResult>> getAllWorkResults() {
        List<WorkResult> workResults = workResultService.getAllWorkResults();
        return ResponseEntity.ok(workResults);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkResult> getWorkResultById(@PathVariable Long id) {
        return workResultService.getWorkResultById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<WorkResult>> getWorkResultsByEmployeeAndDate(
            @PathVariable Long employeeId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(required = false) Integer week) {
        List<WorkResult> workResults;
        if (week != null) {
            workResults = workResultService.getWorkResultsByEmployeeAndDate(employeeId, year, month, week);
        } else {
            workResults = workResultService.getWorkResultsByEmployeeAndMonth(employeeId, year, month);
        }
        return ResponseEntity.ok(workResults);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<WorkResult>> getWorkResultsByOrderAndDate(
            @PathVariable Long orderId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(required = false) Integer week) {
        List<WorkResult> workResults;
        if (week != null) {
            workResults = workResultService.getWorkResultsByOrderAndDate(orderId, year, month, week);
        } else {
            workResults = workResultService.getWorkResultsByOrderAndMonth(orderId, year, month);
        }
        return ResponseEntity.ok(workResults);
    }

    @GetMapping("/employee/{employeeId}/order/{orderId}")
    public ResponseEntity<List<WorkResult>> getWorkResultsByEmployeeAndOrder(
            @PathVariable Long employeeId,
            @PathVariable Long orderId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week) {
        List<WorkResult> workResults;
        if (year != null && month != null && week != null) {
            workResults = workResultService.getWorkResultsByEmployeeOrderAndDate(employeeId, orderId, year, month, week);
        } else {
            workResults = workResultService.getWorkResultsByEmployeeAndOrder(employeeId, orderId);
        }
        return ResponseEntity.ok(workResults);
    }

    @GetMapping("/completed-quantity")
    public ResponseEntity<Integer> getCompletedQuantity(
            @RequestParam Long employeeId,
            @RequestParam Long orderId,
            @RequestParam Long modelListId,
            @RequestParam Long sectionListId,
            @RequestParam Long techmapId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam Integer week) {
        Integer quantity = workResultService.getCompletedQuantity(
                employeeId, orderId, modelListId, sectionListId, techmapId, year, month, week);
        return ResponseEntity.ok(quantity);
    }

    @GetMapping("/total-completed-quantity")
    public ResponseEntity<Integer> getTotalCompletedQuantity(
            @RequestParam Long employeeId,
            @RequestParam Long orderId,
            @RequestParam Long modelListId,
            @RequestParam Long sectionListId,
            @RequestParam Long techmapId) {
        Integer quantity = workResultService.getTotalCompletedQuantity(
                employeeId, orderId, modelListId, sectionListId, techmapId);
        return ResponseEntity.ok(quantity);
    }

    @GetMapping("/transferred-quantity")
    public ResponseEntity<Integer> getTransferredQuantity(
            @RequestParam Long orderId,
            @RequestParam Long modelListId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam Integer week) {
        Integer quantity = workResultService.getTransferredQuantityForCurrentWeek(
                orderId, modelListId, year, month, week);
        return ResponseEntity.ok(quantity);
    }

    @GetMapping("/completed-work-by-order-week")
    public ResponseEntity<Map<Long, Integer>> getCompletedWorkByOrderAndWeek(
            @RequestParam Long orderId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam Integer week) {
        Map<Long, Integer> completedWork = workResultService.getCompletedWorkByOrderAndWeek(orderId, year, month, week);
        return ResponseEntity.ok(completedWork);
    }

    @PostMapping
    public ResponseEntity<?> createWorkResult(@RequestBody CreateWorkResultRequest request) {
        try {
            WorkResult workResult = workResultService.createWorkResult(
                    request.getEmployeeId(),
                    request.getOrderId(),
                    request.getModelListId(),
                    request.getSectionListId(),
                    request.getTechmapId(),
                    request.getQuantity(),
                    request.getYear(),
                    request.getMonth(),
                    request.getWeek()
            );
            return ResponseEntity.ok(workResult);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkResult(
            @PathVariable Long id,
            @RequestBody UpdateWorkResultRequest request) {
        try {
            WorkResult workResult = workResultService.updateWorkResult(id, request.getQuantity());
            return ResponseEntity.ok(workResult);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkResult(@PathVariable Long id) {
        boolean deleted = workResultService.deleteWorkResult(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DTO classes for requests
    public static class CreateWorkResultRequest {
        private Long employeeId;
        private Long orderId;
        private Long modelListId;
        private Long sectionListId;
        private Long techmapId;
        private Integer quantity;
        private Integer year;
        private Integer month;
        private Integer week;

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public Long getModelListId() {
            return modelListId;
        }

        public void setModelListId(Long modelListId) {
            this.modelListId = modelListId;
        }

        public Long getSectionListId() {
            return sectionListId;
        }

        public void setSectionListId(Long sectionListId) {
            this.sectionListId = sectionListId;
        }

        public Long getTechmapId() {
            return techmapId;
        }

        public void setTechmapId(Long techmapId) {
            this.techmapId = techmapId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public Integer getMonth() {
            return month;
        }

        public void setMonth(Integer month) {
            this.month = month;
        }

        public Integer getWeek() {
            return week;
        }

        public void setWeek(Integer week) {
            this.week = week;
        }
    }

    public static class UpdateWorkResultRequest {
        private Integer quantity;

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
