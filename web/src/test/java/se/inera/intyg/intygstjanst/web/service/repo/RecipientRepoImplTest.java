/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RecipientRepoImplTest {

    private RecipientRepoImpl repo;

    private List<Recipient> allRecipients;

    @Before
    public void setup() throws IOException {
        repo = new RecipientRepoImpl();
        String recipientFile = "recipients.json";

        injectRecipientFile(recipientFile);

        URI uri = new ClassPathResource("/RecipientRepoImplTest/" + recipientFile).getURI();

        ObjectMapper objectMapper = new ObjectMapper();
        Recipient[] rec = objectMapper.readValue(Files.newInputStream(Paths.get(uri)),
                Recipient[].class);
        allRecipients = Arrays.asList(rec);

        repo.init();
    }

    private void injectRecipientFile(String file) throws IOException {
        String path = new ClassPathResource("/RecipientRepoImplTest/" + file).getURI().getPath();
        ReflectionTestUtils.setField(repo, "recipientFile", path);
    }

    @Test
    public void testListRecipients() {
        assertEquals(5, repo.listRecipients().size());
        assertTrue(repo.listRecipients().containsAll(allRecipients));
        assertTrue(repo.listRecipients().stream().allMatch(Recipient::isTrusted));
    }

    @Test
    public void testChangingToIncorrectFileKeepsPreviousRecipients() {
        try {
            injectRecipientFile("missing");
            repo.update();
        } catch (IOException e) {
        }
        // Make sure the recipients loaded by init() still remain!
        assertEquals(String.format("Expected 5 recipients after update, was %s", repo.listRecipients().size()),
                5, repo.listRecipients().size());
        assertTrue(repo.listRecipients().stream().allMatch(Recipient::isTrusted));
    }

    @Test
    public void testAddingRecipientToFile() throws IOException {
        assertEquals(String.format("Expected 5 recipients before update, was %s", repo.listRecipients().size()),
                5, repo.listRecipients().size());

        // Change file and update
        injectRecipientFile("recipients_updated.json");
        repo.update();

        assertEquals(String.format("Expected 6 recipients after update, was %s", repo.listRecipients().size()),
                6, repo.listRecipients().size());
        assertFalse(repo.listRecipients().stream().allMatch(Recipient::isTrusted));
    }

    @Test
    public void testChangedRecipientInFile() throws IOException, RecipientUnknownException {
        assertEquals(String.format("Expected 5 recipients before update, was %s", repo.listRecipients().size()),
                5, repo.listRecipients().size());
        assertEquals("Transportstyrelsen", repo.getRecipient("TRANSP").getName());

        // Change file and update
        injectRecipientFile("recipients_changed.json");
        repo.update();

        assertEquals(String.format("Expected 5 recipients after update, was %s", repo.listRecipients().size()),
                5, repo.listRecipients().size());
        assertEquals("Changed", repo.getRecipient("TRANSP").getName());
        assertTrue(repo.listRecipients().stream().allMatch(Recipient::isTrusted));
    }

    @Test
    public void testGetPrimaryRecipients() throws IOException {
        List<Recipient> recipients = new ArrayList<>();
        recipients.add(repo.getRecipientFkassa());
        recipients.add(repo.getRecipientHsvard());
        recipients.add(repo.getRecipientInvana());

        List<Recipient> exp = allRecipients.stream()
                .filter(r -> r.getId().equals("FKASSA") || r.getId().equals("INVANA") || r.getId().equals("HSVARD"))
                .collect(Collectors.toList());

        assertEquals(3, recipients.size());
        assertTrue(recipients.containsAll(exp));
        assertTrue(repo.listRecipients().stream().allMatch(Recipient::isTrusted));
    }

    @Test(expected = ServerException.class)
    public void testInitWithMissingPrimaryRecipients() throws IOException {
        // Clear to enable loading a non valid recipient file
        repo.clear();
        // Change file and update
        injectRecipientFile("recipients_missing_primary.json");
        repo.update();
    }
}
