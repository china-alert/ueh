package com.mcinfotech.event.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONLibDataFormatSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**

 */
public class FastJsonUtils {

	private static final SerializeConfig config;

	static {
		config = new SerializeConfig();
		config.put(java.util.Date.class, new JSONLibDataFormatSerializer()); // 使用和json-lib兼容的日期输出格式
		config.put(java.sql.Date.class, new JSONLibDataFormatSerializer()); // 使用和json-lib兼容的日期输出格式
	}

	private static final SerializerFeature[] features = { SerializerFeature.WriteMapNullValue, // 输出空置字段
			SerializerFeature.WriteNullListAsEmpty, // list字段如果为null，输出为[]，而不是null
			SerializerFeature.WriteNullNumberAsZero, // 数值字段如果为null，输出为0，而不是null
			SerializerFeature.WriteNullBooleanAsFalse, // Boolean字段如果为null，输出为false，而不是null
			SerializerFeature.WriteNullStringAsEmpty // 字符类型字段如果为null，输出为""，而不是null
	};

	public static String convertObjectToJSON(Object object) {
		return JSON.toJSONString(object, config, features);
	}

	public static String toJSONNoFeatures(Object object) {
		return JSON.toJSONString(object, config);
	}

	public static Object toBean(String text) {
		return JSON.parse(text);
	}

	public static <T> T toBean(String text, Class<T> clazz) {
		return JSON.parseObject(text, clazz);
	}

	// 转换为数组
	public static Object[] toArray(String text) {
		return toArray(text, null);
	}

	// 转换为数组
	public static <T> Object[] toArray(String text, Class<T> clazz) {
		return JSON.parseArray(text, clazz).toArray();
	}
	// 转换为List
	public static <T> List<T> toList(String text, Class<T> clazz) {
		return JSON.parseArray(text, clazz);
	}

	/**
	 * 将string转化为序列化的json字符串
	 */
	public static Object textToJson(String text) {
		return JSON.parse(text);
	}

	/**
	 * json字符串转化为map
	 */
	public static <K, V> Map<K, V> stringToCollect(String s) {
		return (Map<K, V>) JSONObject.parseObject(s);
	}

	/**
	 * 转换JSON字符串为对象
	 */
	public static Object convertJsonToObject(String jsonData, Class<?> clazz) {
		return JSONObject.parseObject(jsonData, clazz);
	}

	public static Object convertJSONToObject(String content, Class<?> clazz) {
		return JSONObject.parseObject(content, clazz);
	}

	/**
	 * 将map转化为string
	 */
	public static <K, V> String collectToString(Map<K, V> m) {
		return JSONObject.toJSONString(m);
	}
	
	/**
	 * 检测jsonStr是否字符串，并且包括指定的属性
	 * @param jsonStr
	 * @param includeAttr
	 * @return
	 */
	public static boolean isValidJson(String jsonStr,String includeAttr){
		if(StringUtils.isEmpty(jsonStr)||StringUtils.isEmpty(includeAttr)){
			return false;
		}
		try {
			Map<String,Object> result=FastJsonUtils.stringToCollect(jsonStr);
			if(result.containsKey(includeAttr)){
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 从JSON字符串中抽取指定属性的值
	 * @param jsonStr
	 * @param includeAttr 指定属性
	 * @return
	 */
	public static Object extractValue(String jsonStr,String includeAttr){
		if(StringUtils.isEmpty(jsonStr)||StringUtils.isEmpty(includeAttr)){
			return null;
		}
		try {
			Map<String,Object> result=FastJsonUtils.stringToCollect(jsonStr);
			return result.get(includeAttr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
