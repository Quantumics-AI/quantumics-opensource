package com.beyondx.vteaf.Hooks;

import com.beyondx.vteaf.Runner.Test_Runner;
import com.beyondx.vteaf.stepDefinition.StepBase;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import cucumber.runtime.CucumberException;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collection;
import java.util.LinkedHashMap;

public class Hooks {
    public static int code;
    public static LinkedHashMap<String, String> scenarioStatus = new LinkedHashMap<String, String>();
    static Logger log = Logger.getLogger(Hooks.class.getName());
    public static Scenario scenario;
    static int scenarioOutline;
    public static Scenario getScenario() {
        return scenario;
    }

    @Before
    public void BeforeScenarioSteps(Scenario scenario) {
        try{
        String name = scenario.getName();
        Collection<String> sourceTagNames = scenario.getSourceTagNames();
        System.out.println("Scenario: "+name);
        System.out.println("Source Tag: "+sourceTagNames);
        this.scenario = scenario;
        StepBase.setScenario(scenario);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new CucumberException(e);
        }

    }

    @After
    public void AfterStep(Scenario s) throws IOException {
        if(scenario.isFailed() && !Test_Runner.TEST_MODE.equalsIgnoreCase("Rest")){
            final byte[] screenshot = ((TakesScreenshot) StepBase.getDriver())
                    .getScreenshotAs(OutputType.BYTES);
//            InputStream is = new ByteArrayInputStream(screenshot);
//            BufferedImage newBi = ImageIO.read(is);
//            BufferedImage resized =StepBase.resizeAndCrop(newBi,800,400 );
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ImageIO.write(resized, "png", baos);
//            byte[] resizedScreenshot = baos.toByteArray();
            scenario.embed(screenshot, "image/png"); // Stick it to HTML report
            StepBase.takeScreenshot(StepBase.getDriver());
        }
    }

    @AfterStep
    public static void AfterScenarioSteps(Scenario scenario){
        String name = scenario.getName();
//        log.debug(name);
        String status = scenario.getStatus().toString();
        System.out.println("Scenario - " + name + " : status - " + status);
        scenarioStatus.put(name, status);
//        System.out.println(scenarioStatus);
    }

}
