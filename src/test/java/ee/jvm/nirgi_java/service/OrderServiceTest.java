package ee.jvm.nirgi_java.service;

import ee.jvm.nirgi_java.classes.DublOrder;
import ee.jvm.nirgi_java.classes.Model;
import ee.jvm.nirgi_java.classes.ModelList;
import ee.jvm.nirgi_java.classes.Order;
import ee.jvm.nirgi_java.repository.DublOrderRepository;
import ee.jvm.nirgi_java.repository.ModelListRepository;
import ee.jvm.nirgi_java.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ModelListRepository modelListRepository;

    @Mock
    private DublOrderRepository dublOrderRepository;

    @InjectMocks
    private OrderService orderService;

    private static Order orderWithModelReference(Long modelListId, Integer count) {
        Order order = new Order(null, "Order-1", 2026, 3, 11);
        Model model = new Model(null, new ModelList(modelListId, null), null, count);
        order.getModels().add(model);
        return order;
    }

    @Test
    void createOrderResolvesModelListsAndBackReferences() {
        Order order = orderWithModelReference(1L, 5);
        ModelList persisted = new ModelList(1L, "Shirts");
        when(dublOrderRepository.findByOrderNameAndOriginalWeek("Order-1", 2026, 3, 11)).thenReturn(Optional.empty());
        when(modelListRepository.findById(1L)).thenReturn(Optional.of(persisted));
        when(orderRepository.save(order)).thenReturn(order);

        Order created = orderService.createOrder(order);

        Model model = created.getModels().get(0);
        assertThat(model.getModelList()).isSameAs(persisted);
        assertThat(model.getOrder()).isSameAs(created);
    }

    @Test
    void createOrderClearsModelListWhenReferenceIsUnknown() {
        Order order = orderWithModelReference(99L, 5);
        when(dublOrderRepository.findByOrderNameAndOriginalWeek("Order-1", 2026, 3, 11)).thenReturn(Optional.empty());
        when(modelListRepository.findById(99L)).thenReturn(Optional.empty());
        when(orderRepository.save(order)).thenReturn(order);

        Order created = orderService.createOrder(order);

        assertThat(created.getModels().get(0).getModelList()).isNull();
    }

    @Test
    void createOrderRejectedWhenWeekAlreadyTransferred() {
        Order order = orderWithModelReference(1L, 5);
        when(dublOrderRepository.findByOrderNameAndOriginalWeek("Order-1", 2026, 3, 11))
                .thenReturn(Optional.of(new DublOrder()));

        assertThatThrownBy(() -> orderService.createOrder(order))
                .isInstanceOf(IllegalStateException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrderReplacesModelsAndScalarFields() {
        Order existing = new Order(1L, "Old", 2025, 1, 2);
        existing.addModel(new Model(10L, new ModelList(1L, "Shirts"), existing, 3));

        Order details = orderWithModelReference(2L, 7);
        details.setName("New");
        details.setYear(2026);
        details.setMonth(4);
        details.setWeek(15);
        ModelList trousers = new ModelList(2L, "Trousers");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(modelListRepository.findById(2L)).thenReturn(Optional.of(trousers));
        when(orderRepository.save(existing)).thenReturn(existing);

        Order updated = orderService.updateOrder(1L, details);

        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getYear()).isEqualTo(2026);
        assertThat(updated.getMonth()).isEqualTo(4);
        assertThat(updated.getWeek()).isEqualTo(15);
        assertThat(updated.getModels()).hasSize(1);
        assertThat(updated.getModels().get(0).getModelList()).isSameAs(trousers);
        assertThat(updated.getModels().get(0).getOrder()).isSameAs(existing);
    }

    @Test
    void updateOrderReturnsNullForUnknownId() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThat(orderService.updateOrder(404L, new Order())).isNull();
    }

    @Test
    void deleteOrderReportsRepositoryOutcome() {
        assertThat(orderService.deleteOrder(1L)).isTrue();

        doThrow(new RuntimeException("constraint violation")).when(orderRepository).deleteById(2L);
        assertThat(orderService.deleteOrder(2L)).isFalse();
    }

    @Test
    void queryMethodsDelegateToRepository() {
        Order order = new Order(1L, "Order-1", 2026, 3, 11);
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.findByYearAndMonthAndWeek(2026, 3, 11)).thenReturn(List.of(order));
        when(orderRepository.findByYear(2026)).thenReturn(List.of(order));
        when(orderRepository.findByYearAndMonth(2026, 3)).thenReturn(List.of(order));

        assertThat(orderService.getAllOrders()).containsExactly(order);
        assertThat(orderService.getOrderById(1L)).contains(order);
        assertThat(orderService.getOrdersByYearMonthWeek(2026, 3, 11)).containsExactly(order);
        assertThat(orderService.getOrdersByYear(2026)).containsExactly(order);
        assertThat(orderService.getOrdersByYearMonth(2026, 3)).containsExactly(order);
    }
}
