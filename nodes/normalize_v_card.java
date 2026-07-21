package nodes;

import axiom.AxiomContext;
import ezvcard.VCardVersion;
import gen.Messages.VCardTextInput;
import gen.Messages.VCardTextOutput;

import java.util.Map;

public class NormalizeVCard {

    /**
     * Canonicalize a document: parse every vCard in it and re-serialize each
     * at its own original version with consistent RFC-compliant property
     * ordering, line folding, and parameter formatting, dropping no data.
     * Useful for byte-stable comparison or diffing of two vCards that are
     * semantically identical but formatted differently.
     *
     * @param ax    The AxiomContext (ADR-001): logging, secrets, reflection, mutation.
     * @param input The decoded VCardTextInput for this invocation.
     */
    public static VCardTextOutput normalizeVCard(AxiomContext ax, VCardTextInput input) {
        ax.log().info("normalizeVCard handling", Map.of());
        if (input.hasError()) {
            return VCardTextOutput.newBuilder().setError(input.getError()).build();
        }
        try {
            var cards = VCardHelper.parseAll(input.getText());
            StringBuilder out = new StringBuilder();
            for (var card : cards) {
                VCardVersion version = card.getVersion() != null ? card.getVersion() : VCardVersion.V4_0;
                out.append(VCardHelper.render(card, version));
            }
            return VCardTextOutput.newBuilder().setText(out.toString()).build();
        } catch (VCardHelper.VCardException e) {
            return VCardTextOutput.newBuilder().setError(VCardHelper.toProtoError(e)).build();
        } catch (Exception e) {
            return VCardTextOutput.newBuilder().setError(VCardHelper.internalError(e)).build();
        }
    }
}
