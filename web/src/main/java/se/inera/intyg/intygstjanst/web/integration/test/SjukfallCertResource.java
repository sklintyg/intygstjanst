/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.web.integration.test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;

/**
 * @author andreaskaltenbach
 */
@RestController
@RequestMapping("/sjukfallcert")
@Profile({"dev", "testability-api"})
public class SjukfallCertResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SjukfallCertResource.class);

    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    public void setTxManager(@Qualifier("transactionManager") PlatformTransactionManager txManager) {
        this.transactionTemplate = new TransactionTemplate(txManager);
    }

    @GetMapping("/{id}")
    public SjukfallCertificate getSjukfallCertificate(@PathVariable("id") String id) {
        return entityManager.find(SjukfallCertificate.class, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSjukfallCertificate(@PathVariable("id") final String id) {
        return transactionTemplate.execute(new TransactionCallback<ResponseEntity<?>>() {
            @Override
            public ResponseEntity<?> doInTransaction(TransactionStatus status) {
                try {
                    SjukfallCertificate cert = entityManager.find(SjukfallCertificate.class, id);
                    if (cert != null) {
                        entityManager.remove(cert);
                    }
                    return ResponseEntity.ok().build();
                } catch (Exception e) {
                    status.setRollbackOnly();
                    LOGGER.warn("deleted sjukfall certificate with id {} failed: {}", id, e);
                    return ResponseEntity.internalServerError().build();
                }
            }
        });
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteAllSjukfallCertificates() {
        return transactionTemplate.execute(new TransactionCallback<ResponseEntity<?>>() {
            @Override
            public ResponseEntity<?> doInTransaction(TransactionStatus status) {
                try {
                    @SuppressWarnings("unchecked")
                    List<SjukfallCertificate> certificates = entityManager.createQuery("SELECT sc FROM SjukfallCertificate sc")
                        .getResultList();
                    for (SjukfallCertificate sjukfallCert : certificates) {
                        entityManager.remove(sjukfallCert);
                    }
                    return ResponseEntity.ok().build();
                } catch (Exception e) {
                    status.setRollbackOnly();
                    LOGGER.warn("delete all sjukfall certificates failed: {}", e);
                    return ResponseEntity.internalServerError().build();
                }
            }
        });
    }
}
