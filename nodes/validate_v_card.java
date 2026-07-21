package nodes;

import axiom.AxiomContext;
import ezvcard.VCardVersion;
import gen.Messages.VCardTextInput;
import gen.Messages.VCardValidationResult;

import java.util.Map;

public class ValidateVCard {

    /**
     * Validate a document containing exactly one vCard against its own
     * declared (or, if absent, inferred 4.0) version's rules: reports
     * valid=false with one issue per problem ez-vcard's validator raises
     * (e.g. a vCard 4 card missing its required VERSION/FN) rather than
     * throwing. valid=true means the document is exactly one vCard, it
     * parses, and no issue was raised. Same single-card discipline as
     * ParseVCard: a structured error, not a guess, when the document holds
     * zero or more than one vCard.
     *
     * @param ax    The AxiomContext (ADR-001): logging, secrets, reflection, mutation.
     * @param input The decoded VCardTextInput for this invocation.
     */
    public static VCardValidationResult validateVCard(AxiomContext ax, VCardTextInput input) {
        ax.log().info("validateVCard handling", Map.of());
        if (input.hasError()) {
            return VCardValidationResult.newBuilder().setError(input.getError()).build();
        }
        try {
            var vcard = VCardHelper.parseExactlyOne(input.getText());
            VCardVersion version = vcard.getVersion() != null ? vcard.getVersion() : VCardVersion.V4_0;
            var warnings = vcard.validate(version);
            var issues = VCardHelper.validationIssues(warnings);
            return VCardValidationResult.newBuilder()
                    .setValid(issues.isEmpty())
                    .addAllIssues(issues)
                    .build();
        } catch (VCardHelper.VCardException e) {
            return VCardValidationResult.newBuilder()
                    .setValid(false)
                    .addIssues(gen.Messages.ValidationIssue.newBuilder()
                            .setSeverity("ERROR")
                            .setMessage(String.valueOf(e.getMessage()))
                            .build())
                    .build();
        } catch (Exception e) {
            return VCardValidationResult.newBuilder().setError(VCardHelper.internalError(e)).build();
        }
    }
}
