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

    private List<String> kommentars;

    private LocalDateTime signeringsDatum;

    private LocalDateTime skickatDatum;

    private Patient patient;

    private HosPersonal skapadAv;

    private List<Aktivitet> aktiviteter;

    private List<Observation> observations;

    private List<Vardkontakt> vardkontakter;

    private List<Referens> referenser;

    /**
     * To which point in time is this certificate considered valid.
     * Modules implementing this model should use their own getters calculating the date suitable for the certificate
     * type and rules.
     */
    private Partial validToDate;

    /**
     * From which point in time is this certificate considered valid.
     * Modules implementing this model should use their own getters calculating the date suitable for the certificate
     * type and rules.
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

    public List<String> getKommentars() {
        return kommentars;
    }

    public void setKommentars(List<String> kommentar) {
        this.kommentars = kommentar;
    }

    public LocalDateTime getSigneringsDatum() {
        return signeringsDatum;
    }

    public void setSigneringsDatum(LocalDateTime signeringsDatum) {
        this.signeringsDatum = signeringsDatum;
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
        return aktiviteter;
    }

    public void setAktiviteter(List<Aktivitet> aktiviteter) {
        this.aktiviteter = aktiviteter;
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public void setObservations(List<Observation> observations) {
        this.observations = observations;
    }

    public List<Vardkontakt> getVardkontakter() {
        return vardkontakter;
    }

    public void setVardkontakter(List<Vardkontakt> vardkontakter) {
        this.vardkontakter = vardkontakter;
    }

    public List<Referens> getReferenser() {
        return referenser;
    }

    public void setReferenser(List<Referens> referenser) {
        this.referenser = referenser;
    }

    public LocalDateTime getSkickatDatum() {
        return skickatDatum;
    }

    public void setSkickatDatum(LocalDateTime skickatDatum) {
        this.skickatDatum = skickatDatum;
    }

    public List<Observation> getObservationsByKod(Kod observationsKod) {
        List<Observation> observations = new ArrayList<>();
        for (Observation observation : this.observations) {
            if (observation.getObservationsKod() != null && observation.getObservationsKod().equals(observationsKod)) {
                observations.add(observation);
            }
        }
        return observations;
    }

    public List<Observation> getObservationsByKategori(Kod observationsKategori) {
        List<Observation> observations = new ArrayList<>();
        for (Observation observation : this.observations) {
            if (observation.getObservationsKategori() != null
                    && observation.getObservationsKategori().equals(observationsKategori)) {
                observations.add(observation);
            }
        }
        return observations;
    }

    public Observation findObservationByKategori(final Kod observationsKategori) {
        return find(observations, new Predicate<Observation>() {
            @Override
            public boolean apply(Observation observation) {
                return observation.getObservationsKategori() != null
                        && observation.getObservationsKategori().equals(observationsKategori);
            }
        }, null);
    }

    public Observation findObservationByKod(final Kod observationsKod) {
        return find(observations, new Predicate<Observation>() {
            @Override
            public boolean apply(Observation observation) {
                return observation.getObservationsKod() != null
                        && observation.getObservationsKod().equals(observationsKod);
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
