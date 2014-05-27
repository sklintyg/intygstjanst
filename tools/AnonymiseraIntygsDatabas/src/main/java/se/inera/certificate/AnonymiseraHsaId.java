package se.inera.certificate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class AnonymiseraHsaId {

    private final Random random = new Random();
    private final Map<String, String> actualToAnonymized = new HashMap<>();
    private final Set<String> anonymizedSet = new HashSet<>();

    public String anonymisera(String hsaId) {
        String anonymized = actualToAnonymized.get(hsaId);
        if (anonymized == null) {
            anonymized = getUniqueRandomHsaId(hsaId);
        }
        return anonymized;
    }

    private String getUniqueRandomHsaId(String hsaId) {
        String anonymized;
        do {
            anonymized = newRandomHsaId();
        } while (anonymizedSet.contains(anonymized) || hsaId == anonymized);
        anonymizedSet.add(anonymized);
        actualToAnonymized.put(hsaId, anonymized);
        return anonymized;
    }

    // CHECKSTYLE:OFF MagicNumber
    private String newRandomHsaId() {
        int number = random.nextInt(1000000000);
        return "SE" + number;
    }
    // CHECKSTYLE:ON MagicNumber

}
