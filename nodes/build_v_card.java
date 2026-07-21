package nodes;

import axiom.AxiomContext;
import ezvcard.VCardVersion;
import gen.Messages.VCard;
import gen.Messages.VCardTextOutput;

import java.util.Map;

public class BuildVCard {

    /**
     * Generate a valid .vcf document for a single vCard from a normalized
     * VCard structure — the reverse of ParseVCard; Parse(Build(x)) round-trips
     * every field ParseVCard populates. Serializes at the version named in
     * VCard.version ("2.1", "3.0", or "4.0"; defaults to "4.0" when empty). If
     * formatted_name is empty but name has usable content, a formatted name
     * is synthesized from the name components (RFC 6350 requires FN on every
     * vCard). Rejects a VCard with both formatted_name and name empty with a
     * structured error rather than emitting an invalid vCard.
     *
     * @param ax    The AxiomContext (ADR-001): logging, secrets, reflection, mutation.
     * @param input The decoded VCard for this invocation.
     */
    public static VCardTextOutput buildVCard(AxiomContext ax, VCard input) {
        ax.log().info("buildVCard handling", Map.of());
        if (input.hasError()) {
            return VCardTextOutput.newBuilder().setError(input.getError()).build();
        }
        try {
            VCardVersion version = VCardHelper.parseVersionArg(input.getVersion(), "version", true);
            if (version == null) {
                version = VCardVersion.V4_0;
            }
            var ezVcard = VCardHelper.toEzVcard(input);
            String text = VCardHelper.render(ezVcard, version);
            return VCardTextOutput.newBuilder().setText(text).build();
        } catch (VCardHelper.VCardException e) {
            return VCardTextOutput.newBuilder().setError(VCardHelper.toProtoError(e)).build();
        } catch (RuntimeException e) {
            return VCardTextOutput.newBuilder().setError(VCardHelper.toProtoError(e)).build();
        } catch (Exception e) {
            return VCardTextOutput.newBuilder().setError(VCardHelper.internalError(e)).build();
        }
    }
}
