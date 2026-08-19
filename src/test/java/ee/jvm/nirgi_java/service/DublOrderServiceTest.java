package ee.jvm.nirgi_java.service;

import ee.jvm.nirgi_java.classes.DublModel;
import ee.jvm.nirgi_java.classes.DublOrder;
import ee.jvm.nirgi_java.classes.Model;
import ee.jvm.nirgi_java.classes.ModelList;
import ee.jvm.nirgi_java.classes.Order;
import ee.jvm.nirgi_java.repository.DublOrderRepository;
import ee.jvm.nirgi_java.repository.ModelListRepository;
import ee.jvm.nirgi_java.repository.OrderRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DublOrderServiceTest {

    @Mock
    private DublOrderRepository dublOrderRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ModelListRepository modelListRepository;

    @InjectMocks
    private DublOrderService dublOrderService;

    private ModelList shirts;
    private ModelList trousers;
    private Order order;

    @BeforeEach
    void setUp() {
        shirts = new ModelList(1L, "Shirts");
        trousers = new ModelList(2L, "Trousers");
        order = new Order(100L, "Order-1", 2026, 3, 11);
        order.addModel(new Model(10L, shirts, order, 10));
        order.addModel(new Model(11L, trousers, order, 4));

        when(dublOrderRepository.save(any(DublOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createDublOrderCopiesOnlyModelsWithRemainingWork() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(dublOrderRepository.findByOrderIdAndTargetYearAndTargetMonthAndTargetWeek(100L, 2026, 3, 12))
                .thenReturn(Optional.empty());

        DublOrder created = dublOrderService.createDublOrder(100L, Map.of(1L, 6, 2L, 4), 2026, 3, 12);

        assertThat(created.getOrder()).isSameAs(order);
        assertThat(created.getOriginalYear()).isEqualTo(2026);
        assertThat(created.getOriginalMonth()).isEqualTo(3);
        assertThat(created.getOriginalWeek()).isEqualTo(11);
        assertThat(created.getTargetYear()).isEqualTo(2026);
        assertThat(created.getTargetMonth()).isEqualTo(3);
        assertThat(created.getTargetWeek()).isEqualTo(12);

        assertThat(created.getDublModels()).hasSize(1);
        DublModel dublModel = created.getDublModels().get(0);
        assertThat(dublModel.getModelList()).isSameAs(shirts);
        assertThat(dublModel.getTotalCount()).isEqualTo(10);
        assertThat(dublModel.getCompletedInOriginalWeek()).isEqualTo(6);
        assertThat(dublModel.getRemainingCount()).isEqualTo(4);
        assertThat(dublModel.getDublOrder()).isSameAs(created);
        verify(dublOrderRepository).save(created);
    }

    @Test
    void createDublOrderTreatsMissingCompletedWorkAsZero() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(dublOrderRepository.findByOrderIdAndTargetYearAndTargetMonthAndTargetWeek(anyLong(), any(), any(), any()))
                .thenReturn(Optional.empty());

        DublOrder created = dublOrderService.createDublOrder(100L, Map.of(), 2026, 3, 12);

        assertThat(created.getDublModels()).hasSize(2);
        assertThat(created.getDublModels())
                .allSatisfy(dublModel -> assertThat(dublModel.getCompletedInOriginalWeek()).isZero());
        assertThat(created.getDublModels())
                .extracting(DublModel::getRemainingCount)
                .containsExactly(10, 4);
    }

    @Test
    void createDublOrderFailsWhenOrderMissing() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dublOrderService.createDublOrder(404L, Map.of(), 2026, 3, 12))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("404");
    }

    @Test
    void createDublOrderFailsWhenDuplicateAlreadyExistsForTargetWeek() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(dublOrderRepository.findByOrderIdAndTargetYearAndTargetMonthAndTargetWeek(100L, 2026, 3, 12))
                .thenReturn(Optional.of(new DublOrder()));

        assertThatThrownBy(() -> dublOrderService.createDublOrder(100L, Map.of(), 2026, 3, 12))
                .isInstanceOf(IllegalStateException.class);

        verify(dublOrderRepository, never()).save(any());
    }

    @Test
    void createDublOrderFailsWhenCompletedWorkExceedsOrderedCount() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(dublOrderRepository.findByOrderIdAndTargetYearAndTargetMonthAndTargetWeek(100L, 2026, 3, 12))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dublOrderService.createDublOrder(100L, Map.of(1L, 11), 2026, 3, 12))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Completed work cannot exceed");
    }

    @Test
    void updateCompletedWorkRecalculatesRemainingCount() {
        DublOrder dublOrder = new DublOrder();
        DublModel dublModel = new DublModel(5L, dublOrder, shirts, 10, 2, 8);
        dublOrder.addDublModel(dublModel);
        when(dublOrderRepository.findById(1L)).thenReturn(Optional.of(dublOrder));

        DublOrder updated = dublOrderService.updateCompletedWork(1L, 5L, 7);

        assertThat(updated.getDublModels().get(0).getCompletedInOriginalWeek()).isEqualTo(7);
        assertThat(updated.getDublModels().get(0).getRemainingCount()).isEqualTo(3);
        verify(dublOrderRepository).save(dublOrder);
    }

    @Test
    void updateCompletedWorkFailsForUnknownDublOrderOrModel() {
        when(dublOrderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> dublOrderService.updateCompletedWork(1L, 5L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DublOrder not found");

        DublOrder dublOrder = new DublOrder();
        dublOrder.addDublModel(new DublModel(5L, dublOrder, shirts, 10, 0, 10));
        when(dublOrderRepository.findById(2L)).thenReturn(Optional.of(dublOrder));
        assertThatThrownBy(() -> dublOrderService.updateCompletedWork(2L, 999L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DublModel not found");
    }

    @Test
    void updateCompletedWorkRejectsValuesOutsideTotalCount() {
        DublOrder dublOrder = new DublOrder();
        dublOrder.addDublModel(new DublModel(5L, dublOrder, shirts, 10, 0, 10));
        when(dublOrderRepository.findById(1L)).thenReturn(Optional.of(dublOrder));

        assertThatThrownBy(() -> dublOrderService.updateCompletedWork(1L, 5L, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> dublOrderService.updateCompletedWork(1L, 5L, 11))
                .isInstanceOf(IllegalArgumentException.class);

        verify(dublOrderRepository, never()).save(any());
    }

    @Test
    void deleteDublOrderReportsRepositoryOutcome() {
        assertThat(dublOrderService.deleteDublOrder(1L)).isTrue();

        doThrow(new RuntimeException("constraint violation")).when(dublOrderRepository).deleteById(2L);
        assertThat(dublOrderService.deleteDublOrder(2L)).isFalse();
    }

    @Test
    void getCompletedWorkForOriginalWeekSumsPerOrder() {
        DublOrder first = new DublOrder();
        first.setOrder(order);
        first.addDublModel(new DublModel(1L, first, shirts, 10, 6, 4));
        first.addDublModel(new DublModel(2L, first, trousers, 4, 1, 3));

        DublOrder second = new DublOrder();
        second.setOrder(order);
        second.addDublModel(new DublModel(3L, second, shirts, 10, 2, 8));

        when(dublOrderRepository.findByOriginalYearAndOriginalMonthAndOriginalWeek(2026, 3, 11))
                .thenReturn(List.of(first, second));

        assertThat(dublOrderService.getCompletedWorkForOriginalWeek(2026, 3, 11))
                .containsExactly(Map.entry(100L, 9));
    }

    @Test
    void queryMethodsDelegateToRepository() {
        DublOrder dublOrder = new DublOrder();
        when(dublOrderRepository.findAll()).thenReturn(List.of(dublOrder));
        when(dublOrderRepository.findById(1L)).thenReturn(Optional.of(dublOrder));
        when(dublOrderRepository.findByTargetYearAndTargetMonthAndTargetWeek(2026, 3, 12)).thenReturn(List.of(dublOrder));
        when(dublOrderRepository.findByOriginalYearAndOriginalMonthAndOriginalWeek(2026, 3, 11)).thenReturn(List.of(dublOrder));
        when(dublOrderRepository.findByOrderId(100L)).thenReturn(List.of(dublOrder));

        assertThat(dublOrderService.getAllDublOrders()).containsExactly(dublOrder);
        assertThat(dublOrderService.getDublOrderById(1L)).contains(dublOrder);
        assertThat(dublOrderService.getDublOrdersByTargetWeek(2026, 3, 12)).containsExactly(dublOrder);
        assertThat(dublOrderService.getDublOrdersByOriginalWeek(2026, 3, 11)).containsExactly(dublOrder);
        assertThat(dublOrderService.getDublOrdersByOrderId(100L)).containsExactly(dublOrder);
    }
}
