package nodes;

import axiom.AxiomContext;
import gen.Messages.EmailList;
import gen.Messages.VCardTextInput;

import java.util.Map;

public class ExtractEmails {

    /**
     * Extract just the EMAIL properties from a document containing exactly
     * one vCard, each with its TYPE parameters and PREF rank. Same
     * single-card discipline as ParseVCard: a structured error, not a guess,
     * when the document holds zero or more than one vCard.
     *
     * @param ax    The AxiomContext (ADR-001): logging, secrets, reflection, mutation.
     * @param input The decoded VCardTextInput for this invocation.
     */
    public static EmailList extractEmails(AxiomContext ax, VCardTextInput input) {
        ax.log().info("extractEmails handling", Map.of());
        if (input.hasError()) {
            return EmailList.newBuilder().setError(input.getError()).build();
        }
        try {
            var vcard = VCardHelper.parseExactlyOne(input.getText());
            var proto = VCardHelper.toProto(vcard);
            return EmailList.newBuilder().addAllEmails(proto.getEmailsList()).build();
        } catch (VCardHelper.VCardException e) {
            return EmailList.newBuilder().setError(VCardHelper.toProtoError(e)).build();
        } catch (Exception e) {
            return EmailList.newBuilder().setError(VCardHelper.internalError(e)).build();
        }
    }
}
