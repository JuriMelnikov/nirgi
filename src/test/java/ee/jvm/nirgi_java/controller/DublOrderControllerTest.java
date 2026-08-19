package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.DublOrder;
import ee.jvm.nirgi_java.service.DublOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DublOrderControllerTest {

    @Mock
    private DublOrderService dublOrderService;

    @InjectMocks
    private DublOrderController dublOrderController;

    private static DublOrder dublOrder(Long id) {
        DublOrder dublOrder = new DublOrder();
        dublOrder.setId(id);
        dublOrder.setOriginalYear(2025);
        dublOrder.setOriginalMonth(8);
        dublOrder.setOriginalWeek(33);
        dublOrder.setTargetYear(2025);
        dublOrder.setTargetMonth(8);
        dublOrder.setTargetWeek(34);
        return dublOrder;
    }

    private static DublOrderController.CreateDublOrderRequest createRequest(Map<Long, Integer> completedWork) {
        DublOrderController.CreateDublOrderRequest request = new DublOrderController.CreateDublOrderRequest();
        request.setOrderId(1L);
        request.setCompletedWork(completedWork);
        request.setTargetYear(2025);
        request.setTargetMonth(8);
        request.setTargetWeek(34);
        return request;
    }

    @Test
    void readEndpointsDelegateToService() {
        DublOrder dublOrder = dublOrder(7L);
        when(dublOrderService.getAllDublOrders()).thenReturn(List.of(dublOrder));
        when(dublOrderService.getDublOrderById(7L)).thenReturn(Optional.of(dublOrder));
        when(dublOrderService.getDublOrderById(404L)).thenReturn(Optional.empty());
        when(dublOrderService.getDublOrdersByTargetWeek(2025, 8, 34)).thenReturn(List.of(dublOrder));
        when(dublOrderService.getDublOrdersByOriginalWeek(2025, 8, 33)).thenReturn(List.of(dublOrder));
        when(dublOrderService.getDublOrdersByOrderId(1L)).thenReturn(List.of(dublOrder));
        when(dublOrderService.getCompletedWorkForOriginalWeek(2025, 8, 33)).thenReturn(Map.of(3L, 12));

        assertThat(dublOrderController.getAllDublOrders().getBody()).containsExactly(dublOrder);
        assertThat(dublOrderController.getDublOrderById(7L).getBody()).isSameAs(dublOrder);
        assertThat(dublOrderController.getDublOrderById(404L).getStatusCode().value()).isEqualTo(404);
        assertThat(dublOrderController.getDublOrdersByTargetWeek(2025, 8, 34).getBody()).containsExactly(dublOrder);
        assertThat(dublOrderController.getDublOrdersByOriginalWeek(2025, 8, 33).getBody()).containsExactly(dublOrder);
        assertThat(dublOrderController.getDublOrdersByOrderId(1L).getBody()).containsExactly(dublOrder);
        assertThat(dublOrderController.getCompletedWorkForOriginalWeek(2025, 8, 33).getBody())
                .containsEntry(3L, 12);
    }

    @Test
    void createDublOrderReturnsCreatedTransfer() {
        DublOrder created = dublOrder(7L);
        Map<Long, Integer> completedWork = Map.of(3L, 12);
        when(dublOrderService.createDublOrder(1L, completedWork, 2025, 8, 34)).thenReturn(created);

        var response = dublOrderController.createDublOrder(createRequest(completedWork));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(created);
    }

    @Test
    void createDublOrderMapsValidationToBadRequestAndConflictToConflict() {
        Map<Long, Integer> invalid = Map.of(3L, -1);
        Map<Long, Integer> duplicate = Map.of(3L, 12);
        when(dublOrderService.createDublOrder(1L, invalid, 2025, 8, 34))
                .thenThrow(new IllegalArgumentException("Некорректное количество"));
        when(dublOrderService.createDublOrder(1L, duplicate, 2025, 8, 34))
                .thenThrow(new IllegalStateException("Перенос уже существует"));

        var badRequest = dublOrderController.createDublOrder(createRequest(invalid));
        var conflict = dublOrderController.createDublOrder(createRequest(duplicate));

        assertThat(badRequest.getStatusCode().value()).isEqualTo(400);
        assertThat(badRequest.getBody()).isEqualTo(Map.of("message", "Некорректное количество"));
        assertThat(conflict.getStatusCode().value()).isEqualTo(409);
        assertThat(conflict.getBody()).isEqualTo(Map.of("message", "Перенос уже существует"));
    }

    @Test
    void updateCompletedWorkReturnsOkOrBadRequest() {
        DublOrder updated = dublOrder(7L);
        DublOrderController.UpdateCompletedWorkRequest request = new DublOrderController.UpdateCompletedWorkRequest();
        request.setCompletedInOriginalWeek(12);
        when(dublOrderService.updateCompletedWork(7L, 70L, 12)).thenReturn(updated);
        when(dublOrderService.updateCompletedWork(7L, 404L, 12))
                .thenThrow(new IllegalArgumentException("DublModel не найден"));

        assertThat(dublOrderController.updateCompletedWork(7L, 70L, request).getBody()).isSameAs(updated);
        var failed = dublOrderController.updateCompletedWork(7L, 404L, request);
        assertThat(failed.getStatusCode().value()).isEqualTo(400);
        assertThat(failed.getBody()).isEqualTo(Map.of("message", "DublModel не найден"));
    }

    @Test
    void deleteDublOrderReportsNotFoundWhenNothingWasDeleted() {
        when(dublOrderService.deleteDublOrder(7L)).thenReturn(true);
        when(dublOrderService.deleteDublOrder(404L)).thenReturn(false);

        assertThat(dublOrderController.deleteDublOrder(7L).getStatusCode().value()).isEqualTo(200);
        assertThat(dublOrderController.deleteDublOrder(404L).getStatusCode().value()).isEqualTo(404);
    }
}
