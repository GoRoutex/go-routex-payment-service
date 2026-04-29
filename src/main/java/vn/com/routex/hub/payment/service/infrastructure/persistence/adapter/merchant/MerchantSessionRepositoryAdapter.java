package vn.com.routex.hub.payment.service.infrastructure.persistence.adapter.merchant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.com.routex.hub.payment.service.domain.merchant.MerchantSessionStatus;
import vn.com.routex.hub.payment.service.domain.merchant.model.MerchantSessionAggregate;
import vn.com.routex.hub.payment.service.domain.merchant.port.MerchantSessionRepositoryPort;
import vn.com.routex.hub.payment.service.infrastructure.persistence.jpa.merchant.repository.MerchantSessionEntityRepository;

import java.util.Optional;


@Component
@RequiredArgsConstructor
public class MerchantSessionRepositoryAdapter implements MerchantSessionRepositoryPort {

    private final MerchantSessionEntityRepository merchantSessionEntityRepository;
    private final MerchantSessionPersistenceMapper merchantSessionPersistenceMapper;
    @Override
    public Optional<MerchantSessionAggregate> findLatestByPaymentIdAndStatus(String paymentId, MerchantSessionStatus status) {
        return merchantSessionEntityRepository.findLatestByPaymentIdAndStatus(paymentId, status)
                .map(merchantSessionPersistenceMapper::toDomain);
    }

    @Override
    public int countByPaymentId(String id) {
        return merchantSessionEntityRepository.countByPaymentId(id);
    }

    @Override
    public void save(MerchantSessionAggregate session) {
        merchantSessionEntityRepository.save(merchantSessionPersistenceMapper.toEntity(session));
    }
}
