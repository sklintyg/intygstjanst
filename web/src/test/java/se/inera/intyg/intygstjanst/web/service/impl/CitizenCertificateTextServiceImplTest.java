package se.inera.intyg.intygstjanst.web.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateTextServiceImplTest {
    private static final String TYPE_NAME = "TYPE_NAME";
    private static final String ADDITIONAL_INFO_LABEL = "Avser";


    @Mock
    IntygModuleRegistry intygModuleRegistry;

    @InjectMocks
    CitizenCertificateTextServiceImpl citizenCertificateTextService;

    @Nested
    class TypeName {
        private final ModuleEntryPoint entryPoint = mock(ModuleEntryPoint.class);

        @BeforeEach
        void setup() throws ModuleNotFoundException {
            Mockito.when(intygModuleRegistry.getModuleEntryPoint(any())).thenReturn(entryPoint);
            Mockito.when(entryPoint.getModuleName()).thenReturn(TYPE_NAME);
        }

        @Test
        void shouldReturnValue() throws ModuleNotFoundException {
            final var result = citizenCertificateTextService.getTypeName("id");

            assertEquals(TYPE_NAME, result);
        }
    }

    @Nested
    class AdditionalInfoLabel {
        private final ModuleApi moduleApi = mock(ModuleApi.class);

        @BeforeEach
        void setup() throws ModuleNotFoundException {
            Mockito.when(intygModuleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
            //Mockito.when(moduleApi.getAdditionalInfoLabel()).thenReturn(ADDITIONAL_INFO_LABEL);
        }

        @Test
        void shouldReturnValue() throws ModuleNotFoundException {
            final var result = citizenCertificateTextService.getAdditionalInfoLabel("id", "version");

            assertEquals(ADDITIONAL_INFO_LABEL, result);
        }
    }
}