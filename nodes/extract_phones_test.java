package nodes;

import axiom.AxiomContext;
import gen.Messages.PhoneList;
import gen.Messages.VCardTextInput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExtractPhonesTest {

    @Test
    public void testExtractPhones_knownFieldsAgainstHandAuthoredFixture() {
        AxiomContext ax = TestSupport.ax();
        PhoneList r = ExtractPhones.extractPhones(ax,
                VCardTextInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_40).build());
        assertFalse(r.hasError());
        assertEquals(2, r.getPhonesCount());
        assertEquals(java.util.List.of("cell"), r.getPhones(0).getTypesList());
        assertEquals("+1 555 555 0199", r.getPhones(1).getValue());
    }

    @Test
    public void testExtractPhones_multipleTypesPreserved() {
        AxiomContext ax = TestSupport.ax();
        PhoneList r = ExtractPhones.extractPhones(ax,
                VCardTextInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_30).build());
        assertFalse(r.hasError());
        assertEquals(1, r.getPhonesCount());
        assertTrue(r.getPhones(0).getTypesList().contains("work"));
        assertTrue(r.getPhones(0).getTypesList().contains("voice"));
    }

    @Test
    public void testExtractPhones_multipleCardsIsInvalidArgument() {
        AxiomContext ax = TestSupport.ax();
        String doc = TestSupport.SAMPLE_VCARD_40 + TestSupport.SAMPLE_VCARD_30;
        PhoneList r = ExtractPhones.extractPhones(ax, VCardTextInput.newBuilder().setText(doc).build());
        assertTrue(r.hasError());
        assertEquals("INVALID_ARGUMENT", r.getError().getCode());
    }
}
