package com.beyondx.vteaf.stepDefinition;

import org.junit.Assert;

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.minidev.json.JSONObject;

public class APIStepDefinition {
	
	 public static Scenario scenario;
	 
	 @Before
	 public void Before(Scenario scenario) {
		 this.scenario = scenario;
	 }
	
	
	
	
	@Then("I GET response from '(.*)' and check status code is '(.*)'")
	public static void testResponseCode(String url, int statusCode) {
		Response resp = RestAssured.get(url);
		int code = resp.getStatusCode();
		String data = resp.asString();
		scenario.write("Status Code: "+code);
		double time =resp.getTime()*0.001;
		scenario.write(time+" sec");
		scenario.write(data);
		Assert.assertEquals(statusCode, code);
		
	}
	
	@Then("I POST response from '(.*)' url and add body Name: '(.*)' Position: '(.*)' check status code is '(.*)'")
	public static void testPostReq(String url , String Name, String Position,int statusCode) {
		RequestSpecification req = RestAssured.given();
		req.headers("Content-Type","application/json");
		JSONObject json = new JSONObject();
		json.put("name", Name);
		json.put("job", Position);
		req.body(json.toJSONString());
		Response res = req.post(url);
		int status =res.getStatusCode();
		double time =res.getTime()*0.001;
		String data=res.asString();
		scenario.write("Status Code: "+status);
		scenario.write(time+" sec");
		scenario.write(data);
		Assert.assertEquals(statusCode, status);

	}
	
	public static void testPostLogin(String username, String password) {
		RequestSpecification req = RestAssured.given();
		req.headers("Content-Type","application/json");
		JSONObject json = new JSONObject();
		json.put("email", username);
		json.put("password", password);
		req.body(json.toJSONString());
		Response res = req.post("https://reqres.in/api/login");
		int status =res.getStatusCode();
		double time =res.getTime()*0.001;
		String data=res.asString();
		System.out.println(status);
		System.out.println(time+" sec");
		System.out.println(data);
	}
	
	public static void testPostRegister(String username, String password) {
		RequestSpecification req = RestAssured.given();
		req.headers("Content-Type","application/json");
		JSONObject json = new JSONObject();
		json.put("email", username);
		json.put("password", password);
		req.body(json.toJSONString());
		Response res = req.post("https://reqres.in/api/register");
		int status =res.getStatusCode();
		double time =res.getTime()*0.001;
		String data=res.asString();
		System.out.println(status);
		System.out.println(time+" sec");
		System.out.println(data);
	}
	
	public static void testPutRegister(String username, String password,String id) {
		RequestSpecification req = RestAssured.given();
		req.headers("Content-Type","application/json");
		JSONObject json = new JSONObject();
		json.put("email", username);
		json.put("password", password);
		req.body(json.toJSONString());
		Response res = req.put("https://reqres.in/api/api/users/"+id);
		int status =res.getStatusCode();
		double time =res.getTime()*0.001;
		String data=res.asString();
		System.out.println(status);
		System.out.println(time+" sec");
		System.out.println(data);
	}
	
	public static void testDeleteUser(String id) {
		RequestSpecification req = RestAssured.given();
		Response res = req.delete("https://reqres.in/api/api/users/"+id);
		int status =res.getStatusCode();
		double time =res.getTime()*0.001;
		System.out.println(status);
		System.out.println(time+" sec");
	}
	
	public static void main(String[] args) {
		testDeleteUser("2");
	}
}
