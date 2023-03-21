package com.beyondx.vteaf.stepDefinition;


import cucumber.api.Scenario;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import net.masterthought.cucumber.Reportable;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class StepBase {
    public static WebDriver driver;
    public static Properties prop;
    public static String REPORT ="file://"+System.getProperty("user.dir")+"/src/test/java/com/TestResults/cucumber-output/cucumber-html-reports/overview-features.html";
    public static Logger log = LogManager.getLogger(StepDefinition.class);

    public static WebDriver getDriver() {
        return driver;
    }

    public static String getOS(){
        String os= System.getProperty("os.name");
        System.out.println(os);
        return os;
    }

    public static void SetUp(String Url,String browserName,String Platform) {
        String os = getOS();
        if(browserName.equalsIgnoreCase("chrome")&& Platform.equalsIgnoreCase("desktop")) {
//            System.setProperty("webdriver.chrome.driver", "src/test/java/com/beyondx/vteaf/resources/chromedriver");

            if(os.contains("Windows")){
                System.setProperty("webdriver.chrome.driver", "src/test/java/com/beyondx/vteaf/resources/drivers/chromedriver.exe");
            }else {
                System.setProperty("webdriver.chrome.driver", "src/test/java/com/beyondx/vteaf/resources/drivers/chromedriver");
            }
            ChromeOptions Options = new ChromeOptions();
   //         Options.addArguments("--kiosk");
            Options.setCapability("acceptSslCerts", true);
              Options.addArguments("headless");
              Options.addArguments("window-size=1920,1080");
            Options.addArguments("--disable-dev-shm-usage");
            Options.addArguments("--no-sandbox");
            Map<String, Object> prefs = new HashMap<String, Object>();
            prefs.put("download.default_directory",  System.getProperty("user.dir")+ File.separator + "Downloads");
            Options.setExperimentalOption("prefs", prefs);
            driver= new ChromeDriver(Options);
            driver.manage().window().maximize();
            i_open_url(Url);
        }
        if (browserName.equalsIgnoreCase("firefox")&& Platform.equalsIgnoreCase("desktop")){
            FirefoxBinary firefoxBinary = new FirefoxBinary();
         firefoxBinary.addCommandLineOptions("--headless");
            if(os.contains("Windows")){
                System.setProperty("webdriver.gecko.driver", "src/test/java/com/beyondx/vteaf/resources/drivers/geckodriver.exe");
            }else {
                System.setProperty("webdriver.gecko.driver", "src/test/java/com/beyondx/vteaf/resources/drivers/geckodriver");
            }
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            firefoxOptions.setBinary(firefoxBinary);
            driver= new FirefoxDriver(firefoxOptions);
            driver.manage().window().maximize();
            i_open_url(Url);
        }
        if(browserName.equalsIgnoreCase("NA") && Platform.equalsIgnoreCase("rest-service")) {
            System.out.println("Initiating Rest Service");
        }
    }

    public static void i_open_url(String url){
        driver.get(url);
    }

    public static void setScenario(Scenario cScenario) throws Exception {
        Scenario crScenario = cScenario;
    }



    public static void tearDown() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            driver.quit();
        }
        catch (Exception e) {
            // TODO: handle exception
            System.out.println("Driver Not Opened");
        }

    }


    public static void GenerateCucumberReport() {
        DateFormat dateFormat = new SimpleDateFormat("yy_MM_dd__HH_mm_ss");
        Date date = new Date();
        String timeStamp = dateFormat.format(date);
        String reportPath = "src/test/java/com/TestResults/execution_reports/test-output_"+timeStamp;
        File reportOutputDirectory = new File(reportPath);
        ArrayList jsonFiles = new ArrayList();
        jsonFiles.add("src/test/java/com/TestResults/cucumber-report/cucumber_1.json");
        String buildNumber = "1";
        String projectName = "cucumberProject";
        Configuration configuration = new Configuration(reportOutputDirectory, projectName);
        ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, configuration);
        Reportable result = reportBuilder.generateReports();
        Utilities.reportstoZipFile(reportPath,"TestExecution_Report");
        System.out.println("Cucumber Report Generated...");
    }

    public static Properties LoadConfigFile() throws Exception {
        prop = new Properties();
        FileInputStream fis = new FileInputStream("src/test/java/com/beyondx/vteaf/config/config.properties");
        prop.load(fis);
        return prop;

    }

    public static byte[] takeScreenshotByte(WebDriver driver) {
        try {
            byte[] scrFile = null;
                Thread.sleep(1000);
                scrFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.BYTES);
            return scrFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String takeScreenshot(WebDriver driver) {
        try {
            String SSPath = "";
                Thread.sleep(1000);
                File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                File directory = new File(String.valueOf("Screenshots"));
                if (!directory.exists()) {
                    directory.mkdir();
                }
                SSPath = "Screenshots/" + getRequiredDate(0, "yyyy_MM_dd_hh", null) + "/screenshot_"
                        + getRequiredDate(0, "yyyy_MM_dd_hh_mm_ss", null) + ".png";
                FileUtils.copyFile(scrFile, new File(System.getProperty("user.dir") + "/output/" + SSPath));
            return SSPath;
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }


    public static String getRequiredDate(int incrementDays, String expectedDateFormat, String timeZoneId) {
        try {
            DateFormat dateFormat;
            Calendar calendar = Calendar.getInstance();
            dateFormat = new SimpleDateFormat(expectedDateFormat);
            if (timeZoneId != null && !timeZoneId.equals(""))
                dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            calendar.add(Calendar.DAY_OF_MONTH, incrementDays);
            Date tomorrow = calendar.getTime();
            String formattedDate = dateFormat.format(tomorrow);
            return formattedDate;
        } catch (Exception e) {

            return null;
        }
    }

    public static BufferedImage resizeAndCrop(BufferedImage bufferedImage, Integer width, Integer height) {
        Scalr.Mode mode = (double) width / (double) height >= (double) bufferedImage.getWidth() / (double) bufferedImage.getHeight() ? Scalr.Mode.FIT_TO_WIDTH
                : Scalr.Mode.FIT_TO_HEIGHT;
        bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.ULTRA_QUALITY, mode, width, height);
        int x = 0;
        int y = 0;
        if (mode == Scalr.Mode.FIT_TO_WIDTH) {
            y = (bufferedImage.getHeight() - height) / 2;
        } else if (mode == Scalr.Mode.FIT_TO_HEIGHT) {
            x = (bufferedImage.getWidth() - width) / 2;
        }
        bufferedImage = Scalr.crop(bufferedImage, x, y, width, height);
        return bufferedImage;
    }

}
