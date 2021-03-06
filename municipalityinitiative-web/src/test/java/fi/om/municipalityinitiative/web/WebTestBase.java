package fi.om.municipalityinitiative.web;

import com.google.common.collect.Lists;
import fi.om.municipalityinitiative.StartJetty;
import fi.om.municipalityinitiative.conf.PropertyNames;
import fi.om.municipalityinitiative.conf.WebTestConfiguration;
import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.service.email.EmailSenderScheduler;
import fi.om.municipalityinitiative.util.Locales;
import fi.om.municipalityinitiative.util.TestUtil;
import fi.om.municipalityinitiative.validation.NotTooFastSubmitValidator;
import mockit.Mocked;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={WebTestConfiguration.class})
public abstract class WebTestBase {

    protected static final int PORT = 8090; // NOTE: must match port in test.properties/baseUrl

    @Mocked
    EmailSenderScheduler emailSenderScheduler;

    @Resource
    protected TestHelper testHelper;

    @Resource
    protected MessageSource messageSource;

    protected Urls urls;

    protected static WebDriver driver;

    @Inject
    protected Environment env;

    private static Server jettyServer;

    protected static final String VANTAA = "Vantaa";
    protected static Long VANTAA_ID;
    protected static final String HELSINKI = "Helsinki";
    protected static Long HELSINKI_ID;
    protected static final String HYVINKAA = "Hyvinkää";
    protected static Long HYVINKAA_ID;

    @BeforeClass
    public static synchronized void initialize() throws Throwable {
        if (jettyServer == null) {
            System.out.println("starting");

            new Thread(new Runnable() {

                @Override
                public void run() {
                    jettyServer = StartJetty.startService(PORT, "test,disableSecureCookie");
                    try {
                        jettyServer.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            ).start();

            System.out.println("started");
            try {
                while (jettyServer == null || !jettyServer.isStarted()) {
                    Thread.sleep(250);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Before
    public void init() {
        if (urls == null) {
            String baseUrl = env.getRequiredProperty(PropertyNames.baseURL);
            Urls.initUrls(baseUrl, baseUrl, baseUrl, "www.nua.fi", "superhaku.demo.solita.fi", false);
            urls = Urls.FI;
        }

        String driverType = env.getProperty("test.web-driver", "default");

        formatDriver(driverType);

        NotTooFastSubmitValidator.disable(); // Disable fast-submit validation at ui-tests
        if (HELSINKI_ID == null) {
            testHelper.dbCleanup();
            VANTAA_ID = testHelper.createTestMunicipality(VANTAA);
            HELSINKI_ID = testHelper.createTestMunicipality(HELSINKI);
            HYVINKAA_ID = testHelper.createTestMunicipality(HYVINKAA);
        }
        else {
            testHelper.dbCleanupAllButMunicipalities();
        }
        childSetup();
    }

    protected abstract void childSetup();

    protected final void overrideDriverToFirefox(boolean firefox) {
        formatDriver(firefox ? "ff" : "default");

    }

    private static String lastDriver;
    private void formatDriver(String driverType) {
        if (driverType.equals(lastDriver)) {
            return;
        }

        lastDriver = driverType;

        switch (driverType) {
            case "ie":
                driver = new InternetExplorerDriver();
                driver.get(urls.frontpage());
                driver.navigate().to("javascript:document.getElementById('overridelink').click()"); // to skip security certificate problem page
                break;
            case "ff":
                driver = new FirefoxDriver();
                break;
            case "default":
            default:
                driver = new HtmlUnitDriver(true);
                break;
        }

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS); // default is 0!!!
        driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS); // default is 0!!!
        driver.manage().window().setSize(new Dimension(1024, 768));
    }

    @After
    public void teardown() {
        //driver.quit();
        emailSenderScheduler.sendEmails();
        driver.manage().deleteAllCookies();
    }

    public static void destroyDriver() {
        if (driver != null) {
            driver.quit();
            lastDriver = null;
            try {
                jettyServer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //@AfterClass
    public static void destroy() {
        try {
            jettyServer.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helpers
    protected String getMessage(String code) {
        return getMessage(code, null);
    }

    protected String getMessage(String code, Object arg) {
        Object[] args = {arg};
        String text = messageSource.getMessage(code, args, Locales.LOCALE_FI);
        text = text.replace('\u00A0', ' '); //replace non breaking space with normal space, because it would be rendered to it
        text = text.trim();
        return text;
    }

    protected void open(String href) {
        driver.get(href);
    }

    protected void assertTextByTag(String tag, String text) {
        List<String> elementTexts = Lists.newArrayList();
        List<WebElement> elements = driver.findElements(By.tagName(tag));
        for (WebElement element : elements) {
            assertNotNull(element);
            if (text.equals(element.getText().trim())) {
                return;
            }
            elementTexts.add(element.getText().trim());
        }
        fail(tag + " tag with text " + text + " not found. Texts found: " + TestUtil.listValues(elementTexts) + " (Page title: " + driver.getTitle() + ")");
    }

    protected static void assertTextNotContainedByClass(String className, String text) {
        if (elementsContainText(driver.findElements(By.className(className)), text)) {
            fail("Should have NOT found text '" + text + "'with className: " + className + " - but text(s) found.");
        }
    }

    protected void assertSuccessMessage(String text) {
        assertTextContainedByClass("msg-success", text);
    }

    protected void assertWarningMessage(String text) {
        assertTextContainedByClass("msg-warning", text);
    }
    protected void assertInfoMessageContainsText(String text) {
        assertTextContainedByClass("msg-info", text);
    }

    protected static void assertTextContainedByClass(String className, String text) {
        List<String> elementTexts = Lists.newArrayList();
        List<WebElement> elements = driver.findElements(By.className(className));
        if (!elementsContainText(elements, text)) {
            for (WebElement element : elements) {
                elementTexts.add(element.getText().trim());
            }
            fail(className + " class with text " + text + " not found. Texts found: " + TestUtil.listValues(elementTexts) + " (Page title: "+driver.getTitle()+")");
        }
    }

    private static boolean elementsContainText(List<WebElement> elements, String text) {
        for (WebElement element : elements) {
            assertNotNull(element);
            String elementText = element.getText().trim();
            if (elementText.contains(text)) {
                return true;
            }
        }
        return false;
    }

    protected void assertTextContainedByXPath(String xpathExpression, String text) {

        List<String> elementTexts = Lists.newArrayList();
        List<WebElement> elements = driver.findElements(By.xpath(xpathExpression));
        for (WebElement element : elements) {
            assertNotNull(element);
            String elementText = element.getText().trim();
            elementTexts.add(elementText);
            if (elementText.contains(text)) {
                return;
            }
        }
        for (WebElement element : elements) {
        }
        fail(xpathExpression + " xpath with text " + text + " not found. Texts found: " + TestUtil.listValues(elementTexts) + " (Page title: "+driver.getTitle()+")");

    }

    protected String pageTitle() {
        return driver.getTitle();
    }
    protected void assertTitle(String text) {
        String title = driver.getTitle();

        assertThat(title, is(text));
    }

    protected void inputText(String fieldName, String text) {
        WebElement elementWhenClickable = findElementWhenClickable(By.name(fieldName));
        elementWhenClickable.clear();
        elementWhenClickable.sendKeys(text);
    }


    protected void clickInput() {
        WebElement e  = getElement(By.tagName("input"));
        e.click();
    }

    protected void clickElementByClass(String cssClass) {
        findElementWhenClickable(By.cssSelector("." + cssClass)).click();
    }

    protected void clickElementByXPath(String xPath) {
        By.xpath("//form[@id='myform']//input[@type='submit']");
        findElementWhenClickable(By.xpath(xPath)).click();
    }

    protected void clickElementByCSS(String css) {
        findElementWhenClickable(By.cssSelector(css)).click();
    }

    protected void inputTextByCSS(String css, String text) {
        findElementWhenClickable(By.cssSelector(css)).sendKeys(text);
    }

    protected void clickByName(String name) {
        findElementWhenClickable(By.name(name)).click();
    }

    protected void clickById(String id) {
        findElementWhenClickable(By.id(id)).click();
    }

    protected void clickLink(String text) {
        findElementWhenClickable(By.partialLinkText(text)).click();
    }

    protected void clickButton(String containing) {
        getElemContaining(containing, "button").click();
    }

    protected WebElement findElementWhenClickable(By by) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(by));
        } catch (TimeoutException e) {
            throw new TimeoutException(e.getMessage() + " on page with title: " + driver.getTitle(), e);
        }
    }

    protected WebElement getElemContaining(String containing, String tagName) {

        Optional<WebElement> OptionalElement = getOptionalElemContaining(containing, tagName);

        if (!OptionalElement.isPresent()) {
            throw new NullPointerException("Tag not found with text: " + containing);
        }

        return OptionalElement.get();
    }

    protected Optional<WebElement> getOptionalElemContaining(String containing, String tagName) {

        List<WebElement> htmlElements = driver.findElements(By.tagName(tagName));

        // wait.until(ExpectedConditions.elementToBeClickable(By.name(name)));

        for (WebElement e : htmlElements) {
            if (e.getText().contains(containing)) {
                return Optional.of(e);
            }
        }

        return Optional.empty();
    }


    protected void loginAsOmUser() {

        testHelper.createTestAdminUser("admin", "admin", "Oili Oikkonen");
        open(urls.moderatorLogin());
        inputText("u", "admin");
        inputText("p", "admin");
        clickByName("Login");
    }

    protected void loginAsAuthorForLastTestHelperCreatedNormalInitiative() {
        open(urls.loginAuthor(testHelper.getPreviousTestManagementHash()));
        clickByName("Login");
    }

    protected void logout() {
        open(urls.logout());
    }

    protected void assert404() {
        assertThat(getElement(By.tagName("h1")).getText(), is(getMessage("error.404.title")));
    }

    protected WebElement getElement(By by) {
        try {
            return driver.findElement(by);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("\nPage " + driver.getCurrentUrl() + "\nTitle: " + driver.getTitle()+"\nCause: " +e.getMessage(), e);
        }
    }

    protected boolean elementExists(By by) {
        return driver.findElements(by).size() != 0;
    }

    protected WebElement getElementByLabel(String labelText, String elementTag) {
        return getElement(By.xpath("//label[contains(normalize-space(text()), '" + labelText + "')]/following-sibling::"+elementTag));
    }

    protected void clickDialogButtonMsg(String localizationKey) {
        clickDialogButton(getMessage(localizationKey));
    }

    protected void clickDialogButton(String containing) {
        getElemContaining(containing, "span").click();
    }

    protected void vetumaLogin(String userSsn, String municipality) {
        open(urls.login());
        enterVetumaLoginInformationAndSubmit(userSsn, municipality);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    protected void enterVetumaLoginInformationAndSubmit(String userSsn, String municipalityName) {
        for (int i = 0; i < 50; ++i) {
            if (driver.getCurrentUrl().contains("vetumamock"))
                break;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        inputText("EXTRADATA", "HETU=" + userSsn);
        if (municipalityName == null) {
            municipalityName = "Ei kuntaa (Turvakielto)";
        }
        new Select(findElementWhenClickable(By.name("municipalityCode"))).selectByVisibleText(municipalityName);
        getElement(By.id("formsubmit")).click();
        getElement(By.id("returnsubmit")).click();
    }

    protected void assertPageHasValidationErrors() {
        assertTextContainedByClass("errors-summary", "Tietoja ei tallennettu. Syöttämissäsi tiedoissa oli seuraavia virheitä:");
    }

    protected void assertLoginLinkIsVisibleAtHeader() {
        assertTextContainedByClass("logged-in-info", "Tunnistaudu aloitteen ylläpitoon");
    }

    protected void assertTotalEmailsInQueue(int count) {
        assertThat(testHelper.findQueuedEmails(), hasSize(count));
    }

    protected void municipalitySelect(String municipality) throws InterruptedException {
        clickElementByXPath("//div[@id='municipality_chzn']//a");
        getElemContaining(municipality, "li").click();
        Thread.sleep(500);
    }

    protected void homeMunicipalitySelect(String municipality) throws InterruptedException {
        clickElementByXPath("//div[@id='homeMunicipality_chzn']//a");
        getElemContaining(municipality, "li").click();
        Thread.sleep(500);
    }

    protected void assertHomeMunicipality(int initiativeMunicipality, String confirmBtn, boolean checkConfirmBtn) throws InterruptedException {
        getElemContaining("Olen kunnan asukas", "label").click();

        assertConfirmBtnSelectable(confirmBtn, checkConfirmBtn);
        assertThat(getHomeMunicipality(), is(initiativeMunicipality));

        getElemContaining("Olen asukas toisessa kunnassa", "label").click();
        assertConfirmBtnDisabled(confirmBtn, checkConfirmBtn);
        getElemContaining("Olen kunnan asukas", "label").click();

        assertThat(getHomeMunicipality(), is(initiativeMunicipality));

        getElemContaining("Olen asukas toisessa kunnassa", "label").click();
        getElemContaining("Nimenkirjoitusoikeus", "label").click();

        assertThat(getHomeMunicipality(), is(-1));

        String anotherHomeMun = getAnotherHomeMun(initiativeMunicipality);
        int anotherHomeMunId = getMunicipalityIdByName(anotherHomeMun);

        homeMunicipalitySelect(anotherHomeMun); //Selects homeMunicipality other than initiative municipality

        assertConfirmBtnSelectable(confirmBtn, checkConfirmBtn);
        assertThat(getHomeMunicipality(), is(anotherHomeMunId));

        getElemContaining("Hallintaoikeus", "label").click();

        assertThat(getHomeMunicipality(), is(anotherHomeMunId));

        getElemContaining("Ei mitään näistä", "label").click();

        assertConfirmBtnDisabled(confirmBtn, checkConfirmBtn);
        assertThat(getHomeMunicipality(), is(-1));

        getElemContaining("Nimenkirjoitusoikeus", "label").click();

        assertThat(getHomeMunicipality(), is(-1));

        homeMunicipalitySelect(anotherHomeMun);

        assertThat(getHomeMunicipality(), is(anotherHomeMunId));

        getElemContaining("Olen kunnan asukas", "label").click();

        assertConfirmBtnSelectable(confirmBtn, checkConfirmBtn);
        assertThat(getHomeMunicipality(), is(initiativeMunicipality));
        getElemContaining("Olen asukas toisessa kunnassa", "label").click();
        ((JavascriptExecutor)driver).executeScript("document.getElementById('homeMunicipality').style.display = 'none';");
        ((JavascriptExecutor)driver).executeScript("document.getElementById('home-municipality-select').style.display = 'none';");
        ((JavascriptExecutor)driver).executeScript("var municipality = document.getElementById('municipality'); if(municipality) {municipality.style.display = 'none';}");
    }

    private void assertConfirmBtnDisabled(String confirmBtn, boolean checkConfirmBtn) {
        if (checkConfirmBtn) {
            assertThat(getElemContaining(confirmBtn, "button").isEnabled(), is(false));
        }
    }

    private void assertConfirmBtnSelectable(String confirmBtn, boolean checkConfirmBtn) {
        if (checkConfirmBtn) {
            assertThat(getElemContaining(confirmBtn, "button").isEnabled(), is(true));
        }
    }

    private int getMunicipalityIdByName(String name) {
        System.out.println("anotherHomeMun: " + name);
        if (name.equals("")) {
            return -1;
        }
        ((JavascriptExecutor)driver).executeScript("document.getElementById('homeMunicipality').style.display = 'block';");
        ((JavascriptExecutor)driver).executeScript("document.getElementById('home-municipality-select').style.display = 'block';");
        Select homeMunSelect = new Select(getElement(By.id("homeMunicipality")));
        int id = Integer.parseInt(homeMunSelect.getOptions().stream()
                .filter(option -> option.getText().equals(name))
                .findAny()
                .orElse(null)
                .getAttribute("value"));
        return id;
    }

    private String getAnotherHomeMun(int compare) {
        ((JavascriptExecutor)driver).executeScript("document.getElementById('homeMunicipality').style.display = 'block';");
        Select homeMunSelect = new Select(getElement(By.id("homeMunicipality")));
        String another = homeMunSelect.getOptions().stream()
                .filter((WebElement option) -> (option.getAttribute("value").equals("")) ? false : Integer.parseInt(option.getAttribute("value")) != compare)
                .findAny()
                .orElse(null)
                .getText();
        System.out.println("anotherHomeMun:" + another);
        return another;
    }

    private int getHomeMunicipality() {
        ((JavascriptExecutor)driver).executeScript("document.getElementById('homeMunicipality').style.display = 'block';");
        ((JavascriptExecutor)driver).executeScript("document.getElementById('home-municipality-select').style.display = 'block';");
        if (getElement(By.id("homeMunicipality")).isDisplayed()) {
            String homeMunString = getElement(By.id("homeMunicipality")).getAttribute("value");
            int homeMun = (homeMunString.equals("") ? -1 : Integer.parseInt(homeMunString));
            return homeMun;
        }
        return -1;
    }

    protected int getMunicipality() {
        ((JavascriptExecutor)driver).executeScript("document.getElementById('municipality').style.display = 'block';");
        if (getElement(By.id("municipality")).isDisplayed()) {
            int mun = Integer.parseInt(getElement(By.id("municipality")).getAttribute("value"));
            System.out.println("municipality: " + mun);
            return mun;

        }
        return -1;
    }
}
