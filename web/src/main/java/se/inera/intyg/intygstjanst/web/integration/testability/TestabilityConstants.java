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

package se.inera.intyg.intygstjanst.web.integration.testability;

import se.inera.intyg.common.support.common.enumerations.RelationKod;

public class TestabilityConstants {

    // PATIENTS
    public static final String ATHENA_ANDERSSON_ID = "194011306125";
    public static final String ALVE_ALFRIDSSON_ID = "194112128154";
    public static final String BOSTADSLOSE_ANDERSSON_ID = "194110147495";
    public static final String ATLAS_ABRAHAMSSON_ID = "194111299055";
    public static final String ANONYMA_ATTILA_ID = "194012019149";
    public static final String ALEXA_VALFRIDSSON = "194110299221";
    public static final String AGNARSSON_AGNARSSON = "198901192396";
    public static final String ALBERT_ALBERTSSON = "200210282398";
    public static final String ALBERTINA_ALISON = "200210292389";
    public static final String ALBIN_ANDER = "197901242391";
    public static final String ALINE_ANDERSSON = "197901252382";
    public static final String ALLAN_ALLANSON = "199606282391";
    public static final String ALMA_ALMARSSON = "199606292382";
    // RELATIONS
    public static final String DEFAULT_RELATIONS_ID = null;
    public static final RelationKod DEFAULT_RELATIONS_KOD = null;
    // DIAGNOSIS CODES
    public static final String DIAGNOSIS_CODE_A010 = "A010";
    public static final String DIAGNOSIS_CODE_M12 = "M12";
    public static final String DIAGNOSIS_CODE_F430 = "F430";
    public static final String INVALID_CODE = "X";
    public static final String DIAGNOSIS_CODE_N20 = "N20";
    public static final String DIAGNOSIS_CODE_K23 = "K23";
    public static final String DIAGNOSIS_CODE_R12 = "R12";
    public static final String DIAGNOSIS_CODE_Z010 = "Z010";
    public static final String DIAGNOSIS_CODE_P23 = "P23";
    // DOCTORS
    public static final String DOKTOR_AJLA = "TSTNMT2321000156-DRAA";
    public static final String DOKTOR_ALF = "TSTNMT2321000156-DRAF";
    public static final String BEATA_DOKTOR = "TSTNMT2321000156-DRBE";
    // CAREPROVIDERS
    public static final String ALFA_REGIONEN = "TSTNMT2321000156-ALFA";
    public static final String BETA_REGIONEN = "TSTNMT2321000156-BETA";
    // CAREUNITS
    public static final String ALFA_HJARTCENTRUM = "TSTNMT2321000156-ALHC";
    public static final String ALFA_HJARTCENTRUM_FYSIOLOGISKAMOTTAGNINGEN = "TSTNMT2321000156-ALFM";
    public static final String ALFA_MEDICINCENTRUM = "TSTNMT2321000156-ALMC";
    public static final String ALFA_MEDICINCENTRUM_ALLERGIMOTTAGNINGEN = "TSTNMT2321000156-ALAM";
    public static final String ALFA_MEDICINCENTRUM_HUDMOTTAGNINGEN = "TSTNMT2321000156-ALHM";
    public static final String ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN = "TSTNMT2321000156-ALIM";
    public static final String ALFA_PSYKIATRICENTRUM = "TSTNMT2321000156-ALPC";
    public static final String ALFA_PSYKIATRICENTRUM_ALFA_BARN_OCH_UNGDOMS_PSYOKOLOGISKA_MOTTAGNINGEN = "TSTNMT2321000156-ALBM";
    public static final String ALFA_PSYKIATRICENTRUM_PSYKOLOGISKAMOTTAGNINGEN = "TSTNMT2321000156-ALPM";
    public static final String ALFA_VARDCENTRAL = "TSTNMT2321000156-ALVC";
    public static final String ALFA_VARDCENTRAL_LAKARMOTTAGNINGEN = "TSTNMT2321000156-ALLM";
    public static final String BETA_HJARTCENTRUM = "TSTNMT2321000156-BEHC";
    public static final String BETA_HJARTCENTRUM_FYSIOLOGISKAMOTTAGNINGEN = "TSTNMT2321000156-BEFM";
    public static final String BETA_MEDICINCENTRUM = "TSTNMT2321000156-BEMC";
    public static final String BETA_MEDICINCENTRUM_ALLERGIMOTTAGNINGEN = "TSTNMT2321000156-BEAM";
    public static final String BETA_MEDICINCENTRUM_HUDMOTTAGNINGEN = "TSTNMT2321000156-BEHM";
    public static final String BETA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN = "TSTNMT2321000156-BEIM";
    public static final String BETA_PSYKIATRICENTRUM = "TSTNMT2321000156-BEPC";
    public static final String BETA_PSYKIATRICENTRUM_ALFA_BARN_OCH_UNGDOMS_PSYOKOLOGISKA_MOTTAGNINGEN = "TSTNMT2321000156-BEBM";
    public static final String BETA_PSYKIATRICENTRUM_PSYKOLOGISKAMOTTAGNINGEN = "TSTNMT2321000156-BEPM";
    public static final String BETA_VARDCENTRAL = "TSTNMT2321000156-BEVC";
    public static final String BETA_VARDCENTRAL_LAKARMOTTAGNINGEN = "TSTNMT2321000156-BELM";
    // WORK CAPACITIES
    public static final String DEGREE_25 = "EN_FJARDEDEL";
    public static final String DEGREE_75 = "TRE_FJARDEDEL";
    public static final String DEGREE_100 = "HELT_NEDSATT";
    public static final String DEGREE_50 = "HALFTEN";
    // OCCUPATIONS
    public static final String NUVARANDE_ARBETE = "NUVARANDE_ARBETE";
    public static final String ARBETSSOKANDE = "ARBETSSOKANDE";
    public static final String FORADLRARLEDIGHET_VARD_AV_BARN = "FORALDRALEDIG";
    public static final String STUDIER = "STUDIER";
}
