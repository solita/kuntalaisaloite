package fi.om.municipalityinitiative.service;

import com.google.common.collect.Maps;
import fi.om.municipalityinitiative.dto.Author;
import fi.om.municipalityinitiative.dto.service.AuthorInvitation;
import fi.om.municipalityinitiative.dto.service.Initiative;
import fi.om.municipalityinitiative.dto.service.Participant;
import fi.om.municipalityinitiative.dto.ui.ContactInfo;
import fi.om.municipalityinitiative.util.Locales;
import fi.om.municipalityinitiative.util.Task;
import fi.om.municipalityinitiative.web.Urls;
import org.springframework.context.MessageSource;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Task
public class MailSendingEmailService implements EmailService {

    private static final String INITIATIVE_PREPARE_VERIFICATION_TEMPLATE = "initiative-create-verification";
    private static final String NOT_COLLECTABLE_TEMPLATE = "municipality-not-collaborative";
    private static final String STATUS_INFO_TEMPLATE = "status-info-to-author";
    private static final String NOTIFICATION_TO_MODERATOR = "notification-to-moderator";
    private static final String PARTICIPATION_CONFIRMATION = "participant-verification";
    private static final String COLLABORATIVE_TO_MUNICIPALITY = "municipality-collaborative";
    private static final String AUTHOR_INVITATION = "author-invitation";
    private static final String INVITATION_ACCEPTANCE ="invitation-acceptance";
    private static final String AUTHOR_DELETED_TO_OTHER_AUTHORS = "author-deleted-to-other-authors";
    private static final String AUTHOR_DELETED_TO_DELETED_AUTHOR = "author-deleted-to-deleted-author";

    @Resource
    private MessageSource messageSource;

    @Resource
    private EmailMessageConstructor emailMessageConstructor;

    @Override
    public void sendAuthorConfirmedInvitation(Initiative initiative, String authorsEmail, String managementHash, Locale locale) {

        HashMap<String, Object> dataMap = toDataMap(initiative, locale);
        dataMap.put("managementHash", managementHash);

        emailMessageConstructor
                .fromTemplate(INVITATION_ACCEPTANCE)
                .addRecipient(authorsEmail)
                .withSubject(messageSource.getMessage("email.author.invitation.accepted.subject", toArray(), locale))
                .withDataMap(dataMap)
                .send();

    }

    @Override
    public void sendAuthorInvitation(Initiative initiative, AuthorInvitation authorInvitation) {
        Locale locale = Locales.LOCALE_FI;
        HashMap<String, Object> dataMap = toDataMap(initiative, locale);
        dataMap.put("authorInvitation", authorInvitation);

        emailMessageConstructor
                .fromTemplate(AUTHOR_INVITATION)
                .addRecipient(authorInvitation.getEmail())
                .withSubject(messageSource.getMessage("email.author.invitation.subject", toArray(initiative.getName()), locale))
                .withDataMap(dataMap)
                .send();
    }

    @Override
    public void sendSingleToMunicipality(Initiative initiative, List<Author> authors, String municipalityEmail, Locale locale) {
        emailMessageConstructor
                .fromTemplate(NOT_COLLECTABLE_TEMPLATE)
                .addRecipient(municipalityEmail)
                .withSubject(messageSource.getMessage("email.not.collaborative.municipality.subject", toArray(initiative.getName()), locale))
                .withDataMap(toDataMap(initiative, authors, locale))
                .send();
    }

    @Override
    public void sendAuthorDeletedEmailToOtherAuthors(Initiative initiative, List<String> sendTo, ContactInfo removedAuthorsContactInfo) {

        HashMap<String, Object> dataMap = toDataMap(initiative, Locales.LOCALE_FI);
        dataMap.put("contactInfo", removedAuthorsContactInfo);

        emailMessageConstructor
                .fromTemplate(AUTHOR_DELETED_TO_OTHER_AUTHORS)
                .addRecipients(sendTo)
                .withSubject(messageSource.getMessage("email.author.deleted.to.other.authors.subject", toArray(), Locales.LOCALE_FI))
                .withDataMap(dataMap)
                .send();
    }

    @Override
    public void sendAuthorDeletedEmailToDeletedAuthor(Initiative initiative, String authorEmail) {

        emailMessageConstructor
                .fromTemplate(AUTHOR_DELETED_TO_DELETED_AUTHOR)
                .addRecipient(authorEmail)
                .withSubject(messageSource.getMessage("email.author.deleted.to.deleted.author.subject", toArray(), Locales.LOCALE_FI))
                .withDataMap(toDataMap(initiative, Locales.LOCALE_FI))
                .send();
    }

    @Override
    public void sendCollaborativeToMunicipality(Initiative initiative, List<Author> authors, List<Participant> participants, String municipalityEmail, Locale locale) {
        emailMessageConstructor
                .fromTemplate(COLLABORATIVE_TO_MUNICIPALITY)
                .addRecipient(municipalityEmail)
                .withSubject(messageSource.getMessage("email.not.collaborative.municipality.subject", toArray(initiative.getName()), locale))
                .withDataMap(toDataMap(initiative, authors, locale))
                .withAttachment(initiative, participants)
                .send();
    }

    @Override
    public void sendStatusEmail(Initiative initiative, List<String> sendTo, String municipalityEmail, EmailMessageType emailMessageType) {

        Locale locale = Locales.LOCALE_FI;

        HashMap<String, Object> dataMap = toDataMap(initiative, locale);
        dataMap.put("emailMessageType", emailMessageType);
        if (municipalityEmail != null) { // XXX: Hmm. Shiiit.
            dataMap.put("municipalityEmail", municipalityEmail);
        }

        emailMessageConstructor
                .fromTemplate(STATUS_INFO_TEMPLATE)
                .addRecipients(sendTo)
                .withSubject(messageSource.getMessage("email.status.info." + emailMessageType.name() + ".subject", toArray(), locale))
                .withDataMap(dataMap)
                .send();

    }

    @Override
    public void sendPrepareCreatedEmail(Initiative initiative, Long authorId, String managementHash, String authorEmail, Locale locale) {
        HashMap<String, Object> dataMap = toDataMap(initiative, locale);
        dataMap.put("managementHash", managementHash);
        dataMap.put("authorId", authorId);
        emailMessageConstructor
                .fromTemplate(INITIATIVE_PREPARE_VERIFICATION_TEMPLATE)
                .addRecipient(authorEmail)
                .withSubject(messageSource.getMessage("email.prepare.create.subject", toArray(), locale))
                .withDataMap(dataMap)
                .send();
    }

    @Override
    public void sendNotificationToModerator(Initiative initiative, List<Author> authors, String TEMPORARILY_REPLACING_OM_EMAIL) {

        Locale locale = Locales.LOCALE_FI;

        emailMessageConstructor
                .fromTemplate(NOTIFICATION_TO_MODERATOR)
                        //.withSendToModerator()
                .addRecipient(TEMPORARILY_REPLACING_OM_EMAIL)
                .withSubject(messageSource.getMessage("email.notification.to.moderator.subject", toArray(initiative.getName()), locale))
                .withDataMap(toDataMap(initiative, authors, locale))
                .send();
    }

    @Override
    public void sendParticipationConfirmation(Initiative initiative, String participantEmail, Long participantId, String confirmationCode, Locale locale) {
        HashMap<String, Object> dataMap = toDataMap(initiative, locale);
        dataMap.put("participantId", participantId);
        dataMap.put("confirmationCode", confirmationCode);
        emailMessageConstructor
                .fromTemplate(PARTICIPATION_CONFIRMATION)
                .addRecipient(participantEmail)
                .withSubject(messageSource.getMessage("email.participation.confirmation.subject", toArray(), locale))
                .withDataMap(dataMap)
                .send();
    }

    private static String[] toArray(String... name) {
        return name;
    }

    private <T> HashMap<String, Object> toDataMap(T emailModelObject, Locale locale) {
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("initiative", emailModelObject);
        dataMap.put("localizations", new EmailLocalizationProvider(messageSource, locale));
        dataMap.put("urls", Urls.get(locale));
        dataMap.put("locale", locale);
        dataMap.put("altLocale", Locales.getAltLocale(locale));
        addEnum(EmailMessageType.class, dataMap);
        return dataMap;
    }

    private Map<String, Object> toDataMap(Initiative initiative, List<Author> authors, Locale locale) {
        Map<String, Object> stringObjectMap = toDataMap(initiative, locale);
        stringObjectMap.put("authors", authors);
        return stringObjectMap;
    }

    private static <T extends Enum<?>> void addEnum(Class<T> enumType, Map<String, Object> dataMap) {
        Map<String, T> values = Maps.newHashMap();
        for (T value : enumType.getEnumConstants()) {
            values.put(value.name(), value);
        }
        dataMap.put(enumType.getSimpleName(), values);
    }

    public static class EmailLocalizationProvider {
        private final Locale locale;
        private final MessageSource messageSource;

        public EmailLocalizationProvider(MessageSource messageSource, Locale locale) {
            this.messageSource = messageSource;
            this.locale = locale;
        }

        public String getMessage(String key, Locale locale, Object ... args) {
            return messageSource.getMessage(key, args, locale);
        }

        public String getMessage(String key, Object ... args) {
            return messageSource.getMessage(key, args, locale);
        }
    }
}
