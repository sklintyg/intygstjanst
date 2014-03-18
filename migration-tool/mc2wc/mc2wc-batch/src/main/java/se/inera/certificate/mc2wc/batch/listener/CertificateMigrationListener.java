package se.inera.certificate.mc2wc.batch.listener;

import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.SkipListener;
import se.inera.certificate.mc2wc.jpa.model.Certificate;
import se.inera.certificate.mc2wc.message.MigrationMessage;

import java.util.List;

/**
 *
 */
public class CertificateMigrationListener implements ItemReadListener<Certificate>, ItemWriteListener<MigrationMessage>, SkipListener<Object, MigrationMessage> {

    private long readCount = 0;
    private long readError = 0;
    private long writeCount = 0;
    private long writeError = 0;
    private long skipCount = 0;

    public void reset() {
        readCount = 0;
        readError = 0;
        writeCount = 0;
        writeError = 0;
        skipCount = 0;
    }

    @Override
    public void beforeRead() {
    }

    @Override
    public void afterRead(Certificate item) {
        readCount++;
    }

    @Override
    public void onReadError(Exception ex) {
        readError++;
    }

    @Override
    public void beforeWrite(List<? extends MigrationMessage> items) {
    }

    @Override
    public void afterWrite(List<? extends MigrationMessage> items) {
        writeCount += items.size();
    }

    @Override
    public void onWriteError(Exception exception, List<? extends MigrationMessage> items) {
        writeError++;
    }

    @Override
    public void onSkipInRead(Throwable t) {
    }

    @Override
    public void onSkipInWrite(MigrationMessage message, Throwable t) {
        skipCount++;
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        skipCount++;
    }

    public long getReadCount() {
        return readCount;
    }

    public long getReadError() {
        return readError;
    }

    public long getWriteCount() {
        return writeCount;
    }

    public long getWriteError() {
        return writeError;
    }

    public long getSkipCount() {
        return skipCount;
    }
}
