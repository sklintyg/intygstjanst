package se.inera.intyg.intygstjanst.web.service;

public interface CitizenCertificateTextService {
    String getTypeName(String typeId);

    String getAdditionalInfoLabel(String typeId, String typeVersion);
}
