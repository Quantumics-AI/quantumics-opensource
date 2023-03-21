package com.beyondx.vteaf.Runner;

import java.io.FileReader;
import java.util.Collection;
import java.util.Properties;

import com.beyondx.vteaf.Hooks.Hooks;
import com.beyondx.vteaf.stepDefinition.Utilities;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.beyondx.vteaf.stepDefinition.StepBase;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@CucumberOptions(dryRun = false,
		plugin = { "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",
		"pretty",
		"json:src/test/java/com/TestResults/cucumber-report/cucumber_1.json" }, strict = true, junit = "--step-notifications", features = {
				"src/test/java/com/beyondx/vteaf/features" }, glue = {"com.beyondx.vteaf.Hooks","com.beyondx.vteaf.stepDefinition"},
			tags = { "@DeploymentTest", "not @Ignore" }, monochrome = true)

@RunWith(Cucumber.class)
public class Test_Runner {
	public static String URL;
	public static String BROWSER;
	public static String PLATFORM;
	public static Logger log = LogManager.getLogger(Test_Runner.class);
	public static String Environment = "QA";
	public static String appurl=Environment.toLowerCase()+".url.login";
	public static String TEST_MODE="Web";
	public static String RestUrl;

	@BeforeClass
	public static void setup() throws Exception {
		FileReader reader = new FileReader("src/test/java/com/beyondx/vteaf/config/config.properties");
		PropertyConfigurator.configure("src/test/java/com/beyondx/vteaf/config/log4j2.properties");
		BasicConfigurator.configure();
		Properties p = new Properties();
		p.load(reader);
//		TEST_MODE=System.setProperty("TEST_MODE",TEST_MODE);
//		Environment =System.setProperty("Environment",Environment);
		if (TEST_MODE.equalsIgnoreCase("Web")){
			URL = p.getProperty(appurl);
			BROWSER = p.getProperty("browser");
			PLATFORM = p.getProperty("platform");
			System.out.println("URL: " + URL);
			System.out.println("Environment: "+Environment.toUpperCase());
			System.out.println("Browser Name: " + BROWSER);
			System.out.println("Platform: " + PLATFORM);
			StepBase.SetUp(URL, BROWSER, PLATFORM);
		}else if (TEST_MODE.equalsIgnoreCase("Rest")){
					if (Environment.equalsIgnoreCase("QA")){
						RestUrl=p.getProperty("baseURI");
					}else if(Environment.equalsIgnoreCase("Demo")){
						RestUrl=p.getProperty("DemobaseURI");
					}else if(Environment.equalsIgnoreCase("Prod")){
						RestUrl=p.getProperty("ProdbaseURI");
					}
			System.out.println(RestUrl);
		}
		else {
			System.err.println("Provide a valid Test Mode");
		}

	}

	@AfterClass
	public static void tearDown() {
		Collection<String> values = Hooks.scenarioStatus.values();
		String toMailId = null;
		String ccMailId = null;
		boolean flag = false;
		if ((values.contains("FAILED") || values.contains("SKIPPED"))
				&& !(Hooks.code == 520 || Hooks.code == 521)) {
			flag = true;
		}

		System.out.println("Failure Present: " + flag);
		if (flag == true) {
//			toMailId ="ilangovan.muniyandi@quantumics.ai,sridevi.manivannan@quantumics.ai,ganesh.kumar_testleaf@quantumics.ai";
			toMailId = "vishal.vaitheeswaran@quantumics.ai,ilangovan.muniyandi@quantumics.ai,sridevi.manivannan@quantumics.ai,mansoor.pasha@quantumics.ai";
			ccMailId = "";
		} else {
			// Mailing everyone as results look good
//			toMailId ="vishal.vaitheeswaran@quantumics.ai,ilangovan.muniyandi@quantumics.ai,sridevi.manivannan@quantumics.ai,ganesh.kumar_testleaf@quantumics.ai";
			toMailId = "vishal.vaitheeswaran@quantumics.ai,ilangovan.muniyandi@quantumics.ai,sridevi.manivannan@quantumics.ai,mansoor.pasha@quantumics.ai";
			ccMailId = "";
		}
		StepBase.tearDown();
		StepBase.GenerateCucumberReport();
		Utilities.MailSend(toMailId, ccMailId);
		Utilities.deleteZipFiles("TestExecution_Report");
	}


}
