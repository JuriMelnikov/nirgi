package ee.jvm.nirgi_java.repository;

import ee.jvm.nirgi_java.classes.Techmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechmapRepository extends JpaRepository<Techmap, Long> {
    long countByModelListId(Long modelListId);
    long countBySectionListId(Long sectionListId);
}
