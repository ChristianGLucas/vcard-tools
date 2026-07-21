package nodes;

import axiom.AxiomContext;
import gen.Messages.VCardNameResult;
import gen.Messages.VCardTextInput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExtractNameTest {

    @Test
    public void testExtractName_knownFieldsAgainstHandAuthoredFixture() {
        AxiomContext ax = TestSupport.ax();
        VCardNameResult r = ExtractName.extractName(ax,
                VCardTextInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_40).build());
        assertFalse(r.hasError());
        assertEquals("Dr. Jane Q. Doe", r.getFormattedName());
        assertEquals("Doe", r.getName().getFamily());
        assertEquals("Jane", r.getName().getGiven());
        assertEquals(java.util.List.of("Quinn"), r.getName().getAdditionalList());
        assertEquals(java.util.List.of("Dr."), r.getName().getPrefixesList());
    }

    @Test
    public void testExtractName_zeroCardsIsInvalidArgument() {
        AxiomContext ax = TestSupport.ax();
        VCardNameResult r = ExtractName.extractName(ax, VCardTextInput.newBuilder().setText("").build());
        assertTrue(r.hasError());
        assertEquals("INVALID_ARGUMENT", r.getError().getCode());
    }
}
