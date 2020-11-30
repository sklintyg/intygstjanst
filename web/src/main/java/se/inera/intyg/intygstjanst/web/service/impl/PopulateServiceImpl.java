package se.inera.intyg.intygstjanst.web.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.exception.PersistenceException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateMetaData;
import se.inera.intyg.intygstjanst.web.service.PopulateService;

@Service
public class PopulateServiceImpl implements PopulateService {

    // Declare all jobs in enum
    public enum JobNames { METADATA }

    // List all jobs that should be active
    private static final List<JobNames> activeJobs = new ArrayList<>() {
        {
            add(JobNames.METADATA);
        }
    };

    @Autowired
    CertificateDao certificateDao;

    @Autowired
    private IntygModuleRegistryImpl moduleRegistry;

    // Add case for loading of ids to be added to queue
    @Override
    public Map<String, List<String>> getListsOfIdsToProcess(Integer batchSize) {
        var jobLists = new HashMap<String, List<String>>();
        var adjustedBatchSize = batchSize / activeJobs.size();

        activeJobs.forEach(jobName -> {
            switch (jobName) {
                case METADATA:
                    var idList = loadCertificateMetaDataIdsToProcess(adjustedBatchSize);
                    jobLists.put(jobName.name(), idList);
                    break;
                default:
                    throw new RuntimeException("Job name not recognized!");
            }
        });

        return jobLists;
    }

    // Add case for processing of ids for new jobs.
    // processing methods should throw exception if faulty! Success is otherwise assumed.
    @Override
    public void processId(String jobName, String id) {
        switch (JobNames.valueOf(jobName)) {
            case METADATA:
                processCertificateMetadataId(id);
                break;
            default:
                throw new RuntimeException("Job name not recognized!");
        }
    }

    private List<String> loadCertificateMetaDataIdsToProcess(int adjustedBatchSize) {
        return certificateDao.findCertificatesWithoutMetadata(adjustedBatchSize);
    }

    private void processCertificateMetadataId(String id) {
        try {
            var certificate = certificateDao.getCertificate(null, id);
            CertificateMetaData certificateMetaData = new CertificateMetaData();
            certificateMetaData.setCertificate(certificate);
            certificateMetaData.setCertificateId(certificate.getId());
            certificateMetaData.setDoctorName(certificate.getSigningDoctorName());
            certificateMetaData.setRevoked(certificate.isRevoked());
            certificateMetaData.setDoctorId(getDoctorId(certificate));
            certificateDao.storeCertificateMetadata(certificateMetaData);
        } catch (PersistenceException | ModuleException | ModuleNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String getDoctorId(Certificate certificate) throws ModuleException, ModuleNotFoundException {
        ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
        var utlatandeFromXml = moduleApi.getUtlatandeFromXml(certificate.getOriginalCertificate().getDocument());
        return utlatandeFromXml.getGrundData().getSkapadAv().getPersonId();
    }
}
