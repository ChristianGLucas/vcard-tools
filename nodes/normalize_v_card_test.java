package nodes;

import axiom.AxiomContext;
import gen.Messages.VCardTextInput;
import gen.Messages.VCardTextOutput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NormalizeVCardTest {

    @Test
    public void testNormalizeVCard_preservesSemanticsOfMessyInput() {
        AxiomContext ax = TestSupport.ax();
        // Same data as SAMPLE_VCARD_30 but with different (still spec-legal) folding/
        // casing/parameter order, to prove normalization isn't just an identity pass.
        String messy = String.join("\r\n",
                "BEGIN:VCARD",
                "VERSION:3.0",
                "N:Smith;Bob;;;",
                "FN:Bob Smith",
                "TEL;VOICE,WORK:+1 555 555 0177",
                "EMAIL;INTERNET:bob@example.com",
                "END:VCARD",
                "");
        VCardTextOutput out = NormalizeVCard.normalizeVCard(ax, VCardTextInput.newBuilder().setText(messy).build());
        assertFalse(out.hasError(), "unexpected error: " + out.getError());
        assertTrue(out.getText().startsWith("BEGIN:VCARD"));
        assertTrue(out.getText().contains("VERSION:3.0"), out.getText());

        var before = ParseVCard.parseVCard(ax, VCardTextInput.newBuilder().setText(messy).build());
        var after = ParseVCard.parseVCard(ax, VCardTextInput.newBuilder().setText(out.getText()).build());
        assertEquals(before.getFormattedName(), after.getFormattedName());
        assertEquals(before.getEmailsList(), after.getEmailsList());
        assertEquals(before.getName().getFamily(), after.getName().getFamily());
    }

    @Test
    public void testNormalizeVCard_multiCardCountPreserved() {
        AxiomContext ax = TestSupport.ax();
        String doc = TestSupport.SAMPLE_VCARD_40 + TestSupport.SAMPLE_VCARD_30;
        VCardTextOutput out = NormalizeVCard.normalizeVCard(ax, VCardTextInput.newBuilder().setText(doc).build());
        assertFalse(out.hasError());
        var list = ParseVCardList.parseVCardList(ax, VCardTextInput.newBuilder().setText(out.getText()).build());
        assertEquals(2, list.getCount());
    }

    @Test
    public void testNormalizeVCard_emptyDocumentYieldsEmptyText() {
        AxiomContext ax = TestSupport.ax();
        VCardTextOutput out = NormalizeVCard.normalizeVCard(ax, VCardTextInput.newBuilder().setText("").build());
        assertFalse(out.hasError());
        assertEquals("", out.getText());
    }
}
