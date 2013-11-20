package se.inera.certificate.mc2wc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.mc2wc.converter.MigrationMessageConverter;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.MigrationResultType;
import se.inera.certificate.mc2wc.message.QuestionType;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;

public class MigrationServiceImpl implements MigrationService {
    
    @Autowired
    private FragaSvarRepository fragaSvarRepository;
    
    @Autowired
    private MigrationMessageConverter converter;
    
    public MigrationServiceImpl() {

    }

    @Override
    public MigrationResultType processMigrationMessage(MigrationMessage message) {
        
        List<QuestionType> questions = message.getQuestions();
        
        for (QuestionType q : questions) {
            FragaSvar fs = converter.populateFragaSvar(q);
            fragaSvarRepository.save(fs);
        }
        
        return MigrationResultType.OK;    
    }

}
