/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.logging;

import com.google.common.base.Strings;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HashUtility {

    @Value("${hash.salt}")
    private String salt;

    public static final String EMPTY = "EMPTY";
    private static final HashFunction hf = Hashing.sha256();

    public String hash(final String payload) {
        if (Strings.isNullOrEmpty(payload)) {
            return EMPTY;
        }

        final var saltedPayload = salt + payload;
        final var digest = hf.hashString(saltedPayload, StandardCharsets.UTF_8).asBytes();
        return BaseEncoding.base16().lowerCase().encode(digest);
    }
}
