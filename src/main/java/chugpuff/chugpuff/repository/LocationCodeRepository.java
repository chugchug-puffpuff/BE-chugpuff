package chugpuff.chugpuff.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import chugpuff.chugpuff.entity.LocationCode;

public interface LocationCodeRepository extends JpaRepository<LocationCode, Long> {
    LocationCode findByRegionName(String regionName);
}
