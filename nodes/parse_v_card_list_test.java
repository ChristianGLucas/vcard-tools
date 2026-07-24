package nodes;

import axiom.AxiomContext;
import gen.Messages.VCardList;
import gen.Messages.VCardTextInput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ParseVCardListTest {

    @Test
    public void testParseVCardList_twoConcatenatedCards() {
        AxiomContext ax = TestSupport.ax();
        String doc = TestSupport.SAMPLE_VCARD_40 + TestSupport.SAMPLE_VCARD_30;
        VCardList r = ParseVCardList.parseVCardList(ax, VCardTextInput.newBuilder().setText(doc).build());

        assertFalse(r.hasError());
        assertEquals(2, r.getCount());
        assertEquals(2, r.getCardsCount());
        assertEquals("Dr. Jane Q. Doe", r.getCards(0).getFormattedName());
        assertEquals("4.0", r.getCards(0).getVersion());
        assertEquals("Bob Smith", r.getCards(1).getFormattedName());
        assertEquals("3.0", r.getCards(1).getVersion());
    }

    @Test
    public void testParseVCardList_emptyDocumentIsZeroNotError() {
        AxiomContext ax = TestSupport.ax();
        VCardList r = ParseVCardList.parseVCardList(ax, VCardTextInput.newBuilder().setText("").build());
        assertFalse(r.hasError());
        assertEquals(0, r.getCount());
        assertEquals(0, r.getCardsCount());
    }

    @Test
    public void testParseVCardList_singleCard() {
        AxiomContext ax = TestSupport.ax();
        VCardList r = ParseVCardList.parseVCardList(ax,
                VCardTextInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_30).build());
        assertFalse(r.hasError());
        assertEquals(1, r.getCount());
    }
}
