package org.iimsa.aiservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * 수신자 Value Object
 * - receiverId, receiverSlackId, receiverName 을 하나의 개념으로 묶습니다.
 * - @Embeddable 이므로 p_ai 테이블 컬럼 구조는 그대로 유지됩니다.
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Receiver {

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "receiver_id", nullable = false)
    private UUID id;

    @Column(name = "receiver_slack_id", length = 100)
    private String slackId;

    @Column(name = "receiver_name", length = 50)
    private String name;
}
