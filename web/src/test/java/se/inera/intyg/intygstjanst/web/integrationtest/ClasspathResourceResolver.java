/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integrationtest;

import java.io.IOException;
import java.io.InputStream;

import org.apache.cxf.common.xmlschema.LSInputImpl;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class ClasspathResourceResolver implements LSResourceResolver {

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        try {
            return new LSInputImpl(publicId, systemId, load(baseURI, systemId));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream load(String baseURI, String name) throws IOException {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (resourceAsStream == null) {
            String localName = name.replaceAll("^((\\.)+/)+", "");
            resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(localName);
        }
        return resourceAsStream;
    }

}
