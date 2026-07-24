package nodes;

import gen.Messages.Address;
import gen.Messages.Email;
import gen.Messages.Error;
import gen.Messages.Geo;
import gen.Messages.Impp;
import gen.Messages.Name;
import gen.Messages.Phone;
import gen.Messages.Photo;
import gen.Messages.Url;
import gen.Messages.VCard;

import ezvcard.Ezvcard;
import ezvcard.ValidationWarning;
import ezvcard.ValidationWarnings;
import ezvcard.VCardVersion;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.ImageType;
import ezvcard.parameter.ImppType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Anniversary;
import ezvcard.property.Birthday;
import ezvcard.property.Categories;
import ezvcard.property.DateOrTimeProperty;
import ezvcard.property.FormattedName;
import ezvcard.property.Gender;
import ezvcard.property.Kind;
import ezvcard.property.Language;
import ezvcard.property.Nickname;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.Revision;
import ezvcard.property.Role;
import ezvcard.property.Source;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.Title;
import ezvcard.property.Uid;
import ezvcard.property.VCardProperty;
import ezvcard.util.PartialDate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shared parse/build/mapping logic for every node in this package. ez-vcard
 * (the wrapped library) and this package's own proto messages both use the
 * simple names VCard, Email, Address, Url, Impp, Geo, and Photo — this class
 * therefore imports ONLY the proto (gen.Messages.*) versions of those names
 * and refers to ez-vcard's equally-named classes with a fully qualified
 * "ezvcard.VCard" / "ezvcard.property.Email" etc. throughout. Do not add an
 * unqualified import for either colliding pair.
 */
final class VCardHelper {

    private VCardHelper() {}

    /** A structured failure, carrying the same (code, message) shape as the proto Error. */
    static final class VCardException extends Exception {
        final String code;
        VCardException(String code, String message) {
            super(message);
            this.code = code;
        }
        VCardException(String code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }
    }

    static Error toProtoError(VCardException e) {
        return Error.newBuilder().setCode(e.code).setMessage(String.valueOf(e.getMessage())).build();
    }

    /** Maps the handful of unchecked exceptions ez-vcard's property setters throw on
     *  malformed caller-supplied strings (e.g. an IMPP value that is not a valid URI,
     *  or an unparseable REV timestamp) onto the same structured INVALID_ARGUMENT
     *  shape as every other caller-input problem in this package. */
    static Error toProtoError(RuntimeException e) {
        return Error.newBuilder().setCode("INVALID_ARGUMENT").setMessage(String.valueOf(e.getMessage())).build();
    }

    static Error internalError(Throwable t) {
        return Error.newBuilder().setCode("INTERNAL").setMessage(String.valueOf(t.getMessage())).build();
    }

    /** Parses every vCard in the document. Never throws for "zero found" — an empty or
     *  non-vCard document legitimately yields an empty list; only a genuine I/O-level
     *  parse fault throws. */
    static List<ezvcard.VCard> parseAll(String text) throws VCardException {
        if (text == null) {
            throw new VCardException("INVALID_ARGUMENT", "text is required");
        }
        try {
            return Ezvcard.parse(text).all();
        } catch (Exception e) {
            throw new VCardException("INVALID_VCARD", "could not parse vCard text: " + e.getMessage(), e);
        }
    }

    /** Parses the document and requires it to contain EXACTLY ONE vCard — the discipline
     *  shared by ParseVCard, ValidateVCard, ExtractEmails/Phones/Addresses, and
     *  ExtractName, so that "which card do you mean" is never guessed. */
    static ezvcard.VCard parseExactlyOne(String text) throws VCardException {
        List<ezvcard.VCard> all = parseAll(text);
        if (all.isEmpty()) {
            throw new VCardException("INVALID_ARGUMENT", "no vCard found in document; expected exactly one");
        }
        if (all.size() > 1) {
            throw new VCardException("INVALID_ARGUMENT",
                    "document contains " + all.size()
                            + " vCards; expected exactly one — use ParseVCardList for multi-contact documents");
        }
        return all.get(0);
    }

    static VCardVersion parseVersionArg(String v, String fieldName, boolean allow21) throws VCardException {
        if (v == null || v.isEmpty()) {
            return null;
        }
        VCardVersion parsed = VCardVersion.valueOfByStr(v);
        if (parsed == null || (!allow21 && parsed == VCardVersion.V2_1)) {
            throw new VCardException("INVALID_ARGUMENT",
                    fieldName + " \"" + v + "\" is not a supported vCard version (expected \"3.0\" or \"4.0\")");
        }
        return parsed;
    }

    static String render(ezvcard.VCard v, VCardVersion version) {
        return Ezvcard.write(v).version(version).go();
    }

    // ---- ez-vcard VCard -> proto VCard --------------------------------------------------

    static VCard toProto(ezvcard.VCard v) {
        VCard.Builder b = VCard.newBuilder();
        if (v.getVersion() != null) {
            b.setVersion(v.getVersion().getVersion());
        }
        Uid uid = v.getUid();
        if (uid != null && uid.getValue() != null) {
            b.setUid(uid.getValue());
        }
        Kind kind = v.getKind();
        if (kind != null && kind.getValue() != null) {
            b.setKind(kind.getValue());
        }
        FormattedName fn = v.getFormattedName();
        if (fn != null && fn.getValue() != null) {
            b.setFormattedName(fn.getValue());
        }
        StructuredName sn = v.getStructuredName();
        if (sn != null) {
            Name.Builder nb = Name.newBuilder();
            if (sn.getFamily() != null) {
                nb.setFamily(sn.getFamily());
            }
            if (sn.getGiven() != null) {
                nb.setGiven(sn.getGiven());
            }
            nb.addAllAdditional(sn.getAdditionalNames());
            nb.addAllPrefixes(sn.getPrefixes());
            nb.addAllSuffixes(sn.getSuffixes());
            b.setName(nb.build());
        }
        for (Nickname nn : v.getNicknames()) {
            b.addAllNicknames(nn.getValues());
        }
        for (ezvcard.property.Email e : v.getEmails()) {
            Email.Builder eb = Email.newBuilder();
            if (e.getValue() != null) {
                eb.setValue(e.getValue());
            }
            for (EmailType t : e.getTypes()) {
                if (t.getValue() != null) {
                    eb.addTypes(t.getValue().toLowerCase(Locale.ROOT));
                }
            }
            if (e.getPref() != null) {
                eb.setPref(e.getPref());
            }
            b.addEmails(eb.build());
        }
        for (Telephone t : v.getTelephoneNumbers()) {
            Phone.Builder pb = Phone.newBuilder();
            String value = t.getText();
            if ((value == null || value.isEmpty()) && t.getUri() != null) {
                value = t.getUri().toString();
            }
            if (value != null) {
                pb.setValue(value);
            }
            for (TelephoneType tt : t.getTypes()) {
                if (tt.getValue() != null) {
                    pb.addTypes(tt.getValue().toLowerCase(Locale.ROOT));
                }
            }
            if (t.getPref() != null) {
                pb.setPref(t.getPref());
            }
            b.addPhones(pb.build());
        }
        for (ezvcard.property.Address a : v.getAddresses()) {
            Address.Builder ab = Address.newBuilder();
            if (a.getPoBox() != null) {
                ab.setPoBox(a.getPoBox());
            }
            if (a.getExtendedAddress() != null) {
                ab.setExtended(a.getExtendedAddress());
            }
            if (a.getStreetAddress() != null) {
                ab.setStreet(a.getStreetAddress());
            }
            if (a.getLocality() != null) {
                ab.setLocality(a.getLocality());
            }
            if (a.getRegion() != null) {
                ab.setRegion(a.getRegion());
            }
            if (a.getPostalCode() != null) {
                ab.setPostalCode(a.getPostalCode());
            }
            if (a.getCountry() != null) {
                ab.setCountry(a.getCountry());
            }
            for (AddressType at : a.getTypes()) {
                if (at.getValue() != null) {
                    ab.addTypes(at.getValue().toLowerCase(Locale.ROOT));
                }
            }
            if (a.getPref() != null) {
                ab.setPref(a.getPref());
            }
            if (a.getLabel() != null) {
                ab.setLabel(a.getLabel());
            }
            b.addAddresses(ab.build());
        }
        Organization org = v.getOrganization();
        if (org != null && org.getValues() != null && !org.getValues().isEmpty()) {
            List<String> vals = org.getValues();
            String first = vals.get(0);
            b.setOrg(first == null ? "" : first);
            if (vals.size() > 1) {
                b.addAllOrgUnits(vals.subList(1, vals.size()));
            }
        }
        for (Title t : v.getTitles()) {
            if (t.getValue() != null) {
                b.setTitle(t.getValue());
                break;
            }
        }
        for (Role r : v.getRoles()) {
            if (r.getValue() != null) {
                b.setRole(r.getValue());
                break;
            }
        }
        for (ezvcard.property.Url u : v.getUrls()) {
            Url.Builder ub = Url.newBuilder();
            if (u.getValue() != null) {
                ub.setValue(u.getValue());
            }
            if (u.getType() != null) {
                ub.addTypes(u.getType().toLowerCase(Locale.ROOT));
            }
            if (u.getPref() != null) {
                ub.setPref(u.getPref());
            }
            b.addUrls(ub.build());
        }
        String bday = dateOrTimeToString(v.getBirthday());
        if (bday != null) {
            b.setBirthday(bday);
        }
        String anniv = dateOrTimeToString(v.getAnniversary());
        if (anniv != null) {
            b.setAnniversary(anniv);
        }
        for (Note n : v.getNotes()) {
            if (n.getValue() != null) {
                b.addNotes(n.getValue());
            }
        }
        Categories cats = v.getCategories();
        if (cats != null && cats.getValues() != null) {
            b.addAllCategories(cats.getValues());
        }
        List<ezvcard.property.Photo> photos = v.getPhotos();
        if (!photos.isEmpty()) {
            ezvcard.property.Photo p = photos.get(0);
            Photo.Builder pb = Photo.newBuilder();
            if (p.getContentType() != null && p.getContentType().getMediaType() != null) {
                pb.setMimeType(p.getContentType().getMediaType());
            }
            if (p.getUrl() != null) {
                pb.setUrl(p.getUrl());
                pb.setHasInlineData(false);
            } else if (p.getData() != null) {
                pb.setHasInlineData(true);
                pb.setByteSize(p.getData().length);
            } else {
                pb.setHasInlineData(false);
            }
            b.setPhoto(pb.build());
        }
        for (ezvcard.property.Impp imp : v.getImpps()) {
            Impp.Builder ib = Impp.newBuilder();
            if (imp.getUri() != null) {
                ib.setValue(imp.getUri().toString());
            }
            for (ImppType t : imp.getTypes()) {
                if (t.getValue() != null) {
                    ib.addTypes(t.getValue().toLowerCase(Locale.ROOT));
                }
            }
            if (imp.getPref() != null) {
                ib.setPref(imp.getPref());
            }
            b.addImpp(ib.build());
        }
        ezvcard.property.Geo g = v.getGeo();
        if (g != null) {
            Geo.Builder gb = Geo.newBuilder();
            if (g.getLatitude() != null) {
                gb.setLatitude(g.getLatitude());
            }
            if (g.getLongitude() != null) {
                gb.setLongitude(g.getLongitude());
            }
            if (g.getGeoUri() != null) {
                gb.setRaw(g.getGeoUri().toString());
            }
            b.setGeo(gb.build());
        }
        Gender gender = v.getGender();
        if (gender != null) {
            String code = gender.getGender();
            String text = gender.getText();
            StringBuilder sb = new StringBuilder();
            if (code != null) {
                sb.append(code);
            }
            if (text != null && !text.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(';');
                }
                sb.append(text);
            }
            if (sb.length() > 0) {
                b.setGender(sb.toString());
            }
        }
        for (Language l : v.getLanguages()) {
            if (l.getValue() != null) {
                b.addLanguages(l.getValue());
            }
        }
        Timezone tz = v.getTimezone();
        if (tz != null) {
            if (tz.getText() != null && !tz.getText().isEmpty()) {
                b.setTimezone(tz.getText());
            } else if (tz.getOffset() != null) {
                b.setTimezone(tz.getOffset().toString());
            }
        }
        Revision rev = v.getRevision();
        if (rev != null && rev.getValue() != null) {
            b.setRevision(rev.getValue().toString());
        }
        for (Source s : v.getSources()) {
            if (s.getValue() != null) {
                b.addSources(s.getValue());
            }
        }
        return b.build();
    }

    private static String dateOrTimeToString(DateOrTimeProperty prop) {
        if (prop == null) {
            return null;
        }
        Temporal date = prop.getDate();
        if (date != null) {
            return date.toString();
        }
        PartialDate pd = prop.getPartialDate();
        if (pd != null) {
            return pd.toISO8601(true);
        }
        String text = prop.getText();
        if (text != null && !text.isEmpty()) {
            return text;
        }
        return null;
    }

    // ---- proto VCard -> ez-vcard VCard (BuildVCard) -------------------------------------

    /** Builds an ez-vcard VCard from the normalized proto VCard. Throws VCardException for
     *  the one structural requirement this package enforces (a usable name); malformed
     *  individual field values (e.g. an IMPP value that is not a valid URI) surface as the
     *  library's own IllegalArgumentException, which callers of this method should catch
     *  and map with {@link #toProtoError(RuntimeException)}. */
    static ezvcard.VCard toEzVcard(VCard in) throws VCardException {
        boolean hasStructuredContent = in.hasName()
                && (!in.getName().getFamily().isEmpty() || !in.getName().getGiven().isEmpty()
                        || in.getName().getAdditionalCount() > 0);
        if (in.getFormattedName().isEmpty() && !hasStructuredContent) {
            throw new VCardException("INVALID_ARGUMENT",
                    "formatted_name or name is required to build a vCard");
        }

        ezvcard.VCard v = new ezvcard.VCard();

        if (!in.getFormattedName().isEmpty()) {
            v.setFormattedName(in.getFormattedName());
        } else {
            v.setFormattedName(synthesizeFormattedName(in.getName()));
        }

        if (in.hasName()) {
            Name n = in.getName();
            StructuredName sn = new StructuredName();
            if (!n.getFamily().isEmpty()) {
                sn.setFamily(n.getFamily());
            }
            if (!n.getGiven().isEmpty()) {
                sn.setGiven(n.getGiven());
            }
            sn.getAdditionalNames().addAll(n.getAdditionalList());
            sn.getPrefixes().addAll(n.getPrefixesList());
            sn.getSuffixes().addAll(n.getSuffixesList());
            v.setStructuredName(sn);
        }

        if (!in.getUid().isEmpty()) {
            v.setUid(new Uid(in.getUid()));
        }
        if (!in.getKind().isEmpty()) {
            v.setKind(new Kind(in.getKind()));
        }
        if (!in.getNicknamesList().isEmpty()) {
            v.setNickname(in.getNicknamesList().toArray(new String[0]));
        }
        for (Email e : in.getEmailsList()) {
            ezvcard.property.Email em = new ezvcard.property.Email(e.getValue());
            for (String t : e.getTypesList()) {
                em.getTypes().add(EmailType.get(t));
            }
            if (e.getPref() != 0) {
                em.setPref(e.getPref());
            }
            v.addEmail(em);
        }
        for (Phone p : in.getPhonesList()) {
            Telephone tel = new Telephone(p.getValue());
            for (String t : p.getTypesList()) {
                tel.getTypes().add(TelephoneType.get(t));
            }
            if (p.getPref() != 0) {
                tel.setPref(p.getPref());
            }
            v.addTelephoneNumber(tel);
        }
        for (Address a : in.getAddressesList()) {
            ezvcard.property.Address ad = new ezvcard.property.Address();
            if (!a.getPoBox().isEmpty()) {
                ad.setPoBox(a.getPoBox());
            }
            if (!a.getExtended().isEmpty()) {
                ad.setExtendedAddress(a.getExtended());
            }
            if (!a.getStreet().isEmpty()) {
                ad.setStreetAddress(a.getStreet());
            }
            if (!a.getLocality().isEmpty()) {
                ad.setLocality(a.getLocality());
            }
            if (!a.getRegion().isEmpty()) {
                ad.setRegion(a.getRegion());
            }
            if (!a.getPostalCode().isEmpty()) {
                ad.setPostalCode(a.getPostalCode());
            }
            if (!a.getCountry().isEmpty()) {
                ad.setCountry(a.getCountry());
            }
            for (String t : a.getTypesList()) {
                ad.getTypes().add(AddressType.get(t));
            }
            if (a.getPref() != 0) {
                ad.setPref(a.getPref());
            }
            if (!a.getLabel().isEmpty()) {
                ad.setLabel(a.getLabel());
            }
            v.addAddress(ad);
        }
        if (!in.getOrg().isEmpty() || !in.getOrgUnitsList().isEmpty()) {
            List<String> vals = new ArrayList<>();
            vals.add(in.getOrg());
            vals.addAll(in.getOrgUnitsList());
            v.setOrganization(vals.toArray(new String[0]));
        }
        if (!in.getTitle().isEmpty()) {
            v.addTitle(in.getTitle());
        }
        if (!in.getRole().isEmpty()) {
            v.addRole(in.getRole());
        }
        for (Url u : in.getUrlsList()) {
            ezvcard.property.Url uu = new ezvcard.property.Url(u.getValue());
            if (!u.getTypesList().isEmpty()) {
                uu.setType(u.getTypesList().get(0));
            }
            if (u.getPref() != 0) {
                uu.setPref(u.getPref());
            }
            v.addUrl(uu);
        }
        if (!in.getBirthday().isEmpty()) {
            v.setBirthday(buildBirthday(in.getBirthday()));
        }
        if (!in.getAnniversary().isEmpty()) {
            v.setAnniversary(buildAnniversary(in.getAnniversary()));
        }
        for (String note : in.getNotesList()) {
            if (!note.isEmpty()) {
                v.addNote(note);
            }
        }
        if (!in.getCategoriesList().isEmpty()) {
            v.setCategories(in.getCategoriesList().toArray(new String[0]));
        }
        // Photo: this envelope carries metadata only (never raw bytes, to keep node
        // output bounded regardless of source image size — see the Photo message doc).
        // A photo can therefore only be rebuilt when it was itself URL-referenced;
        // one that was inline data at parse time cannot be round-tripped and is
        // deliberately, silently not re-embedded (there is nothing to embed).
        if (in.hasPhoto() && !in.getPhoto().getUrl().isEmpty()) {
            String mime = in.getPhoto().getMimeType();
            ImageType type = mime.isEmpty() ? null : ImageType.get(null, mime, null);
            v.addPhoto(new ezvcard.property.Photo(in.getPhoto().getUrl(), type));
        }
        for (Impp i : in.getImppList()) {
            ezvcard.property.Impp imp = new ezvcard.property.Impp(i.getValue());
            for (String t : i.getTypesList()) {
                imp.getTypes().add(ImppType.get(t));
            }
            if (i.getPref() != 0) {
                imp.setPref(i.getPref());
            }
            v.addImpp(imp);
        }
        if (in.hasGeo() && in.getGeo().hasLatitude() && in.getGeo().hasLongitude()) {
            v.setGeo(in.getGeo().getLatitude(), in.getGeo().getLongitude());
        }
        if (!in.getGender().isEmpty()) {
            String g = in.getGender();
            int idx = g.indexOf(';');
            String code = idx >= 0 ? g.substring(0, idx) : g;
            String text = idx >= 0 ? g.substring(idx + 1) : null;
            Gender gd = new Gender(code);
            if (text != null && !text.isEmpty()) {
                gd.setText(text);
            }
            v.setGender(gd);
        }
        for (String lang : in.getLanguagesList()) {
            if (!lang.isEmpty()) {
                v.addLanguage(lang);
            }
        }
        if (!in.getTimezone().isEmpty()) {
            v.setTimezone(new Timezone(in.getTimezone()));
        }
        if (!in.getRevision().isEmpty()) {
            v.setRevision(new Revision(Instant.parse(in.getRevision())));
        }
        for (String src : in.getSourcesList()) {
            if (!src.isEmpty()) {
                v.addSource(src);
            }
        }
        return v;
    }

    private static String synthesizeFormattedName(Name n) {
        if (n == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        appendJoined(sb, n.getPrefixesList());
        appendPart(sb, n.getGiven());
        appendJoined(sb, n.getAdditionalList());
        appendPart(sb, n.getFamily());
        appendJoined(sb, n.getSuffixesList());
        return sb.length() == 0 ? "" : sb.toString();
    }

    private static void appendPart(StringBuilder sb, String part) {
        if (part != null && !part.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(part);
        }
    }

    private static void appendJoined(StringBuilder sb, List<String> parts) {
        for (String p : parts) {
            appendPart(sb, p);
        }
    }

    private static Birthday buildBirthday(String s) {
        try {
            return new Birthday(LocalDate.parse(s));
        } catch (DateTimeParseException e1) {
            try {
                return new Birthday(PartialDate.parse(s));
            } catch (Exception e2) {
                return new Birthday(s);
            }
        }
    }

    private static Anniversary buildAnniversary(String s) {
        try {
            return new Anniversary(LocalDate.parse(s));
        } catch (DateTimeParseException e1) {
            try {
                return new Anniversary(PartialDate.parse(s));
            } catch (Exception e2) {
                return new Anniversary(s);
            }
        }
    }

    // ---- validation --------------------------------------------------------------------

    static List<gen.Messages.ValidationIssue> validationIssues(ValidationWarnings warnings) {
        List<gen.Messages.ValidationIssue> out = new ArrayList<>();
        for (Map.Entry<VCardProperty, List<ValidationWarning>> entry : warnings) {
            String propName = entry.getKey() == null ? "" : entry.getKey().getClass().getSimpleName();
            for (ValidationWarning w : entry.getValue()) {
                out.add(gen.Messages.ValidationIssue.newBuilder()
                        .setSeverity("ERROR")
                        .setProperty(propName)
                        .setMessage(String.valueOf(w.getMessage()))
                        .build());
            }
        }
        return out;
    }
}
