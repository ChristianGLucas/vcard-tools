package nodes;

import axiom.AxiomContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Shared no-op AxiomContext and fixtures for node unit tests — factored out once
 *  rather than duplicated in every *_test.java file. */
final class TestSupport {
    private TestSupport() {}

    /**
     * A single hand-authored RFC 6350 vCard 4.0 fixture, reused across this
     * package's tests. Every expected value asserted against it in the *_test.java
     * files was worked out BY HAND from the RFC 6350 grammar (§6), not derived by
     * running this package's own code — that is what makes it an independent
     * oracle rather than a self-consistency check.
     */
    static final String SAMPLE_VCARD_40 = String.join("\r\n",
            "BEGIN:VCARD",
            "VERSION:4.0",
            "UID:contact-1@example.com",
            "KIND:individual",
            "FN:Dr. Jane Q. Doe",
            "N:Doe;Jane;Quinn;Dr.;",
            "NICKNAME:Janie,JQ",
            "EMAIL;TYPE=work;PREF=1:jane@example.com",
            "EMAIL;TYPE=home:jane.doe@personal.example",
            "TEL;VALUE=uri;TYPE=cell:tel:+1-555-555-0100",
            "TEL;TYPE=work:+1 555 555 0199",
            "ADR;TYPE=work:;Suite 100;123 Main St;Springfield;IL;62704;USA",
            "ORG:Acme Corp;Engineering;Widgets",
            "TITLE:Senior Engineer",
            "ROLE:Individual Contributor",
            "URL;TYPE=work:https://example.com/jane",
            "BDAY:1990-04-15",
            "ANNIVERSARY:2015-06-20",
            "NOTE:First note.",
            "NOTE:Second note.",
            "CATEGORIES:VIP,Engineering",
            "IMPP;TYPE=work:xmpp:jane@example.com",
            "GEO:geo:37.386013,-122.082932",
            "GENDER:F;cisgender woman",
            "LANG:en-US",
            "LANG:fr-FR",
            "TZ:America/New_York",
            "REV:20260701T120000Z",
            "SOURCE:https://example.com/contacts/jane.vcf",
            "END:VCARD",
            "");

    static final String SAMPLE_VCARD_30 = String.join("\r\n",
            "BEGIN:VCARD",
            "VERSION:3.0",
            "FN:Bob Smith",
            "N:Smith;Bob;;;",
            "EMAIL;TYPE=INTERNET:bob@example.com",
            "TEL;TYPE=WORK,VOICE:+1 555 555 0177",
            "END:VCARD",
            "");

    static AxiomContext ax() {
        return new AxiomContext() {
            public Logger log() {
                return new Logger() {
                    public void debug(String m, Map<String, String> a) {}
                    public void info(String m, Map<String, String> a) {}
                    public void warn(String m, Map<String, String> a) {}
                    public void error(String m, Map<String, String> a) {}
                };
            }
            public Secrets secrets() {
                return name -> Optional.empty();
            }
            public String executionId() {
                return "test-execution-id";
            }
            public String flowId() {
                return "test-flow-id";
            }
            public String tenantId() {
                return "test-tenant-id";
            }
            public Reflection reflection() {
                return () -> new FlowReflection() {
                    public List<ReflectionNode> nodes() {
                        return List.of();
                    }
                    public List<ReflectionEdge> edges() {
                        return List.of();
                    }
                    public List<ReflectionEdge> loopEdges() {
                        return List.of();
                    }
                    public FlowPosition position() {
                        return new FlowPosition(0, 0, Map.of(), List.of());
                    }
                    public String graphId() {
                        return "";
                    }
                };
            }
            public Mutation mutation() {
                return () -> new FlowMutation() {
                    public int addNode(String pkg, String ver, CanvasPosition pos) {
                        return 0;
                    }
                    public void addEdge(int src, int dst, EdgeCondition cond) {}
                };
            }
        };
    }
}
