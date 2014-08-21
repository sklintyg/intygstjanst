package se.inera.certificate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

public class AnonymiseraPersonId {

    private static final String PERSON_NUMBER_WITHOUT_DASH_REGEX = "[0-9]{12}";

    Set<String> testPersonNr = new HashSet<>();
    List<String> kvarvarandeTestPersonNr = new LinkedList<>();
    private final Random random = new Random();
    private final Map<String, String> actualToAnonymized = new HashMap<>();

    public AnonymiseraPersonId() {
        BufferedReader pnr = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/pnr.txt")));
        try {
            String line = null;
            while ((line = pnr.readLine()) != null) {
                testPersonNr.add(normalisera(line));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        kvarvarandeTestPersonNr.addAll(testPersonNr);
    }
    
    public String anonymisera(String patientId) {
        String anonymized = actualToAnonymized.get(patientId);
        if (anonymized == null) {
            anonymized = getUniqueRandomPersonid(patientId);
        }
        return anonymized;
    }

    String normalisera(String personnr) {
        if (Pattern.matches(PERSON_NUMBER_WITHOUT_DASH_REGEX, personnr)) {
            return personnr.substring(0, 8) + "-" + personnr.substring(8);
        } else {
            return personnr;
        }
    }
    
    private String getUniqueRandomPersonid(String nummer) {
        if (kvarvarandeTestPersonNr.isEmpty()) {
            System.out.println("Återanvänder personnr");
            kvarvarandeTestPersonNr.addAll(testPersonNr);
        }
        int index = getRandomIndex();
        String anonymized = kvarvarandeTestPersonNr.remove(index);
        actualToAnonymized.put(nummer, anonymized);
        return anonymized;
    }

    protected int getRandomIndex() {
        return random.nextInt(kvarvarandeTestPersonNr.size());
    }
}
