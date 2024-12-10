package com.mcinfotech.event.handler.algorithm;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.mcinfotech.event.handler.domain.PlatformColumnMapping;
import com.mcinfotech.event.handler.domain.PlatformEventSourceSeverityValueMapping;
import com.mcinfotech.event.handler.domain.PlatformProbeColumnMapping;
import com.mcinfotech.event.handler.domain.PlatformProbeSeverityMapping;
import com.mcinfotech.event.utils.DateFormat;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

/**
 * 事件处理之事件字段映射、级别映射
 * 事件平台提供数据类型包括，包括string(字符串)，int(整型数字)，float(非整型数字)，longtimestamp(时间戳)
 * 事件平台能够接受的数据类型包括包括unixtimstamp、date、string、int；其中unixtimestamp为10的时间戳，longtimestamp为13位的时间戳，date格式为2021-04-03 12:00:23或者2021-05-14’T’12:00:23.213
 * 事件来源中某些字段在事件平台中不存在时，可以填入默认值的方式，也可以将事件平台多个字段组合的方式获取数值
 * 在设计文档中需要补充取值类型：拼接、表达式

 *
 */
public class EventMapping {
	private static Logger logger=LogManager.getLogger(EventMapping.class);
	/**
	 * 将事件信息按照映射规则进行字段映射和级别映射。
	 * 映射的流程
	 * @param severitySettings 级别映射规则
	 * @return 映射过的消息
	 */
	public static Map<String, Object> excute(Map<String,Object> rawMessages,PlatformProbeColumnMapping columnsSettings,PlatformProbeSeverityMapping severitySettings){
		if(rawMessages==null) {
			return null;
		}
		if(columnsSettings==null) {
			return rawMessages;
		}
		if(severitySettings==null) {
			return rawMessages;
		}
		List<PlatformColumnMapping> columnsMapping=columnsSettings.getColumnMapping();
		if(CollectionUtils.isEmpty(columnsMapping)) {
			return rawMessages;
		}
		Map<String,Object> handleredData=null;
		try {
			handleredData=new HashMap<>();
			handleredData.put("RefIntegratedRules",rawMessages.get("RefIntegratedRules"));
			handleredData.put("RefFilterRules",rawMessages.get("RefFilterRules"));
			handleredData.put("RefCompressRules",rawMessages.get("RefCompressRules"));
			handleredData.put("RefGroupRules",rawMessages.get("RefGroupRules"));
			handleredData.put("RefRecoveryRules",rawMessages.get("RefRecoveryRules"));
			handleredData.put("RefUpdownRules",rawMessages.get("RefUpdownRules"));
			handleredData.put("RefNotifyRules",rawMessages.get("RefNotifyRules"));
			handleredData.put("RefRichRules",rawMessages.get("RefRichRules"));
			for(PlatformColumnMapping columnMapping:columnsMapping){
				//如果没有映射值设置则不处理
				String sourceColumn=columnMapping.getSourceColumn();
				String sourceValue=columnMapping.getSourceValue();//a1 默认值
				String platformColumnName=columnMapping.getPlatformColumn();
				String platformDataType=columnMapping.getPlatformDataType();
				String sourceDataType=columnMapping.getSourceDataType();
				Object rawSourceValue=rawMessages.get(platformColumnName);//a2 根据平台字段名称获取到的值
				Object sourceVal=rawMessages.get(sourceColumn);//a3 根据源字段名称获取到的值
				String mappingType = columnMapping.getMappingType();

				if ("join".equals(mappingType)) {//拼接
					String[] split = sourceColumn.split("\\+");
					StringBuffer sb = new StringBuffer();
					for (String s : split) {
						if (rawMessages.containsKey(s)) {
							sb.append(rawMessages.get(s));
						} else {
							sb.append(s);
						}
					}
					sourceVal = sb.toString();
				} else if ("expression".equals(mappingType)) {//表达式
					sourceVal = buildBody(sourceColumn, rawMessages);
				}

				//3.2.1如果没有设置事件源映射字段，则取mappingValue
				if((StringUtils.isEmpty(sourceColumn)||StringUtils.isNotEmpty(sourceColumn))&&(StringUtils.isNotEmpty(sourceValue))){
					if(platformDataType.equalsIgnoreCase("int")){
						if(StringUtils.isEmpty(sourceDataType)||sourceDataType.equalsIgnoreCase("string")){
							handleredData.put(platformColumnName, new BigDecimal(sourceValue).intValue());
						}else if(sourceDataType.equalsIgnoreCase("float")){
							handleredData.put(platformColumnName, new BigDecimal(sourceValue).intValue());
						}else if(sourceDataType.equalsIgnoreCase("date")){
							handleredData.put(platformColumnName, new Timestamp(DateFormat.parseT((String)sourceValue).getMillis()));
						}else if(sourceDataType.equalsIgnoreCase("unixtimestamp")){
							handleredData.put(platformColumnName, new BigDecimal(sourceValue).intValue()*1000);
						}else if(sourceDataType.equalsIgnoreCase("longtimestamp")){
							handleredData.put(platformColumnName, new BigDecimal(sourceValue).intValue());
						}else{
							handleredData.put(platformColumnName, sourceValue);
						}
					}else if(platformDataType.equalsIgnoreCase("longtimestamp")){
						if(StringUtils.isEmpty(sourceDataType)||sourceDataType.equalsIgnoreCase("date")||sourceDataType.equalsIgnoreCase("string")){
							handleredData.put(platformColumnName, new Timestamp(DateFormat.parseT(sourceValue).getMillis()));
						}else if(sourceDataType.equalsIgnoreCase("longtimestamp")){
							handleredData.put(platformColumnName, new Timestamp(new BigDecimal(sourceValue).longValue()));
						}else if(sourceDataType.equalsIgnoreCase("unixtimestamp")){
							handleredData.put(platformColumnName, new Timestamp(new BigDecimal(sourceValue).longValue()*1000));
						}else if(sourceDataType.equalsIgnoreCase("int")||sourceDataType.equalsIgnoreCase("float")){
							handleredData.put(platformColumnName, new Timestamp(new BigDecimal(sourceValue).longValue()));
						}
					}else if(platformDataType.equalsIgnoreCase("float")){
						handleredData.put(platformColumnName, new BigDecimal(sourceValue).floatValue());
					}else{
						handleredData.put(platformColumnName, sourceValue);
					}
				}else if(StringUtils.isNotEmpty(sourceColumn)&&(StringUtils.isEmpty(sourceValue))){
					if(sourceColumn.equalsIgnoreCase(severitySettings.getEventSourceSeverityColumn())){
						//3.2.2是否是事件级别字段
						//从当前事件中取到的值
						int platformSeverityValue=0;
						//用当前取到的值去定级
						for(PlatformEventSourceSeverityValueMapping severityMapping:severitySettings.getValueMapping()){
							String platformSeveritySettingValue=severityMapping.getSourceSeverity();
							String platformSeverityOperator=severityMapping.getOperator();
							if(sourceDataType.equalsIgnoreCase("string")){
								String eventSourceSevertiyStringValue=String.valueOf(sourceVal) ;
								if(platformSeverityOperator.equalsIgnoreCase("=")){
									if(eventSourceSevertiyStringValue.equalsIgnoreCase(platformSeveritySettingValue)){
										platformSeverityValue=Integer.parseInt(severityMapping.getPlatformSeverity());
										break;
									}
								}else if(platformSeverityOperator.equalsIgnoreCase("in")){
									if(platformSeveritySettingValue.contains(eventSourceSevertiyStringValue)){
										platformSeverityValue=Integer.parseInt(severityMapping.getPlatformSeverity());
										break;
									}
								}
							}else if(sourceDataType.equalsIgnoreCase("int")){
								int eventSourceSevertiyIntValue=(int) sourceVal;
								if(platformSeverityOperator.equalsIgnoreCase("=")){
									if(eventSourceSevertiyIntValue==Integer.parseInt(platformSeveritySettingValue)){
										platformSeverityValue=Integer.parseInt(severityMapping.getPlatformSeverity());
										break;
									}
								}else if(platformSeverityOperator.equalsIgnoreCase("in")){
									if(platformSeveritySettingValue.contains(new Integer(eventSourceSevertiyIntValue).toString())){
										platformSeverityValue=Integer.parseInt(severityMapping.getPlatformSeverity());
										break;
									}
								}
							}
						}
						handleredData.put(platformColumnName, platformSeverityValue);
					}else{
						if((rawMessages.containsKey(columnMapping.getSourceColumn()) && "one".equals(mappingType)) || "join".equals(mappingType) || "expression".equals(mappingType)){
							//String platformColumn
							if(platformDataType.equalsIgnoreCase("int")){
								if(sourceVal==null){
									handleredData.put(platformColumnName, -9999);
								}else{
									/*if(StringUtils.isEmpty(sourceDataType)||sourceDataType.equalsIgnoreCase("string")){
										handleredData.put(platformColumnName, new BigDecimal(sourceVal.toString()).intValue());
									}else{
										handleredData.put(platformColumnName, sourceVal);
									}*/
									if(StringUtils.isEmpty(sourceDataType)||sourceDataType.equalsIgnoreCase("string")){
										handleredData.put(platformColumnName, new BigDecimal(sourceVal.toString()).intValue());
									}else if(sourceDataType.equalsIgnoreCase("float")){
										handleredData.put(platformColumnName, new BigDecimal(sourceVal.toString()).intValue());
									}else if(sourceDataType.equalsIgnoreCase("date")){
										handleredData.put(platformColumnName, new Timestamp(DateFormat.parseT(sourceVal.toString()).getMillis()));
									}else if(sourceDataType.equalsIgnoreCase("unixtimestamp")){
										handleredData.put(platformColumnName, new BigDecimal(sourceVal.toString()).intValue()*1000);
									}else if(sourceDataType.equalsIgnoreCase("longtimestamp")){
										handleredData.put(platformColumnName, new BigDecimal(sourceVal.toString()).intValue());
									}else{
										handleredData.put(platformColumnName, sourceVal);
									}
								}
							}else if(platformDataType.equalsIgnoreCase("longtimestamp")){
								if(sourceVal==null){
									handleredData.put(platformColumnName, System.currentTimeMillis());
								}else{
									if(StringUtils.isEmpty(sourceDataType)||sourceDataType.equalsIgnoreCase("date")||sourceDataType.equalsIgnoreCase("string")){
										handleredData.put(platformColumnName, new Timestamp(DateFormat.parseT(sourceVal.toString()).getMillis()).getTime());
									}else if(sourceDataType.equalsIgnoreCase("longtimestamp")){
										handleredData.put(platformColumnName, new Timestamp(Long.parseLong(sourceVal.toString())).getTime());
									}else if(sourceDataType.equalsIgnoreCase("unixtimestamp")){
										handleredData.put(platformColumnName, new Timestamp(Long.parseLong(sourceVal.toString())*1000).getTime());
									}else if(sourceDataType.equalsIgnoreCase("int")||sourceDataType.equalsIgnoreCase("float")){
										handleredData.put(platformColumnName, new Timestamp(new BigDecimal(sourceVal.toString()).longValue()));
									}
								}
							}else if(platformDataType.equalsIgnoreCase("float")){
								if(sourceVal==null){
									handleredData.put(platformColumnName, -9999f);
								}else{
									if(StringUtils.isEmpty(sourceDataType)||sourceDataType.equalsIgnoreCase("string")){
										handleredData.put(platformColumnName, new BigDecimal(sourceVal.toString()).floatValue());
									}else{
										handleredData.put(platformColumnName, sourceVal);
									}
								}
							}else {
								if(sourceVal==null){
									handleredData.put(platformColumnName, "NA");
								}else{
									handleredData.put(platformColumnName, sourceVal);
								}
							}
						}else{
							//如果事件中不包括该字段的话，eventid，acknowledged这两个字段会赋值
							if(!rawMessages.containsKey(platformColumnName)){
								if(platformColumnName.equalsIgnoreCase("eventid")){
									handleredData.put(platformColumnName, UUID.randomUUID());
								}else if(platformColumnName.equalsIgnoreCase("acknowledged")){
									handleredData.put(platformColumnName, 0);
								}else if(platformColumnName.equalsIgnoreCase("isComponent")){
									handleredData.put(platformColumnName, 0);
								}else{
									if(platformDataType.equalsIgnoreCase("int")){
										handleredData.put(platformColumnName, -9999);
									}else if(platformDataType.equalsIgnoreCase("longtimestamp")){
										handleredData.put(platformColumnName, 0);
									}else if(platformDataType.equalsIgnoreCase("float")){
										handleredData.put(platformColumnName, -9999f);
									}else {
										handleredData.put(platformColumnName, "NA");
									}
								}
							}
						}
					}
				}else{
					////3.2.1通过设置的事件源映射字段，获取数值
					//
					//有一种情况是：rawMessages包括的字段与columnMapping一致并且SourceColumn和SourceValue为空时没有处理，按照目前的处理方式：这种情况包括的字段不能映射
					//if(StringUtils.isEmpty(sourceValue)&&StringUtils.isEmpty(sourceColumn)){
					////有一种情况是：rawMessages包括的字段与columnMapping一致并且SourceColumn和SourceValue为空时没有处理，按照目前的处理方式：这种情况包括的字段不能映射
					//这种情况不做处理，无法判断事件来源的数据类型
					//如果事件中不包括该字段的话，eventid，acknowledged这两个字段会赋值
					if(!rawMessages.containsKey(platformColumnName)){
						if(platformColumnName.equalsIgnoreCase("eventid")){
							handleredData.put(platformColumnName, UUID.randomUUID());
						}else if(platformColumnName.equalsIgnoreCase("acknowledged")){
							handleredData.put(platformColumnName, 0);
						}else if(platformColumnName.equalsIgnoreCase("isComponent")){
							handleredData.put(platformColumnName, 0);
						}else{
							if(platformDataType.equalsIgnoreCase("int")){
								handleredData.put(platformColumnName, -9999);
							}else if(platformDataType.equalsIgnoreCase("longtimestamp")){
								handleredData.put(platformColumnName, 0);
							}else if(platformDataType.equalsIgnoreCase("float")){
								handleredData.put(platformColumnName, -9999f);
							}else {
								handleredData.put(platformColumnName, "NA");
							}
						}
					}
					continue;
					//}
				}
			}
			if(logger.isDebugEnabled()){
				logger.debug(handleredData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return handleredData;
		}
	}
	private static String buildBody(String templet, Map<String, Object> params) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		stringLoader.putTemplate("msgContents", templet);
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
		cfg.setNumberFormat("#");
		cfg.setTemplateLoader(stringLoader);
		Writer out = new StringWriter(4096);
		Template template = cfg.getTemplate("msgContents");
		template.process(params, out);
		return out.toString();
	}
}