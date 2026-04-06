package org.iimsa.aiservice.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.iimsa.aiservice.domain.model.QAiEntity;
import org.iimsa.aiservice.domain.repository.AiRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * AI 레포지토리 구현체 (Port Adapter)
 * - QueryDSL 기반 동적 쿼리
 */
@Repository
@RequiredArgsConstructor
public class AiRepositoryImpl implements AiRepository {

    private final JpaAiRepository jpaAiRepository;
    private final JPAQueryFactory queryFactory;

    private static final QAiEntity ai = QAiEntity.aiEntity;

    @Override
    public AiEntity save(AiEntity aiEntity) {
        return jpaAiRepository.save(aiEntity);
    }

    @Override
    public Optional<AiEntity> findById(UUID id) {
        return jpaAiRepository.findById(id);
    }

    @Override
    public Page<AiEntity> findByReceiverId(UUID receiverId, Pageable pageable) {
        List<AiEntity> content = queryFactory
                .selectFrom(ai)
                .where(ai.receiver.id.eq(receiverId))
                .orderBy(ai.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(ai.count())
                .from(ai)
                .where(ai.receiver.id.eq(receiverId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<AiEntity> findAll(Pageable pageable) {
        List<AiEntity> content = queryFactory
                .selectFrom(ai)
                .orderBy(ai.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(ai.count())
                .from(ai)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}