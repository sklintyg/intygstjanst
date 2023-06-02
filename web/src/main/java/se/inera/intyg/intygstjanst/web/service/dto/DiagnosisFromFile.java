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
package se.inera.intyg.intygstjanst.web.service.dto;

import com.google.common.base.CharMatcher;

/**
 * Created by martin on 10/02/16.
 */
public class DiagnosisFromFile {

    private static final String BOM = "\uFEFF";
    private static final String ASTERISK_TAB_OR_DAGGER_TAB = "\\u002A\t|\u2020\t";
    private static final Character SPACE = ' ';

    private String code;
    private String name;

    public DiagnosisFromFile(String line, boolean firstLineInFile) {
        initFromString(line, firstLineInFile);
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    private void initFromString(String line, boolean firstLineInFile) {
        if (line != null && line.length() > 0) {
            String cleanedLine = removeUnwantedCharacters(line, firstLineInFile);

            int firstSpacePos = cleanedLine.indexOf(SPACE);
            if (firstSpacePos == -1) {
                return;
            }

            this.code = cleanedLine.substring(0, firstSpacePos);
            this.name = cleanedLine.substring(firstSpacePos + 1);
        }
    }

    private String removeUnwantedCharacters(String line, boolean firstLineInFile) {
        String cleanedLine = line;
        if (firstLineInFile) {
            cleanedLine = cleanedLine.replaceFirst(BOM, "");
        }
        cleanedLine = cleanedLine.replaceFirst(ASTERISK_TAB_OR_DAGGER_TAB, String.valueOf(SPACE));
        return CharMatcher.whitespace().trimAndCollapseFrom(cleanedLine, SPACE);
    }

}
