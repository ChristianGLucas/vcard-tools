package nodes;

import axiom.AxiomContext;
import gen.Messages.VCardConvertInput;
import gen.Messages.VCardTextInput;
import gen.Messages.VCardTextOutput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConvertVersionTest {

    @Test
    public void testConvertVersion_30to40ChangesVersionLinePreservesFn() {
        AxiomContext ax = TestSupport.ax();
        VCardTextOutput out = ConvertVersion.convertVersion(ax, VCardConvertInput.newBuilder()
                .setText(TestSupport.SAMPLE_VCARD_30).setTargetVersion("4.0").build());
        assertFalse(out.hasError(), "unexpected error: " + out.getError());
        assertTrue(out.getText().contains("VERSION:4.0"), out.getText());
        assertFalse(out.getText().contains("VERSION:3.0"), out.getText());

        // Re-parse to confirm the FN survived the conversion (semantic check).
        var reparsed = ParseVCard.parseVCard(ax, VCardTextInput.newBuilder().setText(out.getText()).build());
        assertEquals("Bob Smith", reparsed.getFormattedName());
        assertEquals("4.0", reparsed.getVersion());
    }

    @Test
    public void testConvertVersion_multiCardDocumentConvertsEachCard() {
        AxiomContext ax = TestSupport.ax();
        String doc = TestSupport.SAMPLE_VCARD_30 + TestSupport.SAMPLE_VCARD_40;
        VCardTextOutput out = ConvertVersion.convertVersion(ax,
                VCardConvertInput.newBuilder().setText(doc).setTargetVersion("3.0").build());
        assertFalse(out.hasError());
        var list = ParseVCardList.parseVCardList(ax, VCardTextInput.newBuilder().setText(out.getText()).build());
        assertEquals(2, list.getCount());
        assertEquals("3.0", list.getCards(0).getVersion());
        assertEquals("3.0", list.getCards(1).getVersion());
    }

    @Test
    public void testConvertVersion_targetVersion21IsRejected() {
        AxiomContext ax = TestSupport.ax();
        VCardTextOutput out = ConvertVersion.convertVersion(ax, VCardConvertInput.newBuilder()
                .setText(TestSupport.SAMPLE_VCARD_30).setTargetVersion("2.1").build());
        assertTrue(out.hasError());
        assertEquals("INVALID_ARGUMENT", out.getError().getCode());
    }

    @Test
    public void testConvertVersion_missingTargetVersionIsInvalidArgument() {
        AxiomContext ax = TestSupport.ax();
        VCardTextOutput out = ConvertVersion.convertVersion(ax,
                VCardConvertInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_30).build());
        assertTrue(out.hasError());
        assertEquals("INVALID_ARGUMENT", out.getError().getCode());
    }

    @Test
    public void testConvertVersion_garbageTargetVersionIsInvalidArgument() {
        AxiomContext ax = TestSupport.ax();
        VCardTextOutput out = ConvertVersion.convertVersion(ax, VCardConvertInput.newBuilder()
                .setText(TestSupport.SAMPLE_VCARD_30).setTargetVersion("9.9").build());
        assertTrue(out.hasError());
        assertEquals("INVALID_ARGUMENT", out.getError().getCode());
    }
}
