package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.exceptions.OperationNotAllowedException;
import fi.om.municipalityinitiative.newdao.AuthorDao;
import fi.om.municipalityinitiative.newdao.InitiativeDao;
import fi.om.municipalityinitiative.newdto.Author;
import fi.om.municipalityinitiative.newdto.service.Initiative;
import fi.om.municipalityinitiative.newdto.service.Municipality;
import fi.om.municipalityinitiative.newdto.ui.ContactInfo;
import fi.om.municipalityinitiative.newdto.ui.InitiativeDraftUIEditDto;
import fi.om.municipalityinitiative.newdto.ui.InitiativeUIUpdateDto;
import fi.om.municipalityinitiative.util.*;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static fi.om.municipalityinitiative.util.TestUtil.precondition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.stub;

public class InitiativeManagementServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Resource
    InitiativeManagementService service;

    @Resource
    InitiativeDao initiativeDao;

    @Resource
    TestHelper testHelper;

    @Resource
    AuthorDao authorDao;

    private static Municipality testMunicipality;

    @Before
    public void setup() {
        testHelper.dbCleanup();

        String municipalityName = "Test municipality";
        testMunicipality = new Municipality(testHelper.createTestMunicipality(municipalityName), municipalityName, municipalityName, false);

    }

    @Test
    public void all_functions_require_om_rights() throws InvocationTargetException, IllegalAccessException {

        for (Method method : InitiativeManagementService.class.getDeclaredMethods()) {
            if (method.getModifiers() != 1) {
                continue;
            }
            Object[] parameters = new Object[method.getParameterTypes().length];
            parameters[1] = TestHelper.unknownLoginUserHolder;
            try {
                System.out.println("Checking that method requires om rights: " + method.getName());
                method.invoke(service, parameters);
                fail("Should have checked om-rights for user: " + method.getName());
            } catch (InvocationTargetException e) {
                Assert.assertThat(e.getCause(), instanceOf(AccessDeniedException.class));
            }
        }
    }

    @Test(expected = OperationNotAllowedException.class)
    public void get_initiative_for_edit_fails_if_initiative_accepted() {
        Long collectableAccepted = testHelper.createCollectableAccepted(testMunicipality.getId());
        service.getInitiativeDraftForEdit(collectableAccepted, TestHelper.authorLoginUserHolder);
    }

    @Test(expected = AccessDeniedException.class)
    public void editing_initiative_throws_exception_if_wrong_author() {
        Long initiativeId = testHelper.createDraft(testMunicipality.getId());

        InitiativeDraftUIEditDto editDto = InitiativeDraftUIEditDto.parse(ReflectionTestUtils.modifyAllFields(new Initiative()), new ContactInfo());

        service.editInitiativeDraft(initiativeId, TestHelper.unknownLoginUserHolder, editDto);
    }

    @Test(expected = AccessDeniedException.class)
    public void getting_initiativeDraft_for_edit_throws_exception_if_not_allowed() {
        service.getInitiativeDraftForEdit(null, TestHelper.unknownLoginUserHolder);
    }

    @Test(expected = OperationNotAllowedException.class)
    public void edit_initiative_fails_if_initiative_accepted() {
        Long collectableAccepted = testHelper.createCollectableAccepted(testMunicipality.getId());
        service.editInitiativeDraft(collectableAccepted, TestHelper.authorLoginUserHolder, new InitiativeDraftUIEditDto());
    }

    @Test
    public void get_initiative_for_update_sets_all_required_information() {
        Long initiativeId = testHelper.createCollectableReview(testMunicipality.getId());
        stubAuthorLoginUserHolderWith(initiativeId);
        InitiativeUIUpdateDto initiativeForUpdate = service.getInitiativeForUpdate(initiativeId, TestHelper.authorLoginUserHolder);
        ReflectionTestUtils.assertNoNullFields(initiativeForUpdate);
    }

    private void stubAuthorLoginUserHolderWith(Long initiativeId) {
        stub(TestHelper.authorLoginUserHolder.getInitiative()).toReturn(Maybe.of(initiativeDao.get(initiativeId)));
    }

    @Test(expected = AccessDeniedException.class)
    public void get_initiative_for_update_fails_if_not_allowed() {
        Long initiativeId = testHelper.createCollectableAccepted(testMunicipality.getId());
        service.getInitiativeForUpdate(initiativeId, TestHelper.unknownLoginUserHolder);
    }

    @Test(expected = OperationNotAllowedException.class)
    public void get_initiative_for_update_fails_if_sent() {
        Long sent = testHelper.createSingleSent(testMunicipality.getId());
        service.getInitiativeForUpdate(sent, TestHelper.authorLoginUserHolder);
    }

    @Test
    public void editing_initiative_updates_all_required_fields() {

        Long initiativeId = testHelper.createDraft(testMunicipality.getId());

        InitiativeDraftUIEditDto editDto = InitiativeDraftUIEditDto.parse(
                ReflectionTestUtils.modifyAllFields(new Initiative()),
                ReflectionTestUtils.modifyAllFields(new ContactInfo())
        );

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setEmail("updated email");
        contactInfo.setAddress("updated address");
        contactInfo.setPhone("updated phone");
        contactInfo.setName("updated author name");
        contactInfo.setShowName(false); // As far as default is true ...
        editDto.setContactInfo(contactInfo);
        editDto.setName("updated initiative name");
        editDto.setProposal("updated proposal");
        editDto.setExtraInfo("updated extrainfo");

        service.editInitiativeDraft(initiativeId, TestHelper.authorLoginUserHolder, editDto);

        InitiativeDraftUIEditDto updated = service.getInitiativeDraftForEdit(initiativeId, TestHelper.authorLoginUserHolder);

        ReflectionTestUtils.assertReflectionEquals(updated.getContactInfo(), contactInfo);
        assertThat(updated.getName(), is(editDto.getName()));
        assertThat(updated.getProposal(), is(editDto.getProposal()));
        assertThat(updated.getContactInfo().isShowName(), is(editDto.getContactInfo().isShowName()));
        assertThat(updated.getExtraInfo(), is(editDto.getExtraInfo()));
        ReflectionTestUtils.assertNoNullFields(updated);

    }

    @Test
    public void send_initiative_as_review_sents_state_as_review_and_leaves_type_as_null_if_not_single() {
        Long initiativeId = testHelper.createDraft(testMunicipality.getId());

        service.sendReviewOnlyForAcceptance(initiativeId, TestHelper.authorLoginUserHolder, null);

        Initiative updated = initiativeDao.get(initiativeId);

        assertThat(updated.getState(), is(InitiativeState.REVIEW));
        assertThat(updated.getType(), is(InitiativeType.UNDEFINED));
    }

    @Test
    public void send_initiative_as_review_sets_state_as_review_and_type_as_single_if_single() {
        Long initiativeId = testHelper.createDraft(testMunicipality.getId());
        service.sendReviewAndStraightToMunicipality(initiativeId, TestHelper.authorLoginUserHolder, null, null);

        Initiative updated = initiativeDao.get(initiativeId);

        assertThat(updated.getState(), is(InitiativeState.REVIEW));
        assertThat(updated.getType(), is(InitiativeType.SINGLE));
    }

    @Test(expected = OperationNotAllowedException.class)
    public void send_review_and_to_municipality_fails_if_initiative_accepted() {
        Long accepted = testHelper.createCollectableAccepted(testMunicipality.getId());
        service.sendReviewAndStraightToMunicipality(accepted, TestHelper.authorLoginUserHolder, null, null);
    }

    @Test(expected = OperationNotAllowedException.class)
    public void send_review_not_single_fails_if_initiative_accepted() {
        Long accepted = testHelper.createCollectableAccepted(testMunicipality.getId());
        service.sendReviewOnlyForAcceptance(accepted, TestHelper.authorLoginUserHolder, null);
    }

    @Test(expected = AccessDeniedException.class)
    public void send_single_to_review_fails_if_no_right_to_initiative() {
        Long accepted = testHelper.createCollectableAccepted(testMunicipality.getId());
        service.sendReviewAndStraightToMunicipality(accepted, TestHelper.unknownLoginUserHolder, null, null);
    }

    @Test(expected = AccessDeniedException.class)
    public void send_to_review_fails_if_no_right_to_initiative() {
        Long accepted = testHelper.createCollectableAccepted(testMunicipality.getId());
        service.sendReviewOnlyForAcceptance(accepted, TestHelper.unknownLoginUserHolder, null);
    }

    @Test(expected = AccessDeniedException.class)
    public void send_fix_to_review_fails_if_no_right_to_initiative() {
        Long accepted = testHelper.createCollectableAccepted(testMunicipality.getId());
        service.sendFixToReview(accepted, TestHelper.unknownLoginUserHolder);
    }

    @Test(expected = OperationNotAllowedException.class)
    public void send_fix_to_review_fails_if_initiative_sent() {
        Long sent = testHelper.createSingleSent(testMunicipality.getId());
        service.sendFixToReview(sent, TestHelper.authorLoginUserHolder);
    }

    @Test
    public void send_fix_to_review_sets_fixState_as_review() {
        Long accepted = testHelper.createInitiative(new TestHelper.InitiativeDraft(testMunicipality.getId())
                .withState(InitiativeState.PUBLISHED)
                .withFixState(FixState.FIX)
                .applyAuthor()
                .toInitiativeDraft());

        precondition(initiativeDao.get(accepted).getFixState(), is(FixState.FIX));

        service.sendFixToReview(accepted, TestHelper.authorLoginUserHolder);

        assertThat(initiativeDao.get(accepted).getFixState(), is(FixState.REVIEW));
    }

    @Test(expected = OperationNotAllowedException.class)
    public void publish_initiative_fails_if_not_accepted() {
        Long review = testHelper.createCollectableReview(testMunicipality.getId());
//        service.publishAcceptedInitiative(review, false, authorLoginUserHolder, null);
        service.publishAndStartCollecting(review, TestHelper.authorLoginUserHolder);
    }

    @Test
    public void publish_initiative_and_start_collecting_sets_all_data() {
        Long accepted = testHelper.create(testMunicipality.getId(), InitiativeState.ACCEPTED, InitiativeType.UNDEFINED);

//        service.publishAcceptedInitiative(accepted, true, authorLoginUserHolder, null);
        service.publishAndStartCollecting(accepted, TestHelper.authorLoginUserHolder);

        Initiative collecting = initiativeDao.get(accepted);
        assertThat(collecting.getState(), is(InitiativeState.PUBLISHED));
        assertThat(collecting.getType(), is(InitiativeType.COLLABORATIVE));
        assertThat(collecting.getSentTime().isPresent(), is(false));
    }

    @Test(expected = AccessDeniedException.class)
    public void publish_inititive_and_send_to_municipality_fails_of_not_author() {
        Long accepted = testHelper.create(testMunicipality.getId(), InitiativeState.ACCEPTED, InitiativeType.UNDEFINED);
//        service.publishAcceptedInitiative(accepted, true, unknownLoginUserHolder, null);
        service.publishAndSendToMunicipality(accepted, TestHelper.unknownLoginUserHolder, "", null);
    }

    @Test
    public void publish_initiative_and_send_to_municipality_sets_all_data() {
        Long accepted = testHelper.create(testMunicipality.getId(), InitiativeState.ACCEPTED, InitiativeType.UNDEFINED);

//        service.publishAcceptedInitiative(accepted, false, authorLoginUserHolder, null);
        service.publishAndSendToMunicipality(accepted, TestHelper.authorLoginUserHolder, "some sent comment", null);

        Initiative sent = initiativeDao.get(accepted);
        assertThat(sent.getState(), is(InitiativeState.PUBLISHED));
        assertThat(sent.getType(), is(InitiativeType.SINGLE));
        assertThat(sent.getSentTime().isPresent(), is(true));
        assertThat(sent.getSentComment(), is("some sent comment"));
    }

    @Test(expected = OperationNotAllowedException.class)
    public void sending_collaborative_to_municipality_fails_if_already_sent() {
        Long collaborativeSent = testHelper.createInitiative(new TestHelper.InitiativeDraft(testMunicipality.getId())
                .withState(InitiativeState.PUBLISHED)
                .withType(InitiativeType.COLLABORATIVE)
                .withSent(DateTime.now()));

        service.sendCollaborativeToMunicipality(collaborativeSent, TestHelper.authorLoginUserHolder, "", null);
    }

    @Test(expected = AccessDeniedException.class)
    public void sending_collaborative_to_municipality_fails_if_no_rights_to_initiative() {
        Long collectableAccepted = testHelper.createCollectableAccepted(testMunicipality.getId());

        service.sendCollaborativeToMunicipality(collectableAccepted, TestHelper.unknownLoginUserHolder, "", null);
    }

    @Test
    public void sending_collobarative_to_municipality_sets_sent_time_and_sent_comment() {
        Long collaborative = testHelper.createInitiative(new TestHelper.InitiativeDraft(testMunicipality.getId())
                .withState(InitiativeState.PUBLISHED)
                .withType(InitiativeType.COLLABORATIVE)
                .applyAuthor().toInitiativeDraft());

        service.sendCollaborativeToMunicipality(collaborative, TestHelper.authorLoginUserHolder, "my sent comment", null);

        Initiative sent = initiativeDao.get(collaborative);
        assertThat(sent.getSentTime().isPresent(), is(true));
        assertThat(sent.getSentComment(), is("my sent comment"));
    }

    @Test
    public void sendToMunicipality_marks_initiative_as_sigle_if_not_marked_as_collaboratibe() {
        Long initiativeId = testHelper.create(testMunicipality.getId(), InitiativeState.ACCEPTED, InitiativeType.UNDEFINED);
        service.sendToMunicipality(initiativeId, TestHelper.authorLoginUserHolder, "comment for municipality", null);
        Initiative sent = initiativeDao.get(initiativeId);
        assertThat(sent.getType(), is(InitiativeType.SINGLE));
        assertThat(sent.getSentTime().isPresent(), is(true));
        assertThat(sent.getSentComment(), is("comment for municipality"));
    }

    @Test
    public void sendToMunicipality_marks_initiative_as_sent_if_marked_as_collaborative() {
        Long initiativeId = testHelper.create(testMunicipality.getId(), InitiativeState.ACCEPTED, InitiativeType.COLLABORATIVE);
        service.sendToMunicipality(initiativeId, TestHelper.authorLoginUserHolder, "comment for municipality", null);
        Initiative sent = initiativeDao.get(initiativeId);
        assertThat(sent.getType(), is(InitiativeType.COLLABORATIVE));
        assertThat(sent.getSentTime().isPresent(), is(true));
        assertThat(sent.getSentComment(), is("comment for municipality"));
    }

    @Test
    public void update_initiative_updates_given_fields() {

        Long initiativeId = testHelper.createCollectableAccepted(testMunicipality.getId());

        InitiativeUIUpdateDto updateDto = new InitiativeUIUpdateDto();
        ContactInfo contactInfo = new ContactInfo();
        updateDto.setContactInfo(contactInfo);

        updateDto.setExtraInfo("Modified extra info");
        contactInfo.setName("Modified Name");
        contactInfo.setAddress("Modified Address");
        contactInfo.setPhone("Modified Phone");
        contactInfo.setEmail("Modified Email");
        contactInfo.setShowName(false);
        updateDto.setContactInfo(contactInfo);
        service.updateInitiative(initiativeId, TestHelper.authorLoginUserHolder, updateDto);

        assertThat(initiativeDao.get(initiativeId).getExtraInfo(), is(updateDto.getExtraInfo()));

        Author author = authorDao.getAuthor(testHelper.getLastAuthorId());
        ReflectionTestUtils.assertReflectionEquals(author.getContactInfo(), contactInfo);

        // TODO: Assert extraInfo

    }

    @Test(expected = OperationNotAllowedException.class)
    public void update_initiative_fails_if_initiative_sent() {
        Long sent = testHelper.createSingleSent(testMunicipality.getId());
        service.updateInitiative(sent, TestHelper.authorLoginUserHolder, new InitiativeUIUpdateDto());
    }
}