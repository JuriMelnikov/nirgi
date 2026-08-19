package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.DublOrder;
import ee.jvm.nirgi_java.service.DublOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dubl-orders")
public class DublOrderController {

    @Autowired
    private DublOrderService dublOrderService;

    @GetMapping
    public ResponseEntity<List<DublOrder>> getAllDublOrders() {
        List<DublOrder> dublOrders = dublOrderService.getAllDublOrders();
        return ResponseEntity.ok(dublOrders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DublOrder> getDublOrderById(@PathVariable Long id) {
        return dublOrderService.getDublOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/target-week")
    public ResponseEntity<List<DublOrder>> getDublOrdersByTargetWeek(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam Integer week) {
        List<DublOrder> dublOrders = dublOrderService.getDublOrdersByTargetWeek(year, month, week);
        return ResponseEntity.ok(dublOrders);
    }

    @GetMapping("/original-week")
    public ResponseEntity<List<DublOrder>> getDublOrdersByOriginalWeek(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam Integer week) {
        List<DublOrder> dublOrders = dublOrderService.getDublOrdersByOriginalWeek(year, month, week);
        return ResponseEntity.ok(dublOrders);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<DublOrder>> getDublOrdersByOrderId(@PathVariable Long orderId) {
        List<DublOrder> dublOrders = dublOrderService.getDublOrdersByOrderId(orderId);
        return ResponseEntity.ok(dublOrders);
    }

    @PostMapping
    public ResponseEntity<?> createDublOrder(@RequestBody CreateDublOrderRequest request) {
        try {
            DublOrder dublOrder = dublOrderService.createDublOrder(
                    request.getOrderId(),
                    request.getCompletedWork(),
                    request.getTargetYear(),
                    request.getTargetMonth(),
                    request.getTargetWeek()
            );
            return ResponseEntity.ok(dublOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{dublOrderId}/models/{dublModelId}")
    public ResponseEntity<?> updateCompletedWork(
            @PathVariable Long dublOrderId,
            @PathVariable Long dublModelId,
            @RequestBody UpdateCompletedWorkRequest request) {
        try {
            DublOrder dublOrder = dublOrderService.updateCompletedWork(
                    dublOrderId,
                    dublModelId,
                    request.getCompletedInOriginalWeek()
            );
            return ResponseEntity.ok(dublOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDublOrder(@PathVariable Long id) {
        boolean deleted = dublOrderService.deleteDublOrder(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/completed-work-stats")
    public ResponseEntity<Map<Long, Integer>> getCompletedWorkForOriginalWeek(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam Integer week) {
        Map<Long, Integer> stats = dublOrderService.getCompletedWorkForOriginalWeek(year, month, week);
        return ResponseEntity.ok(stats);
    }

    // DTO classes for requests
    public static class CreateDublOrderRequest {
        private Long orderId;
        private Map<Long, Integer> completedWork;
        private Integer targetYear;
        private Integer targetMonth;
        private Integer targetWeek;

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public Map<Long, Integer> getCompletedWork() {
            return completedWork;
        }

        public void setCompletedWork(Map<Long, Integer> completedWork) {
            this.completedWork = completedWork;
        }

        public Integer getTargetYear() {
            return targetYear;
        }

        public void setTargetYear(Integer targetYear) {
            this.targetYear = targetYear;
        }

        public Integer getTargetMonth() {
            return targetMonth;
        }

        public void setTargetMonth(Integer targetMonth) {
            this.targetMonth = targetMonth;
        }

        public Integer getTargetWeek() {
            return targetWeek;
        }

        public void setTargetWeek(Integer targetWeek) {
            this.targetWeek = targetWeek;
        }
    }

    public static class UpdateCompletedWorkRequest {
        private Integer completedInOriginalWeek;

        public Integer getCompletedInOriginalWeek() {
            return completedInOriginalWeek;
        }

        public void setCompletedInOriginalWeek(Integer completedInOriginalWeek) {
            this.completedInOriginalWeek = completedInOriginalWeek;
        }
    }
}
