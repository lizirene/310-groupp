package cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Run all the cucumber tests in the current package.
 */
@RunWith(Cucumber.class)
@CucumberOptions(strict = true, features = {
	"src/test/resources/cucumber/login.feature",
	"src/test/resources/cucumber/register.feature",
	"src/test/resources/cucumber/portfolio.feature",
	"src/test/resources/cucumber/add_portfolio_stock.feature",
	"src/test/resources/cucumber/view_historical_stock.feature",
	"src/test/resources/cucumber/select_stock.feature",
	"src/test/resources/cucumber/upload_csv.feature",
	"src/test/resources/cucumber/remove_stock.feature",
	"src/test/resources/cucumber/security.feature",
	"src/test/resources/cucumber/visualize_stockdata.feature"
})
public class RunCucumberTests {
	@BeforeClass
	public static void setup() {
		WebDriverManager.chromedriver().setup();
	}

}
