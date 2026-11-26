package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.ModelList;
import ee.jvm.nirgi_java.classes.SectionList;
import ee.jvm.nirgi_java.classes.Techmap;
import ee.jvm.nirgi_java.repository.ModelRepository;
import ee.jvm.nirgi_java.repository.ModelListRepository;
import ee.jvm.nirgi_java.repository.SectionListRepository;
import ee.jvm.nirgi_java.repository.TechmapRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ModelController {

    private final ModelRepository modelRepository;
    private final ModelListRepository modelListRepository;
    private final TechmapRepository techmapRepository;
    private final SectionListRepository sectionListRepository;

    public ModelController(ModelRepository modelRepository,
                           ModelListRepository modelListRepository,
                           TechmapRepository techmapRepository,
                           SectionListRepository sectionListRepository) {
        this.modelRepository = modelRepository;
        this.modelListRepository = modelListRepository;
        this.techmapRepository = techmapRepository;
        this.sectionListRepository = sectionListRepository;
    }

    @GetMapping("/models")
    public String models(Model model) {
        List<ModelList> modelLists = modelListRepository.findAll();
        List<SectionList> sectionLists = sectionListRepository.findAll();
        List<Techmap> techmaps = techmapRepository.findAll();
        
        model.addAttribute("modelLists", modelLists);
        model.addAttribute("sectionLists", sectionLists);
        model.addAttribute("techmaps", techmaps);
        
        return "models";
    }
}
