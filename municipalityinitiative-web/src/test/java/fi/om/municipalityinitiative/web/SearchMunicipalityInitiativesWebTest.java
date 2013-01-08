package fi.om.municipalityinitiative.web;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class SearchMunicipalityInitiativesWebTest extends NEWWebTestBase {

    @Test
    public void page_opens_when_navigation_link_clicked() {
        open(urls.search());
        assertThat(pageTitle(), is("Selaa kuntalaisaloitteita - Kuntalaisaloitepalvelu"));
    }

    @Test
    @Ignore
    // XXX: This is just an example test case.
    public void helsinki_is_listed_as_one_municipality() {
        newTestHelper.createTestMunicipality("Tuusula");
        open(urls.search());

        WebElement municipalities = driver.findElement(By.className("municipalities"));
        assertThat(municipalities.getText(), containsString("Tuusula"));


    }
}
