package se.inera.certificate.mc2wc.batch.processors;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import se.inera.certificate.mc2wc.message.MigrationMessage;

public class DummyItemWriter implements ItemWriter<MigrationMessage> {

    public DummyItemWriter() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void write(List<? extends MigrationMessage> messages) throws Exception {
        
        // NO-OP
        
    }

}
