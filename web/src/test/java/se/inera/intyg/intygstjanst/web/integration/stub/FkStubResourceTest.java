/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration.stub;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.common.support.stub.MedicalCertificatesStore.MAKULERAD;
import static se.inera.intyg.common.support.stub.MedicalCertificatesStore.MAKULERAD_JA;
import static se.inera.intyg.common.support.stub.MedicalCertificatesStore.MAKULERAD_NEJ;
import static se.inera.intyg.common.support.stub.MedicalCertificatesStore.MEDDELANDE;
import static se.inera.intyg.common.support.stub.MedicalCertificatesStore.PERSONNUMMER;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.stub.MedicalCertificatesStore;

@RunWith(MockitoJUnitRunner.class)
public class FkStubResourceTest {

    @Mock
    MedicalCertificatesStore store;

    @Mock
    Map<String, Map<String, String>> map;

    @Mock
    Set<Map.Entry<String, Map<String, String>>> entrySet;

    @Mock
    Iterator<Entry<String, Map<String, String>>> iterator;

    @InjectMocks
    private FkStubResource stub = new FkStubResource();

    @Mock
    private Entry<String, Map<String, String>> value;

    @Test
    public void testGetCount() throws Exception {
        when(store.getCount()).thenReturn(15);
        assertEquals(15, stub.count());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCertificates() throws Exception {
        when(store.getAll()).thenReturn(map);
        when(map.entrySet()).thenReturn(entrySet);
        when(entrySet.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, true, true, false);
        when(iterator.next()).thenReturn(value);
        when(value.getKey()).thenReturn("1", "2", "3");

        Map<String, String> v1 = newHashMap();
        v1.put(PERSONNUMMER, "19121212-1111");
        v1.put(MAKULERAD, MAKULERAD_NEJ);
        v1.put(MEDDELANDE, "m1");
        Map<String, String> v2 = newHashMap();
        v2.put(PERSONNUMMER, "19121212-2222");
        v2.put(MAKULERAD, MAKULERAD_NEJ);
        v2.put(MEDDELANDE, "m2");
        Map<String, String> v3 = newHashMap();
        v3.put(PERSONNUMMER, "19121212-3333");
        v3.put(MAKULERAD, MAKULERAD_JA);
        v3.put(MEDDELANDE, "m3");
        when(value.getValue()).thenReturn(v1, v1, v1, v2, v2, v2, v3, v3, v3);

        String v1Str = "<tr><td>1</td><td>19121212-1111</td><td>NEJ</td><td>m1</td></tr>";
        String v2Str = "<tr><td>2</td><td>19121212-2222</td><td>NEJ</td><td>m2</td></tr>";
        String v3Str = "<tr><td>3</td><td>19121212-3333</td><td>JA</td><td>m3</td></tr>";

        String result = stub.certificates();

        assertTrue(result.contains(v1Str));
        assertTrue(result.contains(v2Str));
        assertTrue(result.contains(v3Str));
    }

    @Test
    public void testCertificatesJson() throws Exception {

        Map<String, String> v1 = newHashMap();
        v1.put(PERSONNUMMER, "19121212-1111");
        v1.put(MAKULERAD, MAKULERAD_NEJ);
        Map<String, String> v2 = newHashMap();
        v2.put(PERSONNUMMER, "19121212-2222");
        v2.put(MAKULERAD, MAKULERAD_NEJ);
        Map<String, String> v3 = newHashMap();
        v3.put(PERSONNUMMER, "19121212-3333");
        v3.put(MAKULERAD, MAKULERAD_JA);
        Map<String, Map<String, String>> expected = newHashMap();
        expected.put("1", v1);
        expected.put("2", v2);
        expected.put("3", v3);
        when(store.getAll()).thenReturn(expected);

        Map<String, Map<String, String>> result = stub.certificatesJson();

        assertEquals(expected, result);
    }

    @Test
    public void testClear() throws Exception {
        stub.clear();
        verify(store).clear();
    }

    @Test
    public void testClearJson() throws Exception {
        stub.clearJson();
        verify(store).clear();
    }

}
