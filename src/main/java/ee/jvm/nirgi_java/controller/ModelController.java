package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.ModelList;
import ee.jvm.nirgi_java.classes.SectionList;
import ee.jvm.nirgi_java.classes.Techmap;
import ee.jvm.nirgi_java.repository.ModelRepository;
import ee.jvm.nirgi_java.repository.ModelListRepository;
import ee.jvm.nirgi_java.repository.SectionListRepository;
import ee.jvm.nirgi_java.repository.TechmapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ModelController {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelListRepository modelListRepository;

    @Autowired
    private TechmapRepository techmapRepository;

    @Autowired
    private SectionListRepository sectionListRepository;

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
