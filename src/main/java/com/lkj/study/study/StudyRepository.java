package com.lkj.study.study;

import com.lkj.study.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long> {

    boolean existsByPath(String path);

    /**
     * FETCH : entity graph 에 명시한 attribute -> EAGER, 나머지 attribute -> LAZY 로 fetch
     * LOAD : entity graph 에 명시한 attribute -> EAGER, 나머지 attribute -> entity 에 명시한 fetch type 이나 디폴트 FetchType 으로 패치
     * (e.g. @OneToMany -> LAZY, @ManyToOne -> EAGER 등이 디폴트이다.)
     */
    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    @EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsByPath(String path);

    @EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithZonesByPath(String path);

    @EntityGraph(value = "Study.withManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithManagersByPath(String path);

    @EntityGraph(value = "Study.withMembers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithMembersByPath(String path);

    Study findStudyOnlyByPath(String path);
}
