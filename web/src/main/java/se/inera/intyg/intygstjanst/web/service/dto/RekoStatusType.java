package se.inera.intyg.intygstjanst.web.service.dto;

public enum RekoStatusType {

    REKO_1("Ingen"),
    REKO_2("Kontaktad"),
    REKO_3("Aktiv"),
    REKO_4("Uppföljning"),
    REKO_5("Avslutad"),
    REKO_6("Avböjt");

    private final String name;
    RekoStatusType(String name) {
        this.name = name;
    }
}
