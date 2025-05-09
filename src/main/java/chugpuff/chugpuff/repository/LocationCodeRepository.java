package chugpuff.chugpuff.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import chugpuff.chugpuff.entity.LocationCode;

import java.util.List;

public interface LocationCodeRepository extends JpaRepository<LocationCode, Long> {

    // regionName으로 검색하는 메소드
    List<LocationCode> findByRegionName(String regionName);

    // locBcd로 검색하는 메소드 추가 (2차 근무지 조회)
    List<LocationCode> findByLocBcd(String locBcd);

}
