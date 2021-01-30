package com.lkj.study.modules.study;

import com.lkj.study.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryExtension {

    boolean existsByPath(String path);

    /**
     * FETCH : entity graph 에 명시한 attribute -> EAGER, 나머지 attribute -> LAZY 로 fetch
     * LOAD : entity graph 에 명시한 attribute -> EAGER, 나머지 attribute -> entity 에 명시한 fetch type 이나 디폴트 FetchType 으로 패치
     * (e.g. @OneToMany -> LAZY, @ManyToOne -> EAGER 등이 디폴트이다.)
     */
    //@EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    @EntityGraph(attributePaths = {"tags", "zones", "managers", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    //@EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    @EntityGraph(attributePaths = {"tags", "managers"})
    Study findStudyWithTagsByPath(String path); //default -> EntityGraph.EntityGraphType.FETCH

    //@EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    @EntityGraph(attributePaths = {"zones", "managers"})
    Study findStudyWithZonesByPath(String path);

    //@EntityGraph(value = "Study.withManagers", type = EntityGraph.EntityGraphType.FETCH)
    @EntityGraph(attributePaths = "managers")
    Study findStudyWithManagersByPath(String path);

    //@EntityGraph(value = "Study.withMembers", type = EntityGraph.EntityGraphType.FETCH)
    @EntityGraph(attributePaths = "members")
    Study findStudyWithMembersByPath(String path);

    Study findStudyOnlyByPath(String path);

    //@EntityGraph(value = "Study.withTagsAndZones", type = EntityGraph.EntityGraphType.FETCH)
    @EntityGraph(attributePaths = {"zones", "tags"})
    Study findStudyWithTagsAndZonesById(Long id);

    @EntityGraph(attributePaths = {"members", "managers"})
    Study findStudyWithManagersAndMemebersById(Long id);

    @EntityGraph(attributePaths = {"zones", "tags"})
    List<Study> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    List<Study> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Study> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}
