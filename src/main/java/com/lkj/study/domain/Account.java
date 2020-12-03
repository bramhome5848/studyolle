package com.lkj.study.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")   //해시 코드 생성시 id만 사용
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb = true;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb = true;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;

    @ManyToMany
    private Set<Tag> tags;  //단방향, ManyToMany -> 조인 테이브을 사용해 다대다 관계 표현(중간 테이블)

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSingUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }
}
