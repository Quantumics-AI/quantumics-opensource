package com.beyondx.vteaf.stepDefinition;

import com.beyondx.vteaf.Runner.Test_Runner;
import cucumber.api.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.*;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import static com.beyondx.vteaf.stepDefinition.GetPageObjectRead.API_READ;
import static com.beyondx.vteaf.stepDefinition.GetPageObjectRead.Global_WRITE;

public class APIBase {


    public static String getAccessToken(String env) throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(new File("./src/test/java/com/beyondx/vteaf/config/config.properties")));
        Response response= RestAssured
                .given()
                .contentType(ContentType.JSON)
                .when()
                .body("{\r\n" + "  \"email\": \""+prop.getProperty("APIusername")+"\",\r\n" +
                        "  \"password\": \""+prop.getProperty("APIpassword")+"\"\r\n" + "}")
                 .post(env+prop.getProperty("tokenGenerationendPoint"));
        System.out.println(response.asString());
        String token=response.jsonPath().getJsonObject("token").toString();
        System.out.println(token);
            return token;
    }

    public static Response getWithHeadertokenPOC(String httpMethod, String token, String URL) {
        return RestAssured
                .given()
                .when()
                .header("authorization","Bearer "+token)
                .get(URL);
    }

    public static Response postWithHeaderAndJsonBodyFromExcelPOC(String httpMethod,String token, String requestBody, String URL) {
        return RestAssured
                .given()
                .when()
                .header("authorization","Bearer "+token)
                .contentType("application/json")
                .body(requestBody)
                .post(URL);
    }
    public static Response updatewithHeaderAndJsonBodyFromExcelPOC(String httpMethod,String token,
                                                                   String requestBody, String URL) {
        return RestAssured
                .given()
                .when()
                .header("authorization","Bearer "+token)
                .contentType("application/json")
                .body(requestBody)
                .put(URL);
    }

    public static Response deleteWithHeaderAndPathParamPOC(String httpMethod,String token,
                                                           String requestBody, String URL) {
        return RestAssured
                .given()
                .when()
                .header("authorization","Bearer "+token)
                .contentType("application/json")
                .body(requestBody)
                .delete(URL);
    }

    public static Response uploadWithHeaderAndPathParamPOC(String httpMethod,String token,
                                                           String requestBody, String URL) {
        File file = new File("src/test/java/com/beyondx/vteaf/resources/Data/Like.csv");
        return (Response) RestAssured
                .given()
                .when()
                .header("authorization","Bearer "+token)
                .contentType("text/csv")
                .body(file)
                .post(URL);
    }



    public static String replaceJson(String json,String key, String value){
        JSONObject js = new JSONObject(json);
        js.put(key, value);
        return js.toString();
    }

    public static String GetJsonString(String json,String key){
        JSONObject js = new JSONObject(json);
        String valu = String.valueOf(js.getInt(key));
        return valu;
    }

    public static String replaceGlobalFile(String locatorName, String JsonType,String key1, String key2) throws InterruptedException {
        ArrayList value = API_READ(locatorName);
        String res = value.get(6).toString();
        System.out.println(res);
        String result =null;
        if (JsonType.equalsIgnoreCase("parent")){
            result = GetJsonString(res, key1);
        }else {
            result =getChildJsonString(res, key1, key2);
        }
        System.out.println(result);
        return result;
    }

    public static String getChildJsonString(String json, String key1, String key2){
        JSONObject js = new JSONObject(json);
        JSONObject jsonChildObject = (JSONObject)js.get(key1);
        String valu = String.valueOf(jsonChildObject.getInt(key2));
        return valu;
    }

    @Then("I TEST REST API TEST: '(.*)'")
    public static void API_COMMON_GET_TEST_CASE(String Test) throws IOException, InterruptedException {

        ArrayList<String> data = API_READ(Test);
        String EndPoint	= data.get(0);
        String httpmethod	= data.get(1);
        String RequestBody	= data.get(2);
        String verifyContentType	= data.get(3);
        String VerifyStausCode	= data.get(4);
        String Result	= data.get(5);
        Response response = null;
        Random rand = new Random();
        int rand_int1 = rand.nextInt(100000);
        if (Test.contains("FileUpload")||Test.contains("DeleteFolder")||Test.contains("DeleteGlossary")||Test.contains("DeleteDictionary") ){
            Global_WRITE("folderId", replaceGlobalFile("CreateFolders","child","result","folderId"));
            Global_WRITE("glossaryId",replaceGlobalFile("SaveBusinessGloassary","parent","result",""));
            Global_WRITE("DictionaryID",replaceGlobalFile("SaveDataDictionary","parent","result",""));
        }
        if (Test.contains("CreateProject")) {
            RequestBody = replaceJson(RequestBody, "projectName", "project" + rand_int1);
        }

//

        if (httpmethod.equalsIgnoreCase("GET")){

             response = getWithHeadertokenPOC(httpmethod,getAccessToken(Test_Runner.RestUrl),Test_Runner.RestUrl+EndPoint);
        }
        else if (httpmethod.equalsIgnoreCase("POST")){
            try {
                if (RequestBody.contains("engFlowName")) {
                    RequestBody = replaceJson(RequestBody, "engFlowName", "Test" + rand_int1);
                }
                if (RequestBody.contains("folderName")) {
                    RequestBody = replaceJson(RequestBody, "folderName", "Folder" + rand_int1);
                }
                if (RequestBody.contains("userEmail")) {
                    RequestBody = replaceJson(RequestBody, "userEmail", "user" + rand_int1 + "@quantumics.ai");
                }

            }
            catch(NullPointerException e){
                System.out.println("NUll Values Present");
            }
            System.out.println(RequestBody);
             response = postWithHeaderAndJsonBodyFromExcelPOC(httpmethod,getAccessToken(Test_Runner.RestUrl),RequestBody,Test_Runner.RestUrl+EndPoint);
            System.out.println(response.asString());

        }
        else if (httpmethod.equalsIgnoreCase("PUT")){

             response = updatewithHeaderAndJsonBodyFromExcelPOC(httpmethod,getAccessToken(Test_Runner.RestUrl),RequestBody,Test_Runner.RestUrl+EndPoint);
        }
        else if (httpmethod.equalsIgnoreCase("DELETE")){

             response = deleteWithHeaderAndPathParamPOC(httpmethod,getAccessToken(Test_Runner.RestUrl),RequestBody,Test_Runner.RestUrl+EndPoint);
        }
        else if (httpmethod.equalsIgnoreCase("Upload")){
            response = uploadWithHeaderAndPathParamPOC(httpmethod,getAccessToken(Test_Runner.RestUrl),RequestBody,Test_Runner.RestUrl+EndPoint);
        }
        else{
            System.err.println("Invalid Http Method");
        }


        String acontentType= response.getContentType().toLowerCase();
        int code = response.statusCode();
        String aStatusCode = Integer.toString(code);
        Assert.assertEquals(acontentType,verifyContentType.toLowerCase());
        Assert.assertEquals(VerifyStausCode,aStatusCode);
        String actualResult = response.asString();
        GetPageObjectRead.WRITE_EXCEL_RES(Test,actualResult);
//        System.err.println(actualResult);
        if (Result!=null) {
            if (actualResult.contains(Result)) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(false);
            }
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        API_COMMON_GET_TEST_CASE("SaveBusinessGloassary");
//        replaceGlobalFile("SaveBusinessGloassary","parent","result","");
        replaceGlobalFile("CreateFolders","child","result","folderId");
    }
}
