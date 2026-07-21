package nodes;

import axiom.AxiomContext;
import gen.Messages.VCardTextInput;
import gen.Messages.VCardVersionResult;

import java.util.Map;

public class DetectVersion {

    /**
     * Detect the vCard version of a document: `version` is the first vCard's
     * version, and `card_versions` lists every vCard block's version in
     * document order — so a caller can tell at a glance whether a
     * multi-contact document mixes versions. Both empty when the document
     * contains no vCard at all (not an error: absence of a vCard is a valid
     * detection result).
     *
     * @param ax    The AxiomContext (ADR-001): logging, secrets, reflection, mutation.
     * @param input The decoded VCardTextInput for this invocation.
     */
    public static VCardVersionResult detectVersion(AxiomContext ax, VCardTextInput input) {
        ax.log().info("detectVersion handling", Map.of());
        if (input.hasError()) {
            return VCardVersionResult.newBuilder().setError(input.getError()).build();
        }
        try {
            var cards = VCardHelper.parseAll(input.getText());
            VCardVersionResult.Builder b = VCardVersionResult.newBuilder();
            for (var card : cards) {
                String v = card.getVersion() != null ? card.getVersion().getVersion() : "";
                b.addCardVersions(v);
            }
            if (!cards.isEmpty() && cards.get(0).getVersion() != null) {
                b.setVersion(cards.get(0).getVersion().getVersion());
            }
            return b.build();
        } catch (VCardHelper.VCardException e) {
            return VCardVersionResult.newBuilder().setError(VCardHelper.toProtoError(e)).build();
        } catch (Exception e) {
            return VCardVersionResult.newBuilder().setError(VCardHelper.internalError(e)).build();
        }
    }
}
