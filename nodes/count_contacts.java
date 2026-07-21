package nodes;

import axiom.AxiomContext;
import gen.Messages.VCardCountResult;
import gen.Messages.VCardTextInput;

import java.util.Map;

public class CountContacts {

    /**
     * Count the vCards (BEGIN:VCARD/END:VCARD blocks) in a document. 0 is a
     * legitimate result for an empty document or one with no recognizable
     * vCard content — this node only errors on an actual parse/size failure,
     * never on "zero found."
     *
     * @param ax    The AxiomContext (ADR-001): logging, secrets, reflection, mutation.
     * @param input The decoded VCardTextInput for this invocation.
     */
    public static VCardCountResult countContacts(AxiomContext ax, VCardTextInput input) {
        ax.log().info("countContacts handling", Map.of());
        if (input.hasError()) {
            return VCardCountResult.newBuilder().setError(input.getError()).build();
        }
        try {
            var cards = VCardHelper.parseAll(input.getText());
            return VCardCountResult.newBuilder().setCount(cards.size()).build();
        } catch (VCardHelper.VCardException e) {
            return VCardCountResult.newBuilder().setError(VCardHelper.toProtoError(e)).build();
        } catch (Exception e) {
            return VCardCountResult.newBuilder().setError(VCardHelper.internalError(e)).build();
        }
    }
}
