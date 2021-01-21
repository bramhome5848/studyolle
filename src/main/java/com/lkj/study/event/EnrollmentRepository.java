package com.lkj.study.event;

import com.lkj.study.domain.Account;
import com.lkj.study.domain.Enrollment;
import com.lkj.study.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);
}
