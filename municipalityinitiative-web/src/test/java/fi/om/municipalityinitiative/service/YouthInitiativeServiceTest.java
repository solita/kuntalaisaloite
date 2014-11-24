package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.conf.IntegrationTestFakeEmailConfiguration;
import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.dto.YouthInitiativeCreateDto;
import fi.om.municipalityinitiative.dto.service.Initiative;
import fi.om.municipalityinitiative.dto.ui.ContactInfo;
import fi.om.municipalityinitiative.exceptions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={IntegrationTestFakeEmailConfiguration.class})
public class YouthInitiativeServiceTest {

    @Resource
    private YouthInitiativeService youthInitiativeService;

    @Resource
    private TestHelper testHelper;
    private Long unactiveMunicipality;
    private Long activeMunicipality;

    @Before
    public void setup() {
        unactiveMunicipality = testHelper.createTestMunicipality(randomAlphabetic(10), false);
        activeMunicipality = testHelper.createTestMunicipality(randomAlphabetic(10), true);

    }

    @Test(expected = AccessDeniedException.class)
    public void rejectsIfMunicipalityNotActive() {

        YouthInitiativeCreateDto editDto = new YouthInitiativeCreateDto();
        editDto.setMunicipality(unactiveMunicipality);
        youthInitiativeService.prepareYouthInitiative(editDto);
    }

    @Test
    public void youthInitiativeIsCreated() {
        YouthInitiativeCreateDto editDto = new YouthInitiativeCreateDto();

        editDto.setMunicipality(activeMunicipality);

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setName("testinimi");
        contactInfo.setAddress("testiosoite");
        contactInfo.setEmail("testiemail");
        contactInfo.setPhone("1234567");
        contactInfo.setShowName(true);

        editDto.setContactInfo(contactInfo);
        editDto.setYouthInitiativeId(-1L);
        editDto.setName("testialoite");
        editDto.setProposal("sisältö");
        editDto.setExtraInfo("lisätiedot");

        Long initiativeId = youthInitiativeService.prepareYouthInitiative(editDto);

        Initiative createdInitiative = testHelper.getInitiative(initiativeId);

        assertThat(createdInitiative.getName(), is(editDto.getName()));
        assertThat(createdInitiative.getProposal(), is(editDto.getProposal()));
        assertThat(createdInitiative.getExtraInfo(), is(editDto.getExtraInfo()));
        assertThat(createdInitiative.getMunicipality().getId(), is(editDto.getMunicipality()));

    }


}