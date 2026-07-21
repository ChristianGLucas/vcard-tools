package nodes;

import axiom.AxiomContext;
import gen.Messages.VCard;
import gen.Messages.VCardTextInput;

import java.util.Map;

public class ParseVCard {

    /**
     * Parse a document containing EXACTLY ONE vCard into a normalized VCard:
     * formatted/structured name, nicknames, emails, phones, addresses,
     * org/title/role, URLs, birthday/anniversary, notes, categories, photo
     * metadata, IMPP handles, and geo. Returns a structured INVALID_ARGUMENT
     * error (rather than silently picking one) when the document contains
     * zero or more than one vCard — use ParseVCardList for multi-contact
     * documents. Input capped at 5 MiB.
     *
     * @param ax    The AxiomContext (ADR-001): logging, secrets, reflection, mutation.
     * @param input The decoded VCardTextInput for this invocation.
     */
    public static VCard parseVCard(AxiomContext ax, VCardTextInput input) {
        ax.log().info("parseVCard handling", Map.of());
        if (input.hasError()) {
            return VCard.newBuilder().setError(input.getError()).build();
        }
        try {
            var vcard = VCardHelper.parseExactlyOne(input.getText());
            return VCardHelper.toProto(vcard);
        } catch (VCardHelper.VCardException e) {
            return VCard.newBuilder().setError(VCardHelper.toProtoError(e)).build();
        } catch (Exception e) {
            return VCard.newBuilder().setError(VCardHelper.internalError(e)).build();
        }
    }
}
