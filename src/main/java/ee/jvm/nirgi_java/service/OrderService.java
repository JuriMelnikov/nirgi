package ee.jvm.nirgi_java.service;

import ee.jvm.nirgi_java.classes.Model;
import ee.jvm.nirgi_java.classes.ModelList;
import ee.jvm.nirgi_java.classes.Order;
import ee.jvm.nirgi_java.repository.DublOrderRepository;
import ee.jvm.nirgi_java.repository.ModelListRepository;
import ee.jvm.nirgi_java.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ModelListRepository modelListRepository;
    private final DublOrderRepository dublOrderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByYearMonthWeek(Integer year, Integer month, Integer week) {
        return orderRepository.findByYearAndMonthAndWeek(year, month, week);
    }

    public List<Order> getOrdersByYear(Integer year) {
        return orderRepository.findByYear(year);
    }

    public List<Order> getOrdersByYearMonth(Integer year, Integer month) {
        return orderRepository.findByYearAndMonth(year, month);
    }

    public Order createOrder(Order order) {
        // Проверяем, не существует ли уже DublOrder для этого заказа с теми же параметрами
        var existingDublOrder = dublOrderRepository.findByOrderNameAndOriginalWeek(
                order.getName(),
                order.getYear(),
                order.getMonth(),
                order.getWeek()
        );
        
        if (existingDublOrder.isPresent()) {
            throw new IllegalStateException("Нельзя создать заказ в оригинальной неделе, так как он уже был перенесен на другую неделю");
        }

        // Set the order reference and fetch ModelList for each model
        if (order.getModels() != null) {
            for (Model model : order.getModels()) {
                // Fetch the ModelList by ID if only ID is provided
                if (model.getModelList() != null && model.getModelList().getId() != null) {
                    ModelList modelList = modelListRepository.findById(model.getModelList().getId())
                            .orElse(null);
                    model.setModelList(modelList);
                }
                model.setOrder(order);
            }
        }
        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order orderDetails) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setName(orderDetails.getName());
                    order.setYear(orderDetails.getYear());
                    order.setMonth(orderDetails.getMonth());
                    order.setWeek(orderDetails.getWeek());
                    
                    // Clear existing models
                    order.getModels().clear();
                    
                    // Add new models
                    if (orderDetails.getModels() != null) {
                        for (Model model : orderDetails.getModels()) {
                            // Fetch the ModelList by ID if only ID is provided
                            if (model.getModelList() != null && model.getModelList().getId() != null) {
                                ModelList modelList = modelListRepository.findById(model.getModelList().getId())
                                        .orElse(null);
                                model.setModelList(modelList);
                            }
                            model.setOrder(order);
                            order.getModels().add(model);
                        }
                    }
                    
                    return orderRepository.save(order);
                })
                .orElse(null);
    }

    public boolean deleteOrder(Long id) {
        try {
            orderRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
