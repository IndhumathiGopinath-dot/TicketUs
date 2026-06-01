package com.ticketsystem.qa.ui.tests;

import com.ticketsystem.qa.ui.pages.CreateTicketPage;
import com.ticketsystem.qa.ui.pages.LoginPage;
import com.ticketsystem.qa.ui.pages.TicketDetailPage;
import com.ticketsystem.qa.utils.ConfigReader;
import com.ticketsystem.qa.utils.ExcelReader;
import com.ticketsystem.qa.utils.ResultRecorder;
import com.ticketsystem.qa.utils.TestResult;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class UiTicketCreationTest extends UiBaseTest {

    private static final String SHEET = "UI-TicketCreation";

    @Override
    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        super.setUp();
        new LoginPage(driver).open(baseUrl)
                .login(ConfigReader.get("employee.email"), ConfigReader.get("employee.password"));
            new com.ticketsystem.qa.ui.pages.DashboardPage(driver).isLoaded();
            }

    @DataProvider(name = "ticketData")
    public Object[][] ticketData() {
        return ExcelReader.readSheet("testdata/ticket_data.xlsx", "TicketCases");
    }

    @Test(dataProvider = "ticketData", description = "UI ticket creation across categories")
    public void createTicketViaUi(Map<String, String> row) {
        String testId           = row.get("testId");
        String desc             = row.get("description");
        String category         = row.get("category");
        String title            = row.get("title");
        String tDesc            = row.get("ticketDescription");
        String expectedPriority = row.get("expectedPriority");

        TestResult tr = TestResult.of(testId, desc)
                .input("category=" + category + ", title='" + title + "'");

        try {
            CreateTicketPage create = new CreateTicketPage(driver).open(baseUrl)
                    .selectCategory(category)
                    .enterTitle(title)
                    .enterDescription(tDesc);

            switch (category.toUpperCase()) {
                case "IT"  -> create.enterAssetTag(row.get("assetTag"));
                case "BUG" -> create.enterSeverity(row.get("severity")).enterAppVersion(row.get("appVersion"));
                case "HR"  -> {
                    create.enterRequestType(row.get("requestType"));
                    if (Boolean.parseBoolean(row.get("confidential"))) create.checkConfidential();
                }
            }
            create.submit();

            TicketDetailPage detail = new TicketDetailPage(driver);
            boolean loaded = detail.isLoaded();
            boolean titleOK = loaded && detail.getTitle().equals(title);
            boolean openOK  = loaded && detail.hasBadge("OPEN");
            boolean prioOK  = expectedPriority.isBlank() || (loaded && detail.hasBadge(expectedPriority));

            boolean passed = titleOK && openOK && prioOK;

            tr.expected("Detail page with title='" + title + "', badges include OPEN" +
                        (expectedPriority.isBlank() ? "" : " + " + expectedPriority))
              .actual(loaded
                      ? "Title='" + detail.getTitle() + "', badges=" + detail.badges()
                      : "Did not reach detail page")
              .observed(loaded ? detail.observed() : create.observed())
              .pass(passed);
            ResultRecorder.record(SHEET, tr);
            Assert.assertTrue(passed, testId + " — verification failed");
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw t;
        }
    }
}
