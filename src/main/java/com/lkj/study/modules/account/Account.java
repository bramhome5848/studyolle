package com.lkj.study.modules.account;

import com.lkj.study.modules.study.Study;
import com.lkj.study.modules.tag.Tag;
import com.lkj.study.modules.zone.Zone;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
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

    /**
     * @Lob -> Large OBject
     * - 문자열 길이를 지정하지 않으면 default -> varchar(255)
     * 사진을 저장하는 경우 더 많은 자리수를 요구하기 때문에 Lob 사용
     * @Basic -> Optional Annotation 으로 Entity 의 필드에 대한 설정 가능
     * optional = false -> not null, default -> true
     * fetch = FetchType.lazy -> lazy operation, default -> FetchType.eager
     * content 의 크기가 크다면 고려해볼 수 있음
     *
     * @Basic vs @Column
     * @Basic
     * - JPA entity 에 적용
     * - optional = false -> JPA entity filed 의 null 허용 여부를 결정
     * - lazy, eager fetch 적용
     *
     * @Column
     * - Database column 에 적용
     * - Database 의 column 이 null 을 허용하면 @Column 의 filed 역시 허용
     * - Database 의 어떤 column 과 매핑 되는지 명시 가능
     */
    //@Lob -> Large OBject -> 문자열 길이를 지정하지 않으면 default -> varchar(255)
    //사진을 저장하는 경우 더 많은 자리수를 요구함 -> Lob 사용
    //@Basic -> optional annotation
    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb = true;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb = true;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;

    @ManyToMany
    private Set<Tag> tags =  new HashSet<>();  //단방향, ManyToMany -> 조인 테이브을 사용해 다대다 관계 표현(중간 테이블)

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

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
