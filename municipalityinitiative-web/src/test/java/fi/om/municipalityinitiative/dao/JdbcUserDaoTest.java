package fi.om.municipalityinitiative.dao;

import fi.om.municipalityinitiative.conf.IntegrationTestConfiguration;
import fi.om.municipalityinitiative.dto.service.Municipality;
import fi.om.municipalityinitiative.dto.service.VerifiedUserDbDetails;
import fi.om.municipalityinitiative.dto.ui.ContactInfo;
import fi.om.municipalityinitiative.service.id.VerifiedUserId;
import fi.om.municipalityinitiative.util.InitiativeState;
import fi.om.municipalityinitiative.util.InitiativeType;
import fi.om.municipalityinitiative.util.ReflectionTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

import static fi.om.municipalityinitiative.util.OptionalMatcher.isNotPresent;
import static fi.om.municipalityinitiative.util.OptionalMatcher.isPresent;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={IntegrationTestConfiguration.class})
@Transactional
public class JdbcUserDaoTest {

    public static final String EMAIL = "email";
    public static final String NAME = "name";
    public static final String PHONE = "phone";
    public static final String ADDRESS = "address";
    public static final String HASH = "hash";

    @Resource
    private TestHelper testHelper;

    @Resource
    private UserDao userDao;

    @Resource
    ParticipantDao participantDao;

    private Optional<Municipality> testMunicipality;
    private Long testMunicipalityId;
    private Long testInitiativeId;
    private Long testVerifiedInitiativeId;
    private Long otherMunicipalityId;

    @Before
    public void setup() throws Exception {
        testHelper.dbCleanup();
        testMunicipality = Optional.of(new Municipality(testHelper.createTestMunicipality("Municipality"), "Municipality", "Municipality", true));
        testMunicipalityId = testMunicipality.get().getId();
        testInitiativeId = testHelper.createWithAuthor(testMunicipalityId, InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE);
        testVerifiedInitiativeId = testHelper.createWithAuthor(testMunicipalityId, InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE_CITIZEN);
    }

    @Test
    public void create_and_get_verified_user() {
        ContactInfo contactInfo = contactInfo();
        VerifiedUserId verifiedUserId = userDao.addVerifiedUser(HASH, contactInfo, testMunicipality);
        Optional<VerifiedUserDbDetails> verifiedUser = userDao.getVerifiedUser(HASH);
        assertThat(verifiedUser, isPresent());
        assertThat(verifiedUserId, is(notNullValue()));
        ReflectionTestUtils.assertReflectionEquals(verifiedUser.get().getContactInfo(), contactInfo);
        assertThat(verifiedUser.get().getHomeMunicipality().get().getId(), is(testMunicipality.get().getId()));
    }

    @Test
    public void update_contact_info() {
        userDao.addVerifiedUser(HASH, contactInfo(), testMunicipality);

        ContactInfo updatedContactInfo = ReflectionTestUtils.modifyAllFields(new ContactInfo());
        userDao.updateUserInformation(HASH, updatedContactInfo);

        ContactInfo result = userDao.getVerifiedUser(HASH).get().getContactInfo();
        assertThat(result.getPhone(), is(updatedContactInfo.getPhone()));
        assertThat(result.getAddress(), is(updatedContactInfo.getAddress()));
        assertThat(result.getEmail(), is(updatedContactInfo.getEmail()));
        assertThat(result.getName(), is(not(updatedContactInfo.getName()))); // Name should not be changed);
    }

    @Test
    public void update_email_for_verified_user() {
        Long verifiedUserId = userDao.addVerifiedUser(HASH, contactInfo(), testMunicipality).toLong();

        String newEmail = "new@example.com";
        userDao.updateEmailForVerifiedUser(verifiedUserId, newEmail);

        ContactInfo result = userDao.getVerifiedUser(HASH).get().getContactInfo();
        assertThat(result.getEmail(), is(newEmail));
    }

    @Test
    public void update_name_and_municipality() {

        userDao.addVerifiedUser(HASH, contactInfo(), testMunicipality);

        String newName = "New Name";
        String newMunicipalityName = "name";
        userDao.updateUserInformation(HASH, newName, Optional.of(new Municipality(testHelper.createTestMunicipality(newMunicipalityName), newMunicipalityName, newMunicipalityName, true)));

        VerifiedUserDbDetails result = userDao.getVerifiedUser(HASH).get();
        assertThat(result.getContactInfo().getName(), is(newName));
        assertThat(result.getHomeMunicipality(), isPresent());
        assertThat(result.getHomeMunicipality().get().getNameFi(), is(newMunicipalityName));
    }

    @Test
    public void get_returns_absent_if_not_found() {
        assertThat(userDao.getVerifiedUser("unknown-user-hash"), isNotPresent());
    }

    @Test
    public void get_initiatives_verified_user_has_participated() {

        userDao.addVerifiedUser(HASH, contactInfo(), testMunicipality);
        // Verified user participates verified initiative
        Long verifiedUserId = userDao.getVerifiedUserId(HASH).get().toLong();
        testHelper.createVerifiedParticipantWithVerifiedUserId(new TestHelper.AuthorDraft(testVerifiedInitiativeId, testMunicipalityId).withVerifiedUserId(verifiedUserId));

        VerifiedUserDbDetails user = userDao.getVerifiedUser(HASH).get();

        assertThat(user.getInitiativesWithParticipation(), contains(testVerifiedInitiativeId));
    }

    private static ContactInfo contactInfo() {
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setEmail(EMAIL);
        contactInfo.setName(NAME);
        contactInfo.setPhone(PHONE);
        contactInfo.setAddress(ADDRESS);
        return contactInfo;
    }
}
