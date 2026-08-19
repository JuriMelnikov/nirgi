package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.WorkResult;
import ee.jvm.nirgi_java.service.WorkResultService;
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
class WorkResultControllerTest {

    @Mock
    private WorkResultService workResultService;

    @InjectMocks
    private WorkResultController workResultController;

    private static WorkResult workResult(Long id, int quantity) {
        WorkResult workResult = new WorkResult();
        workResult.setId(id);
        workResult.setQuantity(quantity);
        workResult.setYear(2025);
        workResult.setMonth(8);
        workResult.setWeek(33);
        return workResult;
    }

    private static WorkResultController.CreateWorkResultRequest createRequest(Integer quantity) {
        WorkResultController.CreateWorkResultRequest request = new WorkResultController.CreateWorkResultRequest();
        request.setEmployeeId(1L);
        request.setOrderId(2L);
        request.setModelListId(3L);
        request.setSectionListId(4L);
        request.setTechmapId(5L);
        request.setQuantity(quantity);
        request.setYear(2025);
        request.setMonth(8);
        request.setWeek(33);
        return request;
    }

    @Test
    void listAndGetByIdDelegateToService() {
        WorkResult result = workResult(1L, 5);
        when(workResultService.getAllWorkResults()).thenReturn(List.of(result));
        when(workResultService.getWorkResultById(1L)).thenReturn(Optional.of(result));
        when(workResultService.getWorkResultById(404L)).thenReturn(Optional.empty());

        assertThat(workResultController.getAllWorkResults().getBody()).containsExactly(result);
        assertThat(workResultController.getWorkResultById(1L).getBody()).isSameAs(result);
        assertThat(workResultController.getWorkResultById(404L).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void employeeLookupFallsBackToMonthWhenWeekIsMissing() {
        WorkResult weekly = workResult(1L, 5);
        WorkResult monthly = workResult(2L, 9);
        when(workResultService.getWorkResultsByEmployeeAndDate(1L, 2025, 8, 33)).thenReturn(List.of(weekly));
        when(workResultService.getWorkResultsByEmployeeAndMonth(1L, 2025, 8)).thenReturn(List.of(monthly));

        assertThat(workResultController.getWorkResultsByEmployeeAndDate(1L, 2025, 8, 33).getBody())
                .containsExactly(weekly);
        assertThat(workResultController.getWorkResultsByEmployeeAndDate(1L, 2025, 8, null).getBody())
                .containsExactly(monthly);
    }

    @Test
    void orderLookupFallsBackToMonthWhenWeekIsMissing() {
        WorkResult weekly = workResult(1L, 5);
        WorkResult monthly = workResult(2L, 9);
        when(workResultService.getWorkResultsByOrderAndDate(2L, 2025, 8, 33)).thenReturn(List.of(weekly));
        when(workResultService.getWorkResultsByOrderAndMonth(2L, 2025, 8)).thenReturn(List.of(monthly));

        assertThat(workResultController.getWorkResultsByOrderAndDate(2L, 2025, 8, 33).getBody())
                .containsExactly(weekly);
        assertThat(workResultController.getWorkResultsByOrderAndDate(2L, 2025, 8, null).getBody())
                .containsExactly(monthly);
    }

    @Test
    void employeeAndOrderLookupUsesDateOnlyWhenAllPartsArePresent() {
        WorkResult dated = workResult(1L, 5);
        WorkResult undated = workResult(2L, 9);
        when(workResultService.getWorkResultsByEmployeeOrderAndDate(1L, 2L, 2025, 8, 33)).thenReturn(List.of(dated));
        when(workResultService.getWorkResultsByEmployeeAndOrder(1L, 2L)).thenReturn(List.of(undated));

        assertThat(workResultController.getWorkResultsByEmployeeAndOrder(1L, 2L, 2025, 8, 33).getBody())
                .containsExactly(dated);
        assertThat(workResultController.getWorkResultsByEmployeeAndOrder(1L, 2L, 2025, 8, null).getBody())
                .containsExactly(undated);
    }

    @Test
    void quantityEndpointsReturnServiceValues() {
        when(workResultService.getCompletedQuantity(1L, 2L, 3L, 4L, 5L, 2025, 8, 33)).thenReturn(7);
        when(workResultService.getTotalCompletedQuantity(1L, 2L, 3L, 4L, 5L)).thenReturn(19);
        when(workResultService.getTransferredQuantityForCurrentWeek(2L, 3L, 2025, 8, 33)).thenReturn(4);
        when(workResultService.getCompletedWorkByOrderAndWeek(2L, 2025, 8, 33)).thenReturn(Map.of(3L, 7));

        assertThat(workResultController.getCompletedQuantity(1L, 2L, 3L, 4L, 5L, 2025, 8, 33).getBody()).isEqualTo(7);
        assertThat(workResultController.getTotalCompletedQuantity(1L, 2L, 3L, 4L, 5L).getBody()).isEqualTo(19);
        assertThat(workResultController.getTransferredQuantity(2L, 3L, 2025, 8, 33).getBody()).isEqualTo(4);
        assertThat(workResultController.getCompletedWorkByOrderAndWeek(2L, 2025, 8, 33).getBody())
                .containsEntry(3L, 7);
    }

    @Test
    void createWorkResultReturnsSavedResult() {
        WorkResult saved = workResult(1L, 5);
        when(workResultService.createWorkResult(1L, 2L, 3L, 4L, 5L, 5, 2025, 8, 33)).thenReturn(saved);

        var response = workResultController.createWorkResult(createRequest(5));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(saved);
    }

    @Test
    void createWorkResultTranslatesValidationErrorsToBadRequest() {
        when(workResultService.createWorkResult(1L, 2L, 3L, 4L, 5L, 999, 2025, 8, 33))
                .thenThrow(new IllegalArgumentException("Превышено количество"));

        var response = workResultController.createWorkResult(createRequest(999));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo(Map.of("message", "Превышено количество"));
    }

    @Test
    void updateWorkResultReturnsOkOrBadRequest() {
        WorkResult updated = workResult(1L, 8);
        WorkResultController.UpdateWorkResultRequest request = new WorkResultController.UpdateWorkResultRequest();
        request.setQuantity(8);
        when(workResultService.updateWorkResult(1L, 8)).thenReturn(updated);
        when(workResultService.updateWorkResult(2L, 8)).thenThrow(new IllegalArgumentException("Изменение запрещено"));

        assertThat(workResultController.updateWorkResult(1L, request).getBody()).isSameAs(updated);
        var failed = workResultController.updateWorkResult(2L, request);
        assertThat(failed.getStatusCode().value()).isEqualTo(400);
        assertThat(failed.getBody()).isEqualTo(Map.of("message", "Изменение запрещено"));
    }

    @Test
    void deleteWorkResultReportsNotFoundWhenNothingWasDeleted() {
        when(workResultService.deleteWorkResult(1L)).thenReturn(true);
        when(workResultService.deleteWorkResult(404L)).thenReturn(false);

        assertThat(workResultController.deleteWorkResult(1L).getStatusCode().value()).isEqualTo(200);
        assertThat(workResultController.deleteWorkResult(404L).getStatusCode().value()).isEqualTo(404);
    }
}
