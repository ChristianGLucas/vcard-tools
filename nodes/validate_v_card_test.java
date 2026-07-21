package nodes;

import axiom.AxiomContext;
import gen.Messages.VCardTextInput;
import gen.Messages.VCardValidationResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidateVCardTest {

    @Test
    public void testValidateVCard_specConformantCardIsValid() {
        AxiomContext ax = TestSupport.ax();
        VCardValidationResult r = ValidateVCard.validateVCard(ax,
                VCardTextInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_40).build());
        assertFalse(r.hasError(), "unexpected error: " + r.getError());
        assertTrue(r.getValid(), "expected valid; issues=" + r.getIssuesList());
        assertEquals(0, r.getIssuesCount());
    }

    @Test
    public void testValidateVCard_missingRequiredFnIsInvalid() {
        AxiomContext ax = TestSupport.ax();
        // RFC 6350 requires FN on every vCard 4.0 — omitting it must fail validation.
        String noFn = String.join("\r\n",
                "BEGIN:VCARD",
                "VERSION:4.0",
                "UID:no-fn@example.com",
                "END:VCARD",
                "");
        VCardValidationResult r = ValidateVCard.validateVCard(ax, VCardTextInput.newBuilder().setText(noFn).build());
        assertFalse(r.hasError());
        assertFalse(r.getValid());
        assertTrue(r.getIssuesCount() >= 1);
        assertEquals("ERROR", r.getIssues(0).getSeverity());
    }

    @Test
    public void testValidateVCard_multipleCardsReportsIssueNotError() {
        AxiomContext ax = TestSupport.ax();
        String twoCards = TestSupport.SAMPLE_VCARD_40 + TestSupport.SAMPLE_VCARD_30;
        VCardValidationResult r = ValidateVCard.validateVCard(ax, VCardTextInput.newBuilder().setText(twoCards).build());
        assertFalse(r.hasError());
        assertFalse(r.getValid());
        assertEquals(1, r.getIssuesCount());
    }

    @Test
    public void testValidateVCard_noCardsReportsIssueNotError() {
        AxiomContext ax = TestSupport.ax();
        VCardValidationResult r = ValidateVCard.validateVCard(ax, VCardTextInput.newBuilder().setText("nope").build());
        assertFalse(r.hasError());
        assertFalse(r.getValid());
        assertEquals(1, r.getIssuesCount());
    }
}
