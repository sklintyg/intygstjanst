package se.inera.intyg.intygstjanst.logging;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.MDC;

public class MdcCloseableMap implements Closeable {
    private final Set<String> keys;

    private MdcCloseableMap(Map<String, String> entries) {
        this.keys = Collections.unmodifiableSet(entries.keySet());
        entries.forEach(MDC::put);
    }

    @Override
    public void close() {
        keys.forEach(MDC::remove);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> mdc = new ConcurrentHashMap<>();

        public Builder put(String key, String value) {
            mdc.put(key, value);
            return this;
        }

        public MdcCloseableMap build() {
            return new MdcCloseableMap(mdc);
        }
    }
}
