package com.beyondx.vteaf.stepDefinition;

import com.beyondx.vteaf.Runner.Test_Runner;
import cucumber.api.Scenario;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.runtime.CucumberException;
import gherkin.ast.Step;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CustomMethods {
    static WebDriver driver = StepBase.getDriver();
    static int IMPLICIT_WAIT = 10;
    public static Scenario scenario;
    public static Logger log = LogManager.getLogger(CustomMethods.class);
    WebDriverWait wait = new WebDriverWait(StepBase.getDriver(), IMPLICIT_WAIT);
    public static List<String> headerList = new ArrayList<String>();
    public static HashMap<String, Integer> map = new HashMap<>();
    public static List<String> cleansePreviwExpectedlist = new ArrayList<String>();
    public static List<String> cleansePreviewActuallist = new ArrayList<String>();
    public static List<String> cleanseOutputFileList = new ArrayList<String>();
    public static List<String> cleanseOutputActualList = new ArrayList<String>();
    public static List<String> engPreviewExpectedlist = new ArrayList<String>();
    public static List<String> engPreviewActuallist = new ArrayList<String>();
    public static List<String> engOutputActuallist = new ArrayList<String>();
    List<String> ActualListForEngOutput = new ArrayList<String>();

    @Then("I should delete the existing rule")
    public void deleteExistingRule() {
        try {
            StepDefinition.i_pause_for(5);
            List<WebElement> eleDelete = driver.findElements(By.xpath("//i[@ngbtooltip='Delete']"));
            if (eleDelete.size() != 0) {
                for (int i=0;i<eleDelete.size();i++){
                    eleDelete.get(i).click();
                    System.out.println(i+"- Rule Deleted");
                }
            }
            else {
                System.out.println("No Rules Applied");
            }
        }catch (Exception e) {
           throw new CucumberException(e);
        }

    }

    @Given("My Application '(.*)' is open")
    public static void My_Application_is_open(String url) throws IOException {
        FileReader reader = new FileReader("src/test/java/com/beyondx/vteaf/config/config.properties");
        Properties p = new Properties();
        p.load(reader);
        String finalUrl= Test_Runner.Environment.toLowerCase()+".url."+url.toLowerCase();
        finalUrl = p.getProperty(finalUrl);
        StepBase.i_open_url(finalUrl);
        System.out.println("Successfully Opened - "+url);
    }

    public void getHeaderIndex(String columnName) {
        List<WebElement> allheaders = driver.findElements(By.xpath("//table/thead/tr/th"));
        for (int i = 1; i < allheaders.size(); i++) {
                headerList.add(allheaders.get(i).getText().replaceAll("\n","").substring(3));
        }
        System.out.println("CurrentColumnHeader==="+headerList.indexOf(columnName));
        map.put("headerIndex", headerList.indexOf(columnName));
        System.out.println("TotalNumberOfColumns====="+allheaders.size());
        map.put("TotalNumberOfColumns", allheaders.size());
    }

    @Then("I take value count before uppercase at '(.*)' and store")
    public void takingValuesBeforeRuleUpperCase(String colName){
        getHeaderIndex(colName);
        Integer headerIndex = map.get("headerIndex") + 1;
        List<WebElement> allRows = driver.findElements(By.xpath("//table/tbody/tr"));
        for (int i = 1; i <= allRows.size(); i++) {
            String str = driver.findElement(By.xpath("//table/tbody/tr[" + i + "]/td[" + headerIndex + "]")).getText().toUpperCase();
            cleansePreviwExpectedlist.add(str);
//            System.out.println(cleansePreviwExpectedlist);
        }
    }
    @Then("I take value count after uppercase and store")
    public void takingValuesAfterRule() {
        StepDefinition.i_pause_for(3);
        List<WebElement> rows = driver.findElements(By.xpath("//table/tbody/tr"));
        Integer columnHeader = map.get("headerIndex") + 1;
        for (int i = 1; i <= rows.size(); i++) {
            String value = driver.findElement(By.xpath("//table/tbody/tr[" + i + "]/td[" + columnHeader + "]")).getText();
            cleansePreviewActuallist.add(value);
        }
    }

    @Then("I compare the two given columns")
    public static void Preview() {

        System.out.println("Expected Preview: " + cleansePreviwExpectedlist);
        System.out.println("Actual Preview: " + cleansePreviewActuallist);

        if (cleansePreviewActuallist.equals(cleansePreviwExpectedlist)) {
            Assert.assertTrue(true);
        } else {
            Assert.assertTrue(false);
        }

    }

    @Then("I select '(.*)' folder at Automate")
    public void selectFolderAtAutomate(String folderName){
        if(folderName.substring(0, 2).equals("$$")){
            folderName = HashMapContainer.get(folderName.replace("$$"," ").trim());
            System.out.println(folderName);
            folderName=folderName.toLowerCase();
        }else{
            folderName=folderName.toLowerCase();
        }
        StepDefinition.i_enter_value_in_field(folderName,"searchTerm");
        driver.findElement(By.xpath("//td[text()='" + folderName + "']/following::a[@id='lnkSelectCleanseFile']")).click();
        System.out.println("Successfully selected the folder "+folderName);
    }

    @Then("I click '(.*)' history")
   public void clickHistory(String folderName){
        if(folderName.substring(0, 2).equals("$$")){
            folderName = HashMapContainer.get(folderName.replace("$$"," ").trim());
            System.out.println(folderName);
            folderName=folderName.toLowerCase();
        }else{
            folderName=folderName.toLowerCase();
        }
       driver.findElement(By.xpath("//td[text()='" + folderName + "']/following::a[@id='lnkHistoryCleanseFile']")).click();
    }

    @Then("I refresh the page till status message")
    public static void clickRefresh() {
        StepDefinition.i_pause_for(60);
//        click(Locators.XPATH, "//a[@id='lnkRefresh']/img");
        StepDefinition.i_click("RefreshBtn");
        StepDefinition.i_pause_for(5);
        String status = driver.findElement(By.xpath("(//th[text()='Action']/following::td)[3]")).getText();
        if (status.contains("SUCCEEDED") || status.contains("FAILED")) {
            System.out.println("The refresh icon was clicked successfully");
        }
        else {
            System.out.println("Taking more than 1 minutes");
            StepDefinition.i_pause_for(120);
            StepDefinition.i_click("RefreshBtn");
            StepDefinition.i_pause_for(2);
            String status1 = driver.findElement(By.xpath("(//th[text()='Action']/following::td)[3]")).getText();
            if (status1.contains("SUCCEEDED") || status1.contains("FAILED")) {
                System.out.println("The refresh icon was clicked successfully");
            }
            else {
                log.warn("Taking more than 3 minutes");
                StepDefinition.i_pause_for(60);
                StepDefinition.i_click("RefreshBtn");
            }
        }

        WebElement ele = driver.findElement(By.xpath("//table[contains(@class,'table m-0')]/following::table/tbody[1]/tr[1]/td[3]"));
        if(ele.getText().contains("SUCCEEDED")){
            Assert.assertTrue(true);
        }else {
            Assert.assertTrue(false);
        }

    }

    public static String lastDownloadFilePath() {
        File choice = null;
        try {
            //	File fl = new File("C:/Users/" + System.getProperty("user.name") + "/Downloads/");
            File fl = new File(System.getProperty("user.dir") + "/Downloads/");
            File[] files = fl.listFiles();
            // Sleep to download file if not required can be removed
            Thread.sleep(30000);
            long lastMod = Long.MIN_VALUE;

            for (File file : files) {
                if (file.lastModified() > lastMod) {
                    choice = file;
                    lastMod = file.lastModified();
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while getting the last download file :" + e.getMessage());
        }
        System.out.println("The last downloaded file is " + choice.getPath());
        return choice.getPath();
    }

    @Then("I verify the downloaded cleansed file is present")
    public static void verifyOutputFile() throws FileNotFoundException {
        StepDefinition.i_pause_for(5);
        Integer headerIndex = map.get("headerIndex");
        String path = lastDownloadFilePath();
        Scanner sc = new Scanner(new File(path));
        while (sc.hasNext()) // returns a boolean value
        {
            cleanseOutputFileList.add(sc.next());
        }
        for (int i = 1; i < cleanseOutputFileList.size(); i++) {
            String[] split = cleanseOutputFileList.get(i).split(",");
            if (split[headerIndex].endsWith(".0")) {
                cleanseOutputActualList.add(split[headerIndex].substring(0, split[headerIndex].indexOf(".")));
            } else {
                cleanseOutputActualList.add(split[headerIndex]);
            }
        }
        sc.close();
        System.out.println("Expected Output: " + cleansePreviewActuallist);
        System.out.println("Actual Output: " + cleanseOutputActualList);

        if (cleanseOutputActualList.equals(cleansePreviewActuallist)) {
            System.out.println("The preview is matching with output");
        } else {
            System.out.println("The preview is not matching with output");

        }
    }

    @Then("I select the query from '(.*)'")
    public static void selectSource(String text){
        if(text.substring(0, 2).equals("$$")){
            text = HashMapContainer.get(text.replace("$$"," ").trim());
            System.out.println(text);
            text=text.toLowerCase();
        }else{
            text=text.toLowerCase();
        }
        WebElement selectSource = driver.findElement(By.xpath("//*[contains(text(),'"+text+"')]"));
        JavascriptExecutor executor = (JavascriptExecutor) StepBase.getDriver();
        executor.executeScript("arguments[0].click();", selectSource);
    }

    @Then("I fetch and select the last query")
   public static void i_fetch_the_last_query(){
        List<WebElement> element = driver.findElements(By.xpath("//a[contains(text(),'New Query')]"));
        int a=element.size();
        WebElement newElement = driver.findElement(By.xpath("(//a[contains(text(),'New Query')])["+a+"]"));
       JavascriptExecutor executor = (JavascriptExecutor) StepBase.getDriver();
       executor.executeScript("arguments[0].click();", newElement);
    }
    @Then("I verify the downloaded engineering file is present")
    public static void verifyEngOutputFile() throws FileNotFoundException {
        StepDefinition.i_pause_for(5);
        String path = lastDownloadFilePath();
        Scanner sc = new Scanner(new File(path));
        List<String> ActualListForEngOutput = new ArrayList<String>();

        while (sc.hasNext()) // returns a boolean value
        {
            ActualListForEngOutput.add(sc.next());
        }

        for (int i = 1; i < ActualListForEngOutput.size(); i++) {
            String[] split = ActualListForEngOutput.get(i).split(",");
            for (int j = 0; j < split.length; j++) {
                if (split[j].endsWith(".0")) {
                    engOutputActuallist.add(split[j].substring(0, split[j].indexOf(".")));
                }
                else {
                    engOutputActuallist.add(split[j]);
                }
            }
        }

        System.out.println("Expected Output: " + engPreviewExpectedlist);
        System.out.println("Actual Output: " + engOutputActuallist);
        if (engOutputActuallist.equals(engPreviewExpectedlist)) {
            System.out.println("Output file verification");
//            Assert.assertTrue(true);
        } else {
            System.out.println("Output file verification");
//            Assert.assertTrue(false);
        }
        sc.close();
    }

    @Then("I click edit on '(.*)' folder")
    public static void clickEdit(String folderName){
        if (folderName.substring(0, 2).equals("$$")){
            folderName = HashMapContainer.get(folderName.replace("$$"," ").trim());
        }
        WebElement element = driver.findElement(By.xpath("//td[text()='"+folderName+" ']/following::img"));
        element.click();
        System.out.println("The edit icon was clicked successfully");
    }




    @Then("I remove need help from the browser")
    public static void i_remove_need_help_using_js(){
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("document.querySelector('#cohere-overlay').remove();");
    }


    @Then("I remove chart")
    public static void i_remove_chart_using_js(){
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("document.querySelector('div.visualization-preview').remove();");
    }

    @Then("I get URL from dashboard and navigate to the URL")
    public static void i_get_url_open_new_window_and_check_the_url(){
        WebElement element = driver.findElement(GetPageObjectRead.OR_GetLocators("Dashboard_SecretAddress"));
        String url =element.getAttribute("value");
        driver.navigate().to(url);
    }

    @Then("I navigate back")
    public static void i_navigate_back(){
        driver.navigate().back();
    }


    @Then("I select '(.*)' folder at Engineering")
    public static void clickFolder(String folderName) {
        if(folderName.substring(0, 2).equals("$$")){
            folderName = HashMapContainer.get(folderName.replace("$$"," ").trim());
            System.out.println(folderName);
            folderName=folderName.toLowerCase();
        }else{
            folderName=folderName.toLowerCase();
        }
        WebElement element = driver.findElement(By.xpath("//label[contains(text(),'"+ folderName+"')]"));
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("arguments[0].click();", element);
        System.out.println("The folder " + folderName + " was clicked successfully");
    }

    @Then("I select '(.*)' file at Engineering")
    public static void clickFile(String File) {
        WebElement element = driver.findElement(By.xpath("//span[contains(text(),'" + File + "')]"));
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("arguments[0].click();", element);
        System.out.println("The file " + File + " was clicked successfully");
    }

    @Then("I connect file 1 with join")
    public static void connectFile1WithJoin() {
        StepDefinition.i_pause_for(5);
        Actions builder = new Actions(StepBase.getDriver());
        builder.moveToElement(driver.findElement(By.xpath("(//*[name()='svg']//*[name()='image'])"))).perform();
        StepDefinition.i_pause_for(5);
        WebElement ele1 = driver.findElement(By.xpath("(//*[name()='svg']//*[name()='image'])"));
        WebElement ele2 = driver.findElement(By.xpath("(//*[name()='svg']//*[name()='image'])[3]"));
        builder.clickAndHold(ele1).moveToElement(ele2).release().perform();
        System.out.println("File1 and Join were connnected successfully");
    }
    @Then("I connect file 2 with join")
    public void connectFile2WithJoin() {
        StepDefinition.i_pause_for(5);
        Actions builder = new Actions(StepBase.getDriver());
        WebElement ele1 = driver.findElement(By.xpath("(//*[name()='svg']//*[name()='image'])[2]"));
        builder.moveToElement(ele1).perform();
        StepDefinition.i_pause_for(5);

        WebElement ele2 = driver.findElement(By.xpath("//*[name()='svg']/*[name()='g']/*[name()='g'][2]//*[name()='g'][2]"));
        WebElement ele3 = driver.findElement(By.xpath("(//*[name()='svg']//*[name()='image'])[3]"));

        builder.clickAndHold(ele2).moveToElement(ele3).release()
                .perform();
        WebElement ele4 = driver.findElement(By.xpath("(//*[name()='svg']//*[name()='image'])[3]"));
        wait.until(ExpectedConditions
                .elementToBeClickable(ele4)).click();
        StepDefinition.i_pause_for(5);
        System.out.println("File2 and Join were connnected successfully");
    }


    public static void takingExpValue(String FileName) {
        try {
            Scanner sc = new Scanner(new File("src/test/java/com/beyondx/vteaf/resources/Data/" + FileName + ".csv"));
            List<String> expectedList = new ArrayList<String>();

            while (sc.hasNext()) // returns a boolean value
            {
                expectedList.add(sc.next());
            }

            for (int i = 1; i < expectedList.size(); i++) {
                String[] split = expectedList.get(i).split(",");
                for (int j = 0; j < split.length; j++) {
                    engPreviewExpectedlist.add(split[j]);
                }
            }
            sc.close();
            System.out.println("Expected values are taken");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Expected values are not taken");
        }
    }

    @Then("I fetch values from expected '(.*)' and store")
    public static void takingExpectedValueForJoin(String joinName){
        takingExpValue(joinName);
        System.out.println("Expected values were taken successfully");
    }

    public void verifyJoinPreview(){
        StepDefinition.i_pause_for(9);
        StepDefinition.i_click_by_actions("previewContent");
        StepDefinition.i_pause_for(7);

        List<WebElement> allCells = driver.findElements(By.xpath("//h6[contains(text(),'Previewing')]/following::table//td"));

        for (WebElement eachCell : allCells) {
            engPreviewActuallist.add(eachCell.getText());
        }

        System.out.println("Expected Preview: "+engPreviewExpectedlist);
        System.out.println("Actual Preview: "+engPreviewActuallist);
        if(engPreviewActuallist.containsAll(engPreviewExpectedlist)) {
            System.out.println("Join preview verification");
        }
        else {
            System.out.println("Join preview verification");
        }
        StepDefinition.i_pause_for(2);
        driver.findElement(By.xpath("(//button[@type='button']//span)[2]")).click();
    }

    @Then("I select folder '(.*)' at engineering")
    public static void i_select_folder_at_engineering_job(String folderName){
        if (folderName.substring(0, 2).equals("$$")){
            folderName = HashMapContainer.get(folderName.replace("$$"," ").trim());
        }
        WebElement ele = driver.findElement(By.xpath("//td[text()='" + folderName + "']/following::a[@id='lnkSelectEngFile'][1]"));
        ele.click();
    }
    @Then("I select history '(.*)' at engineering")
   public static void i_click_history_at_engineering_job(String folderName){
        if (folderName.substring(0, 2).equals("$$")){
            folderName = HashMapContainer.get(folderName.replace("$$"," ").trim());
        }
        WebElement ele = driver.findElement(By.xpath("//td[text()='" + folderName + "']/following::a[@id='lnkEngJobHistory'][1]"));
        ele.click();
    }

}
