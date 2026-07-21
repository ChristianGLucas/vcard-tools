package nodes;

import axiom.AxiomContext;
import gen.Messages.AddressList;
import gen.Messages.VCardTextInput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExtractAddressesTest {

    @Test
    public void testExtractAddresses_knownFieldsAgainstHandAuthoredFixture() {
        AxiomContext ax = TestSupport.ax();
        AddressList r = ExtractAddresses.extractAddresses(ax,
                VCardTextInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_40).build());
        assertFalse(r.hasError());
        assertEquals(1, r.getAddressesCount());
        var a = r.getAddresses(0);
        assertEquals("Suite 100", a.getExtended());
        assertEquals("123 Main St", a.getStreet());
        assertEquals("Springfield", a.getLocality());
        assertEquals("IL", a.getRegion());
        assertEquals("62704", a.getPostalCode());
        assertEquals("USA", a.getCountry());
    }

    @Test
    public void testExtractAddresses_noAddressesIsEmptyListNotError() {
        AxiomContext ax = TestSupport.ax();
        AddressList r = ExtractAddresses.extractAddresses(ax,
                VCardTextInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_30).build());
        assertFalse(r.hasError());
        assertEquals(0, r.getAddressesCount());
    }
}
