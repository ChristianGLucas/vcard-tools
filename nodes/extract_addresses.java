package nodes;

import axiom.AxiomContext;
import gen.Messages.AddressList;
import gen.Messages.VCardTextInput;

import java.util.Map;

public class ExtractAddresses {

    /**
     * Extract just the ADR properties from a document containing exactly one
     * vCard, each split into its structured components (PO box, street,
     * locality, region, postal code, country) plus TYPE/PREF/LABEL. Same
     * single-card discipline as ParseVCard.
     *
     * @param ax    The AxiomContext (ADR-001): logging, secrets, reflection, mutation.
     * @param input The decoded VCardTextInput for this invocation.
     */
    public static AddressList extractAddresses(AxiomContext ax, VCardTextInput input) {
        ax.log().info("extractAddresses handling", Map.of());
        if (input.hasError()) {
            return AddressList.newBuilder().setError(input.getError()).build();
        }
        try {
            var vcard = VCardHelper.parseExactlyOne(input.getText());
            var proto = VCardHelper.toProto(vcard);
            return AddressList.newBuilder().addAllAddresses(proto.getAddressesList()).build();
        } catch (VCardHelper.VCardException e) {
            return AddressList.newBuilder().setError(VCardHelper.toProtoError(e)).build();
        } catch (Exception e) {
            return AddressList.newBuilder().setError(VCardHelper.internalError(e)).build();
        }
    }
}
