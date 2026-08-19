package ee.jvm.nirgi_java.service;

import ee.jvm.nirgi_java.classes.*;
import ee.jvm.nirgi_java.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkResultService {

    @Autowired
    private WorkResultRepository workResultRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ModelListRepository modelListRepository;

    @Autowired
    private SectionListRepository sectionListRepository;

    @Autowired
    private TechmapRepository techmapRepository;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private DublOrderRepository dublOrderRepository;

    public List<WorkResult> getAllWorkResults() {
        return workResultRepository.findAll();
    }

    public Optional<WorkResult> getWorkResultById(Long id) {
        return workResultRepository.findById(id);
    }

    public List<WorkResult> getWorkResultsByEmployeeAndDate(Long employeeId, Integer year, Integer month, Integer week) {
        return workResultRepository.findByEmployeeIdAndYearAndMonthAndWeek(employeeId, year, month, week);
    }

    public List<WorkResult> getWorkResultsByEmployeeAndMonth(Long employeeId, Integer year, Integer month) {
        return workResultRepository.findByEmployeeIdAndYearAndMonth(employeeId, year, month);
    }

    public List<WorkResult> getWorkResultsByOrderAndDate(Long orderId, Integer year, Integer month, Integer week) {
        return workResultRepository.findByOrderIdAndYearAndMonthAndWeek(orderId, year, month, week);
    }

    public List<WorkResult> getWorkResultsByOrderAndMonth(Long orderId, Integer year, Integer month) {
        return workResultRepository.findByOrderIdAndYearAndMonth(orderId, year, month);
    }

    public List<WorkResult> getWorkResultsByEmployeeAndOrder(Long employeeId, Long orderId) {
        return workResultRepository.findByEmployeeIdAndOrderId(employeeId, orderId);
    }

    public List<WorkResult> getWorkResultsByEmployeeOrderAndDate(Long employeeId, Long orderId, Integer year, Integer month, Integer week) {
        return workResultRepository.findByEmployeeIdAndOrderIdAndYearAndMonthAndWeek(employeeId, orderId, year, month, week);
    }

    public Integer getCompletedQuantity(Long employeeId, Long orderId, Long modelListId, Long sectionListId, Long techmapId, Integer year, Integer month, Integer week) {
        Integer quantity = workResultRepository.sumQuantityByEmployeeOrderModelSectionTechmapAndDate(
                employeeId, orderId, modelListId, sectionListId, techmapId, year, month, week);
        return quantity != null ? quantity : 0;
    }

    public Integer getTotalCompletedQuantity(Long employeeId, Long orderId, Long modelListId, Long sectionListId, Long techmapId) {
        Integer quantity = workResultRepository.sumQuantityByEmployeeOrderModelSectionTechmapIncludingTransferred(
                employeeId, orderId, modelListId, sectionListId, techmapId);
        return quantity != null ? quantity : 0;
    }

    private Integer getTransferredQuantity(Long orderId, Long modelListId, Integer year, Integer month, Integer week) {
        // Check if this week is an original week (has transfers from it)
        List<ee.jvm.nirgi_java.classes.DublOrder> dublOrdersAsOriginal = dublOrderRepository.findByOriginalYearAndOriginalMonthAndOriginalWeek(year, month, week);
        
        Integer transferredFromThisWeek = dublOrdersAsOriginal.stream()
            .filter(dublOrder -> dublOrder.getOrder().getId().equals(orderId))
            .flatMap(dublOrder -> dublOrder.getDublModels().stream())
            .filter(dublModel -> dublModel.getModelList().getId().equals(modelListId))
            .mapToInt(ee.jvm.nirgi_java.classes.DublModel::getRemainingCount)
            .sum();
        
        // For validation, we need to subtract what was transferred FROM this week
        // because those instances are not available in the original week
        return transferredFromThisWeek;
    }

    public Integer getTransferredQuantityForCurrentWeek(Long orderId, Long modelListId, Integer year, Integer month, Integer week) {
        return getTransferredQuantity(orderId, modelListId, year, month, week);
    }

    public WorkResult createWorkResult(Long employeeId, Long orderId, Long modelListId, Long sectionListId, 
                                       Long techmapId, Integer quantity, Integer year, Integer month, Integer week) {
        Integer stopDay = settingsService.getStopDay();
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        
        if (currentDate.getDayOfMonth() > stopDay) {
            throw new IllegalArgumentException("Запрещено добавлять данные после " + stopDay + "-го числа месяца");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
        
        ModelList modelList = modelListRepository.findById(modelListId)
                .orElseThrow(() -> new IllegalArgumentException("ModelList not found with id: " + modelListId));
        
        SectionList sectionList = null;
        if (sectionListId != null) {
            sectionList = sectionListRepository.findById(sectionListId)
                    .orElseThrow(() -> new IllegalArgumentException("SectionList not found with id: " + sectionListId));
        }
        
        Techmap techmap = null;
        if (techmapId != null) {
            techmap = techmapRepository.findById(techmapId)
                    .orElseThrow(() -> new IllegalArgumentException("Techmap not found with id: " + techmapId));
        }

        // Validate quantity against order model count (check total across all periods)
        Model model = order.getModels().stream()
                .filter(m -> m.getModelList().getId().equals(modelListId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Model not found in order"));
        
        Integer totalQuantity = model.getCount();
        Integer completedInCurrentWeek = getCompletedQuantity(employeeId, orderId, modelListId, sectionListId, techmapId, year, month, week);
        
        // Check if there are transferred instances for this model in the current week
        Integer transferredQuantity = getTransferredQuantity(orderId, modelListId, year, month, week);
        
        // Available quantity for current week = total - transferred - completed in current week
        Integer availableQuantity = totalQuantity - transferredQuantity - completedInCurrentWeek;
        
        if (quantity > availableQuantity) {
            throw new IllegalArgumentException("Cannot add " + quantity + " items. Available quantity: " + availableQuantity + " (Total: " + totalQuantity + ", Transferred: " + transferredQuantity + ", Already completed: " + completedInCurrentWeek + ")");
        }

        WorkResult workResult = new WorkResult(employee, order, modelList, sectionList, techmap, quantity, year, month, week);
        return workResultRepository.save(workResult);
    }

    public WorkResult updateWorkResult(Long id, Integer quantity) {
        Integer stopDay = settingsService.getStopDay();
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        
        if (currentDate.getDayOfMonth() > stopDay) {
            throw new IllegalArgumentException("Запрещено изменять данные после " + stopDay + "-го числа месяца");
        }

        WorkResult workResult = workResultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WorkResult not found with id: " + id));
        workResult.setQuantity(quantity);
        return workResultRepository.save(workResult);
    }

    public boolean deleteWorkResult(Long id) {
        Integer stopDay = settingsService.getStopDay();
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        
        if (currentDate.getDayOfMonth() > stopDay) {
            throw new IllegalArgumentException("Запрещено удалять данные после " + stopDay + "-го числа месяца");
        }

        try {
            workResultRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public java.util.Map<Long, Integer> getCompletedWorkByOrderAndWeek(Long orderId, Integer year, Integer month, Integer week) {
        List<WorkResult> workResults = workResultRepository.findByOrderIdAndYearAndMonthAndWeek(orderId, year, month, week);
        
        return workResults.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                wr -> wr.getModelList().getId(),
                java.util.stream.Collectors.summingInt(WorkResult::getQuantity)
            ));
    }
}
