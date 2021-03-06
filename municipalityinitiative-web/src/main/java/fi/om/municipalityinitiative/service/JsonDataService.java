package fi.om.municipalityinitiative.service;

import com.google.common.collect.Lists;
import fi.om.municipalityinitiative.dao.InitiativeDao;
import fi.om.municipalityinitiative.dao.MunicipalityDao;
import fi.om.municipalityinitiative.dao.ParticipantDao;
import fi.om.municipalityinitiative.dto.Author;
import fi.om.municipalityinitiative.dto.InitiativeSearch;
import fi.om.municipalityinitiative.dto.NormalAuthor;
import fi.om.municipalityinitiative.dto.json.InitiativeJson;
import fi.om.municipalityinitiative.dto.json.InitiativeListJson;
import fi.om.municipalityinitiative.dto.service.Initiative;
import fi.om.municipalityinitiative.dto.service.Municipality;
import fi.om.municipalityinitiative.dto.service.NormalParticipant;
import fi.om.municipalityinitiative.dto.service.Participant;
import fi.om.municipalityinitiative.dto.ui.ContactInfo;
import fi.om.municipalityinitiative.dto.ui.InitiativeListInfo;
import fi.om.municipalityinitiative.dto.ui.ParticipantCount;
import fi.om.municipalityinitiative.dto.ui.PublicAuthors;
import fi.om.municipalityinitiative.exceptions.AccessDeniedException;
import fi.om.municipalityinitiative.service.ui.AuthorService;
import fi.om.municipalityinitiative.util.InitiativeType;
import fi.om.municipalityinitiative.web.controller.ApiController;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JsonDataService {

    @Resource
    InitiativeDao initiativeDao;

    @Resource
    ParticipantDao participantDao;

    @Resource
    MunicipalityDao municipalityDao;

    @Resource
    AuthorService authorService;

    @Transactional(readOnly = true)
    public List<InitiativeListJson> findJsonInitiatives(InitiativeSearch search) {
        List<InitiativeListJson> result = Lists.newArrayList();
        for (InitiativeListInfo initiativeListInfo : initiativeDao.findCached(search).list) {
            result.add(new InitiativeListJson(initiativeListInfo));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public InitiativeJson getInitiative(Long id) {
        Initiative initiativeInfo = initiativeDao.get(id);

        if (!initiativeInfo.isPublic()) {
            throw new AccessDeniedException("No access for initiative with id: " + id);
        }

        ParticipantCount participantCount = new ParticipantCount();
        participantCount.setPublicNames(initiativeInfo.getParticipantCountPublic());
        participantCount.setPrivateNames(initiativeInfo.getParticipantCount() - participantCount.getPublicNames());
        return InitiativeJson.from(
                initiativeInfo,
                participantCount,
                authorService.findPublicAuthors(id));

    }

    @Transactional(readOnly = true)
    public List<Municipality> getMunicipalities() {
        return municipalityDao.findMunicipalities(true);
    }

    @Transactional(readOnly = true)
    public Municipality getMunicipality(Long id) {
        return municipalityDao.getMunicipality(id);
    }

    public static List<InitiativeListJson> createInitiativeListJsonObject() {
        InitiativeListInfo initiative = new InitiativeListInfo();
        initiative.setMunicipality(ApiController.TAMPERE);
        initiative.setSentTime(Optional.of(new LocalDate(2012, 12, 24)));
        initiative.setCollaborative(true);
        initiative.setStateTime(new LocalDate(2012, 12, 1));
        initiative.setId(1L);
        initiative.setName("Tämä on esimerkkialoitteen otsikko");
        initiative.setParticipantCount(2);
        initiative.setType(InitiativeType.COLLABORATIVE_CITIZEN);
        return Collections.singletonList(new InitiativeListJson(initiative));
    }

    public static InitiativeJson createInitiativeJsonObject() {

        ParticipantCount participantCount = new ParticipantCount();
        participantCount.setPrivateNames(10);
        participantCount.setPublicNames(1);

        ArrayList<Participant> publicParticipants = Lists.newArrayList();

        NormalParticipant participant = new NormalParticipant();
        participant.setParticipateDate(new LocalDate(2010, 1, 1));
        participant.setName("Teemu Teekkari");
        participant.setHomeMunicipality(Optional.of(ApiController.TAMPERE));

        publicParticipants.add(participant);

        Initiative initiativeInfo = new Initiative();
        initiativeInfo.setId(1L);
        initiativeInfo.setName("Tämä on esimerkkialoitteen otsikko");
        initiativeInfo.setProposal("Tämä on esimerkkialoitteen sisältö");
        initiativeInfo.setMunicipality(ApiController.TAMPERE);
        initiativeInfo.setSentTime(Optional.<LocalDate>ofNullable(null));
        initiativeInfo.setStateTime(new LocalDate(2010, 1, 1));

        initiativeInfo.setType(InitiativeType.COLLABORATIVE);

        PublicAuthors authors = new PublicAuthors(createAuthors());

        InitiativeJson initiativeJson = InitiativeJson.from(initiativeInfo, participantCount, authors);

        return initiativeJson;

    }

    private static List<Author> createAuthors() {
        List<Author> authors = Lists.newArrayList();

        Author author1 = new NormalAuthor();
        ContactInfo contactInfo1 = new ContactInfo();
        contactInfo1.setName("Teemu Teekkari");
        contactInfo1.setShowName(true);
        author1.setContactInfo(contactInfo1);
        author1.setMunicipality(Optional.of(ApiController.TAMPERE));
        authors.add(author1);

        Author author2 = new NormalAuthor();
        ContactInfo contactInfo2 = new ContactInfo();
        contactInfo2.setShowName(false);
        author2.setContactInfo(contactInfo2);
        author2.setMunicipality(Optional.of(ApiController.TAMPERE));
        authors.add(author2);

        return authors;
    }
}
