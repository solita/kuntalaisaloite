package fi.om.municipalityinitiative.service.ui;

import fi.om.municipalityinitiative.dao.ParticipantDao;
import fi.om.municipalityinitiative.dao.UserDao;
import fi.om.municipalityinitiative.dto.service.Municipality;
import fi.om.municipalityinitiative.dto.ui.InitiativeViewInfo;
import fi.om.municipalityinitiative.exceptions.NotFoundException;
import fi.om.municipalityinitiative.dao.AuthorDao;
import fi.om.municipalityinitiative.dao.InitiativeDao;
import fi.om.municipalityinitiative.dto.Author;
import fi.om.municipalityinitiative.dto.user.LoginUserHolder;
import fi.om.municipalityinitiative.dto.service.Initiative;
import fi.om.municipalityinitiative.dto.service.ManagementSettings;
import fi.om.municipalityinitiative.dto.ui.ContactInfo;
import fi.om.municipalityinitiative.dto.ui.InitiativeDraftUIEditDto;
import fi.om.municipalityinitiative.dto.ui.InitiativeUIUpdateDto;
import fi.om.municipalityinitiative.service.email.EmailMessageType;
import fi.om.municipalityinitiative.service.email.EmailService;
import fi.om.municipalityinitiative.service.operations.InitiativeManagementServiceOperations;
import fi.om.municipalityinitiative.util.InitiativeType;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.Locale;

import static fi.om.municipalityinitiative.util.SecurityUtil.assertAllowance;

public class InitiativeManagementService {

    @Resource
    InitiativeDao initiativeDao;

    @Resource
    AuthorDao authorDao;

    @Resource
    EmailService emailService;

    @Resource
    InitiativeManagementServiceOperations operations;

    @Resource
    UserDao userDao;

    @Resource
    ParticipantDao participantDao;

    @Transactional(readOnly = true)
    public InitiativeDraftUIEditDto getInitiativeDraftForEdit(Long initiativeId, LoginUserHolder loginUserHolder) {
        loginUserHolder.assertManagementRightsForInitiative(initiativeId);
        Initiative initiative = initiativeDao.get(initiativeId);
        assertAllowance("Edit initiative", ManagementSettings.of(initiative).isAllowEdit());
        ContactInfo contactInfo;

        if (initiative.getType().isNotVerifiable()) {
            contactInfo = authorDao.getAuthor(loginUserHolder.getNormalLoginUser().getAuthorId()).getContactInfo();
        }
        else {
            //contactInfo = userDao.getVerifiedUser(loginUserHolder.getVerifiedUser().getHash()).get().getContactInfo();
            contactInfo = authorDao.getVerifiedAuthorContactInfo(initiativeId, loginUserHolder.getVerifiedUser().getHash());
        }
        return InitiativeDraftUIEditDto.parse(
                initiative,
                contactInfo
        );
    }

    private ManagementSettings getManagementSettings(Long initiativeId) {
        return ManagementSettings.of(initiativeDao.get(initiativeId));
    }

    @Transactional(readOnly = false)
    public void editInitiativeDraft(Long initiativeId, LoginUserHolder loginUserHolder, InitiativeDraftUIEditDto editDto) {
        loginUserHolder.assertManagementRightsForInitiative(initiativeId);

        Initiative initiative = initiativeDao.get(initiativeId);

        assertAllowance("Edit initiative", ManagementSettings.of(initiative).isAllowEdit());
        initiativeDao.editInitiativeDraft(initiativeId, editDto);
        if (initiative.getType().isNotVerifiable()) {
            authorDao.updateAuthorInformation(loginUserHolder.getNormalLoginUser().getAuthorId(), editDto.getContactInfo());
        }
        else {
            String hash = loginUserHolder.getVerifiedUser().getHash();
            userDao.updateUserInformation(hash, editDto.getContactInfo());
            participantDao.updateVerifiedParticipantShowName(initiativeId, hash, editDto.getContactInfo().isShowName());
        }
    }

    @Transactional(readOnly = true)
    public InitiativeUIUpdateDto getInitiativeForUpdate(Long initiativeId, LoginUserHolder loginUserHolder) {

        loginUserHolder.assertManagementRightsForInitiative(initiativeId);
        assertAllowance("Update initiative", getManagementSettings(initiativeId).isAllowUpdate());

        Initiative initiative = initiativeDao.get(initiativeId);

        ContactInfo contactInfo = authorDao.getAuthor(loginUserHolder.getNormalLoginUser().getAuthorId()).getContactInfo();

        InitiativeUIUpdateDto updateDto = new InitiativeUIUpdateDto();
        updateDto.setContactInfo(contactInfo);
        updateDto.setExtraInfo(initiative.getExtraInfo());
        updateDto.setExternalParticipantCount(initiative.getExternalParticipantCount());

        return updateDto;
    }

    @Transactional(readOnly = true)
    // TODO: Tests?
    public Author getAuthorInformation(Long initiativeId, LoginUserHolder loginUserHolder) {
        loginUserHolder.assertManagementRightsForInitiative(initiativeId);
        Initiative initiative = initiativeDao.get(initiativeId);
        if (initiative.getType().isNotVerifiable()) {
            for (Author author : authorDao.findAuthors(initiativeId)) {
                if (author.getId().equals(loginUserHolder.getNormalLoginUser().getAuthorId())) {
                    return author;
                }
            }
        }
        else {
            Author author = new Author();
            author.setContactInfo(userDao.getVerifiedUser(loginUserHolder.getVerifiedUser().getHash()).get().getContactInfo());
            author.setMunicipality(new Municipality(-1L, "Kunta", "Municipality", false));
            return author;
        }
        throw new NotFoundException("Author", initiativeId);
    }

    @Transactional(readOnly = false)
    public void updateInitiative(Long initiativeId, LoginUserHolder loginUserHolder, InitiativeUIUpdateDto updateDto) {
        loginUserHolder.assertManagementRightsForInitiative(initiativeId);
        assertAllowance("Update initiative", getManagementSettings(initiativeId).isAllowUpdate());

        initiativeDao.updateExtraInfo(initiativeId, updateDto.getExtraInfo(), updateDto.getExternalParticipantCount());
        authorDao.updateAuthorInformation(loginUserHolder.getNormalLoginUser().getAuthorId(), updateDto.getContactInfo());
    }

    public void sendReviewAndStraightToMunicipality(Long initiativeId, LoginUserHolder loginUserHolder, String sentComment) {
        loginUserHolder.assertManagementRightsForInitiative(initiativeId);
        operations.doSendReviewStraightToMunicipality(initiativeId, sentComment);

        emailService.sendStatusEmail(initiativeId, EmailMessageType.SENT_TO_REVIEW);
        emailService.sendNotificationToModerator(initiativeId);
    }

    public void sendReviewOnlyForAcceptance(Long initiativeId, LoginUserHolder loginUserHolder) {
        loginUserHolder.assertManagementRightsForInitiative(initiativeId);

        operations.doSendReviewOnlyForAcceptance(initiativeId);

        emailService.sendStatusEmail(initiativeId, EmailMessageType.SENT_TO_REVIEW);
        emailService.sendNotificationToModerator(initiativeId);
    }

    public void sendFixToReview(Long initiativeId, LoginUserHolder requiredLoginUserHolder) {
        requiredLoginUserHolder.assertManagementRightsForInitiative(initiativeId);
        operations.toSendFixToReview(initiativeId);

        emailService.sendStatusEmail(initiativeId, EmailMessageType.SENT_FIX_TO_REVIEW);
        emailService.sendNotificationToModerator(initiativeId);
    }

    public void publishAndStartCollecting(Long initiativeId, LoginUserHolder loginUserHolder) {
        loginUserHolder.assertManagementRightsForInitiative(initiativeId);
        operations.doPublishAndStartCollecting(initiativeId);
        emailService.sendStatusEmail(initiativeId, EmailMessageType.PUBLISHED_COLLECTING);
    }

    public void sendToMunicipality(Long initiativeId, LoginUserHolder requiredLoginUserHolder, String sentComment, Locale locale) {

        requiredLoginUserHolder.assertManagementRightsForInitiative(initiativeId);

        InitiativeType initiativeType = operations.doSendToMunicipality(initiativeId, sentComment);

        if (initiativeType.isCollaborative()) {
            emailService.sendCollaborativeToAuthors(initiativeId);
            emailService.sendCollaborativeToMunicipality(initiativeId, locale);
        } else {
            emailService.sendStatusEmail(initiativeId, EmailMessageType.SENT_TO_MUNICIPALITY);
            emailService.sendSingleToMunicipality(initiativeId, locale);
        }
    }

    @Transactional(readOnly = true)
    public InitiativeViewInfo getMunicipalityInitiative(Long initiativeId, LoginUserHolder loginUserHolder) {
        loginUserHolder.assertManagementRightsForInitiative(initiativeId);
        return InitiativeViewInfo.parse(initiativeDao.get(initiativeId));
    }
}
