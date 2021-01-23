package com.lkj.study.modules.account;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * jdbcTokenRepositoryImpl 이 사용하는 table 이 존재해야함
 * jpa 를 사용하고 있고 현재 In memory db 를 사용하기 때문에 entity 정보를 보고 테이블을 만들어줌
 * 따라서 jdbcTokenRepositoryImpl 이 사용하는 table 스키마에 해당하는 테이블이 생성될 수 있도록 entity 추가
 */
@Table(name = "persistent_logins")
@Entity
@Getter @Setter
public class PersistentLogins {

    @Id
    @Column(length = 64)
    private String series;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 64)
    private String token;

    @Column(name = "last_used", nullable = false, length = 64)
    private LocalDateTime lastUsed;
}
