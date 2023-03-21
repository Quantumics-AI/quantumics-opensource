package com.beyondx.vteaf.stepDefinition;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.beyondx.vteaf.Runner.Test_Runner;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static com.beyondx.vteaf.stepDefinition.APIBase.replaceGlobalFile;


public class GetPageObjectRead {
	
	public WebDriver driver;
	public Properties prop;
	static By by;
	public static XSSFWorkbook book;
	public static Sheet sheet;
	public static Logger log = LogManager.getLogger(GetPageObjectRead.class);
	
	public final static String PAGE_OBJECTS_SHEET_PATH = "src/test/java/com/beyondx/vteaf/PageObjects/pageObjects.xlsx";
	public final static String API_SHEET_PATH = "src/test/java/com/beyondx/vteaf/resources/API_Automation.xlsx";
	public final static String GLOBAL_SHEET_PATH ="src/test/java/com/beyondx/vteaf/resources/API_GlobalFile.xlsx";

	public static ArrayList<String> OR_READ(String locatorName) {
		FileInputStream file = null;
		ArrayList<String> Locators = new ArrayList<String>();
		try {
			file = new FileInputStream(PAGE_OBJECTS_SHEET_PATH);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			 book = new XSSFWorkbook(file);
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		sheet = book.getSheetAt(0);
//		System.out.println(sheet.getLastRowNum());
		for (int i = 1; i <=sheet.getLastRowNum(); i++) {
			String LocatorName = sheet.getRow(i).getCell(0).toString().trim();
//			System.out.println(LocatorName);
			if(locatorName.equals(LocatorName)) {
				
				String LocatorType = sheet.getRow(i).getCell(1).toString().trim();
				String LocatorValue = sheet.getRow(i).getCell(2).toString().trim();
				Locators.add(LocatorType);
				Locators.add(LocatorValue);
			}
		}
		return Locators;
	}

	public static Sheet ExcelSheet(){
		FileInputStream file = null;
		try {
			file = new FileInputStream(API_SHEET_PATH);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			book = new XSSFWorkbook(file);
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(Test_Runner.Environment.equalsIgnoreCase("QA")){
			sheet = book.getSheetAt(0);}
		else if(Test_Runner.Environment.equalsIgnoreCase("Demo")){
			sheet = book.getSheetAt(1);
		}
		else if(Test_Runner.Environment.equalsIgnoreCase("Prod")){
			sheet = book.getSheetAt(2);
		}else{
			sheet = book.getSheetAt(0);
		}
		return sheet;
	}

	public static ArrayList<String> API_READ(String locatorName) throws InterruptedException {

		String RequestBody =null;
		String Result=null;
		ArrayList<String> API = new ArrayList<String>();
		sheet = ExcelSheet();
		int buffer=0;
		System.out.println(sheet.getLastRowNum());
		for (int i = 1; i <=sheet.getLastRowNum(); i++) {
			buffer=0+i;
			String TestCaseName = sheet.getRow(buffer).getCell(0).toString().trim();
//			System.out.println(LocatorName);
			if(locatorName.equals(TestCaseName)) {

				String httpmethod	= sheet.getRow(i).getCell(1).toString().trim();

				try{
				 RequestBody	= sheet.getRow(i).getCell(2).toString().trim();
				 Result	= sheet.getRow(i).getCell(6).toString().trim();

				}
				catch (Exception e){
					RequestBody	= null;
					Result=null;
				}

				// Store the obtained response
				String EndPoint	= sheet.getRow(i).getCell(3).toString().trim();
				String verifyContentType	= sheet.getRow(i).getCell(4).toString().trim();
				String VerifyStausCode	= sheet.getRow(i).getCell(5).toString().trim();
				String resp= null;
				try {
					 resp = sheet.getRow(i).getCell(7).toString().trim();
				}
				catch (NullPointerException e){
					System.out.println("No Response yet");
				}
				if(EndPoint.contains("{")) {
					ArrayList Globals = ReadGlobalFile(getValueFromBraces(EndPoint));
//					System.out.println(Globals);
					String newEndPoint = replaceEndPoint(EndPoint, getValueFromBraces(EndPoint), Globals);
					API.add(newEndPoint);
				}else{
					API.add(EndPoint);
				}
				API.add(httpmethod);
				API.add(RequestBody);
				API.add(verifyContentType);
				API.add(VerifyStausCode);
				API.add(Result);
				API.add(resp);
			}
		}
		System.out.println(API);
		return API;
	}

	public static void Global_WRITE(String locatorName,String response) throws IOException {
		FileInputStream file = null;
		XSSFRow row = null;
		try {
			file = new FileInputStream(GLOBAL_SHEET_PATH);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			book = new XSSFWorkbook(file);
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sheet = book.getSheetAt(0);
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			String TestCaseName = sheet.getRow(i).getCell(0).toString().trim();
			if (locatorName.equals(TestCaseName)) {
				row = (XSSFRow) sheet.getRow(i);
				Cell cell = row.createCell(1);
				System.out.println(response);
				try {
					cell.setCellValue(response);
					System.out.println(response);
				} catch (IllegalArgumentException e) {
					cell.setCellValue(response.substring(0, 32));
				}

			}
		}
		FileOutputStream out = new FileOutputStream(
				new File(GLOBAL_SHEET_PATH));
		sheet.getWorkbook().write(out);
		out.close();
	}

	public static void WRITE_EXCEL_RES(String locatorName,String response) throws IOException {
			sheet = ExcelSheet();
			XSSFRow row = null;
		for (int i = 1; i <=sheet.getLastRowNum(); i++) {
			String TestCaseName = sheet.getRow(i).getCell(0).toString().trim();
//			System.out.println(LocatorName);
			if(locatorName.equals(TestCaseName)) {
				row = (XSSFRow) sheet.getRow(i);
				Cell cell= row.createCell(7);
				System.out.println(response);
				try{
					cell.setCellValue(response);
				}catch (IllegalArgumentException e){
					cell.setCellValue(response.substring(0,32));
				}

			}
		}

		FileOutputStream out = new FileOutputStream(
				new File(API_SHEET_PATH));
		sheet.getWorkbook().write(out);
		out.close();

	}

	public static ArrayList<String>  getValueFromBraces(String str){
		Matcher m = Pattern.compile("(?<=\\{).+?(?=\\})").matcher(str);
		ArrayList<String> value= new ArrayList<>();
		while(m.find()){
			value.add(m.group().trim());
		}
//		System.out.println(value);
		return value;
	}

	public static String replaceEndPoint(String endpoint, ArrayList v1, ArrayList v2){
		Matcher m = Pattern.compile("(?<=\\{).+?(?=\\})").matcher(endpoint);
		ArrayList<String> value= new ArrayList<>();
		String newEndPoint = endpoint;
		while(m.find()){
			value.add(m.group().trim());
		}
		int index = value.size();
		for (int i =0; i<index;i++){
			if (v1.get(i).equals(value.get(i))){
				newEndPoint = newEndPoint.replace("{"+ value.get(i)+"}",v2.get(i).toString());
			}
		}
		return newEndPoint;
	}
	
	public static By OR_GetLocators(String locatorName) {
		BasicConfigurator. configure();
		ArrayList<String> Locators = OR_READ(locatorName);
		String LocatorType = Locators.get(0).toLowerCase().toString().trim();
		String LocatorValue = Locators.get(1).toString().trim();
//		System.out.println(LocatorType+" "+LocatorValue);
		System.out.println("Successfully found the Locator - "+locatorName);
		 if (LocatorType.equalsIgnoreCase("id")) {
	            by = By.id(LocatorValue);
	        } else if (LocatorType.equalsIgnoreCase("classname")) {
	            by = By.className(LocatorValue);
	        } else if (LocatorType.equalsIgnoreCase("name")) {
	            by = By.name(LocatorValue);
	        } else if (LocatorType.equalsIgnoreCase("linktext")) {
	            by = By.linkText(LocatorValue);
	        } else if (LocatorType.equalsIgnoreCase("partiallinktext")) {
	            by = By.partialLinkText(LocatorValue);
	        } else if (LocatorType.equalsIgnoreCase("cssselector")) {
	            by = By.cssSelector(LocatorValue);
	        } else if (LocatorType.equalsIgnoreCase("xpath")) {
	            by = By.xpath(LocatorValue);
	        } else if (LocatorType.equalsIgnoreCase("tagname")) {
	            by = By.tagName(LocatorValue);
	        }
	        return by;
	}

	public static void read_values_from_excelsheet(){
		File folder = new File("D:\\Users\\vishal\\Downloads\\qa-automation_latest\\data\\data1");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				System.out.println("File " + listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}
	}

	public static ArrayList ReadGlobalFile(ArrayList<String> locatorName) {
		FileInputStream file = null;
		ArrayList gloabls = new ArrayList();
		try {
			file = new FileInputStream(GLOBAL_SHEET_PATH);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			book = new XSSFWorkbook(file);
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sheet = book.getSheetAt(0);
		int index = locatorName.size();
		for (int j=0;j<index;j++) {
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				String TestCaseName = sheet.getRow(i).getCell(0).toString().trim();
				if (locatorName.get(j).equals(TestCaseName)) {
					String value = sheet.getRow(i).getCell(1).toString();
					int integer = (int) Double.parseDouble(value);
					gloabls.add(integer);
				}
			}
		}
		return gloabls;
	}
	
	
	
	public static void main(String[] args) throws InterruptedException, IOException {

//		OR_GetLocators("CleanseData");
//		API_READ("GetAllusers");
//		API_READ("GetAllusers");
//		Global_WRITE("glossaryId",replaceGlobalFile("SaveBusinessGloassary"));
//		read_values_from_excelsheet("");
//		WRITE_EXCEL("GetAllusers","Sample");
	}
	

}
