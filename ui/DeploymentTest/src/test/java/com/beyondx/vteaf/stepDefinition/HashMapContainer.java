package com.beyondx.vteaf.stepDefinition;

import org.apache.log4j.Logger;

import java.util.Map;


public class HashMapContainer {

	static Map<String, String> hm = new java.util.HashMap<String, String>();
	static Map<String, String> hmPO = new java.util.HashMap<String, String>();

	static Logger log = Logger.getLogger(HashMapContainer.class);

	public static void add(String key, String value){
		hm.put(key, value);
	}	
	
	public static void addPO(String key, String value){
		if(hmPO.get(key)!=null){
			
		//	System.out.println("KeyValue: "+key);
			log.debug("KeyValue: "+key);
		}else {
		hmPO.put(key, value);
		}
	}	

	public static String get(String key){
		return hm.get(key);
	}
	
	public static String getPO(String key){
		return hmPO.get(key.toLowerCase());
	}

	public static void remove(String key)throws NullPointerException{
		if(hm.get(key)!=null){
			//hm.remove(key);
		}
	}
	
	public static void removPO(String key)throws NullPointerException{
		if(hmPO.get(key)!=null){
		//	hmPO.remove(key);
		}
	}
	
	public static void ClearHM(){
		hm.clear();
		hmPO.clear();
	}
}
