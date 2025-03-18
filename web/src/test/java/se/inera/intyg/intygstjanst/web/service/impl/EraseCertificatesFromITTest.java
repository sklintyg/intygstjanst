package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiverDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.ArendeRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;

@ExtendWith(MockitoExtension.class)
class EraseCertificatesFromITTest {

    @Mock
    CertificateRepository certificateRepository;
    @Mock
    ArendeRepository arendeRepository;
    @Mock
    ApprovedReceiverDao approvedReceiverDao;
    @Mock
    RelationDao relationDao;
    @Mock
    SjukfallCertificateDao sjukfallCertificateDao;
    @Mock
    CertificateDao certificateDao;
    @InjectMocks
    EraseCertificatesFromIT eraseCertificatesFromIT;
    @Captor
    ArgumentCaptor<List<String>> certIdCaptor;
    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final int PAGE = 0;
    private static final int ERASE_SIZE = 4;
    private static final Pageable ERASE_PAGEABLE = PageRequest.of(PAGE, ERASE_SIZE, Sort.by(Direction.ASC, "signedDate", "id"));
    private final List<List<String>> certIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eraseCertificatesFromIT, "eraseCertificatesPageSize", ERASE_SIZE);
    }

    @Nested
    class EraseCertificates {

        @AfterEach
        public void cleanup() {
            certIds.clear();
        }

        @Test
        public void shouldCallForNewCertificateIdsForEachBatch() {
            setupPageMock(true);
            setupEraseMocks(false);

            eraseCertificatesFromIT.eraseCertificates(CARE_PROVIDER_ID);

            verify(certificateRepository, times(3)).findCertificateIdsForCareProvider(CARE_PROVIDER_ID, ERASE_PAGEABLE);
        }

        @Test
        public void shouldEraseApprovedReceiversInBatches() {
            setupPageMock(true);
            setupEraseMocks(false);

            eraseCertificatesFromIT.eraseCertificates(CARE_PROVIDER_ID);

            assertAll(
                () -> verify(approvedReceiverDao, times(3)).eraseApprovedReceivers(certIdCaptor.capture(), eq(CARE_PROVIDER_ID)),
                () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
                () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
                () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
            );
        }

        @Test
        public void shouldEraseRelationsInBatches() {
            setupPageMock(true);
            setupEraseMocks(false);

            eraseCertificatesFromIT.eraseCertificates(CARE_PROVIDER_ID);

            assertAll(
                () -> verify(relationDao, times(3)).eraseCertificateRelations(certIdCaptor.capture(), eq(CARE_PROVIDER_ID)),
                () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
                () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
                () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
            );
        }

        @Test
        public void shouldEraseArendenInBatches() {
            setupPageMock(true);
            setupEraseMocks(false);

            eraseCertificatesFromIT.eraseCertificates(CARE_PROVIDER_ID);

            assertAll(
                () -> verify(arendeRepository, times(3)).eraseArendenByCertificateIds(certIdCaptor.capture()),
                () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
                () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
                () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
            );
        }

        @Test
        public void shouldEraseSjukfallCertificatesInBatches() {
            setupPageMock(true);
            setupEraseMocks(false);

            eraseCertificatesFromIT.eraseCertificates(CARE_PROVIDER_ID);

            assertAll(
                () -> verify(sjukfallCertificateDao, times(3)).eraseCertificates(certIdCaptor.capture(), eq(CARE_PROVIDER_ID)),
                () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
                () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
                () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
            );
        }

        @Test
        public void shouldEraseCertificatesInBatches() {
            setupPageMock(true);
            setupEraseMocks(false);

            eraseCertificatesFromIT.eraseCertificates(CARE_PROVIDER_ID);

            assertAll(
                () -> verify(certificateDao, times(3)).eraseCertificates(certIdCaptor.capture(), eq(CARE_PROVIDER_ID)),
                () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
                () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
                () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
            );
        }

        @Test
        public void shouldRethrowAnyCaughtException() {
            setupPageMock(true);
            setupEraseMocks(true);

            assertThrows(IllegalArgumentException.class, () -> eraseCertificatesFromIT
                .eraseCertificates(CARE_PROVIDER_ID));

            final var exception = assertThrows(IllegalArgumentException.class, () -> eraseCertificatesFromIT
                .eraseCertificates(CARE_PROVIDER_ID));

            assertEquals("TestException", exception.getMessage());
        }
    }

    @Test
    public void shouldNotMakeEraseCallsIfCareProviderWithoutCertificates() {
        setupPageMock(false);

        eraseCertificatesFromIT.eraseCertificates(CARE_PROVIDER_ID);

        assertAll(
            () -> verify(certificateRepository, times(1)).findCertificateIdsForCareProvider(CARE_PROVIDER_ID, ERASE_PAGEABLE),
            () -> verifyNoInteractions(approvedReceiverDao),
            () -> verifyNoInteractions(relationDao),
            () -> verifyNoInteractions(arendeRepository),
            () -> verifyNoInteractions(sjukfallCertificateDao),
            () -> verifyNoMoreInteractions(certificateDao)
        );
    }

    private void setupPageMock(boolean careProviderHasCertificates) {
        if (careProviderHasCertificates) {
            final var page1 = new PageImpl<>(getCertificateIds(4), ERASE_PAGEABLE, 10L);
            final var page2 = new PageImpl<>(getCertificateIds(4), ERASE_PAGEABLE, 6L);
            final var page3 = new PageImpl<>(getCertificateIds(2), ERASE_PAGEABLE, 2L);
            doReturn(page1, page2, page3).when(certificateRepository)
                .findCertificateIdsForCareProvider(any(String.class), any(Pageable.class));
        } else {
            final var emptyPage = new PageImpl<>(getCertificateIds(0), ERASE_PAGEABLE, 0L);
            doReturn(emptyPage).when(certificateRepository).findCertificateIdsForCareProvider(any(String.class), any(Pageable.class));
        }
    }

    private void setupEraseMocks(boolean withException) {
        doReturn(4, 4, 2).when(arendeRepository).eraseArendenByCertificateIds(any());
        doReturn(4, 4, 2).when(sjukfallCertificateDao).eraseCertificates(any(), any(String.class));

        if (!withException) {
            doReturn(4, 4, 2).when(certificateDao).eraseCertificates(any(), any(String.class));
        } else {
            when(certificateDao.eraseCertificates(any(), any(String.class))).thenReturn(4, 4)
                .thenThrow(new IllegalArgumentException("TestException"));
        }
    }

    private List<String> getCertificateIds(int count) {
        final var certificateIds = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            certificateIds.add(UUID.randomUUID().toString());
        }
        certIds.add(certificateIds);
        return certificateIds;
    }
}