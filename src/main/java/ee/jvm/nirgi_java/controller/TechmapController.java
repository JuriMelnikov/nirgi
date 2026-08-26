package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.Techmap;
import ee.jvm.nirgi_java.repository.TechmapRepository;
import ee.jvm.nirgi_java.repository.ModelListRepository;
import ee.jvm.nirgi_java.repository.SectionListRepository;
import ee.jvm.nirgi_java.repository.WorkResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/techmaps")
@CrossOrigin(origins = "*")
public class TechmapController {

    @Autowired
    private TechmapRepository techmapRepository;

    @Autowired
    private ModelListRepository modelListRepository;

    @Autowired
    private SectionListRepository sectionListRepository;

    @Autowired
    private WorkResultRepository workResultRepository;

    @GetMapping
    public List<Techmap> getAllTechmaps() {
        return techmapRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Techmap> getTechmapById(@PathVariable Long id) {
        return techmapRepository.findById(id)
                .map(techmap -> ResponseEntity.ok().body(techmap))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTechmap(@RequestBody Techmap techmap) {
        try {
            // Validate and set ModelList
            if (techmap.getModelList() != null && techmap.getModelList().getId() != null) {
                techmap.setModelList(modelListRepository.findById(techmap.getModelList().getId()).orElse(null));
            }
            
            // Validate and set SectionList
            if (techmap.getSectionList() != null && techmap.getSectionList().getId() != null) {
                techmap.setSectionList(sectionListRepository.findById(techmap.getSectionList().getId()).orElse(null));
            }
            
            Techmap savedTechmap = techmapRepository.save(techmap);
            return ResponseEntity.ok(savedTechmap);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating techmap: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Techmap> updateTechmap(@PathVariable Long id, @RequestBody Techmap techmapDetails) {
        return techmapRepository.findById(id)
                .map(techmap -> {
                    techmap.setSerial(techmapDetails.getSerial());
                    techmap.setDescriptor(techmapDetails.getDescriptor());
                    techmap.setTime(techmapDetails.getTime());
                    techmap.setPrice(techmapDetails.getPrice());
                    
                    // Update ModelList
                    if (techmapDetails.getModelList() != null && techmapDetails.getModelList().getId() != null) {
                        techmap.setModelList(modelListRepository.findById(techmapDetails.getModelList().getId()).orElse(null));
                    }
                    
                    // Update SectionList
                    if (techmapDetails.getSectionList() != null && techmapDetails.getSectionList().getId() != null) {
                        techmap.setSectionList(sectionListRepository.findById(techmapDetails.getSectionList().getId()).orElse(null));
                    }
                    
                    Techmap updatedTechmap = techmapRepository.save(techmap);
                    return ResponseEntity.ok().body(updatedTechmap);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTechmap(@PathVariable Long id) {
        return techmapRepository.findById(id)
                .map(techmap -> {
                    long workResultCount = workResultRepository.countByTechmapId(id);
                    if (workResultCount > 0) {
                        return ResponseEntity.badRequest()
                                .body("Невозможно удалить технологическую карту. Существуют связанные выполненные работы (" + workResultCount + " шт.).");
                    }
                    try {
                        techmapRepository.delete(techmap);
                        return ResponseEntity.ok().build();
                    } catch (Exception e) {
                        return ResponseEntity.badRequest()
                                .body("Невозможно удалить технологическую карту. Она используется в системе.");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
