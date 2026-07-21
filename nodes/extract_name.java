package nodes;

import axiom.AxiomContext;
import gen.Messages.VCardNameResult;
import gen.Messages.VCardTextInput;

import java.util.Map;

public class ExtractName {

    /**
     * Extract just the FN (formatted_name) and structured N (name) from a
     * document containing exactly one vCard, without parsing every other
     * property. Same single-card discipline as ParseVCard.
     *
     * @param ax    The AxiomContext (ADR-001): logging, secrets, reflection, mutation.
     * @param input The decoded VCardTextInput for this invocation.
     */
    public static VCardNameResult extractName(AxiomContext ax, VCardTextInput input) {
        ax.log().info("extractName handling", Map.of());
        if (input.hasError()) {
            return VCardNameResult.newBuilder().setError(input.getError()).build();
        }
        try {
            var vcard = VCardHelper.parseExactlyOne(input.getText());
            var proto = VCardHelper.toProto(vcard);
            VCardNameResult.Builder b = VCardNameResult.newBuilder().setFormattedName(proto.getFormattedName());
            if (proto.hasName()) {
                b.setName(proto.getName());
            }
            return b.build();
        } catch (VCardHelper.VCardException e) {
            return VCardNameResult.newBuilder().setError(VCardHelper.toProtoError(e)).build();
        } catch (Exception e) {
            return VCardNameResult.newBuilder().setError(VCardHelper.internalError(e)).build();
        }
    }
}
