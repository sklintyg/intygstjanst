/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.intygstjanst.web.integration.testability.util;

import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.AGNARSSON_AGNARSSON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALBERTINA_ALISON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALBERT_ALBERTSSON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALBIN_ANDER;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALEXA_VALFRIDSSON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_HJARTCENTRUM;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_HJARTCENTRUM_FYSIOLOGISKAMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_MEDICINCENTRUM;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_MEDICINCENTRUM_ALLERGIMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_MEDICINCENTRUM_HUDMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_PSYKIATRICENTRUM;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_PSYKIATRICENTRUM_ALFA_BARN_OCH_UNGDOMS_PSYOKOLOGISKA_MOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_PSYKIATRICENTRUM_PSYKOLOGISKAMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_REGIONEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_VARDCENTRAL;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALFA_VARDCENTRAL_LAKARMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALINE_ANDERSSON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALLAN_ALLANSON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALMA_ALMARSSON;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ALVE_ALFRIDSSON_ID;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ANONYMA_ATTILA_ID;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ARBETSSOKANDE;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ATHENA_ANDERSSON_ID;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.ATLAS_ABRAHAMSSON_ID;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BEATA_DOKTOR;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BETA_HJARTCENTRUM;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BETA_HJARTCENTRUM_FYSIOLOGISKAMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BETA_MEDICINCENTRUM;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BETA_MEDICINCENTRUM_ALLERGIMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BETA_MEDICINCENTRUM_HUDMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BETA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BETA_PSYKIATRICENTRUM;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BETA_PSYKIATRICENTRUM_ALFA_BARN_OCH_UNGDOMS_PSYOKOLOGISKA_MOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BETA_PSYKIATRICENTRUM_PSYKOLOGISKAMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BETA_REGIONEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BETA_VARDCENTRAL;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BETA_VARDCENTRAL_LAKARMOTTAGNINGEN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.BOSTADSLOSE_ANDERSSON_ID;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DEGREE_100;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DEGREE_25;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DEGREE_50;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DEGREE_75;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_A010;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_F430;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_K23;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_M12;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_N20;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_P23;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_R12;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DIAGNOSIS_CODE_Z010;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DOKTOR_AJLA;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.DOKTOR_ALF;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.FORADLRARLEDIGHET_VARD_AV_BARN;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.INVALID_CODE;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.NUVARANDE_ARBETE;
import static se.inera.intyg.intygstjanst.web.integration.testability.TestabilityConstants.STUDIER;

import java.util.List;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.web.integration.testability.model.CareProvider;
import se.inera.intyg.intygstjanst.web.integration.testability.model.CareUnit;
import se.inera.intyg.intygstjanst.web.integration.testability.model.Doctor;
import se.inera.intyg.intygstjanst.web.integration.testability.model.Occupation;
import se.inera.intyg.intygstjanst.web.integration.testability.model.Patient;
import se.inera.intyg.intygstjanst.web.integration.testability.model.Relation;
import se.inera.intyg.intygstjanst.web.integration.testability.model.WorkCapacity;

public class TestDataProvider {

    public static List<CareProvider> getCareProviders() {
        return List.of(
            new CareProvider(ALFA_REGIONEN, "Alfa Regionen"),
            new CareProvider(BETA_REGIONEN, "Beta Regionen")
        );
    }

    public static List<CareUnit> getCareUnits() {
        return List.of(
            new CareUnit(ALFA_HJARTCENTRUM, "Alfa Hjärtcentrum"),
            new CareUnit(ALFA_HJARTCENTRUM_FYSIOLOGISKAMOTTAGNINGEN, "Alfa Hjärtcentrum - Fysiologiska mottagningen"),
            new CareUnit(ALFA_MEDICINCENTRUM, "Alfa Medicincentrum"),
            new CareUnit(ALFA_MEDICINCENTRUM_ALLERGIMOTTAGNINGEN, "Alfa Medicincentrum - Allergimottagnignen"),
            new CareUnit(ALFA_MEDICINCENTRUM_HUDMOTTAGNINGEN, "Alfa Medicincentrum - Hudmottagningen"),
            new CareUnit(ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN, "Alfa Medicincentrum - Infektionsmottagningen"),
            new CareUnit(ALFA_PSYKIATRICENTRUM, "Alfa Psykiatricentrum"),
            new CareUnit(ALFA_PSYKIATRICENTRUM_ALFA_BARN_OCH_UNGDOMS_PSYOKOLOGISKA_MOTTAGNINGEN,
                "Alfa Psykiatricentrum - Alfa barn och ungdoms psykologiska-mottagningen"),
            new CareUnit(ALFA_PSYKIATRICENTRUM_PSYKOLOGISKAMOTTAGNINGEN, "Alfa Psykiatricentrum - Psykologiskamottagningen"),
            new CareUnit(ALFA_VARDCENTRAL, "Alfa Vårdcentral"),
            new CareUnit(ALFA_VARDCENTRAL_LAKARMOTTAGNINGEN, "Alfa Vårdcentral - Läkarmottagnignen"),
            new CareUnit(BETA_HJARTCENTRUM, "Beta Hjärtcentrum"),
            new CareUnit(BETA_HJARTCENTRUM_FYSIOLOGISKAMOTTAGNINGEN, "Beta Hjärtcentrum - Fysiologiska mottagningen"),
            new CareUnit(BETA_MEDICINCENTRUM, "Beta Medicincentrum"),
            new CareUnit(BETA_MEDICINCENTRUM_ALLERGIMOTTAGNINGEN, "Beta Medicincentrum - Allergimottagnignen"),
            new CareUnit(BETA_MEDICINCENTRUM_HUDMOTTAGNINGEN, "Beta Medicincentrum - Hudmottagningen"),
            new CareUnit(BETA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN, "Beta Medicincentrum - Infektionsmottagningen"),
            new CareUnit(BETA_PSYKIATRICENTRUM, "Beta Psykiatricentrum"),
            new CareUnit(BETA_PSYKIATRICENTRUM_ALFA_BARN_OCH_UNGDOMS_PSYOKOLOGISKA_MOTTAGNINGEN,
                "Beta Psykiatricentrum - Alfa barn och ungdoms psykologiska-mottagningen"),
            new CareUnit(BETA_PSYKIATRICENTRUM_PSYKOLOGISKAMOTTAGNINGEN, "Beta Psykiatricentrum - Psykologiskamottagningen"),
            new CareUnit(BETA_VARDCENTRAL, "Beta Vårdcentral"),
            new CareUnit(BETA_VARDCENTRAL_LAKARMOTTAGNINGEN, "Beta Vårdcentral - Läkarmottagnignen")
        );
    }

    public static List<Doctor> getDoctorIds() {
        return List.of(
            new Doctor(DOKTOR_AJLA, "Doktor Ajla"),
            new Doctor(DOKTOR_ALF, "Doktor Alf"),
            new Doctor(BEATA_DOKTOR, "Beata Doktor")
        );
    }

    public static List<Patient> getPatientIds() {
        return List.of(
            new Patient(ATHENA_ANDERSSON_ID, "Athena Andersson"),
            new Patient(ALVE_ALFRIDSSON_ID, "Alve Alfridsson"),
            new Patient(BOSTADSLOSE_ANDERSSON_ID, "Bostadslöse Andersson"),
            new Patient(ATLAS_ABRAHAMSSON_ID, "Atlast Abrahamsson (Avliden)"),
            new Patient(ANONYMA_ATTILA_ID, "Anonyma Attila (person med sekretessmarkerade personuppgifter)"),
            new Patient(ALEXA_VALFRIDSSON, "Alexa Valfridsson"),
            new Patient(AGNARSSON_AGNARSSON, "Agnarsson Agnarsson"),
            new Patient(ALBERT_ALBERTSSON, "Albert Albertsson"),
            new Patient(ALBERTINA_ALISON, "Albertina Alison"),
            new Patient(ALBIN_ANDER, "Albin Ander"),
            new Patient(ALINE_ANDERSSON, "Aline Andersson"),
            new Patient(ALLAN_ALLANSON, "Allan Allanson"),
            new Patient(ALMA_ALMARSSON, "Alma Almarsson")
        );
    }

    public static List<Relation> getRelationCodes() {
        return List.of(
            new Relation(RelationKod.ERSATT.value(), RelationKod.ERSATT.getKlartext()),
            new Relation(RelationKod.FRLANG.value(), RelationKod.FRLANG.getKlartext()),
            new Relation(RelationKod.KOMPLT.value(), RelationKod.KOMPLT.getKlartext()),
            new Relation(RelationKod.KOPIA.value(), RelationKod.KOPIA.getKlartext())
        );
    }

    public static List<String> getDiagnosisCodes() {
        return List.of(
            DIAGNOSIS_CODE_A010,
            DIAGNOSIS_CODE_F430,
            DIAGNOSIS_CODE_M12,
            DIAGNOSIS_CODE_N20,
            DIAGNOSIS_CODE_K23,
            DIAGNOSIS_CODE_R12,
            DIAGNOSIS_CODE_P23,
            DIAGNOSIS_CODE_Z010,
            INVALID_CODE
        );
    }

    public static List<Occupation> getOccupations() {
        return List.of(
            new Occupation(NUVARANDE_ARBETE, "Nuvarande arbete"),
            new Occupation(ARBETSSOKANDE, "Arbetssökande"),
            new Occupation(FORADLRARLEDIGHET_VARD_AV_BARN, "Föräldraledig"),
            new Occupation(STUDIER, "Studier")
        );
    }

    public static List<WorkCapacity> getWorkCapacities() {
        return List.of(
            new WorkCapacity(DEGREE_25, "25%"),
            new WorkCapacity(DEGREE_50, "50%"),
            new WorkCapacity(DEGREE_75, "75%"),
            new WorkCapacity(DEGREE_100, "100%")
        );
    }
}
