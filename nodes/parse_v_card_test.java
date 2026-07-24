package nodes;

import axiom.AxiomContext;
import gen.Messages.VCard;
import gen.Messages.VCardTextInput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ParseVCardTest {

    @Test
    public void testParseVCard_knownFieldsAgainstHandAuthoredFixture() {
        AxiomContext ax = TestSupport.ax();
        VCard r = ParseVCard.parseVCard(ax, VCardTextInput.newBuilder().setText(TestSupport.SAMPLE_VCARD_40).build());

        assertFalse(r.hasError(), "unexpected error: " + r.getError());
        assertEquals("4.0", r.getVersion());
        assertEquals("contact-1@example.com", r.getUid());
        assertEquals("individual", r.getKind());
        assertEquals("Dr. Jane Q. Doe", r.getFormattedName());

        assertEquals("Doe", r.getName().getFamily());
        assertEquals("Jane", r.getName().getGiven());
        assertEquals(java.util.List.of("Quinn"), r.getName().getAdditionalList());
        assertEquals(java.util.List.of("Dr."), r.getName().getPrefixesList());
        assertEquals(java.util.List.of(), r.getName().getSuffixesList());

        assertEquals(java.util.List.of("Janie", "JQ"), r.getNicknamesList());

        assertEquals(2, r.getEmailsCount());
        assertEquals("jane@example.com", r.getEmails(0).getValue());
        assertEquals(java.util.List.of("work"), r.getEmails(0).getTypesList());
        assertEquals(1, r.getEmails(0).getPref());
        assertEquals("jane.doe@personal.example", r.getEmails(1).getValue());
        assertEquals(java.util.List.of("home"), r.getEmails(1).getTypesList());
        assertEquals(0, r.getEmails(1).getPref());

        assertEquals(2, r.getPhonesCount());
        assertEquals(java.util.List.of("cell"), r.getPhones(0).getTypesList());
        assertEquals("+1 555 555 0199", r.getPhones(1).getValue());
        assertEquals(java.util.List.of("work"), r.getPhones(1).getTypesList());

        assertEquals(1, r.getAddressesCount());
        var addr = r.getAddresses(0);
        assertEquals("", addr.getPoBox());
        assertEquals("Suite 100", addr.getExtended());
        assertEquals("123 Main St", addr.getStreet());
        assertEquals("Springfield", addr.getLocality());
        assertEquals("IL", addr.getRegion());
        assertEquals("62704", addr.getPostalCode());
        assertEquals("USA", addr.getCountry());
        assertEquals(java.util.List.of("work"), addr.getTypesList());

        assertEquals("Acme Corp", r.getOrg());
        assertEquals(java.util.List.of("Engineering", "Widgets"), r.getOrgUnitsList());
        assertEquals("Senior Engineer", r.getTitle());
        assertEquals("Individual Contributor", r.getRole());

        assertEquals(1, r.getUrlsCount());
        assertEquals("https://example.com/jane", r.getUrls(0).getValue());
        assertEquals(java.util.List.of("work"), r.getUrls(0).getTypesList());

        assertEquals("1990-04-15", r.getBirthday());
        assertEquals("2015-06-20", r.getAnniversary());

        assertEquals(java.util.List.of("First note.", "Second note."), r.getNotesList());
        assertEquals(java.util.List.of("VIP", "Engineering"), r.getCategoriesList());

        assertEquals(1, r.getImppCount());
        assertEquals(java.util.List.of("work"), r.getImpp(0).getTypesList());

        assertTrue(r.hasGeo());
        assertEquals(37.386013, r.getGeo().getLatitude(), 0.000001);
        assertEquals(-122.082932, r.getGeo().getLongitude(), 0.000001);

        assertEquals("F;cisgender woman", r.getGender());
        assertEquals(java.util.List.of("en-US", "fr-FR"), r.getLanguagesList());
        assertEquals("America/New_York", r.getTimezone());
        assertEquals(java.util.List.of("https://example.com/contacts/jane.vcf"), r.getSourcesList());
    }

    @Test
    public void testParseVCard_zeroCardsIsInvalidArgument() {
        AxiomContext ax = TestSupport.ax();
        VCard r = ParseVCard.parseVCard(ax, VCardTextInput.newBuilder().setText("not a vcard at all").build());
        assertTrue(r.hasError());
        assertEquals("INVALID_ARGUMENT", r.getError().getCode());
    }

    @Test
    public void testParseVCard_multipleCardsIsInvalidArgument() {
        AxiomContext ax = TestSupport.ax();
        String twoCards = TestSupport.SAMPLE_VCARD_40 + TestSupport.SAMPLE_VCARD_30;
        VCard r = ParseVCard.parseVCard(ax, VCardTextInput.newBuilder().setText(twoCards).build());
        assertTrue(r.hasError());
        assertEquals("INVALID_ARGUMENT", r.getError().getCode());
    }


    @Test
    public void testParseVCard_propagatesUpstreamError() {
        AxiomContext ax = TestSupport.ax();
        gen.Messages.Error upstream = gen.Messages.Error.newBuilder()
                .setCode("INVALID_ARGUMENT").setMessage("upstream failed").build();
        VCard r = ParseVCard.parseVCard(ax, VCardTextInput.newBuilder().setError(upstream).build());
        assertTrue(r.hasError());
        assertEquals("upstream failed", r.getError().getMessage());
    }
}
