package org.iimsa.notificationservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.iimsa.common.domain.BaseEntity;

@Getter
@Entity
@Table(name = "p_notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id")
    private UUID id;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_slack_id", nullable = false, length = 100)
    private String receiverSlackId;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    public static NotificationEntity create(
            UUID receiverId, String receiverName, String receiverSlackId, String message) {
        NotificationEntity entity = new NotificationEntity();
        entity.receiverId = receiverId;
        entity.receiverName = receiverName;
        entity.receiverSlackId = receiverSlackId;
        entity.message = message;
        return entity;
    }

    public void softDelete(String deletedBy) {
        super.delete(deletedBy);
    }
}
