package nodes;

import axiom.AxiomContext;
import gen.Messages.VCardTextInput;
import gen.Messages.VCardVersionResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DetectVersionTest {

    @Test
    public void testDetectVersion_singleCard40() {
        AxiomContext ax = TestSupport.ax();
        VCardVersionResult r = DetectVersion.detectVersion(ax,
                VCardTextInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_40).build());
        assertFalse(r.hasError());
        assertEquals("4.0", r.getVersion());
        assertEquals(java.util.List.of("4.0"), r.getCardVersionsList());
    }

    @Test
    public void testDetectVersion_mixedVersionsInDocumentOrder() {
        AxiomContext ax = TestSupport.ax();
        String doc = TestSupport.SAMPLE_VCARD_30 + TestSupport.SAMPLE_VCARD_40;
        VCardVersionResult r = DetectVersion.detectVersion(ax, VCardTextInput.newBuilder().setText(doc).build());
        assertFalse(r.hasError());
        assertEquals("3.0", r.getVersion());
        assertEquals(java.util.List.of("3.0", "4.0"), r.getCardVersionsList());
    }

    @Test
    public void testDetectVersion_noVCardIsEmptyNotError() {
        AxiomContext ax = TestSupport.ax();
        VCardVersionResult r = DetectVersion.detectVersion(ax, VCardTextInput.newBuilder().setText("nope").build());
        assertFalse(r.hasError());
        assertEquals("", r.getVersion());
        assertEquals(0, r.getCardVersionsCount());
    }
}
