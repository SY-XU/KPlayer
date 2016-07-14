package com.xk.player.tools;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;



public class JSONUtil {
public static ObjectMapper mapper=new ObjectMapper();
	
	public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {   
		return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);   
    } 
	
	public static Map<String,Object> fromJson(String params){
		try {
			JavaType jType=getCollectionType(Map.class,String.class,Object.class);
			return mapper.readValue(params, jType);
		} catch (Exception e) {
			return Collections.EMPTY_MAP;
		} 
	}
	
	public static <T>T toBean(String params,JavaType javaType){
		try {
			return mapper.readValue(params, javaType);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static <T>T toBean(String params,Class<T> clazz) {
		try {
			return mapper.readValue(params, clazz);
		} catch (Exception e) {
			return null;
		} 
	}
	
	public static String toJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			return null;
		} 
	}
}
