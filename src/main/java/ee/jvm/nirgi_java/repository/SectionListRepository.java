package ee.jvm.nirgi_java.repository;

import ee.jvm.nirgi_java.classes.SectionList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionListRepository extends JpaRepository<SectionList, Long> {
}
