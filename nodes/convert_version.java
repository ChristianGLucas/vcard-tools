package nodes;

import axiom.AxiomContext;
import ezvcard.VCardVersion;
import gen.Messages.VCardConvertInput;
import gen.Messages.VCardTextOutput;

import java.util.Map;

public class ConvertVersion {

    /**
     * Convert every vCard in a document to a target version ("3.0" or
     * "4.0"). Re-parses each source vCard and re-serializes it at the target
     * version, remapping version-specific constructs (e.g. vCard 2.1/3.0's
     * bare TEL values become vCard 4's "tel:" URIs; vCard 4-only properties
     * such as KIND/ANNIVERSARY/GENDER are dropped when the target is 3.0,
     * since 3.0 has no equivalent). Rejects a target_version other than
     * "3.0"/"4.0" with a structured error.
     *
     * @param ax    The AxiomContext (ADR-001): logging, secrets, reflection, mutation.
     * @param input The decoded VCardConvertInput for this invocation.
     */
    public static VCardTextOutput convertVersion(AxiomContext ax, VCardConvertInput input) {
        ax.log().info("convertVersion handling", Map.of());
        if (input.hasError()) {
            return VCardTextOutput.newBuilder().setError(input.getError()).build();
        }
        try {
            VCardVersion target = VCardHelper.parseVersionArg(input.getTargetVersion(), "target_version", false);
            if (target == null) {
                return VCardTextOutput.newBuilder()
                        .setError(gen.Messages.Error.newBuilder()
                                .setCode("INVALID_ARGUMENT")
                                .setMessage("target_version is required (\"3.0\" or \"4.0\")")
                                .build())
                        .build();
            }
            var cards = VCardHelper.parseAll(input.getText());
            StringBuilder out = new StringBuilder();
            for (var card : cards) {
                out.append(VCardHelper.render(card, target));
            }
            return VCardTextOutput.newBuilder().setText(out.toString()).build();
        } catch (VCardHelper.VCardException e) {
            return VCardTextOutput.newBuilder().setError(VCardHelper.toProtoError(e)).build();
        } catch (Exception e) {
            return VCardTextOutput.newBuilder().setError(VCardHelper.internalError(e)).build();
        }
    }
}
