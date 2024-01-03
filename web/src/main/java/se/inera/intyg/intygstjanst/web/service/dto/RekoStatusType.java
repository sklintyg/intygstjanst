/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

    public String getName() {
        return name;
    }

    public static RekoStatusType fromId(String id) {
        for (final var type : values()) {
            if (type.toString().equals(id)) {
                return type;
            }
        }

        return REKO_1;
    }
}
