package org.iimsa.aiservice.domain.model;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.iimsa.aiservice.domain.event.AiEvent;
import org.iimsa.common.domain.BaseEntity;

/**
 * AI 애그리거트 루트 - 외부 AI API 요청/응답 이력을 관리합니다. - 수신자(receiver) 정보를 스냅샷으로 보관합니다.
 */
@Getter
@Entity
@Table(name = "p_ai")
@SQLRestriction("deleted_at IS NULL")
@Access(AccessType.FIELD)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiEntity extends BaseEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;

    @Embedded
    private Receiver receiver;

    @Column(columnDefinition = "TEXT")
    private String generatedText;

    private LocalDateTime requestedAt;

    private LocalDateTime responseAt;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String reason;

    public static AiEntity create(Receiver receiver, String prompt) {
        AiEntity entity = new AiEntity();
        entity.receiver = receiver;
        entity.prompt = prompt;
        entity.requestedAt = LocalDateTime.now();
        return entity;
    }

    public void complete(String generatedText, String reason) {
        this.generatedText = generatedText;
        this.reason = reason;
        this.responseAt = LocalDateTime.now();
    }

    public void softDelete(String deletedBy) {
        super.delete(deletedBy);
    }

    public void publishCompleted(AiEvent aiEvent) {
        aiEvent.analysisCompleted(this);
    }

}
