package fi.om.municipalityinitiative.dao;

import fi.om.municipalityinitiative.conf.IntegrationTestConfiguration;
import fi.om.municipalityinitiative.newdao.MunicipalityDao;
import fi.om.municipalityinitiative.newdto.service.Municipality;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={IntegrationTestConfiguration.class})
public class JdbcMunicipalityDaoTest {

    @Resource
    MunicipalityDao municipalityDao;

    @Resource
    TestHelper testHelper;

    @Before
    public void setup() {
        testHelper.dbCleanup();
    }

    @Test
    public void find_all_municipalities() {
        testHelper.createTestMunicipality("some test municipality");
        List<Municipality> result = municipalityDao.findMunicipalities(true);
        assertThat(result, is(not(empty())));
        assertThat(result.get(0).getFinnishName(), is("some test municipality"));
        assertThat(result.get(0).getSwedishName(), is("some test municipality sv"));
    }

    @Test
    public void municipalities_are_ordered_by_name() {
        List<Municipality> result = municipalityDao.findMunicipalities(true);

        String last = null;
        for (Municipality municipalityInfo : result) {
            if (municipalityInfo.getFinnishName().toLowerCase().contains("å")) {
                continue; // Posgtre seems to think that ä is before å at the alphabets
            }

            if (last != null) {
                assertThat(municipalityInfo.getFinnishName(), is(greaterThan(last)));
            }
            last = municipalityInfo.getFinnishName();
        }
    }

    @Test
    public void get_municipality_email() {
        Long municipalityId = testHelper.createTestMunicipality("tuusula");

        String email = municipalityDao.getMunicipalityEmail(municipalityId);
        assertThat(email, is("tuusula@example.com"));
    }

    @Test
    public void get_municipality() {
        Long municipalityId = testHelper.createTestMunicipality("Tuusula");

        Municipality municipality = municipalityDao.getMunicipality(municipalityId);

        assertThat(municipality.getFinnishName(), is("Tuusula"));
        assertThat(municipality.getSwedishName(), is("Tuusula sv"));
        assertThat(municipality.getId(), is(municipalityId));
    }
}
