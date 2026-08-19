package ee.jvm.nirgi_java.dto;

import ee.jvm.nirgi_java.classes.ModelList;
import java.util.List;

public record CombinedOrderDTO(
    Long id,
    String name,
    Integer year,
    Integer month,
    Integer week,
    List<ModelInfo> models,
    boolean isTransferred,
    Long originalOrderId
) {
    public record ModelInfo(
        Long modelListId,
        String modelListName,
        Integer count,
        Integer remainingCount
    ) {
        public ModelInfo(ModelList modelList, Integer count, Integer remainingCount) {
            this(modelList.getId(), modelList.getName(), count, remainingCount);
        }
    }
}
