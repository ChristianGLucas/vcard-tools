package nodes;

import axiom.AxiomContext;
import gen.Messages.EmailList;
import gen.Messages.VCardTextInput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExtractEmailsTest {

    @Test
    public void testExtractEmails_knownFieldsAgainstHandAuthoredFixture() {
        AxiomContext ax = TestSupport.ax();
        EmailList r = ExtractEmails.extractEmails(ax,
                VCardTextInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_40).build());
        assertFalse(r.hasError());
        assertEquals(2, r.getEmailsCount());
        assertEquals("jane@example.com", r.getEmails(0).getValue());
        assertEquals(1, r.getEmails(0).getPref());
        assertEquals("jane.doe@personal.example", r.getEmails(1).getValue());
    }

    @Test
    public void testExtractEmails_noEmailsIsEmptyListNotError() {
        AxiomContext ax = TestSupport.ax();
        EmailList r = ExtractEmails.extractEmails(ax,
                VCardTextInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_30).build());
        assertFalse(r.hasError());
        assertEquals(1, r.getEmailsCount());
    }

    @Test
    public void testExtractEmails_zeroCardsIsInvalidArgument() {
        AxiomContext ax = TestSupport.ax();
        EmailList r = ExtractEmails.extractEmails(ax, VCardTextInput.newBuilder().setText("").build());
        assertTrue(r.hasError());
        assertEquals("INVALID_ARGUMENT", r.getError().getCode());
    }
}
