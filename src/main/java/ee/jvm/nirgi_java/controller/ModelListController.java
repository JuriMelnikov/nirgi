package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.ModelList;
import ee.jvm.nirgi_java.repository.ModelListRepository;
import ee.jvm.nirgi_java.repository.TechmapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/model-list")
@CrossOrigin(origins = "*")
public class ModelListController {

    @Autowired
    private ModelListRepository modelListRepository;

    @Autowired
    private TechmapRepository techmapRepository;

    @GetMapping
    public List<ModelList> getAllModelLists() {
        return modelListRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModelList> getModelListById(@PathVariable Long id) {
        return modelListRepository.findById(id)
                .map(modelList -> ResponseEntity.ok().body(modelList))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createModelList(@RequestBody ModelList modelList) {
        try {
            ModelList savedModelList = modelListRepository.save(modelList);
            return ResponseEntity.ok(savedModelList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating model list: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModelList> updateModelList(@PathVariable Long id, @RequestBody ModelList modelListDetails) {
        return modelListRepository.findById(id)
                .map(modelList -> {
                    modelList.setName(modelListDetails.getName());
                    ModelList updatedModelList = modelListRepository.save(modelList);
                    return ResponseEntity.ok().body(updatedModelList);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteModelList(@PathVariable Long id) {
        return modelListRepository.findById(id)
                .map(modelList -> {
                    long techmapCount = techmapRepository.countByModelListId(id);
                    if (techmapCount > 0) {
                        return ResponseEntity.badRequest().body("Невозможно удалить модель. Существуют связанные технологические карты (" + techmapCount + " шт.). Сначала удалите их.");
                    }
                    modelListRepository.delete(modelList);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
