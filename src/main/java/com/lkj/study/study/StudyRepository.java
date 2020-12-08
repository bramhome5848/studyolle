package com.lkj.study.study;

import com.lkj.study.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long> {
    boolean existsByPath(String path);
}
