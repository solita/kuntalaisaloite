package fi.om.municipalityinitiative.dto.json;

import fi.om.municipalityinitiative.dto.ui.AuthorInfo;
import fi.om.municipalityinitiative.dto.ui.PublicAuthors;

import java.util.ArrayList;
import java.util.List;

public class PublicAuthorsJson {

    private PublicAuthors publicAuthors;

    public PublicAuthorsJson(PublicAuthors publicAuthors) {
        this.publicAuthors = publicAuthors;
    }

    public List<AuthorJson> getPublicAuthors() {
        return parseAuthors(publicAuthors.getPublicAuthors());
    }

    private static List<AuthorJson> parseAuthors(List<AuthorInfo> publicAuthors) {
        List<AuthorJson> authorJsons = new ArrayList<>();
        for (AuthorInfo publicAuthor : publicAuthors) {
            authorJsons.add(new AuthorJson(publicAuthor));
        }
        return authorJsons;
    }

    public int getPublicNames() {
        return publicAuthors.getPublicNameCount();
    }

    public int getPrivateNames() {
        return publicAuthors.getPrivateNameCount();
    }
}
