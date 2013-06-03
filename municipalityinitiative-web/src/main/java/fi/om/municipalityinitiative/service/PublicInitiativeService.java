package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.dto.InitiativeCounts;
import fi.om.municipalityinitiative.newdao.AuthorDao;
import fi.om.municipalityinitiative.newdao.InitiativeDao;
import fi.om.municipalityinitiative.newdao.MunicipalityDao;
import fi.om.municipalityinitiative.newdao.ParticipantDao;
import fi.om.municipalityinitiative.dto.InitiativeSearch;
import fi.om.municipalityinitiative.dto.user.LoginUserHolder;
import fi.om.municipalityinitiative.dto.service.ManagementSettings;
import fi.om.municipalityinitiative.dto.service.ParticipantCreateDto;
import fi.om.municipalityinitiative.dto.ui.*;
import fi.om.municipalityinitiative.util.*;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.List;
import java.util.Locale;

import static fi.om.municipalityinitiative.util.SecurityUtil.assertAllowance;


public class PublicInitiativeService {

    @Resource
    InitiativeDao initiativeDao;

    @Resource
    AuthorDao authorDao;

    @Resource
    ParticipantDao participantDao;

    @Resource
    EmailService emailService;

    @Resource
    MunicipalityDao municipalityDao;

    public List<InitiativeListInfo> findMunicipalityInitiatives(InitiativeSearch search, LoginUserHolder loginUserHolder) {

        if (loginUserHolder.getUser().isOmUser() && search.getShow() == InitiativeSearch.Show.all) {
            search.setShow(InitiativeSearch.Show.omAll);
        }

        if (search.getShow().isOmOnly()) {
            loginUserHolder.assertOmUser();
        }

        List<InitiativeListInfo> initiativeListInfos = initiativeDao.find(search);
        if (search.getShow() == InitiativeSearch.Show.omAll)
            search.setShow(InitiativeSearch.Show.all);
        return initiativeListInfos;
    }

    @Transactional(readOnly = true)
    public ManagementSettings getManagementSettings(Long initiativeId) {
        return ManagementSettings.of(initiativeDao.get(initiativeId));
    }

    @Transactional(readOnly = false)
    public Long createParticipant(ParticipantUICreateDto participant, Long initiativeId, Locale locale) {

        assertAllowance("Allowed to participate", getManagementSettings(initiativeId).isAllowParticipate());

        ParticipantCreateDto participantCreateDto = ParticipantCreateDto.parse(participant, initiativeId);
        participantCreateDto.setMunicipalityInitiativeId(initiativeId);

        String confirmationCode = RandomHashGenerator.randomString(20);
        Long participantId = participantDao.create(participantCreateDto, confirmationCode);

        emailService.sendParticipationConfirmation(
                initiativeDao.get(initiativeId),
                participant.getParticipantEmail(),
                participantId,
                confirmationCode,
                locale
        );

        return participantId;
    }

    @Transactional(readOnly = false)
    public Long prepareInitiative(PrepareInitiativeUICreateDto createDto, Locale locale) {

        assertMunicipalityIsActive(createDto.getMunicipality());

        Long initiativeId = initiativeDao.prepareInitiative(createDto.getMunicipality());
        Long participantId = participantDao.prepareParticipant(initiativeId,
                createDto.getHomeMunicipality(),
                createDto.getParticipantEmail(),
                createDto.hasMunicipalMembership() ? createDto.getMunicipalMembership() : Membership.none
        ); // XXX: Remove franchise?
        // XXX: Create dto?
        String managementHash = RandomHashGenerator.randomString(40);
        Long authorId = authorDao.createAuthor(initiativeId, participantId, managementHash);

        emailService.sendPrepareCreatedEmail(initiativeDao.get(initiativeId), authorId, managementHash, createDto.getParticipantEmail(), locale);

        return initiativeId;
    }

    private void assertMunicipalityIsActive(Long municipality) {
        if (!municipalityDao.getMunicipality(municipality).isActive()) {
            throw new AccessDeniedException("Municipality is not active for initiatives: " + municipality);
        }
    }

    @Transactional(readOnly = true)
    public InitiativeViewInfo getMunicipalityInitiative(Long initiativeId) {
        return InitiativeViewInfo.parse(initiativeDao.get(initiativeId));
    }

    @Transactional(readOnly = true)
    public InitiativeCounts getInitiativeCounts(Maybe<Long> municipality, LoginUserHolder loginUserHolder) {
        if (loginUserHolder.getUser().isNotOmUser()) {
            return initiativeDao.getPublicInitiativeCounts(municipality);
        }
        else return initiativeDao.getAllInitiativeCounts(municipality);
    }

    @Transactional(readOnly = false)
    public Long confirmParticipation(Long participantId, String confirmationCode) {
        Long initiativeId = participantDao.getInitiativeIdByParticipant(participantId);
        assertAllowance("Confirm participation", getManagementSettings(initiativeId).isAllowParticipate());

        participantDao.confirmParticipation(participantId, confirmationCode);

        return initiativeId;
    }


}
