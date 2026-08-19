package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.ModelList;
import ee.jvm.nirgi_java.classes.SectionList;
import ee.jvm.nirgi_java.classes.Techmap;
import ee.jvm.nirgi_java.repository.ModelListRepository;
import ee.jvm.nirgi_java.repository.SectionListRepository;
import ee.jvm.nirgi_java.repository.TechmapRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechmapControllerTest {

    @Mock
    private TechmapRepository techmapRepository;

    @Mock
    private ModelListRepository modelListRepository;

    @Mock
    private SectionListRepository sectionListRepository;

    @InjectMocks
    private TechmapController techmapController;

    private static ModelList modelList(Long id) {
        ModelList modelList = new ModelList();
        modelList.setId(id);
        modelList.setName("Model " + id);
        return modelList;
    }

    private static SectionList sectionList(Long id) {
        SectionList sectionList = new SectionList();
        sectionList.setId(id);
        sectionList.setName("Section " + id);
        return sectionList;
    }

    private static Techmap techmap(Long id, ModelList modelList, SectionList sectionList) {
        return new Techmap(id, "S-" + id, "Descriptor " + id, modelList, "10", "5.00", sectionList);
    }

    @Test
    void listAndGetByIdDelegateToRepository() {
        Techmap techmap = techmap(1L, modelList(2L), sectionList(3L));
        when(techmapRepository.findAll()).thenReturn(List.of(techmap));
        when(techmapRepository.findById(1L)).thenReturn(Optional.of(techmap));
        when(techmapRepository.findById(404L)).thenReturn(Optional.empty());

        assertThat(techmapController.getAllTechmaps()).containsExactly(techmap);
        assertThat(techmapController.getTechmapById(1L).getBody()).isSameAs(techmap);
        assertThat(techmapController.getTechmapById(404L).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void createTechmapResolvesReferencedEntities() {
        ModelList storedModelList = modelList(2L);
        SectionList storedSectionList = sectionList(3L);
        Techmap request = techmap(null, modelList(2L), sectionList(3L));
        when(modelListRepository.findById(2L)).thenReturn(Optional.of(storedModelList));
        when(sectionListRepository.findById(3L)).thenReturn(Optional.of(storedSectionList));
        when(techmapRepository.save(request)).thenReturn(request);

        var response = techmapController.createTechmap(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(request.getModelList()).isSameAs(storedModelList);
        assertThat(request.getSectionList()).isSameAs(storedSectionList);
    }

    @Test
    void createTechmapNullsOutUnknownReferences() {
        Techmap request = techmap(null, modelList(2L), sectionList(3L));
        when(modelListRepository.findById(2L)).thenReturn(Optional.empty());
        when(sectionListRepository.findById(3L)).thenReturn(Optional.empty());
        when(techmapRepository.save(request)).thenReturn(request);

        techmapController.createTechmap(request);

        assertThat(request.getModelList()).isNull();
        assertThat(request.getSectionList()).isNull();
    }

    @Test
    void createTechmapReportsPersistenceFailure() {
        Techmap request = techmap(null, null, null);
        when(techmapRepository.save(request)).thenThrow(new RuntimeException("constraint violation"));

        var response = techmapController.createTechmap(request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Error creating techmap: constraint violation");
    }

    @Test
    void updateTechmapReplacesScalarFieldsAndReferences() {
        Techmap stored = techmap(1L, modelList(2L), sectionList(3L));
        ModelList newModelList = modelList(20L);
        SectionList newSectionList = sectionList(30L);
        Techmap details = techmap(1L, modelList(20L), sectionList(30L));
        details.setSerial("S-new");
        details.setDescriptor("New descriptor");
        details.setTime("15");
        details.setPrice("7.50");
        when(techmapRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(modelListRepository.findById(20L)).thenReturn(Optional.of(newModelList));
        when(sectionListRepository.findById(30L)).thenReturn(Optional.of(newSectionList));
        when(techmapRepository.save(stored)).thenReturn(stored);

        var response = techmapController.updateTechmap(1L, details);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(stored.getSerial()).isEqualTo("S-new");
        assertThat(stored.getDescriptor()).isEqualTo("New descriptor");
        assertThat(stored.getTime()).isEqualTo("15");
        assertThat(stored.getPrice()).isEqualTo("7.50");
        assertThat(stored.getModelList()).isSameAs(newModelList);
        assertThat(stored.getSectionList()).isSameAs(newSectionList);
    }

    @Test
    void updateTechmapKeepsReferencesWhenNoneAreSupplied() {
        ModelList existingModelList = modelList(2L);
        SectionList existingSectionList = sectionList(3L);
        Techmap stored = techmap(1L, existingModelList, existingSectionList);
        Techmap details = techmap(1L, null, null);
        when(techmapRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(techmapRepository.save(stored)).thenReturn(stored);

        techmapController.updateTechmap(1L, details);

        assertThat(stored.getModelList()).isSameAs(existingModelList);
        assertThat(stored.getSectionList()).isSameAs(existingSectionList);
    }

    @Test
    void updateAndDeleteReportNotFoundForUnknownId() {
        when(techmapRepository.findById(404L)).thenReturn(Optional.empty());

        assertThat(techmapController.updateTechmap(404L, techmap(404L, null, null)).getStatusCode().value())
                .isEqualTo(404);
        assertThat(techmapController.deleteTechmap(404L).getStatusCode().value()).isEqualTo(404);
        verify(techmapRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void deleteTechmapRemovesExistingRecord() {
        Techmap stored = techmap(1L, null, null);
        when(techmapRepository.findById(1L)).thenReturn(Optional.of(stored));

        assertThat(techmapController.deleteTechmap(1L).getStatusCode().value()).isEqualTo(200);
        verify(techmapRepository).delete(stored);
    }
}
