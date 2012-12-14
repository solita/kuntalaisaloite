package fi.om.municipalityinitiative.service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mysema.commons.lang.Assert;

import fi.om.municipalityinitiative.dto.*;
import fi.om.municipalityinitiative.util.Locales;
import fi.om.municipalityinitiative.util.Task;
import fi.om.municipalityinitiative.web.SummaryMethod;
import fi.om.municipalityinitiative.web.Urls;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Task
public class EmailServiceImpl implements EmailService {

    @Resource FreeMarkerConfigurer freemarkerConfig;
    @Resource MessageSource messageSource;
    @Resource JavaMailSender javaMailSender;

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class); 
    
    private final String baseURL;
    private final String defaultReplyTo;
    private final String testSendTo;
    private final boolean testConsoleOutput;
    private final int invitationExpirationDays;
    private String sendToOM;
    private String sendToVRK;

    private enum NotificationKey {
        OM,
        VRK;
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
    
    public EmailServiceImpl(FreeMarkerConfigurer freemarkerConfig, MessageSource messageSource, JavaMailSender javaMailSender, 
                        String baseURL, String defaultReplyTo, String sendToOM, String sendToVRK, 
                        int invitationExpirationDays, String testSendTo, boolean testConsoleOutput) {
        this.freemarkerConfig = freemarkerConfig;
        this.messageSource = messageSource;
        this.javaMailSender = javaMailSender;
        this.baseURL = baseURL;
        this.defaultReplyTo = defaultReplyTo;
        this.invitationExpirationDays = invitationExpirationDays;
        this.sendToOM = sendToOM;
        this.sendToVRK = sendToVRK;
        
        if (Strings.isNullOrEmpty(testSendTo)) {
            this.testSendTo = null;
        } else {
            this.testSendTo = testSendTo;
        }
        this.testConsoleOutput = testConsoleOutput;
    }

    /* (non-Javadoc)
     * @see fi.om.initiative.service.EmailService#sendInvitation(fi.om.initiative.dto.InitiativeManagement, fi.om.initiative.dto.Invitation)
     */
    @Override
    public void sendInvitation(InitiativeManagement initiative, Invitation invitation) {
        Assert.notNull(invitation, "invitation");
        Assert.notNull(initiative, "initiative");
        Author currentAuthor =  initiative.getCurrentAuthor();
        Assert.notNull(currentAuthor, "currentAuthor");

        String emailSendTo = invitation.getEmail();
        //String emailReplyTo = initiative.getCurrentAuthor().getContactInfo().getEmail();
        String emailReplyTo = null; // currently we use only default reply to address
        String emailSubject;

        if (invitation.isInitiator()) {
            emailSubject = getEmailSubject("invitation.initiator");
        } else if (invitation.isRepresentative()) {
            emailSubject = getEmailSubject("invitation.representative");
        } else if (invitation.isReserve()) {
            emailSubject = getEmailSubject("invitation.reserve");
        } else {
            throw new IllegalArgumentException("Invitation is not representative, reserver nor initiator");
        }
        
        Map<String, Object> dataMap = initMap(new InitiativePublic(initiative), new AuthorInfo(currentAuthor));
        dataMap.put("invitation", invitation);
        dataMap.put("expirationDays", invitationExpirationDays);

        sendEmail(emailSendTo, emailReplyTo, emailSubject, "invitation", dataMap);
    }

    /* (non-Javadoc)
     * @see fi.om.initiative.service.EmailService#sendInvitationSummary(fi.om.initiative.dto.InitiativeManagement)
     */
    @Override
    public void sendInvitationSummary(InitiativeManagement initiative) {
        Assert.notNull(initiative, "initiative");
        Author currentAuthor =  initiative.getCurrentAuthor();
        Assert.notNull(currentAuthor, "currentAuthor");

        String emailSendTo = initiative.getCurrentAuthor().getContactInfo().getEmail();
        if (Strings.isNullOrEmpty(emailSendTo)) {
            return;
        }

        String emailSubject = getEmailSubject("invitation.summary");

        Map<String, Object> dataMap = initMap(new InitiativePublic(initiative), new AuthorInfo(currentAuthor));
        dataMap.put("expirationDays", invitationExpirationDays);
        
        sendEmail(emailSendTo, null, emailSubject, "invitationsummary", dataMap);
    }

    /* (non-Javadoc)
     * @see fi.om.initiative.service.EmailService#sendNotificationToOM(fi.om.initiative.dto.InitiativeManagement)
     */
    @Override
    public void sendNotificationToOM(InitiativeManagement initiative) {
        Assert.notNull(initiative, "initiative");
        sendNotificationTo(initiative, NotificationKey.OM, sendToOM, initMap(new InitiativePublic(initiative)));
    }
    
    /* (non-Javadoc)
     * @see fi.om.initiative.service.EmailService#sendNotificationToVRK(fi.om.initiative.dto.InitiativeManagement, int)
     */
    @Override
    public void sendNotificationToVRK(InitiativeManagement initiative, int batchSize) {
        Assert.notNull(initiative, "initiative");
        Map<String, Object> dataMap = initMap(new InitiativePublic(initiative));
        dataMap.put("batchSize", batchSize);
        sendNotificationTo(initiative, NotificationKey.VRK, sendToVRK, dataMap);
    }

    private void sendNotificationTo(InitiativeManagement initiative, NotificationKey key, String emailSendTo, Map<String, Object> dataMap) {
        Assert.notNull(initiative, "initiative");
        
        String initiativeName = initiative.getName().getFi();
        if (initiativeName == null) {
            initiativeName = initiative.getName().getSv();
        } else if (initiative.getName().getSv() != null) {
            initiativeName += " / " + initiative.getName().getSv();
        }
        String emailSubject = getEmailSubject("notification.to." + key, initiativeName);

        sendEmail(emailSendTo, null, emailSubject, "notification-to-" + key, dataMap);
    }
    
    
    /* (non-Javadoc)
     * @see fi.om.initiative.service.EmailService#sendInvitationRejectedInfoToVEVs(fi.om.initiative.dto.InitiativePublic, java.lang.String, java.util.List)
     */
    @Override
    public void sendInvitationRejectedInfoToVEVs(InitiativeManagement initiative, String rejectedEmail, List<String> authorEmails) {
        Assert.notNull(initiative, "initiative");
        
        Map<String, Object> dataMap = initMap(initiative);
        dataMap.put("rejectedEmail", rejectedEmail);
        dataMap.put("enoughConfirmedAuthors", initiative.isEnoughConfirmedAuthors());
        dataMap.put("totalUnconfirmedCount", initiative.getTotalUnconfirmedCount());

        sendStatusInfoToVEVs(initiative, authorEmails, EmailMessageType.INVITATION_REJECTED, dataMap);
    }

    @Override
    public void sendInvitationAcceptedInfoToVEVs(InitiativeManagement initiative, List<String> authorEmails) {
        Assert.notNull(initiative, "initiative");
        Author currentAuthor =  initiative.getCurrentAuthor();
        sendAuthorStatusInfoToVEVs(initiative, currentAuthor, authorEmails, EmailMessageType.INVITATION_ACCEPTED);
    }

    @Override
    public void sendAuthorConfirmedInfoToVEVs(InitiativeManagement initiative, List<String> authorEmails) {
        Assert.notNull(initiative, "initiative");
        Author currentAuthor =  initiative.getCurrentAuthor();
        sendAuthorStatusInfoToVEVs(initiative, currentAuthor, authorEmails, EmailMessageType.AUTHOR_CONFIRMED);
    }

    @Override
    public void sendAuthorRemovedInfoToVEVs(InitiativeManagement initiative, Author removedAuthor, List<String> authorEmails) {
        sendAuthorStatusInfoToVEVs(initiative, removedAuthor, authorEmails, EmailMessageType.AUTHOR_REMOVED);
    }
    
    private void sendAuthorStatusInfoToVEVs(InitiativeManagement initiative, Author changedAuthor, List<String> authorEmails, EmailMessageType emailMessageType) {
        Assert.notNull(initiative, "initiative");
        Assert.notNull(changedAuthor, "changedAuthor");
        
        Map<String, Object> dataMap = initMap(initiative, new AuthorInfo(changedAuthor));
        dataMap.put("enoughConfirmedAuthors", initiative.isEnoughConfirmedAuthors());
        dataMap.put("totalUnconfirmedCount", initiative.getTotalUnconfirmedCount());

        sendStatusInfoToVEVs(initiative, authorEmails, emailMessageType, dataMap);
    }

    
    
    /* (non-Javadoc)
     * @see fi.om.initiative.service.EmailService#sendConfirmationRequest(fi.om.initiative.dto.InitiativeManagement, fi.om.initiative.dto.Author)
     */
    @Override
    public void sendConfirmationRequest(InitiativeManagement initiative, Author author) {
        Assert.notNull(initiative, "initiative");
        Assert.notNull(author, "author");

        String emailSendTo = author.getContactInfo().getEmail();
        if (Strings.isNullOrEmpty(emailSendTo)) {
            return;

        }
        sendStatusInfoToVEVs(new InitiativePublic(initiative), addAuthorEmail(author, Lists.<String>newArrayList()), EmailMessageType.CONFIRM_ROLE, null);
    }
    
    private List<String> addAuthorEmail(Author author, List<String> emails) {
        String emailSendTo = author.getContactInfo().getEmail();
        if (!Strings.isNullOrEmpty(emailSendTo)) {
            emails.add(emailSendTo);
        }
        return emails;
    }

    /* (non-Javadoc)
     * @see fi.om.initiative.service.EmailService#sendStatusInfoToVEVs(fi.om.initiative.dto.InitiativeManagement, fi.om.initiative.service.EmailMessageType)
     */
    @Override
    public void sendStatusInfoToVEVs(InitiativeManagement initiative, EmailMessageType emailMessageType) {
        Assert.notNull(initiative, "initiative");
        
        Map<String, Object> dataMap = initMap(initiative);
        if (emailMessageType == EmailMessageType.ACCEPTED_BY_OM || emailMessageType == EmailMessageType.REJECTED_BY_OM) {
            dataMap.put("stateComment", initiative.getStateComment());
            dataMap.put("acceptanceIdentifier", initiative.getAcceptanceIdentifier());
        }
        
        sendStatusInfoToVEVs(initiative, getConfirmedAuthorEmails(initiative.getAuthors()), emailMessageType, dataMap);
    }

    private void sendStatusInfoToVEVs(InitiativeBase initiative, List<String> authorEmails, EmailMessageType emailMessageType, Map<String, Object> dataMap) {
        Assert.notNull(initiative, "initiative");

        String emailSubject = getEmailSubject("status.info." + emailMessageType);

        if (dataMap == null) {
            dataMap = initMap(initiative);
        }
        addEnum(EmailMessageType.class, dataMap);
        dataMap.put("emailMessageType", emailMessageType);
        
        sendEmails(authorEmails, null, emailSubject, "status-info-to-vev", dataMap);
    }

    private List<String> getConfirmedAuthorEmails(List<Author> authors) {
        List<String> emails = Lists.newArrayList();
        for (Author author : authors) {
            if (!author.isUnconfirmed()) {
                addAuthorEmail(author, emails);
            }
        }
        return emails;
    }

    private Map<String, Object> initMap(InitiativeBase initiative) {
        return initMap(initiative, null);
    }
    
    private Map<String, Object> initMap(InitiativeBase initiative, AuthorInfo currentAuthor) {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("baseURL", baseURL);
        dataMap.put("urlsFi", Urls.get(Locales.LOCALE_FI));
        dataMap.put("urlsSv", Urls.get(Locales.LOCALE_SV));
        dataMap.put("summaryMethod", SummaryMethod.INSTANCE);
        dataMap.put("dateFormatFi", messageSource.getMessage("date.format", null, Locales.LOCALE_FI));
        dataMap.put("dateFormatSv", messageSource.getMessage("date.format", null, Locales.LOCALE_SV));
        dataMap.put("initiative", initiative);
        if (currentAuthor != null) {
            dataMap.put("currentAuthor", currentAuthor);
        }
        return dataMap;
    }

    private <T extends Enum<?>> void addEnum(Class<T> enumType, Map<String, Object> dataMap) {
        Map<String, T> values = Maps.newHashMap();
        for (T value : enumType.getEnumConstants()) {
            values.put(value.name(), value);
        }
        dataMap.put(enumType.getSimpleName(), values);
    }

    private String getEmailSubject(String code, String param) {
        code = "email.subject." + code;
        if (param == null) {
            return messageSource.getMessage(code, null, Locales.LOCALE_FI);
        } else {
            Object[] args = {param};
            return messageSource.getMessage(code, args, Locales.LOCALE_FI);
        }
    }
    
    private String getEmailSubject(String code) {
        return getEmailSubject(code, null);
    }

    private void sendEmails(List<String> sendTos, String replyTo, String subject, String templateName,  Map<String, Object> dataMap) {
        for (String sendTo : sendTos) {
            sendEmail(sendTo, replyTo, subject, templateName,  dataMap);
        }
    }

    private void sendEmail(String sendTo, String replyTo, String subject, String templateName,  Map<String, Object> dataMap) {
        Assert.notNull(sendTo, "sendTo");

        String text = processTemplate(templateName + "-text", dataMap); 
        String html = processTemplate(templateName + "-html", dataMap); 
        
        text = stripTextRows(text, 2);
        
        if (testSendTo != null) {
            text = "TEST OPTION REPLACED THE EMAIL ADDRESS!\nThe original address was: " + sendTo + "\n\n\n-------------\n" + text;
            html = "TEST OPTION REPLACED THE EMAIL ADDRESS!\nThe original address was: " + sendTo + "<hr>" + html;
            sendTo = testSendTo;
        }
        if (replyTo == null) {
            replyTo = defaultReplyTo;
        }
        
        if (testConsoleOutput) {
            System.out.println("----------------------------------------------------------");
            System.out.println("To: " + sendTo);
            System.out.println("Reply-to: " + replyTo);
            System.out.println("Subject: " + subject);
            System.out.println("---");
            System.out.println(text);
            System.out.println("----------------------------------------------------------");
            return;
        }

        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(sendTo);
            helper.setFrom(defaultReplyTo); //to avoid spam filters
            helper.setReplyTo(replyTo);
            helper.setSubject(subject);
            helper.setText(text, html);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        javaMailSender.send(message);
        log.info("Email message sent to " + sendTo + ": " + subject);
    }

    private String processTemplate(String templateName, Map<String, Object> dataMap) {
        final Configuration cfg = freemarkerConfig.getConfiguration();
      
        try {
            Template template;
            template = cfg.getTemplate("emails/" + templateName + ".ftl");
            Writer out = new StringWriter();
            template.process(dataMap, out);
            out.flush();
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see fi.om.initiative.service.EmailService#sendVRKResolutionToVEVs(fi.om.initiative.dto.InitiativeManagement)
     */
    @Override
    public void sendVRKResolutionToVEVs(InitiativeManagement initiative) {
        sendStatusInfoToVEVs(initiative, EmailMessageType.VRK_RESOLUTION);
    }

    private String stripTextRows(String text, int maxEmptyRows) {
//        Iterable<String> textRows = Splitter.on('\n').trimResults().split(text);
        List<String> rows = Lists.newArrayList(Splitter.on('\n').trimResults().split(text));
       
        int emptyRows = maxEmptyRows;
        for (int i = rows.size()-1; i >= 0; i--) {
            if (Strings.isNullOrEmpty(rows.get(i))) {
                emptyRows++;
            } else {
                emptyRows = 0;
            }
            if (emptyRows > maxEmptyRows) {
                rows.remove(i);
            }
        }
        
        //remove remaining empty rows from the beginning
        while (rows.size() > 0 && Strings.isNullOrEmpty(rows.get(0))) {
            rows.remove(0);
        }
        
        String result = Joiner.on("\r\n").join(rows); // Splitter removes '\r' characters
        return result;
    }
}
