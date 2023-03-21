package com.beyondx.vteaf.stepDefinition;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.When;
import cucumber.runtime.CucumberException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class StepDefinition {
	
	static WebDriver driver = StepBase.getDriver();
	static int IMPLICIT_WAIT = 10;
	public static Scenario scenario;
	public static Logger log = LogManager.getLogger(StepDefinition.class);
	
	@Before
	 public void Before(Scenario scenario) {
		 this.scenario = scenario;
	 }
	
	public static void highLightElement(WebElement element) throws Exception {
        try {
                JavascriptExecutor js = (JavascriptExecutor)driver;

                for(int i = 0; i < 2; ++i) {
                    js.executeScript("arguments[0].setAttribute('style', 'background: green; border: 3px solid green;');", new Object[]{element});
                    Thread.sleep(50L);
                    js.executeScript("arguments[0].setAttribute('style', arguments[1]);", new Object[]{element, ""});
                    Thread.sleep(50L);
                    js.executeScript("arguments[0].setAttribute('style', 'background: green; border: 3px solid green;');", new Object[]{element});
                    Thread.sleep(50L);
                    js.executeScript("arguments[0].setAttribute('style', arguments[1]);", new Object[]{element, ""});
                    Thread.sleep(50L);
                }
        } catch (Exception var3) {
            System.out.println(var3);
        }

    }
	
	@Then("I enter '(.*)' in '(.*)' field")
	public static void i_enter_value_in_field(String value, String location) {
		driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT, TimeUnit.SECONDS);
		WebElement Field = driver.findElement(GetPageObjectRead.OR_GetLocators(location));
		Random rand = new Random();
		int rand_int1 = rand.nextInt(100000);
		try {
			highLightElement(Field);
			System.out.println("Successfully found element "+location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(value.startsWith("rand")){
			value=value.replace("rand","");
			value = value+rand_int1;
			Field.sendKeys(value);
		}
		else if(value.substring(0, 2).equals("$$")){
			value = HashMapContainer.get(value.replace("$$"," ").trim());
			System.out.println(value);
			Field.sendKeys(value);
		}
		else {
			Field.sendKeys(value);
		}
	}

	@Then("I enter '(.*)' in '(.*)' field and store at '(.*)'")
	public static void i_enter_value_in_field_and_store(String value, String location,String store){
		WebElement Field = driver.findElement(GetPageObjectRead.OR_GetLocators(location));
		Random rand = new Random();
		int rand_int1 = rand.nextInt(100000);
		if(value.startsWith("rand")){
			value=value.replace("rand","");
			value = value+rand_int1;
		}
		HashMapContainer.add(store,value);
		i_enter_value_in_field(value,location);
	}

	@Then("I switch to iframe '(.*)'")
	public static void i_switch_to_iframe(String id){
		i_pause_for(5);
		driver.switchTo().frame(id);
		System.out.println("Switched to iframe with "+id);
	}

	@Then("I click '(.*)' by js")
	public static void clickByJS(String location) {
		try {
			WebElement element = driver.findElement(GetPageObjectRead.OR_GetLocators(location));
			JavascriptExecutor executor = (JavascriptExecutor) StepBase.getDriver();
			executor.executeScript("arguments[0].click();", element);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Then("I click '(.*)' by js1")
	public static void clickByJS1(String location) {
		try {
			WebElement element = driver.findElement(GetPageObjectRead.OR_GetLocators(location));
			JavascriptExecutor executor = (JavascriptExecutor) StepBase.getDriver();
			executor.executeScript("arguments[1].click();", element);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Then("I enter '(.*)' in '(.*)' by js")
	public static void typeByJS(String value, String location) {
		try {
			WebElement element = driver.findElement(GetPageObjectRead.OR_GetLocators(location));
			JavascriptExecutor executor = (JavascriptExecutor) StepBase.getDriver();
			executor.executeScript("arguments[0]..value='"+value+"';", element);
		} catch (Exception e) {
			log.error(e);
		}
	}
	@Given("^I scroll till element '(.*)'$")
	public static void i_should_scroll_till_the_element(String locator) throws Exception {
		WebElement element = driver
				.findElement(GetPageObjectRead.OR_GetLocators(locator));
		((JavascriptExecutor) driver)
				.executeScript("arguments[0].scrollIntoView(true);", element);
		Thread.sleep(500);
	}
	
	
	@Then("I click '(.*)'")
	public static void i_click(String location) {
		driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT, TimeUnit.SECONDS);
		WebElement Field = driver.findElement(GetPageObjectRead.OR_GetLocators(location));
		try {
			highLightElement(Field);
			System.out.println("Successfully found element "+location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Field.click();
	}
	
	@Then("I click '(.*)' by actions")
	public static void i_click_by_actions(String location) {
		Actions builder = new Actions(driver);
		 WebElement link_Home = driver.findElement(GetPageObjectRead.OR_GetLocators(location));
		System.out.println("Successfully found element "+location);
        Action clickAction = builder.moveToElement(link_Home).click(link_Home).build();
		clickAction.perform();
	}
	
	@Then("I should see '(.*)' present in page")
	public void i_should_see_element(String location) {
		WebElement element = driver.findElement(GetPageObjectRead.OR_GetLocators(location));
		element.isDisplayed();
		try {
			highLightElement(element);
			System.out.println("Successfully found element "+location);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Then("I wait for '(.*)' sec")
	public static void i_pause_for(int sec) {
		int secs =sec*1000; 
		try {
			Thread.sleep(secs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Then("I select '(.*)' in '(.*)' by text")
	public void i_select_by_value_text(String value,String locator) {
		Select select = new Select(driver.findElement(GetPageObjectRead.OR_GetLocators(locator)));
		System.out.println("Successfully found element "+locator);
		select.selectByVisibleText(value);
	}
	
	@Then("I select '(.*)' in '(.*)' by value")
	public void i_select_by_value_value(String value,String locator) {
		Select select = new Select(driver.findElement(GetPageObjectRead.OR_GetLocators(locator)));
		System.out.println("Successfully found element "+locator);
		select.selectByValue(value);
	}

	@Then("I upload the file '(.*)'")
	public static void i_upload_file(String fileName ){
		String property = System.getProperty("user.dir");
		String path = property+"//src//test//java//com//beyondx//vteaf//resources//Data//"+fileName+".csv";
		driver.findElement(By.xpath("//input[@type='file']")).sendKeys(path);
	}

	
	@Given("I check the page title is '(.*)'")
	public void i_get_page_title(String value) {
		String pageTitle =driver.getTitle();
		Assert.assertEquals(value, pageTitle);
		System.out.println("Successfully Found the element");
	}
	
	@Then("I enter random email address in field '(.*)'")
	public static void i_enter_random_email_address(String locator) {
		driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT, TimeUnit.SECONDS);
		WebElement Field = driver.findElement(GetPageObjectRead.OR_GetLocators(locator));
		try {
			highLightElement(Field);
			System.out.println("Successfully found element "+locator);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Random rand = new Random(); 
        int rand_int1 = rand.nextInt(100000); 
        String mailId = "test"+rand_int1+"@testing.com";
        HashMapContainer.add("randmail",mailId);
        scenario.write("Entered Mail Id is: "+mailId);
		Field.sendKeys(mailId);
	}

	@Then("^I mouse over '(.*)'$")
	public static void I_mouse_over(String element) {
		try {
			Actions action = new Actions(StepBase.getDriver());
			WebElement WElement = driver.findElement(GetPageObjectRead.OR_GetLocators(element));
			highLightElement(WElement);
			action.moveToElement(WElement).build().perform();
			System.out.println("Successfully mouseover the "+element);
		} catch (Exception e) {

			throw new CucumberException(e);
		}
	}

	@When("^I press key: enter$")
	public static void i_press_enter_key_() throws Exception {
		try {
			Actions ac = new Actions(StepBase.getDriver());
			ac.sendKeys(Keys.ENTER).build().perform();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();

		}
	}
	
}
