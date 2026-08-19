package ee.jvm.nirgi_java.service;

import ee.jvm.nirgi_java.classes.DublModel;
import ee.jvm.nirgi_java.classes.DublOrder;
import ee.jvm.nirgi_java.classes.Model;
import ee.jvm.nirgi_java.classes.ModelList;
import ee.jvm.nirgi_java.classes.Order;
import ee.jvm.nirgi_java.repository.DublOrderRepository;
import ee.jvm.nirgi_java.repository.ModelListRepository;
import ee.jvm.nirgi_java.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class DublOrderService {

    @Autowired
    private DublOrderRepository dublOrderRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ModelListRepository modelListRepository;

    public List<DublOrder> getAllDublOrders() {
        return dublOrderRepository.findAll();
    }

    public Optional<DublOrder> getDublOrderById(Long id) {
        return dublOrderRepository.findById(id);
    }

    public List<DublOrder> getDublOrdersByTargetWeek(Integer year, Integer month, Integer week) {
        return dublOrderRepository.findByTargetYearAndTargetMonthAndTargetWeek(year, month, week);
    }

    public List<DublOrder> getDublOrdersByOriginalWeek(Integer year, Integer month, Integer week) {
        return dublOrderRepository.findByOriginalYearAndOriginalMonthAndOriginalWeek(year, month, week);
    }

    public List<DublOrder> getDublOrdersByOrderId(Long orderId) {
        return dublOrderRepository.findByOrderId(orderId);
    }

    /**
     * Создает дубликат ордера для следующей недели с указанием выполненной работы в оригинальной неделе
     * 
     * @param orderId ID оригинального ордера
     * @param completedWork Map где ключ - modelListId, значение - количество выполненной работы в оригинальной неделе
     * @param targetYear Год целевой недели
     * @param targetMonth Месяц целевой недели
     * @param targetWeek Целевая неделя
     * @return Созданный DublOrder
     */
    public DublOrder createDublOrder(Long orderId, Map<Long, Integer> completedWork, 
                                      Integer targetYear, Integer targetMonth, Integer targetWeek) {
        Order originalOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        // Проверяем, не существует ли уже дубликат для этой целевой недели
        Optional<DublOrder> existingDubl = dublOrderRepository
                .findByOrderIdAndTargetYearAndTargetMonthAndTargetWeek(orderId, targetYear, targetMonth, targetWeek);
        
        if (existingDubl.isPresent()) {
            throw new IllegalStateException("DublOrder already exists for this order and target week");
        }

        DublOrder dublOrder = new DublOrder();
        dublOrder.setOrder(originalOrder);
        dublOrder.setOriginalYear(originalOrder.getYear());
        dublOrder.setOriginalMonth(originalOrder.getMonth());
        dublOrder.setOriginalWeek(originalOrder.getWeek());
        dublOrder.setTargetYear(targetYear);
        dublOrder.setTargetMonth(targetMonth);
        dublOrder.setTargetWeek(targetWeek);

        // Создаем DublModel для каждой модели в оригинальном ордере
        for (Model model : originalOrder.getModels()) {
            Long modelListId = model.getModelList().getId();
            Integer totalCount = model.getCount();
            Integer completedInOriginalWeek = completedWork.getOrDefault(modelListId, 0);
            Integer remainingCount = totalCount - completedInOriginalWeek;

            if (remainingCount < 0) {
                throw new IllegalArgumentException("Completed work cannot exceed total count for model: " + modelListId);
            }

            // Создаем только если есть невыполненная работа
            if (remainingCount > 0) {
                DublModel dublModel = new DublModel();
                dublModel.setDublOrder(dublOrder);
                dublModel.setModelList(model.getModelList());
                dublModel.setTotalCount(totalCount);
                dublModel.setCompletedInOriginalWeek(completedInOriginalWeek);
                dublModel.setRemainingCount(remainingCount);
                dublOrder.addDublModel(dublModel);
            }
        }

        return dublOrderRepository.save(dublOrder);
    }

    /**
     * Обновляет количество выполненной работы в оригинальной неделе для конкретной модели
     */
    public DublOrder updateCompletedWork(Long dublOrderId, Long dublModelId, Integer completedInOriginalWeek) {
        DublOrder dublOrder = dublOrderRepository.findById(dublOrderId)
                .orElseThrow(() -> new IllegalArgumentException("DublOrder not found with id: " + dublOrderId));

        DublModel dublModel = dublOrder.getDublModels().stream()
                .filter(dm -> dm.getId().equals(dublModelId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("DublModel not found with id: " + dublModelId));

        if (completedInOriginalWeek < 0 || completedInOriginalWeek > dublModel.getTotalCount()) {
            throw new IllegalArgumentException("Invalid completed work value");
        }

        dublModel.setCompletedInOriginalWeek(completedInOriginalWeek);
        dublModel.setRemainingCount(dublModel.getTotalCount() - completedInOriginalWeek);

        return dublOrderRepository.save(dublOrder);
    }

    public boolean deleteDublOrder(Long id) {
        try {
            dublOrderRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Получает статистику выполненной работы для оригинальной недели (для расчета зарплаты)
     */
    public Map<Long, Integer> getCompletedWorkForOriginalWeek(Integer year, Integer month, Integer week) {
        List<DublOrder> dublOrders = dublOrderRepository.findByOriginalYearAndOriginalMonthAndOriginalWeek(year, month, week);
        
        return dublOrders.stream()
            .collect(java.util.stream.Collectors.toMap(
                dublOrder -> dublOrder.getOrder().getId(),
                dublOrder -> dublOrder.getDublModels().stream()
                    .mapToInt(DublModel::getCompletedInOriginalWeek)
                    .sum(),
                (existing, replacement) -> existing + replacement
            ));
    }
}
