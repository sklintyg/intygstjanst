/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.persistence.config;

/**
 * @author Magnus Ekstrand on 2018-06-15.
 */
public final class JpaConstants {

    public static final String PERSISTANCE_UNIT_NAME = "IneraCertificate";
    public static final String BASE_PACKAGE_TO_SCAN = "se.inera.intyg.intygstjanst.persistence.model";
    public static final String REPOSITORY_PACKAGE_TO_SCAN = BASE_PACKAGE_TO_SCAN + ".dao";

    private JpaConstants() {
        // Checkstyle states: Utility classes should not have a public or default constructor
    }

}
