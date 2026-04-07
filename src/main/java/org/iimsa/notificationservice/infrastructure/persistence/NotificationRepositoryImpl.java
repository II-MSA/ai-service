package org.iimsa.notificationservice.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.iimsa.notificationservice.domain.model.NotificationEntity;
import org.iimsa.notificationservice.domain.model.QNotificationEntity;
import org.iimsa.notificationservice.domain.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    private static final QNotificationEntity notification = QNotificationEntity.notificationEntity;

    @Override
    public NotificationEntity save(NotificationEntity notificationEntity) {
        return jpaRepository.save(notificationEntity);
    }

    @Override
    public Optional<NotificationEntity> findById(UUID notificationId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(notification)
                        .where(
                                notification.id.eq(notificationId),
                                notification.deletedAt.isNull()
                        )
                        .fetchOne()
        );
    }

    @Override
    public Page<NotificationEntity> findByReceiverId(UUID receiverId, Pageable pageable) {
        List<NotificationEntity> content = queryFactory
                .selectFrom(notification)
                .where(
                        notification.receiverId.eq(receiverId),
                        notification.deletedAt.isNull()
                )
                .orderBy(notification.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(notification.count())
                        .from(notification)
                        .where(
                                notification.receiverId.eq(receiverId),
                                notification.deletedAt.isNull()
                        )
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<NotificationEntity> findAll(Pageable pageable) {
        List<NotificationEntity> content = queryFactory
                .selectFrom(notification)
                .where(notification.deletedAt.isNull())
                .orderBy(notification.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(notification.count())
                        .from(notification)
                        .where(notification.deletedAt.isNull())
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }
}
