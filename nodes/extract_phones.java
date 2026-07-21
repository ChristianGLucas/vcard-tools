package nodes;

import axiom.AxiomContext;
import gen.Messages.PhoneList;
import gen.Messages.VCardTextInput;

import java.util.Map;

public class ExtractPhones {

    /**
     * Extract just the TEL properties from a document containing exactly one
     * vCard, each with its TYPE parameters and PREF rank. Same single-card
     * discipline as ParseVCard.
     *
     * @param ax    The AxiomContext (ADR-001): logging, secrets, reflection, mutation.
     * @param input The decoded VCardTextInput for this invocation.
     */
    public static PhoneList extractPhones(AxiomContext ax, VCardTextInput input) {
        ax.log().info("extractPhones handling", Map.of());
        if (input.hasError()) {
            return PhoneList.newBuilder().setError(input.getError()).build();
        }
        try {
            var vcard = VCardHelper.parseExactlyOne(input.getText());
            var proto = VCardHelper.toProto(vcard);
            return PhoneList.newBuilder().addAllPhones(proto.getPhonesList()).build();
        } catch (VCardHelper.VCardException e) {
            return PhoneList.newBuilder().setError(VCardHelper.toProtoError(e)).build();
        } catch (Exception e) {
            return PhoneList.newBuilder().setError(VCardHelper.internalError(e)).build();
        }
    }
}
