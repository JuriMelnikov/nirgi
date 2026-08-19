package ee.jvm.nirgi_java.service;

import ee.jvm.nirgi_java.classes.DublModel;
import ee.jvm.nirgi_java.classes.DublOrder;
import ee.jvm.nirgi_java.classes.Employee;
import ee.jvm.nirgi_java.classes.Model;
import ee.jvm.nirgi_java.classes.ModelList;
import ee.jvm.nirgi_java.classes.Order;
import ee.jvm.nirgi_java.classes.SectionList;
import ee.jvm.nirgi_java.classes.Techmap;
import ee.jvm.nirgi_java.classes.WorkResult;
import ee.jvm.nirgi_java.repository.DublOrderRepository;
import ee.jvm.nirgi_java.repository.EmployeeRepository;
import ee.jvm.nirgi_java.repository.ModelListRepository;
import ee.jvm.nirgi_java.repository.OrderRepository;
import ee.jvm.nirgi_java.repository.SectionListRepository;
import ee.jvm.nirgi_java.repository.TechmapRepository;
import ee.jvm.nirgi_java.repository.WorkResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkResultServiceTest {

    private static final int NEVER_BLOCKING_STOP_DAY = 31;
    private static final int ALWAYS_BLOCKING_STOP_DAY = 0;

    @Mock
    private WorkResultRepository workResultRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ModelListRepository modelListRepository;

    @Mock
    private SectionListRepository sectionListRepository;

    @Mock
    private TechmapRepository techmapRepository;

    @Mock
    private SettingsService settingsService;

    @Mock
    private DublOrderRepository dublOrderRepository;

    @InjectMocks
    private WorkResultService workResultService;

    private Employee employee;
    private ModelList shirts;
    private SectionList section;
    private Techmap techmap;
    private Order order;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setName("Юрий");
        employee.setSurname("Мельников");
        shirts = new ModelList(1L, "Shirts");
        section = new SectionList(2L, "Sewing");
        techmap = new Techmap(3L, "TM-1", "descriptor", shirts, "1.5", "10.0", section);
        order = new Order(100L, "Order-1", 2026, 3, 11);
        order.addModel(new Model(10L, shirts, order, 10));

        when(settingsService.getStopDay()).thenReturn(NEVER_BLOCKING_STOP_DAY);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(modelListRepository.findById(1L)).thenReturn(Optional.of(shirts));
        when(sectionListRepository.findById(2L)).thenReturn(Optional.of(section));
        when(techmapRepository.findById(3L)).thenReturn(Optional.of(techmap));
        when(workResultRepository.save(any(WorkResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createWorkResultPersistsResolvedEntities() {
        WorkResult created = workResultService.createWorkResult(1L, 100L, 1L, 2L, 3L, 4, 2026, 3, 11);

        assertThat(created.getEmployee()).isSameAs(employee);
        assertThat(created.getOrder()).isSameAs(order);
        assertThat(created.getModelList()).isSameAs(shirts);
        assertThat(created.getSectionList()).isSameAs(section);
        assertThat(created.getTechmap()).isSameAs(techmap);
        assertThat(created.getQuantity()).isEqualTo(4);
        assertThat(created.getYear()).isEqualTo(2026);
        assertThat(created.getMonth()).isEqualTo(3);
        assertThat(created.getWeek()).isEqualTo(11);
        verify(workResultRepository).save(created);
    }

    @Test
    void createWorkResultAllowsOmittedSectionAndTechmap() {
        WorkResult created = workResultService.createWorkResult(1L, 100L, 1L, null, null, 1, 2026, 3, 11);

        assertThat(created.getSectionList()).isNull();
        assertThat(created.getTechmap()).isNull();
    }

    @Test
    void createWorkResultBlockedAfterStopDay() {
        when(settingsService.getStopDay()).thenReturn(ALWAYS_BLOCKING_STOP_DAY);

        assertThatThrownBy(() -> workResultService.createWorkResult(1L, 100L, 1L, null, null, 1, 2026, 3, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Запрещено добавлять данные");

        verify(workResultRepository, never()).save(any());
    }

    @Test
    void createWorkResultFailsForUnknownReferences() {
        assertThatThrownBy(() -> workResultService.createWorkResult(404L, 100L, 1L, null, null, 1, 2026, 3, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Employee not found");
        assertThatThrownBy(() -> workResultService.createWorkResult(1L, 404L, 1L, null, null, 1, 2026, 3, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
        assertThatThrownBy(() -> workResultService.createWorkResult(1L, 100L, 404L, null, null, 1, 2026, 3, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ModelList not found");
        assertThatThrownBy(() -> workResultService.createWorkResult(1L, 100L, 1L, 404L, null, 1, 2026, 3, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SectionList not found");
        assertThatThrownBy(() -> workResultService.createWorkResult(1L, 100L, 1L, null, 404L, 1, 2026, 3, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Techmap not found");
    }

    @Test
    void createWorkResultFailsWhenModelIsNotPartOfOrder() {
        ModelList other = new ModelList(9L, "Other");
        when(modelListRepository.findById(9L)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> workResultService.createWorkResult(1L, 100L, 9L, null, null, 1, 2026, 3, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Model not found in order");
    }

    @Test
    void createWorkResultFailsWhenQuantityExceedsAvailableAfterCompletedAndTransferred() {
        when(workResultRepository.sumQuantityByEmployeeOrderModelSectionTechmapAndDate(1L, 100L, 1L, null, null, 2026, 3, 11))
                .thenReturn(3);
        when(dublOrderRepository.findByOriginalYearAndOriginalMonthAndOriginalWeek(2026, 3, 11))
                .thenReturn(List.of(dublOrderTransferring(4)));

        assertThatThrownBy(() -> workResultService.createWorkResult(1L, 100L, 1L, null, null, 4, 2026, 3, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Available quantity: 3");

        assertThat(workResultService.createWorkResult(1L, 100L, 1L, null, null, 3, 2026, 3, 11).getQuantity())
                .isEqualTo(3);
    }

    @Test
    void updateWorkResultChangesQuantity() {
        WorkResult stored = new WorkResult(employee, order, shirts, null, null, 2, 2026, 3, 11);
        when(workResultRepository.findById(7L)).thenReturn(Optional.of(stored));

        assertThat(workResultService.updateWorkResult(7L, 5).getQuantity()).isEqualTo(5);
        verify(workResultRepository).save(stored);
    }

    @Test
    void updateWorkResultFailsAfterStopDayAndForUnknownId() {
        when(settingsService.getStopDay()).thenReturn(ALWAYS_BLOCKING_STOP_DAY);
        assertThatThrownBy(() -> workResultService.updateWorkResult(7L, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Запрещено изменять данные");

        when(settingsService.getStopDay()).thenReturn(NEVER_BLOCKING_STOP_DAY);
        when(workResultRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> workResultService.updateWorkResult(7L, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("WorkResult not found");
    }

    @Test
    void deleteWorkResultRespectsStopDayAndReportsRepositoryOutcome() {
        assertThat(workResultService.deleteWorkResult(7L)).isTrue();

        doThrow(new RuntimeException("constraint violation")).when(workResultRepository).deleteById(8L);
        assertThat(workResultService.deleteWorkResult(8L)).isFalse();

        when(settingsService.getStopDay()).thenReturn(ALWAYS_BLOCKING_STOP_DAY);
        assertThatThrownBy(() -> workResultService.deleteWorkResult(7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Запрещено удалять данные");
    }

    @Test
    void completedQuantitiesFallBackToZeroWhenNoRowsMatch() {
        when(workResultRepository.sumQuantityByEmployeeOrderModelSectionTechmapAndDate(1L, 100L, 1L, 2L, 3L, 2026, 3, 11))
                .thenReturn(null);
        when(workResultRepository.sumQuantityByEmployeeOrderModelSectionTechmapIncludingTransferred(1L, 100L, 1L, 2L, 3L))
                .thenReturn(null);

        assertThat(workResultService.getCompletedQuantity(1L, 100L, 1L, 2L, 3L, 2026, 3, 11)).isZero();
        assertThat(workResultService.getTotalCompletedQuantity(1L, 100L, 1L, 2L, 3L)).isZero();

        when(workResultRepository.sumQuantityByEmployeeOrderModelSectionTechmapAndDate(1L, 100L, 1L, 2L, 3L, 2026, 3, 11))
                .thenReturn(6);
        when(workResultRepository.sumQuantityByEmployeeOrderModelSectionTechmapIncludingTransferred(1L, 100L, 1L, 2L, 3L))
                .thenReturn(9);

        assertThat(workResultService.getCompletedQuantity(1L, 100L, 1L, 2L, 3L, 2026, 3, 11)).isEqualTo(6);
        assertThat(workResultService.getTotalCompletedQuantity(1L, 100L, 1L, 2L, 3L)).isEqualTo(9);
    }

    @Test
    void transferredQuantityCountsOnlyMatchingOrderAndModel() {
        Order otherOrder = new Order(200L, "Order-2", 2026, 3, 11);
        DublOrder otherOrderTransfer = new DublOrder();
        otherOrderTransfer.setOrder(otherOrder);
        otherOrderTransfer.addDublModel(new DublModel(20L, otherOrderTransfer, shirts, 10, 0, 10));

        DublOrder otherModelTransfer = new DublOrder();
        otherModelTransfer.setOrder(order);
        otherModelTransfer.addDublModel(new DublModel(21L, otherModelTransfer, new ModelList(5L, "Other"), 10, 0, 7));

        when(dublOrderRepository.findByOriginalYearAndOriginalMonthAndOriginalWeek(2026, 3, 11))
                .thenReturn(List.of(dublOrderTransferring(4), otherOrderTransfer, otherModelTransfer));

        assertThat(workResultService.getTransferredQuantityForCurrentWeek(100L, 1L, 2026, 3, 11)).isEqualTo(4);
    }

    @Test
    void getCompletedWorkByOrderAndWeekSumsQuantityPerModel() {
        ModelList trousers = new ModelList(2L, "Trousers");
        when(workResultRepository.findByOrderIdAndYearAndMonthAndWeek(100L, 2026, 3, 11)).thenReturn(List.of(
                new WorkResult(employee, order, shirts, null, null, 2, 2026, 3, 11),
                new WorkResult(employee, order, shirts, null, null, 3, 2026, 3, 11),
                new WorkResult(employee, order, trousers, null, null, 4, 2026, 3, 11)
        ));

        assertThat(workResultService.getCompletedWorkByOrderAndWeek(100L, 2026, 3, 11))
                .containsOnly(Map.entry(1L, 5), Map.entry(2L, 4));
    }

    @Test
    void queryMethodsDelegateToRepository() {
        WorkResult workResult = new WorkResult(employee, order, shirts, null, null, 1, 2026, 3, 11);
        when(workResultRepository.findAll()).thenReturn(List.of(workResult));
        when(workResultRepository.findById(7L)).thenReturn(Optional.of(workResult));
        when(workResultRepository.findByEmployeeIdAndYearAndMonthAndWeek(1L, 2026, 3, 11)).thenReturn(List.of(workResult));
        when(workResultRepository.findByEmployeeIdAndYearAndMonth(1L, 2026, 3)).thenReturn(List.of(workResult));
        when(workResultRepository.findByOrderIdAndYearAndMonthAndWeek(100L, 2026, 3, 11)).thenReturn(List.of(workResult));
        when(workResultRepository.findByOrderIdAndYearAndMonth(100L, 2026, 3)).thenReturn(List.of(workResult));
        when(workResultRepository.findByEmployeeIdAndOrderId(1L, 100L)).thenReturn(List.of(workResult));
        when(workResultRepository.findByEmployeeIdAndOrderIdAndYearAndMonthAndWeek(1L, 100L, 2026, 3, 11)).thenReturn(List.of(workResult));

        assertThat(workResultService.getAllWorkResults()).containsExactly(workResult);
        assertThat(workResultService.getWorkResultById(7L)).contains(workResult);
        assertThat(workResultService.getWorkResultsByEmployeeAndDate(1L, 2026, 3, 11)).containsExactly(workResult);
        assertThat(workResultService.getWorkResultsByEmployeeAndMonth(1L, 2026, 3)).containsExactly(workResult);
        assertThat(workResultService.getWorkResultsByOrderAndDate(100L, 2026, 3, 11)).containsExactly(workResult);
        assertThat(workResultService.getWorkResultsByOrderAndMonth(100L, 2026, 3)).containsExactly(workResult);
        assertThat(workResultService.getWorkResultsByEmployeeAndOrder(1L, 100L)).containsExactly(workResult);
        assertThat(workResultService.getWorkResultsByEmployeeOrderAndDate(1L, 100L, 2026, 3, 11)).containsExactly(workResult);
    }

    private DublOrder dublOrderTransferring(int remainingCount) {
        DublOrder dublOrder = new DublOrder();
        dublOrder.setOrder(order);
        dublOrder.addDublModel(new DublModel(30L, dublOrder, shirts, 10, 10 - remainingCount, remainingCount));
        return dublOrder;
    }
}
