package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.DublModel;
import ee.jvm.nirgi_java.classes.DublOrder;
import ee.jvm.nirgi_java.classes.Model;
import ee.jvm.nirgi_java.classes.ModelList;
import ee.jvm.nirgi_java.classes.Order;
import ee.jvm.nirgi_java.dto.CombinedOrderDTO;
import ee.jvm.nirgi_java.service.DublOrderService;
import ee.jvm.nirgi_java.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private DublOrderService dublOrderService;

    @InjectMocks
    private OrderController orderController;

    private static ModelList modelList(Long id, String name) {
        ModelList modelList = new ModelList();
        modelList.setId(id);
        modelList.setName(name);
        return modelList;
    }

    private static Order order(Long id, String name, int week, ModelList modelList, int count) {
        Order order = new Order();
        order.setId(id);
        order.setName(name);
        order.setYear(2025);
        order.setMonth(8);
        order.setWeek(week);
        Model model = new Model();
        model.setId(id == null ? null : id * 10);
        model.setModelList(modelList);
        model.setCount(count);
        model.setOrder(order);
        order.setModels(List.of(model));
        return order;
    }

    @Test
    void getAllOrdersDelegatesToService() {
        Order order = order(1L, "Order 1", 33, modelList(5L, "Model A"), 10);
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        assertThat(orderController.getAllOrders()).containsExactly(order);
    }

    @Test
    void getOrderByIdReturnsOkOrNotFound() {
        Order order = order(1L, "Order 1", 33, modelList(5L, "Model A"), 10);
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(order));
        when(orderService.getOrderById(404L)).thenReturn(Optional.empty());

        assertThat(orderController.getOrderById(1L).getBody()).isSameAs(order);
        assertThat(orderController.getOrderById(404L).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void searchPicksTheMostSpecificQueryAvailable() {
        Order order = order(1L, "Order 1", 33, modelList(5L, "Model A"), 10);
        when(orderService.getOrdersByYearMonthWeek(2025, 8, 33)).thenReturn(List.of(order));
        when(orderService.getOrdersByYearMonth(2025, 8)).thenReturn(List.of(order));
        when(orderService.getOrdersByYear(2025)).thenReturn(List.of(order));
        when(orderService.getAllOrders()).thenReturn(List.of());

        assertThat(orderController.getOrdersByFilters(2025, 8, 33)).containsExactly(order);
        assertThat(orderController.getOrdersByFilters(2025, 8, null)).containsExactly(order);
        assertThat(orderController.getOrdersByFilters(2025, null, 33)).containsExactly(order);
        assertThat(orderController.getOrdersByFilters(null, 8, 33)).isEmpty();
    }

    @Test
    void searchWithTransferredCombinesRegularAndTransferredOrders() {
        ModelList modelListA = modelList(5L, "Model A");
        Order regular = order(1L, "Order 1", 34, modelListA, 10);
        Order original = order(2L, "Order 2", 33, modelListA, 20);

        DublOrder dublOrder = new DublOrder();
        dublOrder.setId(7L);
        dublOrder.setOrder(original);
        dublOrder.setOriginalYear(2025);
        dublOrder.setOriginalMonth(8);
        dublOrder.setOriginalWeek(33);
        dublOrder.setTargetYear(2025);
        dublOrder.setTargetMonth(8);
        dublOrder.setTargetWeek(34);
        DublModel dublModel = new DublModel();
        dublModel.setId(70L);
        dublModel.setDublOrder(dublOrder);
        dublModel.setModelList(modelListA);
        dublModel.setTotalCount(20);
        dublModel.setCompletedInOriginalWeek(12);
        dublModel.setRemainingCount(8);
        dublOrder.setDublModels(List.of(dublModel));

        when(orderService.getOrdersByYearMonthWeek(2025, 8, 34)).thenReturn(List.of(regular));
        when(dublOrderService.getDublOrdersByTargetWeek(2025, 8, 34)).thenReturn(List.of(dublOrder));

        List<CombinedOrderDTO> result = orderController.getOrdersWithTransferred(2025, 8, 34);

        assertThat(result).hasSize(2);
        CombinedOrderDTO regularDto = result.get(0);
        assertThat(regularDto.id()).isEqualTo(1L);
        assertThat(regularDto.isTransferred()).isFalse();
        assertThat(regularDto.originalOrderId()).isNull();
        assertThat(regularDto.models())
                .containsExactly(new CombinedOrderDTO.ModelInfo(5L, "Model A", 10, 10));

        CombinedOrderDTO transferredDto = result.get(1);
        assertThat(transferredDto.id()).isEqualTo(7L);
        assertThat(transferredDto.name()).isEqualTo("Order 2 (перенос с недели: 33)");
        assertThat(transferredDto.week()).isEqualTo(34);
        assertThat(transferredDto.isTransferred()).isTrue();
        assertThat(transferredDto.originalOrderId()).isEqualTo(2L);
        assertThat(transferredDto.models())
                .containsExactly(new CombinedOrderDTO.ModelInfo(5L, "Model A", 20, 8));
    }

    @Test
    void searchWithTransferredRequiresFullWeekCoordinates() {
        assertThat(orderController.getOrdersWithTransferred(2025, 8, null)).isEmpty();
        verifyNoInteractions(orderService, dublOrderService);
    }

    @Test
    void createOrderReturnsConflictWhenWeekWasAlreadyTransferred() {
        Order request = order(null, "Order 1", 33, modelList(5L, "Model A"), 10);
        when(orderService.createOrder(request)).thenThrow(new IllegalStateException("Неделя уже перенесена"));

        var response = orderController.createOrder(request);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isEqualTo(Map.of("message", "Неделя уже перенесена"));
    }

    @Test
    void createOrderReturnsSavedOrder() {
        Order request = order(null, "Order 1", 33, modelList(5L, "Model A"), 10);
        Order saved = order(1L, "Order 1", 33, modelList(5L, "Model A"), 10);
        when(orderService.createOrder(request)).thenReturn(saved);

        var response = orderController.createOrder(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(saved);
    }

    @Test
    void updateAndDeleteReportNotFoundForUnknownIds() {
        Order details = order(1L, "Order 1", 33, modelList(5L, "Model A"), 10);
        when(orderService.updateOrder(1L, details)).thenReturn(details);
        when(orderService.updateOrder(404L, details)).thenReturn(null);
        when(orderService.deleteOrder(1L)).thenReturn(true);
        when(orderService.deleteOrder(404L)).thenReturn(false);

        assertThat(orderController.updateOrder(1L, details).getBody()).isSameAs(details);
        assertThat(orderController.updateOrder(404L, details).getStatusCode().value()).isEqualTo(404);
        assertThat(orderController.deleteOrder(1L).getStatusCode().value()).isEqualTo(200);
        assertThat(orderController.deleteOrder(404L).getStatusCode().value()).isEqualTo(404);
    }
}
