package chugpuff.chugpuff.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import chugpuff.chugpuff.entity.LocationCode;

import java.util.List;

public interface LocationCodeRepository extends JpaRepository<LocationCode, Long> {
    List<LocationCode> findByRegionName(String regionName);
}
