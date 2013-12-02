package se.inera.certificate.mc2wc.batch.processors;

import org.springframework.batch.item.ItemProcessor;

import se.inera.certificate.mc2wc.message.MigrationMessage;

public class MigrationMessageSenderProcessor implements ItemProcessor<MigrationMessage, MigrationMessage> {

    public MigrationMessageSenderProcessor() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public MigrationMessage process(MigrationMessage message) throws Exception {

        return null;
    }

}
