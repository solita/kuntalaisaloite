package fi.om.municipalityinitiative.service;

import com.google.common.collect.Sets;
import fi.om.municipalityinitiative.dao.ParticipantDao;
import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.dao.UserDao;
import fi.om.municipalityinitiative.dto.service.Initiative;
import fi.om.municipalityinitiative.dto.service.Municipality;
import fi.om.municipalityinitiative.dto.service.Participant;
import fi.om.municipalityinitiative.dto.service.VerifiedUserDbDetails;
import fi.om.municipalityinitiative.dto.ui.ParticipantListInfo;
import fi.om.municipalityinitiative.dto.ui.ParticipantUICreateDto;
import fi.om.municipalityinitiative.dto.user.LoginUserHolder;
import fi.om.municipalityinitiative.dto.user.NormalLoginUser;
import fi.om.municipalityinitiative.dto.user.User;
import fi.om.municipalityinitiative.exceptions.InvalidHomeMunicipalityException;
import fi.om.municipalityinitiative.exceptions.OperationNotAllowedException;
import fi.om.municipalityinitiative.service.email.EmailSubjectPropertyKeys;
import fi.om.municipalityinitiative.util.InitiativeState;
import fi.om.municipalityinitiative.util.InitiativeType;
import fi.om.municipalityinitiative.util.Locales;
import fi.om.municipalityinitiative.util.Membership;
import fi.om.municipalityinitiative.util.hash.PreviousHashGetter;
import fi.om.municipalityinitiative.web.Urls;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.List;
import java.util.Optional;

import static fi.om.municipalityinitiative.util.TestUtil.precondition;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

public class ParticipantServiceIntegrationTest extends ServiceIntegrationTestBase{

    @Resource
    private ParticipantService participantService;

    @Resource
    private ParticipantDao participantDao;

    @Resource
    private UserDao userDao;

    private Long testMunicipalityId;
    private Long anotherMunicipality;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Override
    protected void childSetup() {
        testMunicipalityId = testHelper.createTestMunicipality("Some municipality");
        anotherMunicipality = testHelper.createTestMunicipality("Another municipality");
    }

    @Test
    public void findPublicParticipants_for_verified_initiative() {
        Long initiativeId = createVerifiedInitiativeWithAuthor();
        assertThat(participantService.findPublicParticipants(0, initiativeId), hasSize(1));
    }

    @Test
    public void findPublicParticipants_for_normal_initiative() {
        Long initiativeId = createNormalInitiativeWithAuthor();
        assertThat(participantService.findPublicParticipants(0, initiativeId), hasSize(1));
    }

    @Test
    public void findPublicParticipants_are_ordered_by_date_and_pagination_works() {
        Long initiativeId = testHelper.createDefaultInitiative(new TestHelper.InitiativeDraft(testMunicipalityId));

        int participantCount = 90;

        for (int i = 0; i < participantCount; ++i) {
            if (i % 2 == 0) {
                testHelper.createDefaultParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId)
                        .withParticipateDate(LocalDate.now().plusDays(i))
                        .withParticipantName(String.valueOf(i +1)));
            }
            else {
                testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId)
                        .withParticipateDate(LocalDate.now().plusDays(i))
                        .withParticipantName(String.valueOf(i +1)));
            }
        }

        // Get without offset, should be limited
        List<ParticipantListInfo> publicParticipants = participantService.findPublicParticipants(0, initiativeId);
        assertThat(publicParticipants, hasSize(Urls.MAX_PARTICIPANT_LIST_LIMIT));
        assertThat(publicParticipants.get(0).getParticipant().getName(), is("90"));
        assertThat(publicParticipants.get(publicParticipants.size() - 1).getParticipant().getName(), is("41"));

        // Get the rest with offset
        List<ParticipantListInfo> nextParticipants = participantService.findPublicParticipants(Urls.MAX_PARTICIPANT_LIST_LIMIT, initiativeId);
        assertThat(nextParticipants, hasSize(participantCount - Urls.MAX_PARTICIPANT_LIST_LIMIT));
        assertThat(nextParticipants.get(0).getParticipant().getName(), is("40"));
        assertThat(nextParticipants.get(nextParticipants.size() - 1).getParticipant().getName(), is("1"));

    }

    @Test
    public void findAllParticipants_for_verified_initiative() {
        Long initiativeId = createVerifiedInitiativeWithAuthor();
        assertParticipantListSize(initiativeId, 1);
    }

    @Test
    public void findAllParticipants_for_normal_initiative() {
        Long initiativeId = createNormalInitiativeWithAuthor();
        assertParticipantListSize(initiativeId, 1);
    }

    @Test
    public void findAllParticipants_sets_author_flat_true_for_authors_and_false_for_normal_participants() {
        Long defaultInitiative = testHelper.createDefaultInitiative(new TestHelper.InitiativeDraft(testMunicipalityId));

        testHelper.createDefaultAuthorAndParticipant(new TestHelper.AuthorDraft(defaultInitiative, testMunicipalityId)
                .withParticipantName("Normal Author"));

        testHelper.createDefaultParticipant(new TestHelper.AuthorDraft(defaultInitiative, testMunicipalityId)
                .withParticipantName("Normal Participant"));

        testHelper.createVerifiedAuthorAndParticipant(new TestHelper.AuthorDraft(defaultInitiative, testMunicipalityId)
                .withParticipantName("Verified Author"));

        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(defaultInitiative, testMunicipalityId)
                .withParticipantName("Verified Participant"));

        LoginUserHolder loginUserHolder = new LoginUserHolder<NormalLoginUser>(User.normalUser(null, Sets.newHashSet(defaultInitiative)));
        List<ParticipantListInfo> allParticipants = participantService.findAllParticipants(defaultInitiative, loginUserHolder, 0);

        assertThat(allParticipants, hasSize(4));

        assertThat(allParticipants.stream().filter(p -> p.getParticipant().getName().equals("Normal Author")).findAny().get().isAuthor(), is(true));
        assertThat(allParticipants.stream().filter(p -> p.getParticipant().getName().equals("Verified Author")).findAny().get().isAuthor(), is(true));
        assertThat(allParticipants.stream().filter(p -> p.getParticipant().getName().equals("Normal Participant")).findAny().get().isAuthor(), is(false));
        assertThat(allParticipants.stream().filter(p -> p.getParticipant().getName().equals("Verified Participant")).findAny().get().isAuthor(), is(false));
    }

    @Test
    public void findPublicParticipants_returns_only_public_participants() {
        Long defaultInitiative = testHelper.createDefaultInitiative(new TestHelper.InitiativeDraft(testMunicipalityId));

        testHelper.createDefaultAuthorAndParticipant(new TestHelper.AuthorDraft(defaultInitiative, testMunicipalityId)
                .withParticipantName("Normal Author")
                .withShowName(true));

        testHelper.createDefaultParticipant(new TestHelper.AuthorDraft(defaultInitiative, testMunicipalityId)
                .withParticipantName("Normal Participant")
                .withShowName(true));

        testHelper.createVerifiedAuthorAndParticipant(new TestHelper.AuthorDraft(defaultInitiative, testMunicipalityId)
                .withParticipantName("Verified Author")
                .withShowName(false));

        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(defaultInitiative, testMunicipalityId)
                .withParticipantName("Verified Participant")
                .withShowName(false));

        List<ParticipantListInfo> allParticipants = participantService.findPublicParticipants(0, defaultInitiative);

        assertThat(allParticipants, hasSize(2));

        assertThat(allParticipants.stream().filter(p -> p.getParticipant().getName().equals("Normal Author")).findAny().get().isAuthor(), is(true));
        assertThat(allParticipants.stream().filter(p -> p.getParticipant().getName().equals("Normal Participant")).findAny().get().isAuthor(), is(false));
    }

    @Test
    public void delete_participant_decreses_participant_count() {
        Long initiativeId = testHelper.createDefaultInitiative(
                new TestHelper.InitiativeDraft(testMunicipalityId)
                        .withState(InitiativeState.PUBLISHED)
                        .withType(InitiativeType.COLLABORATIVE)
                        .applyAuthor().withShowName(false)
                        .toInitiativeDraft()
        );
        Long participantId = testHelper.createDefaultParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withShowName(true));
        testHelper.denormalizeParticipantCount(initiativeId);

        Initiative initiative = testHelper.getInitiative(initiativeId);
        int participantCountPublicOriginal = initiative.getParticipantCountPublic();
        int participantCountOriginal = initiative.getParticipantCount();
        int participantCountCitizenOriginal = initiative.getParticipantCountCitizen();

        participantService.deleteParticipant(initiativeId, TestHelper.authorLoginUserHolder, participantId, false);

        Initiative updated = testHelper.getInitiative(initiativeId);
        assertThat(updated.getParticipantCount(), is(participantCountOriginal - 1));
        assertThat(updated.getParticipantCountPublic(), is(participantCountPublicOriginal - 1));
        assertThat(updated.getParticipantCountCitizen(), is(participantCountCitizenOriginal - 1));

        assertParticipantListSize(initiativeId, 1);
    }

    private void assertParticipantListSize(Long initiativeId, int size) {
        assertThat(participantService.findAllParticipants(initiativeId, TestHelper.authorLoginUserHolder, 0), hasSize(size));
    }

    @Test
    public void delete_verified_participant_decreases_participant_count_citizen() {
        Long initiativeId = testHelper.createVerifiedInitiative(
                new TestHelper.InitiativeDraft(testMunicipalityId)
                        .withState(InitiativeState.PUBLISHED)
                        .withType(InitiativeType.COLLABORATIVE_CITIZEN)
                        .applyAuthor().withShowName(false)
                        .toInitiativeDraft()
        );
        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withShowName(true));
        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withShowName(true));
        testHelper.denormalizeParticipantCount(initiativeId);

        Initiative initiative = testHelper.getInitiative(initiativeId);
        precondition(initiative.getParticipantCountPublic(), is(2));
        precondition(initiative.getParticipantCount(), is(3));

        assertParticipantListSize(initiativeId, 3);
        participantService.deleteParticipant(initiativeId, TestHelper.authorLoginUserHolder, testHelper.getLastVerifiedUserId(), true);
        assertParticipantListSize(initiativeId, 2);

        Initiative updated = testHelper.getInitiative(initiativeId);
        assertThat(updated.getParticipantCount(), is(2));
        assertThat(updated.getParticipantCountPublic(), is(1));
    }
    @Test
    public void delete_verified_participant_decreases_participant_count_citizen_private_name() {
        Long initiativeId = testHelper.createVerifiedInitiative(
                new TestHelper.InitiativeDraft(testMunicipalityId)
                        .withState(InitiativeState.PUBLISHED)
                        .withType(InitiativeType.COLLABORATIVE_CITIZEN)
                        .applyAuthor().withShowName(false)
                        .toInitiativeDraft()
        );
        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withShowName(false));
        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withShowName(false));
        testHelper.denormalizeParticipantCount(initiativeId);

        Initiative initiative = testHelper.getInitiative(initiativeId);
        precondition(initiative.getParticipantCountPublic(), is(0));
        precondition(initiative.getParticipantCount(), is(3));

        List<ParticipantListInfo> allParticipants = participantService.findAllParticipants(initiativeId, TestHelper.authorLoginUserHolder, 0);

        assertParticipantListSize(initiativeId, 3);
        participantService.deleteParticipant(initiativeId, TestHelper.authorLoginUserHolder, testHelper.getLastVerifiedUserId(), true);
        assertParticipantListSize(initiativeId, 2);

        Initiative updated = testHelper.getInitiative(initiativeId);
        assertThat(updated.getParticipantCount(), is(2));
        assertThat(updated.getParticipantCountPublic(), is(0));
    }

    @Test
    public void deleting_verified_participant_with_normal_id_throws_exception() {

        Long initiativeId = testHelper.createVerifiedInitiative(
                new TestHelper.InitiativeDraft(testMunicipalityId)
                        .withState(InitiativeState.PUBLISHED)
                        .withType(InitiativeType.COLLABORATIVE_CITIZEN)
                        .applyAuthor().withShowName(false)
                        .toInitiativeDraft()
        );
        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withShowName(true));
        testHelper.denormalizeParticipantCount(initiativeId);


        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(containsString("Should have affected only one row. Affected: 0"));

        participantService.deleteParticipant(initiativeId, TestHelper.authorLoginUserHolder, testHelper.getLastVerifiedUserId(), false);

    }

    @Test(expected = OperationNotAllowedException.class)
    public void delete_participant_is_forbidden_if_initiative_sent_to_municipality() {
        Long initiativeId = testHelper.createDefaultInitiative(
                new TestHelper.InitiativeDraft(testMunicipalityId)
                        .withSent(new DateTime())
                        .applyAuthor().withShowName(false)
                        .toInitiativeDraft()
        );
        Long participantId = testHelper.createDefaultParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withShowName(true));

        participantService.deleteParticipant(initiativeId, TestHelper.authorLoginUserHolder, participantId, false);
    }

    @Test(expected = OperationNotAllowedException.class)
    public void participating_allowance_is_checked() {
        Long initiative = testHelper.createCollaborativeReview(testMunicipalityId);

        ParticipantUICreateDto participant = participantUICreateDto();
        participantService.createParticipant(participant, initiative, null);
    }


    @Test
    public void adding_participant_does_not_increase_denormalized_participantCount_but_accepting_does() throws MessagingException, InterruptedException {
        Long initiativeId = testHelper.createWithAuthor(testMunicipalityId, InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE);
        int originalParticipantCount = testHelper.getInitiative(initiativeId).getParticipantCount();
        int originalParticipantCountPublic = testHelper.getInitiative(initiativeId).getParticipantCount();
        int originalParticipantCountCitizen = testHelper.getInitiative(initiativeId).getParticipantCountCitizen();

        Long participantId = participantService.createParticipant(participantUICreateDto(), initiativeId, null);
        assertThat(getSingleInitiativeInfo().getParticipantCount(), is(originalParticipantCount));
        assertThat(getSingleInitiativeInfo().getParticipantCountPublic(), is(originalParticipantCountPublic));
        assertThat(getSingleInitiativeInfo().getParticipantCountCitizen(), is(originalParticipantCountCitizen));

        participantService.confirmParticipation(participantId, PreviousHashGetter.get());
        assertThat(getSingleInitiativeInfo().getParticipantCount(), is(originalParticipantCount + 1));
        assertThat(getSingleInitiativeInfo().getParticipantCountPublic(), is(originalParticipantCountPublic + 1));
        assertThat(getSingleInitiativeInfo().getParticipantCountCitizen(), is(originalParticipantCountCitizen + 1));

        assertUniqueSentEmail(participantUICreateDto().getParticipantEmail(), EmailSubjectPropertyKeys.EMAIL_PARTICIPATION_CONFIRMATION_SUBJECT);
    }

    @Test
    public void confirming_participation_does_not_increase_public_names_if_showName_is_false() {

        Long initiativeId = testHelper.createWithAuthor(testMunicipalityId, InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE);
        int originalParticipantCount = testHelper.getInitiative(initiativeId).getParticipantCount();
        int originalParticipantCountPublic = testHelper.getInitiative(initiativeId).getParticipantCount();

        ParticipantUICreateDto participant = participantUICreateDto();
        participant.setShowName(false);

        Long participantId = participantService.createParticipant(participant, initiativeId, null);
        assertThat(testHelper.getInitiative(initiativeId).getParticipantCount(), is(originalParticipantCount));

        participantService.confirmParticipation(participantId, PreviousHashGetter.get());
        assertThat(getSingleInitiativeInfo().getParticipantCount(), is(originalParticipantCount + 1));
        assertThat(getSingleInitiativeInfo().getParticipantCountPublic(), is(originalParticipantCountPublic));

    }

    @Test
    public void confirming_participation_does_not_increase_citizen_participant_amount_if_from_another_municipality() {
        Long initiativeId = testHelper.createWithAuthor(testMunicipalityId, InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE);
        int originalParticipantCount = testHelper.getInitiative(initiativeId).getParticipantCount();
        int originalParticipantCountPublic = testHelper.getInitiative(initiativeId).getParticipantCount();
        int originalParticipantCountCitizen = testHelper.getInitiative(initiativeId).getParticipantCountCitizen();

        ParticipantUICreateDto participant = participantUICreateDto();
        participant.setHomeMunicipality(anotherMunicipality);
        participant.setMunicipalMembership(Membership.community);
        participant.setShowName(true);

        Long participantId = participantService.createParticipant(participant, initiativeId, null);
        assertThat(testHelper.getInitiative(initiativeId).getParticipantCount(), is(originalParticipantCount));

        participantService.confirmParticipation(participantId, PreviousHashGetter.get());
        assertThat(getSingleInitiativeInfo().getParticipantCount(), is(originalParticipantCount + 1));
        assertThat(getSingleInitiativeInfo().getParticipantCountPublic(), is(originalParticipantCountPublic +1));
        assertThat(getSingleInitiativeInfo().getParticipantCountCitizen(), is(originalParticipantCountCitizen));

    }

    @Test
    public void create_participant_is_not_allowed_if_municipality_mismatch() {

        Long initiative = testHelper.createDefaultInitiative(new TestHelper.InitiativeDraft(testMunicipalityId)
                .withState(InitiativeState.PUBLISHED)
                .withType(InitiativeType.COLLABORATIVE));

        ParticipantUICreateDto participant = new ParticipantUICreateDto() {{
            assignInitiativeMunicipality(testMunicipalityId); // YNGH wuut
            setMunicipalMembership(Membership.none);
            setShowName(true);
            setHomeMunicipality(anotherMunicipality);
            setParticipantEmail("some@example.com");
        }};

        expectedException.expect(InvalidHomeMunicipalityException.class);
        participantService.createParticipant(participant, initiative, Locales.LOCALE_FI);

    }

    @Test
    @Transactional
    public void create_participant_adds_membership_and_verified_info() {

        Long initiative = testHelper.createDefaultInitiative(new TestHelper.InitiativeDraft(testMunicipalityId)
                .withState(InitiativeState.PUBLISHED)
                .withType(InitiativeType.COLLABORATIVE));

        ParticipantUICreateDto participant = new ParticipantUICreateDto() {{
            assignInitiativeMunicipality(testMunicipalityId); // YNGH wuut
            setMunicipalMembership(Membership.community);
            setShowName(true);
            setHomeMunicipality(anotherMunicipality);
            setParticipantEmail("some@example.com");
        }};


        precondition(participantDao.findAllParticipants(initiative, false, 0, Integer.MAX_VALUE), hasSize(0));

        Long participantId = participantService.createParticipant(participant, initiative, Locales.LOCALE_FI);
        participantService.confirmParticipation(participantId, PreviousHashGetter.get());

        List<Participant> allParticipants = participantDao.findAllParticipants(initiative, false, 0, Integer.MAX_VALUE);

        assertThat(allParticipants, hasSize(1));
        Participant theParticipant = allParticipants.get(0);

        assertThat(theParticipant.getMembership(), is(Membership.community));
        assertThat(theParticipant.isMunicipalityVerified(), is(false));
        assertThat(theParticipant.isVerified(), is(false));
        assertThat(((Municipality) theParticipant.getHomeMunicipality().get()).getId(), is(anotherMunicipality));

    }

    private Optional<VerifiedUserDbDetails> refreshVerifiedUser() {
        return userDao.getVerifiedUser(TestHelper.lastLoggedInVerifiedUserHolder.getVerifiedUser().getHash());
    }

    @Test(expected = OperationNotAllowedException.class)
    public void accepting_participation_allowance_is_checked() {
        testHelper.createSingleSent(testMunicipalityId);
        participantService.confirmParticipation(testHelper.getLastParticipantId(), null);
    }

    private ParticipantUICreateDto participantUICreateDto() {
        ParticipantUICreateDto participant = new ParticipantUICreateDto();
        participant.setParticipantName("Some Name");
        participant.setParticipantEmail("participant@example.com");
        participant.setShowName(true);
        participant.setHomeMunicipality(testMunicipalityId);
        participant.assignInitiativeMunicipality(testMunicipalityId);
        return participant;
    }

    private Long createNormalInitiativeWithAuthor() {
        return testHelper.createDefaultInitiative(new TestHelper.InitiativeDraft(testMunicipalityId).applyAuthor().toInitiativeDraft());
    }

    private Long createVerifiedInitiativeWithAuthor() {
        return testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipalityId).applyAuthor().toInitiativeDraft());
    }

    private Initiative getSingleInitiativeInfo() {
        return testHelper.getInitiative(testHelper.getLastInitiativeId());
    }



}
