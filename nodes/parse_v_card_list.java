package nodes;

import axiom.AxiomContext;
import gen.Messages.VCardList;
import gen.Messages.VCardTextInput;

import java.util.Map;

public class ParseVCardList {

    /**
     * Parse a document containing zero or more vCards into a VCardList (one
     * normalized VCard per BEGIN:VCARD/END:VCARD block, in document order,
     * plus the count). An empty or whitespace-only document legitimately
     * parses to an empty list rather than an error. Input capped at 5 MiB.
     *
     * @param ax    The AxiomContext (ADR-001): logging, secrets, reflection, mutation.
     * @param input The decoded VCardTextInput for this invocation.
     */
    public static VCardList parseVCardList(AxiomContext ax, VCardTextInput input) {
        ax.log().info("parseVCardList handling", Map.of());
        if (input.hasError()) {
            return VCardList.newBuilder().setError(input.getError()).build();
        }
        try {
            var cards = VCardHelper.parseAll(input.getText());
            VCardList.Builder b = VCardList.newBuilder();
            for (var card : cards) {
                b.addCards(VCardHelper.toProto(card));
            }
            b.setCount(cards.size());
            return b.build();
        } catch (VCardHelper.VCardException e) {
            return VCardList.newBuilder().setError(VCardHelper.toProtoError(e)).build();
        } catch (Exception e) {
            return VCardList.newBuilder().setError(VCardHelper.internalError(e)).build();
        }
    }
}
