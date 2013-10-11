package se.inera.certificate.model;

import java.util.ArrayList;
import java.util.List;

import static se.inera.certificate.model.util.Iterables.find;

import org.joda.time.LocalDateTime;
import org.joda.time.Partial;
import se.inera.certificate.model.util.Predicate;

/**
 * @author andreaskaltenbach
 */
public class Utlatande {

    private Id id;

    private Kod typ;

    private List<String> kommentarer;

    private LocalDateTime signeringsdatum;

    private LocalDateTime skickatdatum;

    private Patient patient;

    private HosPersonal skapadAv;

    private List<Aktivitet> aktiviteter;

    private List<Observation> observationer;

    private List<Vardkontakt> vardkontakter;

    private List<Rekommendation> rekommendationer;

    private List<Referens> referenser;

    /**
     * To which point in time is this certificate considered valid. Modules implementing this model should use their own
     * getters calculating the date suitable for the certificate type and rules.
     */
    private Partial validToDate;

    /**
     * From which point in time is this certificate considered valid. Modules implementing this model should use their
     * own getters calculating the date suitable for the certificate type and rules.
     */
    private Partial validFromDate;

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Kod getTyp() {
        return typ;
    }

    public void setTyp(Kod typ) {
        this.typ = typ;
    }

    public List<String> getKommentarer() {
        if (kommentarer == null) {
            kommentarer = new ArrayList<>();
        }
        return kommentarer;
    }

    public LocalDateTime getSigneringsdatum() {
        return signeringsdatum;
    }

    public void setSigneringsdatum(LocalDateTime signeringsdatum) {
        this.signeringsdatum = signeringsdatum;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public HosPersonal getSkapadAv() {
        return skapadAv;
    }

    public void setSkapadAv(HosPersonal skapadAv) {
        this.skapadAv = skapadAv;
    }

    public List<Aktivitet> getAktiviteter() {
        if (aktiviteter == null) {
            aktiviteter = new ArrayList<>();
        }
        return aktiviteter;
    }

    public List<Observation> getObservationer() {
        if (observationer == null) {
            observationer = new ArrayList<>();
        }
        return observationer;
    }

    public List<Vardkontakt> getVardkontakter() {
        if (vardkontakter == null) {
            vardkontakter = new ArrayList<>();
        }
        return vardkontakter;
    }

    public List<Rekommendation> getRekommendationer() {
        if (rekommendationer == null) {
            rekommendationer = new ArrayList<>();
        }
        return rekommendationer;
    }

    public List<Referens> getReferenser() {
        if (referenser == null) {
            referenser = new ArrayList<>();
        }
        return referenser;
    }

    public LocalDateTime getSkickatdatum() {
        return skickatdatum;
    }

    public void setSkickatdatum(LocalDateTime skickatdatum) {
        this.skickatdatum = skickatdatum;
    }

    public List<Observation> getObservationsByKod(Kod observationsKod) {
        List<Observation> observations = new ArrayList<>();
        for (Observation observation : this.observationer) {
            if (observation.getObservationskod() != null && observation.getObservationskod().equals(observationsKod)) {
                observations.add(observation);
            }
        }
        return observations;
    }

    public List<Observation> getObservationsByKategori(Kod observationsKategori) {
        List<Observation> observations = new ArrayList<>();
        for (Observation observation : this.observationer) {
            if (observation.getObservationskategori() != null
                    && observation.getObservationskategori().equals(observationsKategori)) {
                observations.add(observation);
            }
        }
        return observations;
    }

    public Observation findObservationByKategori(final Kod observationsKategori) {
        return find(observationer, new Predicate<Observation>() {
            @Override
            public boolean apply(Observation observation) {
                return observation.getObservationskategori() != null
                        && observation.getObservationskategori().equals(observationsKategori);
            }
        }, null);
    }

    public Observation findObservationByKod(final Kod observationsKod) {
        return find(observationer, new Predicate<Observation>() {
            @Override
            public boolean apply(Observation observation) {
                return observation.getObservationskod() != null
                        && observation.getObservationskod().equals(observationsKod);
            }
        }, null);
    }

    public Partial getValidFromDate() {
        return validFromDate;
    }

    public void setValidFromDate(Partial date) {
        validFromDate = date;
    }

    public Partial getValidToDate() {
        return validToDate;
    }

    public void setValidToDate(Partial date) {
        validToDate = date;
    }

    public Aktivitet getAktivitet(final Kod aktivitetsKod) {
        if (aktiviteter == null) {
            return null;
        }

        return find(aktiviteter, new Predicate<Aktivitet>() {
            @Override
            public boolean apply(Aktivitet aktivitet) {
                return aktivitetsKod.equals(aktivitet.getAktivitetskod());
            }
        }, null);
    }

    public Vardkontakt getVardkontakt(final Kod vardkontaktTyp) {
        return find(vardkontakter, new Predicate<Vardkontakt>() {
            @Override
            public boolean apply(Vardkontakt vardkontakt) {
                return vardkontaktTyp.equals(vardkontakt.getVardkontakttyp());
            }
        }, null);
    }

    public Referens getReferens(final Kod referensTyp) {
        return find(referenser, new Predicate<Referens>() {
            @Override
            public boolean apply(Referens referens) {
                return referensTyp.equals(referens.getReferenstyp());
            }
        }, null);
    }

}
