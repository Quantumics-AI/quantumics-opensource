package com.beyondx.vteaf.stepDefinition;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;


public class Algo {
	static Scanner sc = new Scanner(System.in);
	
	public static int inputScanner(Scanner sc) {
		int x;
    	System.out.println("Enter the integer: ");
    	x = sc.nextInt();
    	return x;
	}

    public static void odd_or_even() {
    	int x=inputScanner(sc);
    	if(x%2==0) {
    		System.out.println("Even Number");
    	}
    	else {
    		System.out.println("Odd Number");
    	}
	}
    
    public static void factorial() {
    	 int fact =1;
    	int n=inputScanner(sc);
    	for(int i=1;i<=n;i++) {
    		fact=fact*i;
    	}
    	System.out.println("Factorial is: "+fact);
	}
    
    public static void prime_number() {
    	int n = inputScanner(sc);
    	int count=0;
    	System.out.println(n);
    	for(int i=1;i<=n;i++) {
    		if(n%i==0) {
    			count++;
    		}
    		}
    	
    		if(count==2) {
    			System.out.println("Prime Number");
    		}
    		else {
    			System.out.println("Not a prime number");
    		}
    	}
    
    private static void prime_number_2() {
    	int i = inputScanner(sc);
    	int j=2;
    	int ch =0;
    	
    	if(i<=1) {
    		ch=1;
    	}
    	
    	while(j<=i/2) {
    		if(i%j==0) {
    			ch=1;
    			break;
    		}
    		else {
    			j++;
    		}
    	}
    	
    	if(ch==0) {
    		System.out.println(i+" is Prime");
    	}
    	else {
    		System.out.println(i+" is not prime");
    	}
    	

	}
    
    private static int max_num(int a[],int n) {
    	int i;
    	int m=0;
    	for(i=0;i<n;i++) {
    		if(a[i]>m) {
    			m=a[i];
    		}
    	}
    	return m;
	}
    
    private static void largestNumber() {
    	int n = inputScanner(sc);
    	int max,i;
    	int a[] = new int[n];
    	System.out.println("Enter the numbers: ");
    	for (i = 0; i < n; i++) {
			a[i]=sc.nextInt();
		}
    	
    	max =max_num(a, n);
    	System.out.println("Max Number: "+max);
    	
   
	}
    
    private static void swap_two_numbers() {
    	System.out.println("Enter two numbers");
    	int x = sc.nextInt();
    	int y = sc.nextInt();
    	int temp;
    	System.out.println("Before Swapping\n x="+x+"\n y="+y);
    	temp =x;
    	x=y;
    	y=temp;
    	System.out.println("After Swapping\n x="+x+"\n y="+y);
	}
    
    private static void fibonacci() {
    	int f1=0;
    	int f2=1;
    	int result;
    	int user_no;
    	int count =0;
    	
    	System.out.println("Enter any number:");
    	user_no=sc.nextInt();
    	while(true) {
    		result = f1+f2;
    		count++;
    		if(result>=user_no) {
    			break;
    		}
    		f1=f2;
    		f2=result;
    		System.out.println("\nFibanacci No.["+count+"]->"+result);
    	}

	}
    
    private static void palindrome() {
    	String a,b;
    	StringBuffer sb;
    	System.out.println("Enter a sting to check if it is a palindrome or not");
    	a = sc.nextLine();
    	sb=new StringBuffer(a);
    	b=sb.reverse().toString();
    	if(a.equals(b)) {
    		System.out.println("Entered string is a palindrome");
    	}else {
    		System.out.println("Entered string is not a palindrome");
    	}

	}
    
    	public static int getSecondLargest(int[] a, int total){  
    	int temp;  
    	for (int i = 0; i < total; i++)   
    	        {  
    		System.out.println(total);
    	            for (int j = i + 1; j < total; j++)   
    	            {  
    	                if (a[i] > a[j])   
    	                {  
    	                    temp = a[i];  
    	                    a[i] = a[j];  
    	                    a[j] = temp;  
    	                    System.out.println(total);
    	                }  
    	            }  
    	        }  
    	       return a[total-2];  
    	} 
    	
    	public static boolean isBalanced(String str) {
    		if (str == null || ((str.length() % 2) != 0)) {
    		    return false;
    		}else {
    			char[] ch = str.toCharArray();
    			for (char c : ch) {
					if(!(c=='{' || c=='['||c=='('||c=='}'||c==']'||c==')')) {
						 return false;
					}
				}
    		}
    		
    		while(str.contains("()")||str.contains("[]")||str.contains("{}")) {
    			str =str.replaceAll("\\(\\)", "")
    					.replaceAll("\\[\\]", "")
    					.replaceAll("\\{\\}","");
    		}
    		return (str.length() == 0);
		}


	public static void verifyResponsefromExcelforGet(Response response,String expectedResult) {

		String actualValues = response.asString();
		System.err.println(actualValues);
		System.out.println(expectedResult);
		if(actualValues.contains(expectedResult))
		{
			System.out.println("The JSON response has value "+expectedResult+" as expected");
		}else {
			System.out.println("The JSON response :"+actualValues+" does not have the value "+expectedResult+" as expected.");
		}
	}



	public static void verifyContentType(Response response, String type) {
		if(response.getContentType().toLowerCase().contains(type.toLowerCase())) {
			System.out.println("The Content type "+type+" matches the expected content type");
		}else {
			System.out.println("The Content type "+type+" does not match the expected content type "+response.getContentType());
		}
	}

	public static void verifyResponseCode(Response response, String statusCode) {

		int statusCodeactual = response.statusCode();
		String statusCodeactualConverted = Integer.toString(statusCodeactual);
		System.out.println(statusCodeactual);
		if(statusCodeactualConverted.equals(statusCode)) {

			System.out.println("The status code "+statusCode+" matches the expected code");
		}else {
			System.out.println("The status code "+statusCode+" does not match the expected code"+response.statusCode());
		}
	}

	public static void verifyResponsefromExcelforPost(Response response,String expectedResult) {

		//String actualValues = response.asString();

		String actualResult = response.asString();
		System.err.println(actualResult);
		if(actualResult.contains(expectedResult))
		{
			System.out.println("The JSON response has value "+expectedResult+" as expected. ");
		}else {
			System.out.println("The JSON response :"+actualResult+" does not have the value "+expectedResult+" as expected. ");
		}
	}

	public void getAllUsers(String Environment,String testcaseType,String description,String module,String httpMethod,String endPoint,String contentType,String statusCode,String expectedResult) {
//		Response response = getWithHeadertokenPOC(httpMethod,token,endPoint);
//		verifyContentType(response,contentType);
//		verifyResponseCode(response, statusCode);
//		verifyResponsefromExcelforGet(response, expectedResult);
	}


    	

    
    public static void main(String[] args) {
    	boolean value =isBalanced("({})]");
    	System.out.println(value);
	}

}
