package fi.om.municipalityinitiative.service.ui;

import fi.om.municipalityinitiative.dao.InvitationNotValidException;
import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.dto.Author;
import fi.om.municipalityinitiative.dto.service.AuthorInvitation;
import fi.om.municipalityinitiative.dto.service.EmailDto;
import fi.om.municipalityinitiative.dto.service.Municipality;
import fi.om.municipalityinitiative.dto.ui.AuthorInvitationUIConfirmDto;
import fi.om.municipalityinitiative.dto.ui.AuthorInvitationUICreateDto;
import fi.om.municipalityinitiative.dto.ui.ContactInfo;
import fi.om.municipalityinitiative.dto.ui.InitiativeViewInfo;
import fi.om.municipalityinitiative.dto.user.LoginUserHolder;
import fi.om.municipalityinitiative.dto.user.User;
import fi.om.municipalityinitiative.dto.user.VerifiedUser;
import fi.om.municipalityinitiative.exceptions.AccessDeniedException;
import fi.om.municipalityinitiative.exceptions.InvalidHomeMunicipalityException;
import fi.om.municipalityinitiative.exceptions.NotFoundException;
import fi.om.municipalityinitiative.exceptions.OperationNotAllowedException;
import fi.om.municipalityinitiative.service.ServiceIntegrationTestBase;
import fi.om.municipalityinitiative.service.id.NormalAuthorId;
import fi.om.municipalityinitiative.service.id.VerifiedUserId;
import fi.om.municipalityinitiative.sql.QAuthor;
import fi.om.municipalityinitiative.sql.QAuthorInvitation;
import fi.om.municipalityinitiative.sql.QVerifiedAuthor;
import fi.om.municipalityinitiative.sql.QVerifiedParticipant;
import fi.om.municipalityinitiative.util.InitiativeState;
import fi.om.municipalityinitiative.util.Locales;
import fi.om.municipalityinitiative.util.Membership;
import fi.om.municipalityinitiative.util.ReflectionTestUtils;
import fi.om.municipalityinitiative.util.hash.PreviousHashGetter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static fi.om.municipalityinitiative.service.ui.AuthorService.AuthorInvitationConfirmViewData;
import static fi.om.municipalityinitiative.util.TestUtil.precondition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AuthorServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Resource
    AuthorService authorService;

    private Long testMunicipality;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final NormalAuthorId someAuthorId = new NormalAuthorId(-5);

    @Override
    public void childSetup() {
        testMunicipality = testHelper.createTestMunicipality("municipality");
    }

    @Test(expected = AccessDeniedException.class)
    public void create_invitation_checks_management_rights_for_initiative() {
        authorService.createAuthorInvitation(null, TestHelper.unknownLoginUserHolder, null);
    }

    @Test(expected = OperationNotAllowedException.class)
    public void create_invitation_not_allowed_if_initiative_already_sent() {

        Long initiativeId = testHelper.createSingleSent(testMunicipality);

        authorService.createAuthorInvitation(initiativeId, TestHelper.authorLoginUserHolder, null);
    }

    @Test
    public void create_invitation_sets_required_information() {
        Long initiativeId = testHelper.createCollaborativeAccepted(testMunicipality);

        AuthorInvitationUICreateDto authorInvitationUICreateDto = authorInvitation();

        authorService.createAuthorInvitation(initiativeId, TestHelper.authorLoginUserHolder, authorInvitationUICreateDto);

        AuthorInvitation createdInvitation = testHelper.getAuthorInvitation(PreviousHashGetter.get());
        assertThat(createdInvitation.getConfirmationCode(), is(PreviousHashGetter.get()));
        assertThat(createdInvitation.getName(), is("name"));
        assertThat(createdInvitation.getInvitationTime(), is(notNullValue()));
        assertThat(createdInvitation.getEmail(), is("email"));
    }

    @Test
    public void reject_author_invitation() {
        Long initiativeId = testHelper.createCollaborativeAccepted(testHelper.createTestMunicipality("name"));

        authorService.createAuthorInvitation(initiativeId, TestHelper.authorLoginUserHolder, authorInvitation());
        precondition(testHelper.getAuthorInvitation(PreviousHashGetter.get()).isRejected(), is(false));

        authorService.rejectInvitation(initiativeId, PreviousHashGetter.get());

        assertThat(testHelper.getAuthorInvitation(PreviousHashGetter.get()).isRejected(), is(true));

    }

    @Test
    public void confirm_normal_author_invitation_adds_new_author_with_given_information() {

        Long authorsMunicipality = testHelper.createTestMunicipality("name");
        Long initiativeId = testHelper.createCollaborativeAccepted(authorsMunicipality);
        AuthorInvitation invitation = createInvitation(initiativeId);

        AuthorInvitationUIConfirmDto createDto = new AuthorInvitationUIConfirmDto();
        createDto.setContactInfo(new ContactInfo());
        createDto.assignInitiativeMunicipality(testMunicipality);
        createDto.getContactInfo().setName("name");
        createDto.getContactInfo().setAddress("address");
        createDto.getContactInfo().setEmail("email");
        createDto.getContactInfo().setPhone("phone");
        createDto.getContactInfo().setShowName(true);
        createDto.setConfirmCode(invitation.getConfirmationCode());
        createDto.setMunicipalMembership(Membership.community); //XXX: Not tested
        createDto.setHomeMunicipality(authorsMunicipality);

        testHelper.denormalizeParticipantCount(initiativeId);

        precondition(currentAuthors(initiativeId).size(), is(1));
        precondition(participantCountOfInitiative(initiativeId), is(1));

        authorService.confirmAuthorInvitation(initiativeId, createDto, null);

        // Author count is increased
        precondition(countAllNormalAuthors(), is(2L));

        // Check new author information
        List<? extends Author> currentAuthors = currentAuthors(initiativeId);

        Author createdAuthor = currentAuthors.get(currentAuthors.size() -1);
        assertThat(createdAuthor.getContactInfo().getName(), is(createDto.getContactInfo().getName()));
        assertThat(createdAuthor.getContactInfo().getEmail(), is(createDto.getContactInfo().getEmail()));
        assertThat(createdAuthor.getContactInfo().getAddress(), is(createDto.getContactInfo().getAddress()));
        assertThat(createdAuthor.getContactInfo().getPhone(), is(createDto.getContactInfo().getPhone()));
        assertThat(createdAuthor.getContactInfo().isShowName(), is(createDto.getContactInfo().isShowName()));
        assertThat(((Municipality) createdAuthor.getMunicipality().get()).getId(), is(authorsMunicipality));

        assertThat(participantCountOfInitiative(initiativeId), is(2));
    }

    @Test(expected = InvalidHomeMunicipalityException.class)
    public void confirm_author_invitation_rejects_if_invalid_homeMunicipality_and_membership() {

        Long authorsMunicipality = testHelper.createTestMunicipality("name");
        Long initiativeId = testHelper.createCollaborativeAccepted(authorsMunicipality);
        AuthorInvitation invitation = createInvitation(initiativeId);

        AuthorInvitationUIConfirmDto createDto = new AuthorInvitationUIConfirmDto();
        createDto.setContactInfo(new ContactInfo());
        createDto.assignInitiativeMunicipality(testMunicipality);
        createDto.getContactInfo().setName("name");
        createDto.getContactInfo().setAddress("address");
        createDto.getContactInfo().setEmail("email");
        createDto.getContactInfo().setPhone("phone");
        createDto.getContactInfo().setShowName(true);
        createDto.setConfirmCode(invitation.getConfirmationCode());
        createDto.setMunicipalMembership(Membership.community); //XXX: Not tested
        createDto.setHomeMunicipality(testHelper.createTestMunicipality("asd"));
        createDto.setMunicipalMembership(Membership.none);


        authorService.confirmAuthorInvitation(initiativeId, createDto, Locales.LOCALE_FI);
    }

    private List<? extends Author> currentAuthors(Long initiativeId) {
        return authorService.findAuthors(initiativeId, TestHelper.authorLoginUserHolder);
       // return authorService.findAuthors(initiativeId, new LoginUserHolder<>(User.normalUser(-1L, Collections.singleton(initiativeId))));
    }

    private int participantCountOfInitiative(Long initiativeId) {
        return testHelper.getInitiative(initiativeId).getParticipantCount();
    }

    @Test(expected = OperationNotAllowedException.class)
    public void confirm_author_invitation_not_allowed() {
        Long initiativeId = testHelper.createSingleSent(testMunicipality);

        authorService.confirmAuthorInvitation(initiativeId, new AuthorInvitationUIConfirmDto(), null);
    }

    @Test
    public void confirm_author_with_expired_invitation_throws_exception() {
        Long initiativeId = testHelper.createCollaborativeAccepted(testMunicipality);
        AuthorInvitation expiredInvitation = createExpiredInvitation(initiativeId);

        AuthorInvitationUIConfirmDto confirmDto = new AuthorInvitationUIConfirmDto();
        confirmDto.setConfirmCode(expiredInvitation.getConfirmationCode());

        thrown.expect(InvitationNotValidException.class);
        thrown.expectMessage("Invitation is expired");
        authorService.confirmAuthorInvitation(initiativeId, confirmDto, null);
    }

    @Test
    public void confirm_normal_author_invitation_increases_public_names_count() {
        Long initiativeId = testHelper.createCollaborativeAccepted(testMunicipality);
        AuthorInvitation invitation = createInvitation(initiativeId);

        AuthorInvitationUIConfirmDto confirmDto = ReflectionTestUtils.modifyAllFields(new AuthorInvitationUIConfirmDto());
        confirmDto.getContactInfo().setShowName(true);
        confirmDto.assignInitiativeMunicipality(testMunicipality);
        confirmDto.setHomeMunicipality(testMunicipality);
        confirmDto.setConfirmCode(invitation.getConfirmationCode());

        int originalParticipantCount = testHelper.getInitiative(initiativeId).getParticipantCount();
        int originalPublicParticipantCount = testHelper.getInitiative(initiativeId).getParticipantCountPublic();
        int originalCitizenCount = testHelper.getInitiative(initiativeId).getParticipantCountCitizen();

        authorService.confirmAuthorInvitation(initiativeId, confirmDto, Locales.LOCALE_FI);

        assertThat(testHelper.getInitiative(initiativeId).getParticipantCount(), is(originalParticipantCount + 1));
        assertThat(testHelper.getInitiative(initiativeId).getParticipantCountPublic(), is(originalPublicParticipantCount + 1));
        assertThat(testHelper.getInitiative(initiativeId).getParticipantCountCitizen(), is(originalCitizenCount + 1));

    }

    @Test
    public void confirm_author_with_rejected_invitation_throws_exception() {
        Long initiativeId = testHelper.createCollaborativeAccepted(testMunicipality);
        AuthorInvitation rejectedInvitation = createRejectedInvitation(initiativeId);

        AuthorInvitationUIConfirmDto confirmDto = new AuthorInvitationUIConfirmDto();
        confirmDto.setConfirmCode(rejectedInvitation.getConfirmationCode());

        thrown.expect(InvitationNotValidException.class);
        thrown.expectMessage("Invitation is rejected");
        authorService.confirmAuthorInvitation(initiativeId, confirmDto, null);
    }

    @Test
    public void confirm_author_with_invalid_confirmCode_throws_exception() {
        Long initiativeId = testHelper.createCollaborativeAccepted(testMunicipality);
        createInvitation(initiativeId);

        AuthorInvitationUIConfirmDto invitationUIConfirmDto = new AuthorInvitationUIConfirmDto();
        invitationUIConfirmDto.setConfirmCode("bätmään!");

        thrown.expect(NotFoundException.class);
        thrown.expectMessage(containsString("bätmään"));
        authorService.confirmAuthorInvitation(initiativeId, invitationUIConfirmDto, null);
    }

    @Test
    public void invitation_is_removed_after_confirmation() {

        Long initiativeId = testHelper.createCollaborativeAccepted(testMunicipality);
        AuthorInvitation authorInvitation = createInvitation(initiativeId);

        AuthorInvitationUIConfirmDto confirmDto = ReflectionTestUtils.modifyAllFields(new AuthorInvitationUIConfirmDto());
        confirmDto.setConfirmCode(authorInvitation.getConfirmationCode());
        confirmDto.assignInitiativeMunicipality(testMunicipality);
        confirmDto.setHomeMunicipality(testMunicipality);

        precondition(allCurrentInvitations(), is(1L));
        authorService.confirmAuthorInvitation(initiativeId, confirmDto, null);
        assertThat(allCurrentInvitations(), is(0L));

    }

    private static DateTime expiredInvitationTime() {
        return DateTime.now().minusMonths(1);
    }

    @Test
    public void prefilled_normal_author_confirmation_contains_authors_information() {
        Long municipalityId = testHelper.createTestMunicipality("name");
        Long initiativeId = testHelper.createCollaborativeAccepted(municipalityId);
        authorService.createAuthorInvitation(initiativeId, TestHelper.authorLoginUserHolder, authorInvitation());

        AuthorInvitationUIConfirmDto confirmDto = authorService.getAuthorInvitationConfirmData(initiativeId, PreviousHashGetter.get(), TestHelper.unknownLoginUserHolder).authorInvitationUIConfirmDto;
        assertThat(confirmDto.getMunicipality(), is(municipalityId));
        assertThat(confirmDto.getContactInfo().getName(), is(authorInvitation().getAuthorName()));
        assertThat(confirmDto.getContactInfo().getEmail(), is(authorInvitation().getAuthorEmail()));
        assertThat(confirmDto.getConfirmCode(), is(PreviousHashGetter.get()));
    }

    @Test
    public void getting_prefilled_author_confirmation_returns_empty_userData_if_verified_initiative_and_user_not_vetumaVerified() {
        Long initiativeId = testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipality));
        String invitationEmail = "AnyEmail";
        String invitationUserName = "AnyName";
        String invitationConfirmationCode = testHelper.createInvitation(initiativeId, invitationUserName, invitationEmail).getConfirmationCode();
        AuthorInvitationConfirmViewData authorInvitationConfirmData = authorService.getAuthorInvitationConfirmData(initiativeId, invitationConfirmationCode, TestHelper.unknownLoginUserHolder);

        ContactInfo expectedContactInfo = new ContactInfo();
        expectedContactInfo.setEmail(invitationEmail);
        expectedContactInfo.setName(invitationUserName);

        ReflectionTestUtils.assertReflectionEquals(authorInvitationConfirmData.authorInvitationUIConfirmDto.getContactInfo(), expectedContactInfo);
    }

    @Test
    public void prefilled_verified_author_confirmation_contains_authors_information() {

        Long initiativeId = testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipality).applyAuthor().toInitiativeDraft()
                .withState(InitiativeState.ACCEPTED));
        authorService.createAuthorInvitation(initiativeId, TestHelper.authorLoginUserHolder, authorInvitation());

        String confirmCode = PreviousHashGetter.get();
        LoginUserHolder<VerifiedUser> verifiedLoginUserHolderFor = getVerifiedLoginUserHolderFor(initiativeId);

        verifiedLoginUserHolderFor.getVerifiedUser().getContactInfo().setEmail("email that should not be prefilled in UI");

        AuthorInvitationConfirmViewData authorInvitationConfirmData = authorService.getAuthorInvitationConfirmData(initiativeId, confirmCode, verifiedLoginUserHolderFor);

        assertThat(authorInvitationConfirmData.authorInvitationUIConfirmDto.getConfirmCode(), is(confirmCode));
        assertThat(authorInvitationConfirmData.authorInvitationUIConfirmDto.getMunicipality(), is(testMunicipality));

        ContactInfo expectedContactInfo = new ContactInfo();
        expectedContactInfo.setEmail(authorInvitation().getAuthorEmail());
        expectedContactInfo.setAddress(verifiedLoginUserHolderFor.getVerifiedUser().getContactInfo().getAddress());
        expectedContactInfo.setName(verifiedLoginUserHolderFor.getVerifiedUser().getContactInfo().getName());
        expectedContactInfo.setPhone(verifiedLoginUserHolderFor.getVerifiedUser().getContactInfo().getPhone());
        expectedContactInfo.setShowName(true);
        ReflectionTestUtils.assertReflectionEquals(authorInvitationConfirmData.authorInvitationUIConfirmDto.getContactInfo(),
                expectedContactInfo);

    }

    @Test
    public void prefilled_author_confirmation_throws_exception_if_invitation_expired() {
        Long initiativeId = testHelper.createCollaborativeReview(testMunicipality);
        AuthorInvitation expiredInvitation = createExpiredInvitation(initiativeId);

        thrown.expect(InvitationNotValidException.class);
        thrown.expectMessage("Invitation is expired");
        authorService.getAuthorInvitationConfirmData(initiativeId, expiredInvitation.getConfirmationCode(), TestHelper.unknownLoginUserHolder);

    }

    @Test
    public void prefilled_normal_author_confirmation_has_initiative_info() {
        Long municipalityId = testHelper.createTestMunicipality("name");
        Long initiativeId = testHelper.createCollaborativeAccepted(municipalityId);
        authorService.createAuthorInvitation(initiativeId, TestHelper.authorLoginUserHolder, authorInvitation());

        InitiativeViewInfo confirmDto = authorService.getAuthorInvitationConfirmData(initiativeId, PreviousHashGetter.get(), TestHelper.unknownLoginUserHolder).initiativeViewInfo;
        assertThat(confirmDto.getId(), is(initiativeId));
    }

    @Test
    public void prefilled_verified_author_confirmation_has_initiative_info() {
        Long municipalityId = testHelper.createTestMunicipality("name");
        Long initiativeId = testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipality).applyAuthor().toInitiativeDraft()
                .withState(InitiativeState.ACCEPTED));
        authorService.createAuthorInvitation(initiativeId, TestHelper.authorLoginUserHolder, authorInvitation());

        InitiativeViewInfo confirmDto = authorService.getAuthorInvitationConfirmData(initiativeId, PreviousHashGetter.get(), getVerifiedLoginUserHolderFor(municipalityId)).initiativeViewInfo;
        assertThat(confirmDto.getId(), is(initiativeId));
    }

    @Test
    public void prefilled_author_confirmation_throws_exception_if_invitation_rejected() {
        Long initiativeId = testHelper.createCollaborativeReview(testMunicipality);
        AuthorInvitation rejectedInvitation = createRejectedInvitation(initiativeId);

        thrown.expect(InvitationNotValidException.class);
        thrown.expectMessage("Invitation is rejected");
        authorService.getAuthorInvitationConfirmData(initiativeId, rejectedInvitation.getConfirmationCode(), TestHelper.unknownLoginUserHolder);
    }

    @Test
    public void prefilled_author_confirmation_throws_exception_if_invitation_not_found() {
        Long initiativeId = testHelper.createCollaborativeReview(testMunicipality);

        thrown.expect(InvitationNotValidException.class);
        authorService.getAuthorInvitationConfirmData(initiativeId, "töttöröö", TestHelper.unknownLoginUserHolder);
    }

    @Test
    public void resend_invitation_throws_exception_if_no_management_rights() {
        Long initiativeId = testHelper.createCollaborativeReview(testMunicipality);
        thrown.expect(AccessDeniedException.class);
        authorService.resendInvitation(initiativeId, TestHelper.unknownLoginUserHolder, null);
    }

    @Test
    public void resend_invitation_updates_invitation_time_to_current_time() {
        Long initiativeId = testHelper.createCollaborativeAccepted(testMunicipality);
        DateTime invitationTime = new DateTime(2010, 1, 1, 0, 0);
        AuthorInvitation invitation = createInvitation(initiativeId, invitationTime);

        precondition(testHelper.getAuthorInvitation(invitation.getConfirmationCode()).getInvitationTime(), is(invitationTime));
        precondition(allCurrentInvitations(), is(1L));

        authorService.resendInvitation(initiativeId, TestHelper.authorLoginUserHolder, invitation.getConfirmationCode());

        assertThat(testHelper.getAuthorInvitation(invitation.getConfirmationCode()).getInvitationTime().toLocalDate(), is(new LocalDate()));
        assertThat(allCurrentInvitations(), is(1L));

    }

    @Test
    public void deleting_author_throws_exception_if_not_management_rights() {
        Long initiativeId = testHelper.createCollaborativeAccepted(testMunicipality);

        thrown.expect(AccessDeniedException.class);
        authorService.deleteAuthor(initiativeId, TestHelper.unknownLoginUserHolder, testHelper.getLastNormalAuthorId().toLong(), true);

    }

    @Test
    public void deleting_final_author_is_not_allowed() {
        Long initiativeId = testHelper.createCollaborativeAccepted(testMunicipality);
        LoginUserHolder fakeLoginUserHolderWithManagementRights = new LoginUserHolder(User.normalUser(someAuthorId, Collections.singleton(initiativeId)));

        thrown.expect(OperationNotAllowedException.class);
        thrown.expectMessage(containsString("Unable to delete the final author"));
        authorService.deleteAuthor(initiativeId, fakeLoginUserHolderWithManagementRights, testHelper.getLastNormalAuthorId().toLong(), false);
    }

    @Test
    public void deleting_author_not_allowed_after_initiative_sent_to_municipality() {
        Long initiative = testHelper.createSingleSent(testMunicipality);
        Long anotherAuthor = testHelper.getLastNormalAuthorId().toLong();
        Long currentAuthor = testHelper.createDefaultAuthorAndParticipant(new TestHelper.AuthorDraft(initiative, testMunicipality).withParticipantEmail("author_left@example.com"));

        precondition(countAllNormalAuthors(), is(2L));

        thrown.expect(OperationNotAllowedException.class);
        thrown.expectMessage(containsString("Operation not allowed: Invite authors"));
        authorService.deleteAuthor(initiative, TestHelper.authorLoginUserHolder, anotherAuthor, true);
    }

    @Test
    public void deleting_normal_author_fails_if_initiativeId_and_authorId_mismatch() {
        Long initiative1 = testHelper.createCollaborativeAccepted(testMunicipality);
        Long author1 = testHelper.createDefaultAuthorAndParticipant(new TestHelper.AuthorDraft(initiative1, testMunicipality));

        Long initiative2 = testHelper.createCollaborativeAccepted(testMunicipality);
        Long author2 = testHelper.createDefaultAuthorAndParticipant(new TestHelper.AuthorDraft(initiative2, testMunicipality));

        thrown.expect(NotFoundException.class);
        thrown.expectMessage(containsString("initiative"));
        thrown.expectMessage(containsString("author"));
        authorService.deleteAuthor(initiative2, TestHelper.authorLoginUserHolder, author1, false);
    }

    @Test
    public void deleting_verified_author_fails_if_initiativeId_and_authorId_mismatch() {
        Long verifiedInitiative = testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipality)
                .withState(InitiativeState.PUBLISHED)
                .applyAuthor("121212-1212").toInitiativeDraft());
        testHelper.createVerifiedAuthorAndParticipant(new TestHelper.AuthorDraft(verifiedInitiative, testMunicipality));
        LoginUserHolder authorLoginUserHolder = TestHelper.authorLoginUserHolder;

        Long anotherInitiative = testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipality));
        Long anotherAuthor = testHelper.createVerifiedAuthorAndParticipant(new TestHelper.AuthorDraft(anotherInitiative, testMunicipality));

        thrown.expect(NotFoundException.class);
        thrown.expectMessage(containsString("initiative"));
        thrown.expectMessage(containsString("author"));
        authorService.deleteAuthor(verifiedInitiative, authorLoginUserHolder, anotherAuthor, true);
    }

    @Test
    public void deleting_normal_author_succeeds_and_sends_emails() throws Exception {

        Long initiative = testHelper.createCollaborativeAccepted(testMunicipality);
        Long anotherAuthor = testHelper.getLastNormalAuthorId().toLong();
        Long currentAuthor = testHelper.createDefaultAuthorAndParticipant(new TestHelper.AuthorDraft(initiative, testMunicipality).withParticipantEmail("author_left@example.com"));
        testHelper.denormalizeParticipantCount(initiative);

        int originalParticipantCount = testHelper.getInitiative(initiative).getParticipantCount();
        precondition(countAllNormalAuthors(), is(2L));

        authorService.deleteAuthor(initiative, TestHelper.authorLoginUserHolder, anotherAuthor, false);

        assertThat(testHelper.getInitiative(initiative).getParticipantCount(), is(originalParticipantCount - 1));
        assertThat(countAllNormalAuthors(), is(1L));

        List<EmailDto> emails = testHelper.findQueuedEmails();
        assertThat(emails, hasSize(2));

        assertThat(emails.get(0).getRecipientsAsString(), is("author_left@example.com"));
        assertThat(emails.get(0).getSubject(), containsString("Vastuuhenkilö on poistettu aloitteestasi"));
        assertThat(emails.get(0).getBodyHtml(), containsString(TestHelper.DEFAULT_PARTICIPANT_EMAIL));

        assertThat(emails.get(1).getRecipientsAsString(), is(TestHelper.DEFAULT_PARTICIPANT_EMAIL));
        assertThat(emails.get(1).getSubject(), containsString("Sinut on poistettu aloitteen vastuuhenkilöistä"));

    }

    @Test
    public void deleting_verified_author_deletes_author_and_participant_and_decreases_participantCount() throws Exception {

        Long initiativeId = testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipality).withState(InitiativeState.ACCEPTED).applyAuthor().toInitiativeDraft());
        Long originalAuthor = testHelper.getLastVerifiedUserId();

        testHelper.createVerifiedAuthorAndParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipality));
        testHelper.denormalizeParticipantCount(initiativeId);

        precondition(countAllVerifiedAuthors(), is(2));
        precondition(countAllVerifiedParticipants(), is(2));
        precondition(testHelper.getInitiative(initiativeId).getParticipantCount(), is(2));
        authorService.deleteAuthor(initiativeId, TestHelper.authorLoginUserHolder, originalAuthor, true);
        assertThat(countAllVerifiedAuthors(), is(1));
        assertThat(countAllVerifiedParticipants(), is(1));
        assertThat(testHelper.getInitiative(initiativeId).getParticipantCount(), is(1));
    }

    @Test
    public void deleting_verified_author_fails_if_trying_to_delete_myself() {
        Long initiativeId = testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipality).withState(InitiativeState.ACCEPTED).applyAuthor().toInitiativeDraft());
        Long authorId = testHelper.getLastVerifiedUserId();

        thrown.expect(OperationNotAllowedException.class);
        thrown.expectMessage(containsString("Removing yourself from authors is not allowed"));
        authorService.deleteAuthor(initiativeId, TestHelper.authorLoginUserHolder, authorId, true);

    }

    private int countAllVerifiedParticipants() {
        return testHelper.countAll(QVerifiedParticipant.verifiedParticipant).intValue();
    }


    @Test
    public void find_authors_for_default_initiative_returns_authors() {
        Long initiativeId = testHelper.createDefaultInitiative(new TestHelper.InitiativeDraft(testMunicipality).applyAuthor().toInitiativeDraft());

        assertThat(authorService.findPublicAuthors(initiativeId).getPublicAuthors(), hasSize(1));
        assertThat(authorService.findAuthors(initiativeId, TestHelper.authorLoginUserHolder), hasSize(1));
    }

    @Test
    public void find_authors_for_verified_initiative_returns_authors() {

        Long someOtherInitiative = testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipality).applyAuthor().toInitiativeDraft());

        Long initiativeId = testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipality).applyAuthor().toInitiativeDraft());
        testHelper.createVerifiedAuthorAndParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipality));

        assertThat(authorService.findPublicAuthors(initiativeId).getPublicAuthors(), hasSize(2));
        assertThat(authorService.findAuthors(initiativeId, TestHelper.authorLoginUserHolder), hasSize(2));

    }

    @Test
    public void find_authors_for_verified_initiative_retrieves_all_data() {

        Long initiativeId = testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipality).applyAuthor().toInitiativeDraft());

        Author author = authorService.findAuthors(initiativeId, TestHelper.authorLoginUserHolder).get(0);

        assertThat(author.getContactInfo().getName(), is(TestHelper.DEFAULT_PARTICIPANT_NAME));
        assertThat(author.getContactInfo().getAddress(), is(TestHelper.DEFAULT_AUTHOR_ADDRESS));
        assertThat(author.getContactInfo().getEmail(), is(TestHelper.DEFAULT_PARTICIPANT_EMAIL));
        assertThat(author.getContactInfo().getPhone(), is(TestHelper.DEFAULT_AUTHOR_PHONE));
        assertThat(author.getContactInfo().isShowName(), is(TestHelper.DEFAULT_PUBLIC_NAME));
        assertThat(author.getMunicipality().isPresent(), is(true));
        assertThat(author.getCreateTime(), is(notNullValue()));
        assertThat(author.getId(), is(notNullValue()));
    }

    private Long countAllNormalAuthors() {
        return testHelper.countAll(QAuthor.author);
    }

    private int countAllVerifiedAuthors() {
        return testHelper.countAll(QVerifiedAuthor.verifiedAuthor).intValue();
    }

    private Long allCurrentInvitations() {
        return testHelper.countAll(QAuthorInvitation.authorInvitation);
    }

    private AuthorInvitation createExpiredInvitation(Long initiativeId) {
        AuthorInvitation authorInvitation = ReflectionTestUtils.modifyAllFields(new AuthorInvitation());
        authorInvitation.setInitiativeId(initiativeId);
        authorInvitation.setInvitationTime(expiredInvitationTime());
        testHelper.addAuthorInvitation(authorInvitation, false);
        return authorInvitation;
    }

    private AuthorInvitation createInvitation(Long initiativeId, DateTime invitationTime) {
        AuthorInvitation authorInvitation = ReflectionTestUtils.modifyAllFields(new AuthorInvitation());
        authorInvitation.setInitiativeId(initiativeId);
        authorInvitation.setInvitationTime(invitationTime);
        testHelper.addAuthorInvitation(authorInvitation, false);
        return authorInvitation;
    }
    private AuthorInvitation createInvitation(Long initiativeId) {
        return createInvitation(initiativeId, DateTime.now());
    }

    private AuthorInvitation createRejectedInvitation(Long initiativeId) {
        AuthorInvitation authorInvitation = ReflectionTestUtils.modifyAllFields(new AuthorInvitation());
        authorInvitation.setInitiativeId(initiativeId);
        authorInvitation.setInvitationTime(DateTime.now());
        testHelper.addAuthorInvitation(authorInvitation, true);
        return authorInvitation;
    }

    private static AuthorInvitationUICreateDto authorInvitation() {
        AuthorInvitationUICreateDto authorInvitationUICreateDto = new AuthorInvitationUICreateDto();
        authorInvitationUICreateDto.setAuthorName("name");
        authorInvitationUICreateDto.setAuthorEmail("email");
        return authorInvitationUICreateDto;
    }

    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String ADDRESS = "address";
    public static final String NAME = "name";
    public static final boolean SHOW_NAME = true;
    public LoginUserHolder<VerifiedUser> getVerifiedLoginUserHolderFor(Long initiativeId) {
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setEmail(EMAIL);
        contactInfo.setPhone(PHONE);
        contactInfo.setAddress(ADDRESS);
        contactInfo.setName(NAME);
        contactInfo.setShowName(SHOW_NAME);

        return new LoginUserHolder(User.verifiedUser(new VerifiedUserId(-1L), "hash", contactInfo, Collections.singleton(initiativeId), Collections.singleton(initiativeId), Optional.of(new Municipality(testMunicipality, "nameFi", "nameSv", true)), 20));
    }

}
