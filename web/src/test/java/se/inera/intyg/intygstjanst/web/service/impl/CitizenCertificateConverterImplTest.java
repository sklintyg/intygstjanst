package se.inera.intyg.intygstjanst.web.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRelationConverter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateConverterImplTest {

    @Mock
    CitizenCertificateRecipientConverter citizenCertificateRecipientConverter;

    @Mock
    CitizenCertificateRelationConverter citizenCertificateRelationConverter;

    @InjectMocks
    CitizenCertificateConverterImpl citizenCertificateConverter;

    private static final String CERTIFICATE_ID = "ID";
    private static Certificate certificate;
    private static List<Relation> relations = Collections.emptyList();


    @BeforeEach
    void setup() {
        certificate = mock(Certificate.class);
        Mockito.when(certificate.getId()).thenReturn(CERTIFICATE_ID);
        Mockito.when(certificate.getSignedDate()).thenReturn(LocalDateTime.now());
    }

    @Nested
    class ConversionOfValues {
        @Test
        void shouldConvertId() {
            final var response = citizenCertificateConverter.get(certificate, relations);

            assertEquals(CERTIFICATE_ID, response.getId());
        }
    }
}