package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.DublModel;
import ee.jvm.nirgi_java.classes.DublOrder;
import ee.jvm.nirgi_java.classes.Order;
import ee.jvm.nirgi_java.dto.CombinedOrderDTO;
import ee.jvm.nirgi_java.service.DublOrderService;
import ee.jvm.nirgi_java.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final DublOrderService dublOrderService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(order -> ResponseEntity.ok().body(order))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Order> getOrdersByFilters(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week) {
        if (year != null && month != null && week != null) {
            return orderService.getOrdersByYearMonthWeek(year, month, week);
        } else if (year != null && month != null) {
            return orderService.getOrdersByYearMonth(year, month);
        } else if (year != null) {
            return orderService.getOrdersByYear(year);
        }
        return orderService.getAllOrders();
    }

    @GetMapping("/search-with-transferred")
    public List<CombinedOrderDTO> getOrdersWithTransferred(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week) {
        System.out.println("Searching orders with transferred: year=" + year + ", month=" + month + ", week=" + week);
        List<CombinedOrderDTO> result = new ArrayList<>();

        if (year != null && month != null && week != null) {
            List<Order> regularOrders = orderService.getOrdersByYearMonthWeek(year, month, week);
            System.out.println("Regular orders found: " + regularOrders.size());
            for (Order order : regularOrders) {
                List<CombinedOrderDTO.ModelInfo> modelInfos = order.getModels().stream()
                    .map(m -> new CombinedOrderDTO.ModelInfo(m.getModelList(), m.getCount(), m.getCount()))
                    .toList();
                result.add(new CombinedOrderDTO(order.getId(), order.getName(), order.getYear(), 
                    order.getMonth(), order.getWeek(), modelInfos, false, null));
            }

            List<DublOrder> transferredOrders = dublOrderService.getDublOrdersByTargetWeek(year, month, week);
            System.out.println("Transferred orders found: " + transferredOrders.size());
            for (DublOrder dublOrder : transferredOrders) {
                System.out.println("Transferred order: id=" + dublOrder.getId() + 
                    ", targetYear=" + dublOrder.getTargetYear() + 
                    ", targetMonth=" + dublOrder.getTargetMonth() + 
                    ", targetWeek=" + dublOrder.getTargetWeek());
                Order originalOrder = dublOrder.getOrder();
                List<CombinedOrderDTO.ModelInfo> modelInfos = dublOrder.getDublModels().stream()
                    .map(dm -> new CombinedOrderDTO.ModelInfo(dm.getModelList(), dm.getTotalCount(), dm.getRemainingCount()))
                    .toList();
                result.add(new CombinedOrderDTO(dublOrder.getId(), originalOrder.getName() + " (перенос с недели: " + dublOrder.getOriginalWeek() + ")", 
                    dublOrder.getTargetYear(), dublOrder.getTargetMonth(), dublOrder.getTargetWeek(), 
                    modelInfos, true, originalOrder.getId()));
            }
        }
        System.out.println("Total orders returned: " + result.size());
        return result;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            Order createdOrder = orderService.createOrder(order);
            return ResponseEntity.ok(createdOrder);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order orderDetails) {
        Order updatedOrder = orderService.updateOrder(id, orderDetails);
        if (updatedOrder != null) {
            return ResponseEntity.ok(updatedOrder);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        if (orderService.deleteOrder(id)) {
            return ResponseEntity.ok().<Void>build();
        }
        return ResponseEntity.notFound().build();
    }
}
