# vcard-tools

Composable **vCard (RFC 6350 / 2426 / 2425) contact-CARD** nodes for the
[Axiom](https://axiomide.com) marketplace, published as
`christiangeorgelucas/vcard-tools`. Parse a `.vcf` document (single or
multi-contact) into a normalized `VCard` structure, build a valid `.vcf`
document back from that structure, validate a vCard against its version's
rules, detect and convert between vCard versions, extract individual
property groups (emails/phones/addresses/name) from a card, count contacts
in a document, and normalize/canonicalize a vCard — entirely offline and
deterministically.

Written in **Java**, wrapping one battle-tested, permissively-licensed
library:

| Concern | Library | License |
|---|---|---|
| vCard 2.1/3.0/4.0 parsing, model, validation, version conversion | [`ez-vcard`](https://github.com/mangstadt/ez-vcard) (the mature, comprehensive JVM vCard library) | FreeBSD (BSD-2-Clause) |

Every node is **stateless**, **offline** (no network, no API keys, no
signup), and **deterministic**. Only ez-vcard's plain-text `.vcf`
reader/writer is used — never its xCard (XML), jCard (JSON), or hCard
(HTML) readers — so there is no XML-parsing surface (and therefore no XXE
risk) anywhere in this package's parsing path. Input is capped at 5 MiB,
checked before any parsing is attempted.

The PIM sibling of `christiangeorgelucas/icalendar-tools` (calendar files
rather than contact cards) — the two packages share a snake_case field
vocabulary, the same `Error{code,message}` shape, and the same
`valid`+issue-list validation-result shape, so a flow composing a contact
and a calendar event (e.g. building a meeting invite from a parsed contact)
does not need to re-learn conventions at the boundary.

## Use it from your agent or app

Every node in this package is a **live, auto-scaling API endpoint** on the
[Axiom](https://axiomide.com) marketplace — call it from an AI agent or your own
code, with nothing to self-host.

**📦 See it on the marketplace:**
https://dev.axiomide.com/marketplace/christiangeorgelucas/vcard-tools@0.1.0

**Hook it up to an AI agent (MCP).** Add Axiom's hosted MCP server to any MCP
client and every node becomes a typed tool your agent can call — search the
catalog, inspect a schema, and invoke it directly.

```bash
# Claude Code
claude mcp add --transport http axiom https://api.axiomide.com/mcp \
  --header "Authorization: Bearer $AXIOM_API_KEY"
```

Claude Desktop, Cursor, or any config-based client:

```json
{
  "mcpServers": {
    "axiom": {
      "type": "http",
      "url": "https://api.axiomide.com/mcp",
      "headers": { "Authorization": "Bearer YOUR_AXIOM_API_KEY" }
    }
  }
}
```

**Call it from the CLI.**

```bash
axiom invoke christiangeorgelucas/vcard-tools/ParseVCard --input '{ ... }'
```

**Call it over HTTP.**

```bash
curl -X POST https://api.axiomide.com/invocations/v1/nodes/christiangeorgelucas/vcard-tools/0.1.0/ParseVCard \
  -H "Authorization: Bearer $AXIOM_API_KEY" \
  -H 'Content-Type: application/json' \
  -d '{ ... }'
```

> Input/output schema for each node is on the marketplace page above, or via
> `axiom inspect node christiangeorgelucas/vcard-tools/ParseVCard`.

### Get started free

Install the CLI:

```bash
# macOS / Linux — Homebrew
brew install axiomide/tap/axiom

# macOS / Linux — install script
curl -fsSL https://raw.githubusercontent.com/AxiomIDE/axiom-releases/main/install.sh | sh
```

**Windows:** download the `windows/amd64` `.zip` from the
[releases page](https://github.com/AxiomIDE/axiom-releases/releases), unzip it,
and put `axiom.exe` on your `PATH`.

Then `axiom version` to verify, `axiom login` (GitHub or Google) to authenticate,
and create an API key under **Console → API Keys**. Docs and sign-up at
**[axiomide.com](https://axiomide.com)**.

## Nodes

| Node | What it does |
|---|---|
| `ParseVCard` | Parse a document containing exactly one vCard into a normalized `VCard`. |
| `ParseVCardList` | Parse a document containing zero or more vCards into a `VCardList`. |
| `BuildVCard` | Generate a `.vcf` document for one vCard from a normalized `VCard` — the reverse of `ParseVCard`. |
| `ValidateVCard` | Validate a single vCard against its version's rules, reporting every issue found. |
| `DetectVersion` | Detect the vCard version(s) present in a document. |
| `ConvertVersion` | Convert every vCard in a document to a target version (3.0 ⇄ 4.0). |
| `ExtractEmails` | Extract just the EMAIL properties from a single-card document. |
| `ExtractPhones` | Extract just the TEL properties from a single-card document. |
| `ExtractAddresses` | Extract just the ADR properties from a single-card document. |
| `CountContacts` | Count the vCards in a document. |
| `NormalizeVCard` | Canonicalize a document's formatting without changing its data. |
| `ExtractName` | Extract just the formatted name and structured name from a single-card document. |

## Single-card discipline

`ParseVCard`, `ValidateVCard`, `ExtractEmails`, `ExtractPhones`,
`ExtractAddresses`, and `ExtractName` all require the input document to
contain **exactly one** vCard — a document with zero or more than one
returns a structured `INVALID_ARGUMENT` error rather than silently picking
one. Use `ParseVCardList` for multi-contact documents.

## Bounds & security

- `.vcf` input is capped at 5 MiB, checked before any parsing is attempted.
- No XML parser is ever invoked (see above) — no XXE surface.
- A `PHOTO` property is captured as **metadata only** (MIME type, URL or
  inline-data size) — raw image bytes are never returned, keeping node
  output bounded regardless of how large an embedded photo is. `BuildVCard`
  can therefore only re-emit a `PHOTO` that was itself URL-referenced; one
  that was inline data at parse time is not (and cannot be) round-tripped.
- Malformed input returns a structured `Error { code, message }` rather
  than crashing, for every node.

## Error contract

Every node returns an `Error { code, message }` on malformed input:
`INVALID_VCARD`, `INVALID_ARGUMENT`, `LIMIT_EXCEEDED`, or `INTERNAL`.
`ValidateVCard` is the exception in spirit only — a document that fails to
parse, or that doesn't contain exactly one vCard, is reported as a
validation finding (`valid=false`), not an operational `Error`, since
parseability/cardinality is exactly what that node reports on.

---

Built for the Axiom marketplace. MIT licensed.
