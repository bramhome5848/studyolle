package com.lkj.study.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")   //해시 코드 생성시 id만 사용
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tag {

    @Id @GeneratedValue
    private Long id;

    private String title;
}
