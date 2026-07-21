package nodes;

import axiom.AxiomContext;
import gen.Messages.Email;
import gen.Messages.Name;
import gen.Messages.VCard;
import gen.Messages.VCardTextInput;
import gen.Messages.VCardTextOutput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BuildVCardTest {

    @Test
    public void testBuildVCard_rendersRfc6350LiteralLinesForKnownFields() {
        AxiomContext ax = TestSupport.ax();
        VCard input = VCard.newBuilder()
                .setVersion("4.0")
                .setFormattedName("Carol King")
                .setName(Name.newBuilder().setFamily("King").setGiven("Carol").build())
                .addEmails(Email.newBuilder().setValue("carol@example.com").addTypes("work").setPref(1).build())
                .setOrg("Songwriters Inc")
                .build();

        VCardTextOutput out = BuildVCard.buildVCard(ax, input);
        assertFalse(out.hasError(), "unexpected error: " + out.getError());

        // Independent oracle: assert on the literal RFC 6350 lines the spec grammar
        // requires for these inputs, not on re-parsing our own output.
        String text = out.getText();
        assertTrue(text.contains("VERSION:4.0"), text);
        assertTrue(text.contains("FN:Carol King"), text);
        assertTrue(text.contains("N:King;Carol;;;") || text.contains("N:King;Carol;;;\r\n"), text);
        assertTrue(text.contains("carol@example.com"), text);
        assertTrue(text.contains("Songwriters Inc"), text);
        assertTrue(text.startsWith("BEGIN:VCARD"));
        assertTrue(text.trim().endsWith("END:VCARD"));

        // Round-trip through this package's own parser as a supplementary check.
        VCard reparsed = ParseVCard.parseVCard(ax, VCardTextInput.newBuilder().setText(text).build());
        assertFalse(reparsed.hasError());
        assertEquals("Carol King", reparsed.getFormattedName());
        assertEquals("King", reparsed.getName().getFamily());
        assertEquals(1, reparsed.getEmailsCount());
        assertEquals("carol@example.com", reparsed.getEmails(0).getValue());
    }

    @Test
    public void testBuildVCard_synthesizesFormattedNameFromNameOnly() {
        AxiomContext ax = TestSupport.ax();
        VCard input = VCard.newBuilder()
                .setName(Name.newBuilder().setGiven("Alan").setFamily("Turing").build())
                .build();
        VCardTextOutput out = BuildVCard.buildVCard(ax, input);
        assertFalse(out.hasError());
        assertTrue(out.getText().contains("FN:Alan Turing"), out.getText());
    }

    @Test
    public void testBuildVCard_bothNameFieldsEmptyIsInvalidArgument() {
        AxiomContext ax = TestSupport.ax();
        VCardTextOutput out = BuildVCard.buildVCard(ax, VCard.newBuilder().build());
        assertTrue(out.hasError());
        assertEquals("INVALID_ARGUMENT", out.getError().getCode());
    }

    @Test
    public void testBuildVCard_defaultsToVersion4WhenUnset() {
        AxiomContext ax = TestSupport.ax();
        VCard input = VCard.newBuilder().setFormattedName("No Version Person").build();
        VCardTextOutput out = BuildVCard.buildVCard(ax, input);
        assertFalse(out.hasError());
        assertTrue(out.getText().contains("VERSION:4.0"), out.getText());
    }

    @Test
    public void testBuildVCard_malformedImppUriIsInvalidArgumentNotCrash() {
        AxiomContext ax = TestSupport.ax();
        VCard input = VCard.newBuilder()
                .setFormattedName("Bad Impp")
                .addImpp(gen.Messages.Impp.newBuilder().setValue(":::not a uri:::").build())
                .build();
        VCardTextOutput out = BuildVCard.buildVCard(ax, input);
        assertTrue(out.hasError());
        assertEquals("INVALID_ARGUMENT", out.getError().getCode());
    }
}
