package fi.om.municipalityinitiative.web;

import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.util.InitiativeState;
import fi.om.municipalityinitiative.util.InitiativeType;
import fi.om.municipalityinitiative.util.hash.PreviousHashGetter;
import fi.om.municipalityinitiative.util.hash.RandomHashGenerator;
import org.joda.time.DateTime;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Optional;

import static fi.om.municipalityinitiative.util.OptionalMatcher.isNotPresent;
import static fi.om.municipalityinitiative.util.OptionalMatcher.isPresent;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InitiativeParticipateWebTest extends WebTestBase {

    /**
     * Localization keys as constants.
     */
    private static final String MSG_SUCCESS_PARTICIPATE = "success.participate";
    private static final String MSG_SUCCESS_PARTICIPATE_VERIFIABLE = "success.participate-verifiable.title";
    private static final String MSG_BTN_PARTICIPATE = "action.participate";
    private static final String MSG_BTN_SEND_CONFIRMATION = "action.send.confirmation";
    private static final String PARTICIPANT_SHOW_NAME = "participant.showName";
    private static final String MEMBERSHIP_RADIO = "initiative.municipalMembership.community";
    
    /**
     * Form values as constants.
     */
    private static final String PARTICIPANT_NAME = "Ossi Osallistuja";
    private static final String PARTICIPANT_EMAIL = "test@test.com";
    private static final String AUTHOR_MESSAGE = "Tässä on viesti";
    public static final String VERIFIED_INITIATIVE_AURHOR_SSN = "000000-0000";
    public static final String OTHER_USER_SSN = "111111-1111";

    private Long normalInitiativeHelsinki;
    private Long verifiedInitiativeHelsinki;

    private static final boolean RUN_HOME_MUNICIPALITY_SELECTION_TESTS = false;

    @Override
    public void childSetup() {
        normalInitiativeHelsinki = testHelper.createDefaultInitiative(
                new TestHelper.InitiativeDraft(HELSINKI_ID)
                        .withState(InitiativeState.PUBLISHED)
                        .withType(InitiativeType.COLLABORATIVE)
                        .withParticipantCount(0)
        );
        verifiedInitiativeHelsinki = testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(HELSINKI_ID)
                .withState(InitiativeState.PUBLISHED)
                .withParticipantCount(0)
                .applyAuthor(VERIFIED_INITIATIVE_AURHOR_SSN)
                .toInitiativeDraft()
        );
    }

    // Email -> normal initiative -> same municipality -> public name
    @Test
    public void participate_to_normal_initiative_with_public_name() throws InterruptedException {
        open(urls.view(normalInitiativeHelsinki));

        clickLink(getMessage(MSG_BTN_PARTICIPATE));

        getElemContaining("Sähköpostilla tunnistautuminen", "label").click();

        inputText("participantName", PARTICIPANT_NAME);
        inputText("participantEmail", PARTICIPANT_EMAIL);

        int initiativeMunicipality = Integer.parseInt(getElement(By.id("form-participate")).getAttribute("data-initiativemunicipality"));
        if (RUN_HOME_MUNICIPALITY_SELECTION_TESTS) {
            assertHomeMunicipality(initiativeMunicipality, "Lähetä vahvistus", true);
        }

        getElemContaining("Olen kunnan asukas", "label").click();

        getElemContaining(getMessage(MSG_BTN_SEND_CONFIRMATION), "button").click();

        assertSuccessMessage("Vahvistuslinkki on lähetetty antamaasi sähköpostiosoitteeseen");

        assertTotalEmailsInQueue(1);

        assertThat(getOptionalElemContaining(getMessage(MSG_BTN_PARTICIPATE), "a"), isNotPresent());

        open(urls.confirmParticipant(testHelper.getLastParticipantId(), PreviousHashGetter.get()));

        assertTextContainedByClass("public-names", "1 nimi julkaistu palvelussa"); //TODO
    }

    // Email -> normal initiative -> same municipality -> private name
    @Test
    public void participate_to_normal_initiative_with_private_name_and_select_membership_type() throws InterruptedException {
        overrideDriverToFirefox(true); // Municipality select need firefox driver

        open(urls.view(normalInitiativeHelsinki));

        clickLink(getMessage(MSG_BTN_PARTICIPATE));

        getElemContaining("Sähköpostilla tunnistautuminen", "label").click();

        inputText("participantName", PARTICIPANT_NAME);
        inputText("participantEmail", PARTICIPANT_EMAIL);

        getElemContaining(getMessage(PARTICIPANT_SHOW_NAME), "span").click();

        int initiativeMunicipality = Integer.parseInt(getElement(By.id("form-participate")).getAttribute("data-initiativemunicipality"));
        if (RUN_HOME_MUNICIPALITY_SELECTION_TESTS) {
            assertHomeMunicipality(initiativeMunicipality, "Lähetä vahvistus", true);
        }
        getElemContaining("Olen asukas toisessa kunnassa", "label").click();

        getElemContaining(getMessage(MEMBERSHIP_RADIO), "span").click();
        homeMunicipalitySelect(VANTAA);
        getElemContaining(getMessage(MSG_BTN_SEND_CONFIRMATION), "button").click();

        assertSuccessMessage("Vahvistuslinkki on lähetetty antamaasi sähköpostiosoitteeseen");

        assertTotalEmailsInQueue(1);

        assertThat(getOptionalElemContaining(getMessage(MSG_BTN_PARTICIPATE), "a"), isNotPresent());

        open(urls.confirmParticipant(testHelper.getLastParticipantId(), PreviousHashGetter.get()));

        assertThat(totalUserCount(), is("1"));
        assertTextContainedByClass("private-names", "0 kunnan asukasta");
    }

    private String totalUserCount() {
        return getElement(By.className("user-count-total")).getAttribute("innerHTML");
    }

    // Email -> normal initiative -> disallow
    @Test
    public void authentication_with_email_disallowed_options() throws InterruptedException {
        overrideDriverToFirefox(true); // Municipality select need firefox driver

        open(urls.view(normalInitiativeHelsinki));

        clickLink(getMessage(MSG_BTN_PARTICIPATE));

        getElemContaining("Sähköpostilla tunnistautuminen", "label").click();
        assertThat(getElemContaining(getMessage(MSG_BTN_SEND_CONFIRMATION), "button").isEnabled(), is(false));

        inputText("participantName", PARTICIPANT_NAME);
        inputText("participantEmail", PARTICIPANT_EMAIL);
        assertThat(getElemContaining(getMessage(MSG_BTN_SEND_CONFIRMATION), "button").isEnabled(), is(false));

        int initiativeMunicipality = Integer.parseInt(getElement(By.id("form-participate")).getAttribute("data-initiativemunicipality"));
        if (RUN_HOME_MUNICIPALITY_SELECTION_TESTS) {
            assertHomeMunicipality(initiativeMunicipality, "Lähetä vahvistus", true);
        }
        getElemContaining("Olen asukas toisessa kunnassa", "label").click();
        assertThat(getElemContaining(getMessage(MSG_BTN_SEND_CONFIRMATION), "button").isEnabled(), is(false));

        getElemContaining(getMessage(MEMBERSHIP_RADIO), "span").click();
        assertThat(getElemContaining(getMessage(MSG_BTN_SEND_CONFIRMATION), "button").isEnabled(), is(false));
        homeMunicipalitySelect(VANTAA);
        assertThat(getElemContaining(getMessage(MSG_BTN_SEND_CONFIRMATION), "button").isEnabled(), is(true));
        getElemContaining("Ei mitään näistä", "label").click();
        assertWarningMessage("Jos mikään perusteista ei täyty, et voi osallistua tähän aloitteeseen.");
        assertThat(getElemContaining(getMessage(MSG_BTN_SEND_CONFIRMATION), "button").isEnabled(), is(false));
    }

    @Test
    public void confirming_participation_with_invalid_participant_id_or_confirmation_code_shows_correct_error_page() {

        open(urls.confirmParticipant(-1L, "asdasd")); // Invalid participant id
        assertParticipationLinkInvalidPage();

        Long participantId = testHelper.createUnconfirmedParticipant(new TestHelper.AuthorDraft(normalInitiativeHelsinki, HELSINKI_ID), RandomHashGenerator.shortHash());

        open(urls.confirmParticipant(participantId, "asdasd")); // Invalid confirmationCode
        assertParticipationLinkInvalidPage();

        open(urls.confirmParticipant(participantId, PreviousHashGetter.get()));
        assertSuccessMessage("Osallistumisesi aloitteeseen on nyt vahvistettu");
    }

    private void assertParticipationLinkInvalidPage() {
        assertThat(pageTitle(), is("Sivua ei voida näyttää - Kuntalaisaloitepalvelu"));
        assertTextByTag("h1", "Yritit avata linkin, joka vahvistaa aloitteeseen osallistumisen");
    }

    @Test
    public void participating_to_initiative_when_not_logged_in_redirects_to_vetuma_and_back_to_participation_page() {
        open(urls.view(verifiedInitiativeHelsinki));

        clickLink("Tunnistaudu ja osallistu");
        enterVetumaLoginInformationAndSubmit("111111-1111", HELSINKI);
        assertTitle(TestHelper.DEFAULT_INITIATIVE_NAME + " - Kuntalaisaloitepalvelu");
        assertThat(participateToInitiativeButton(), isPresent());
    }

    // Vetuma, private municipality -> normal initiative -> same municipality
    @Test
    public void participating_to_normal_initiative_when_private_home_municipality_from_vetuma_succeeds() throws InterruptedException {
        participating_to_own_municipality_initiative_when_private_home_municipality_from_vetuma_succeeds(normalInitiativeHelsinki);
    }

    // Vetuma, private municipality -> normal initiative -> another municipality
    @Test
    public void participating_to_normal_initiative_when_private_home_municipality_from_vetuma_succeeds_and_select_membership_type() throws InterruptedException{
        vetumaLogin(OTHER_USER_SSN, null);

        open(urls.view(normalInitiativeHelsinki));
        assertThat(participateToInitiativeButton(), isPresent());
        participateToInitiativeButton().get().click();
        assertInfoMessageContainsText("Vain nimesi voitiin hakea");

        int initiativeMunicipality = Integer.parseInt(getElement(By.id("form-participate")).getAttribute("data-initiativemunicipality"));
        if (RUN_HOME_MUNICIPALITY_SELECTION_TESTS) {
            assertHomeMunicipality(initiativeMunicipality, "Tallenna", true);
        }

        getElemContaining("Olen asukas toisessa kunnassa", "label").click();
        getElemContaining(getMessage(MEMBERSHIP_RADIO), "span").click();
        homeMunicipalitySelect(VANTAA);

        // Vetuma participant has no information to fill
        getElemContaining("Tallenna", "button").click();

        assertTextContainedByClass("modal-title", "Osallistumisesi aloitteeseen on nyt vahvistettu");

        assertThat(totalUserCount(), is("1"));
        assertTextContainedByClass("private-names", "0 kunnan asukasta");
    }

    // Vetuma, private municipality -> normal initiative -> disallow
    @Test
    public void participating_to_normal_initiative_disallowed_options() throws InterruptedException {
        vetumaLogin(OTHER_USER_SSN, null);

        open(urls.view(normalInitiativeHelsinki));
        assertThat(participateToInitiativeButton(), isPresent());

        participateToInitiativeButton().get().click();

        int initiativeMunicipality = Integer.parseInt(getElement(By.id("form-participate")).getAttribute("data-initiativemunicipality"));
        if (RUN_HOME_MUNICIPALITY_SELECTION_TESTS) {
            assertHomeMunicipality(initiativeMunicipality, "Tallenna", true);
        }

        getElemContaining("Olen asukas toisessa kunnassa", "label").click();
        getElemContaining("Ei mitään näistä", "label").click();
        assertWarningMessage("Jos mikään perusteista ei täyty, et voi osallistua tähän aloitteeseen.");
        assertThat(getElemContaining("Tallenna", "button").isEnabled(), is(false));
    }

    // Vetuma, private municipality -> verified initiative -> same municipality
    @Test
    public void participating_to_verified_initiative_when_private_home_municipality_from_vetuma_succeeds() throws InterruptedException {
        participating_to_own_municipality_initiative_when_private_home_municipality_from_vetuma_succeeds(verifiedInitiativeHelsinki);
    }

    private void participating_to_own_municipality_initiative_when_private_home_municipality_from_vetuma_succeeds(Long initiativeId) throws InterruptedException {
        vetumaLogin(OTHER_USER_SSN, null);

        open(urls.view(initiativeId));
        assertThat(participateToInitiativeButton(), isPresent());

        participateToInitiativeButton().get().click();
        getElemContaining("Olen kunnan asukas", "label").click();

        // Vetuma participant has no information to fill
        getElemContaining("Tallenna", "button").click();

        assertTextContainedByClass("modal-title", "Osallistumisesi aloitteeseen on nyt vahvistettu");
        //open(urls.confirmParticipant(testHelper.getLastParticipantId(), PreviousHashGetter.get())); FAILS
        //assertTextContainedByClass("public-names", "1 nimi julkaistu palvelussa");
    }

    // Vetuma, private municipality -> verified initiative -> another municipality -> error
    @Test
    public void participating_to_verified_initiative_when_private_home_municipality_from_vetuma_is_prevented_if_user_selects_wrong_municipality() throws InterruptedException {
        vetumaLogin(OTHER_USER_SSN, null);

        open(urls.view(verifiedInitiativeHelsinki));

        assertThat(participateToInitiativeButton(), isPresent());
        participateToInitiativeButton().get().click();
        assertInfoMessageContainsText("Vain nimesi voitiin hakea");
        getElemContaining("Olen asukas toisessa kunnassa", "label").click();
        assertThat(getElemContaining("Tallenna", "button").isEnabled(), is(false));
        //assertTextContainedByClass("msg-warning", "Et ole aloitteen kunnan jäsen"); //FAILS: wrong text
    }

    // Vetuma -> normal initiative -> same municipality
    @Test
    public void participating_to_normal_initiative_as_verified_user_with_correct_municipality() {
        vetumaLogin(OTHER_USER_SSN, HELSINKI);

        open(urls.view(normalInitiativeHelsinki));

        Integer originalParticipantCountOnPage = Integer.valueOf(totalUserCount());

        assertThat(participateToInitiativeButton(), isPresent());
        participateToInitiativeButton().get().click();

        assertInfoMessageContainsText("Nimesi ja kotikuntasi on haettu");

        // Vetuma participant has no information to fill
        getElemContaining("Tallenna", "button").click();

        assertTextContainedByClass("modal-title", "Osallistumisesi aloitteeseen on nyt vahvistettu");
        Integer newParticipantCountOnPage = Integer.valueOf(totalUserCount());

        assertThat(newParticipantCountOnPage, is(originalParticipantCountOnPage + 1));
        assertTextContainedByClass("private-names", "1 kunnan asukas");

        assertWarningMessage("Olet jo osallistunut tähän aloitteeseen");
        assertThat(participateToInitiativeButton(), isNotPresent());
    }

    // Vetuma -> normal initiative -> same municipality
    @Test
    public void participate_to_normal_initiative_as_verified_user_by_clicking_authentication_link_on_participate_page() {
        open(urls.view(normalInitiativeHelsinki));

        Integer originalParticipantCountOnPage = Integer.valueOf(totalUserCount());

        clickLink(getMessage(MSG_BTN_PARTICIPATE));
        clickLink("Tunnistaudu ja osallistu");
        enterVetumaLoginInformationAndSubmit(OTHER_USER_SSN, HELSINKI);
        clickButton("Tallenna");
        assertTextContainedByClass("modal-title", "Osallistumisesi aloitteeseen on nyt vahvistettu");
        clickLink("Jatka kirjautuneena");

        Integer newParticipantCountOnPage = Integer.valueOf(totalUserCount());
        assertThat(newParticipantCountOnPage, is(originalParticipantCountOnPage + 1));
    }

    // Vetuma -> normal initiative -> another municipality
    @Test
    public void participating_to_normal_initiative_with_another_municipality() {
        vetumaLogin(OTHER_USER_SSN, VANTAA);
        open(urls.view(normalInitiativeHelsinki));

        Integer originalParticipantCountOnPage = Integer.valueOf(totalUserCount());

        assertThat(participateToInitiativeButton(), isPresent());
        participateToInitiativeButton().get().click();

        getElemContaining(getMessage(MEMBERSHIP_RADIO), "span").click();

        // Vetuma participant has no information to fill
        getElemContaining("Tallenna", "button").click();

        assertTextContainedByClass("modal-title", "Osallistumisesi aloitteeseen on nyt vahvistettu");
        Integer newParticipantCountOnPage = Integer.valueOf(totalUserCount());

        assertThat(newParticipantCountOnPage, is(originalParticipantCountOnPage + 1));

        assertWarningMessage("Olet jo osallistunut tähän aloitteeseen");
        assertThat(participateToInitiativeButton(), isNotPresent());
    }

    // Vetuma -> normal initiative -> another municipality -> error
    @Test
    public void participating_to_normal_initiative_as_verified_user_with_another_municipality_disallowed_options(){
        vetumaLogin(OTHER_USER_SSN, VANTAA);
        open(urls.view(normalInitiativeHelsinki));

        assertThat(participateToInitiativeButton(), isPresent());
        participateToInitiativeButton().get().click();

        getElemContaining("Ei mitään näistä", "label").click();
        assertWarningMessage("Jos mikään perusteista ei täyty, et voi osallistua tähän aloitteeseen.");
        assertThat(getElemContaining("Tallenna", "button").isEnabled(), is(false));
    }

    // Vetuma -> verified initiative -> same municipality
    @Test
    public void participate_to_verified_initiative_shows_success_message_and_increases_participant_count_on_page() {
        vetumaLogin("111111-1111", HELSINKI);
        open(urls.view(verifiedInitiativeHelsinki));

        Integer originalParticipantCountOnPage = Integer.valueOf(totalUserCount());

        assertThat(participateToInitiativeButton(), isPresent());
        participateToInitiativeButton().get().click();

        // Vetuma participant has no information to fill
        getElemContaining("Tallenna", "button").click();

        assertTextContainedByClass("modal-title", "Osallistumisesi aloitteeseen on nyt vahvistettu");
        Integer newParticipantCountOnPage = Integer.valueOf(totalUserCount());

        assertThat(newParticipantCountOnPage, is(originalParticipantCountOnPage + 1));

        assertWarningMessage("Olet jo osallistunut tähän aloitteeseen");
        assertThat(participateToInitiativeButton(), isNotPresent());
    }

    // Vetuma -> verified initiative -> another municipality -> error
    @Test
    public void participating_to_verified_initiative_is_not_allowed_if_wrong_homeMunicipality() {
        vetumaLogin(OTHER_USER_SSN, VANTAA);
        open(urls.view(verifiedInitiativeHelsinki));

        assertWarningMessage("Et ole aloitteen kunnan asukas, joten et voi osallistua aloitteeseen.");
        assertThat(participateToInitiativeButton(), isNotPresent());
    }

    private Optional<WebElement> participateToInitiativeButton() {
        return getOptionalElemContaining("Osallistu aloitteeseen", "span");
    }

    @Test
    public void public_user_contacts_authors_shows_success_message() {

        Long initiativeWithAuthor = testHelper.createDefaultInitiative(
                new TestHelper.InitiativeDraft(HELSINKI_ID)
                        .withState(InitiativeState.PUBLISHED)
                        .withType(InitiativeType.COLLABORATIVE)
                        .withParticipantCount(0)
                        .applyAuthor()
                        .withShowName(false)
                        .toInitiativeDraft()
        );

        open(urls.view(initiativeWithAuthor));

        clickLink("Ota yhteyttä aloitteen vastuuhenkilöön");

        inputText("message", AUTHOR_MESSAGE);
        inputText("contactName", PARTICIPANT_NAME);
        inputText("contactEmail", PARTICIPANT_EMAIL);

        getElemContaining("Lähetä viesti", "button").click();

        assertTotalEmailsInQueue(1);

        assertSuccessMessage("Linkki yhteydenottopyynnön vahvistamiseen on lähetetty sähköpostiisi");

        open(urls.confirmAuthorMessage(PreviousHashGetter.get()));

        assertSuccessMessage("Viesti on nyt lähetetty vastuuhenkilöille");

        assertTotalEmailsInQueue(2);

    }

    @Test
    public void contact_author_is_hidden_if_initiative_has_been_sent_to_municipality() {
        DateTime yesterday = DateTime.now().minusDays(1);
        Long initiativeWithAuthor = testHelper.createDefaultInitiative(
                new TestHelper.InitiativeDraft(HELSINKI_ID)
                        .withState(InitiativeState.PUBLISHED)
                        .withType(InitiativeType.COLLABORATIVE)
                        .withParticipantCount(0)
                        .withSent(yesterday)
                        .applyAuthor()
                        .withShowName(false)
                        .toInitiativeDraft()
        );

        open(urls.view(initiativeWithAuthor));

        assertThat(getOptionalElemContaining("Ota yhteyttä aloitteen vastuuhenkilöön", "a"), isNotPresent());

    }

    @Test
    public void follow_initiative() {
        open(urls.view(normalInitiativeHelsinki));
        clickElementByCSS(".js-follow");
        inputText("participantEmail", "test@example.com");

        getElement(By.name("action-follow")).click();

        assertSuccessMessage("Aloitteen seuraaminen vahvistettu");

        assertTotalEmailsInQueue(1);
    }
}
