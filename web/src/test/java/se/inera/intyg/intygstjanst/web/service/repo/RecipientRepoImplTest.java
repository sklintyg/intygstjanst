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
package se.inera.intyg.intygstjanst.web.service.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.intygstjanst.logging.MdcHelper;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;


@ExtendWith(MockitoExtension.class)
public class RecipientRepoImplTest {

    @Mock
    private MdcHelper mdcHelper;

    @InjectMocks
    private RecipientRepoImpl repo;

    private List<Recipient> allRecipients;

    @BeforeEach
    public void setup() throws IOException {
        String recipientFile = "recipients.json";

        injectRecipientFile(recipientFile);

        URI uri = new ClassPathResource("/RecipientRepoImplTest/" + recipientFile).getURI();

        ObjectMapper objectMapper = new ObjectMapper();
        Recipient[] rec = objectMapper.readValue(Files.newInputStream(Paths.get(uri)),
            Recipient[].class);
        allRecipients = Arrays.asList(rec);

        doReturn("traceId").when(mdcHelper).traceId();
        doReturn("spanId").when(mdcHelper).spanId();

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
        assertEquals(5, repo.listRecipients().size(),
            () -> String.format("Expected 5 recipients after update, was %s", repo.listRecipients().size()));
        assertTrue(repo.listRecipients().stream().allMatch(Recipient::isTrusted));
    }

    @Test
    public void testAddingRecipientToFile() throws IOException {
        assertEquals(5, repo.listRecipients().size(),
            () -> String.format("Expected 5 recipients before update, was %s", repo.listRecipients().size()));

        // Change file and update
        injectRecipientFile("recipients_updated.json");
        repo.update();

        assertEquals(6, repo.listRecipients().size(),
            () -> String.format("Expected 6 recipients after update, was %s", repo.listRecipients().size()));
        assertFalse(repo.listRecipients().stream().allMatch(Recipient::isTrusted));
    }

    @Test
    public void testChangedRecipientInFile() throws IOException, RecipientUnknownException {
        assertEquals(5, repo.listRecipients().size(),
            () -> String.format("Expected 5 recipients before update, was %s", repo.listRecipients().size()));
        assertEquals("Transportstyrelsen", repo.getRecipient("TRANSP").getName());

        // Change file and update
        injectRecipientFile("recipients_changed.json");
        repo.update();

        assertEquals(5, repo.listRecipients().size(),
            () -> String.format("Expected 5 recipients after update, was %s", repo.listRecipients().size()));
        assertEquals("Changed", repo.getRecipient("TRANSP").getName());
        assertTrue(repo.listRecipients().stream().allMatch(Recipient::isTrusted));
    }

    @Test
    public void testGetPrimaryRecipients() {
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

    @Test
    public void testInitWithMissingPrimaryRecipients() throws IOException {
        repo.clear();
        injectRecipientFile("recipients_missing_primary.json");

        assertThrows(ServerException.class, () -> repo.update());
    }
}
