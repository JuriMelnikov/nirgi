package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.SectionList;
import ee.jvm.nirgi_java.repository.SectionListRepository;
import ee.jvm.nirgi_java.repository.TechmapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/section-lists")
@CrossOrigin(origins = "*")
public class SectionListController {

    @Autowired
    private SectionListRepository sectionListRepository;

    @Autowired
    private TechmapRepository techmapRepository;

    @GetMapping
    public List<SectionList> getAllSectionLists() {
        return sectionListRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionList> getSectionListById(@PathVariable Long id) {
        return sectionListRepository.findById(id)
                .map(sectionList -> ResponseEntity.ok().body(sectionList))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createSectionList(@RequestBody SectionList sectionList) {
        try {
            SectionList savedSectionList = sectionListRepository.save(sectionList);
            return ResponseEntity.ok(savedSectionList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating section list: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SectionList> updateSectionList(@PathVariable Long id, @RequestBody SectionList sectionListDetails) {
        return sectionListRepository.findById(id)
                .map(sectionList -> {
                    sectionList.setName(sectionListDetails.getName());
                    SectionList updatedSectionList = sectionListRepository.save(sectionList);
                    return ResponseEntity.ok().body(updatedSectionList);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSectionList(@PathVariable Long id) {
        return sectionListRepository.findById(id)
                .map(sectionList -> {
                    long techmapCount = techmapRepository.countBySectionListId(id);
                    if (techmapCount > 0) {
                        return ResponseEntity.badRequest().body("Невозможно удалить раздел. Существуют связанные технологические карты (" + techmapCount + " шт.). Сначала удалите их.");
                    }
                    sectionListRepository.delete(sectionList);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
