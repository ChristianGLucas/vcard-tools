package nodes;

import axiom.AxiomContext;
import gen.Messages.VCardCountResult;
import gen.Messages.VCardTextInput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CountContactsTest {

    @Test
    public void testCountContacts_zeroOneAndMultipleCards() {
        AxiomContext ax = TestSupport.ax();

        assertEquals(0, CountContacts.countContacts(ax,
                VCardTextInput.newBuilder().setText("").build()).getCount());

        assertEquals(1, CountContacts.countContacts(ax,
                VCardTextInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_40).build()).getCount());

        String doc = TestSupport.SAMPLE_VCARD_40 + TestSupport.SAMPLE_VCARD_30 + TestSupport.SAMPLE_VCARD_30;
        assertEquals(3, CountContacts.countContacts(ax,
                VCardTextInput.newBuilder().setText(doc).build()).getCount());
    }

    @Test
    public void testCountContacts_garbageTextIsZeroNotError() {
        AxiomContext ax = TestSupport.ax();
        VCardCountResult r = CountContacts.countContacts(ax,
                VCardTextInput.newBuilder().setText("hello world, not a vcard").build());
        assertFalse(r.hasError());
        assertEquals(0, r.getCount());
    }
}
