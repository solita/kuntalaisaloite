package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.util.Locales;
import fi.om.municipalityinitiative.web.Urls;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class MailSendingEmailServiceStatusTest extends MailSendingEmailServiceTestBase {

    private Urls urls;

    @Before
    public void setup() {
        super.setup();
        urls = Urls.get(Locales.LOCALE_FI);
    }

    @Test
    public void om_accept_initiative_sets_subject_and_contains_all_information() throws Exception {
        emailService.sendStatusEmail(createDefaultInitiative(), CONTACT_EMAIL, EmailMessageType.ACCEPTED_BY_OM, Locales.LOCALE_FI);

        assertThat(getSingleRecipient(), is(CONTACT_EMAIL));
        assertThat(getSingleSentMessage().getSubject(), is("Kuntalaisaloite on hyväksytty"));
        assertThat(getMessageContent().html, containsString(urls.loginAuthor(INITIATIVE_ID, MANAGEMENT_HASH)));
    }

    @Test
    public void om_reject_initiative_sets_subject_and_contains_all_information() throws Exception {
        emailService.sendStatusEmail(createDefaultInitiative(), CONTACT_EMAIL, EmailMessageType.REJECTED_BY_OM, Locales.LOCALE_FI);

        assertThat(getSingleRecipient(), is(CONTACT_EMAIL));
        assertThat(getSingleSentMessage().getSubject(), is("Kuntalaisaloite on palautettu korjattavaksi"));
        assertThat(getMessageContent().html, containsString(urls.loginAuthor(INITIATIVE_ID, MANAGEMENT_HASH)));
    }

}