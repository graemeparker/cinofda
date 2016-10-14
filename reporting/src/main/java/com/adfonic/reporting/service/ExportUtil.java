package com.adfonic.reporting.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import au.com.bytecode.opencsv.CSVWriter;

import com.adfonic.reporting.service.advertiser.dto.BaseReportDto;
import com.adfonic.reporting.service.advertiser.dto.mixin.BaseReportDtoMixin;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExportUtil {
	
	public static final String ADFONIC = "adfonic";
	public static final String FORMAT = "csv";
	public static final String ADVERTISER_CAMPAIGN = "advertiser-campaign";
	public static final String ADVERTISER_BUDGET = "advertiser-budget";
	public static final String ADVERTISER_DEVICE = "advertiser-device";
	public static final String ADVERTISER_CREATIVE = "advertiser-creative";
	public static final String ADVERTISER_OPERATOR = "advertiser-operator";
	public static final String ADVERTISER_LOCATION = "advertiser-location";

	public static File createExportFile(List<BaseReportDto> result, String reportPrefix, Locale locale, BaseReportDto reportDto) 
			throws IOException, IllegalArgumentException, IllegalAccessException {
		String fileName = ExportUtil.ADFONIC.concat("-")
				.concat(reportPrefix).concat("-")
				.concat(String.valueOf(System.currentTimeMillis()).concat(".")
				.concat(ExportUtil.FORMAT));
		File exportCsv = new File(fileName);
		CSVWriter writer = new CSVWriter(new FileWriter(exportCsv));

		Map<Field, Boolean> headerMap = new LinkedHashMap<>();
		String[] headerFields = null;
		List<String[]> rowFields = new ArrayList<>(result.size());
		for(BaseReportDto dto : result) {
			LinkedList<Field> list = getFieldsList(dto);
			for(Field field : list) {
				field.setAccessible(true);
				if(!field.getName().equals("serialVersionUID") && field.get(dto) != null) {
					headerMap.put(field,true);
				}
			}
			rowFields.add(asArray(headerMap, dto, locale));
		}
		headerFields = getHeader(headerMap,result.get(0));
		writer.writeNext(headerFields);
		writer.writeAll(rowFields);
		writer.flush();
		writer.close();
		return exportCsv;
	}
	
	private static String[] getHeader(Map<Field, Boolean> headersMap, BaseReportDto reportDto) throws IllegalArgumentException, IllegalAccessException {
		Map<String, Annotation> mixinFieldsMap = getMixinFields(reportDto.getMixin());
		List<String> headerValues = new ArrayList<>(headersMap.size());
		for(Entry<Field, Boolean> field: headersMap.entrySet()) {
			if(field.getValue()) {
				Annotation annotation = mixinFieldsMap.get(field.getKey().getName());
				if(annotation != null && annotation instanceof JsonProperty) {
					headerValues.add(((JsonProperty)annotation).value());
				}
			}
		}
		return headerValues.toArray(new String[0]);
	}
	
	private static LinkedList<Field> getFieldsList(BaseReportDto reportDto) {
		LinkedList<Field> fieldsList = new LinkedList<>(Arrays.asList(reportDto.getClass().getSuperclass().getDeclaredFields()));
		fieldsList.addAll(Arrays.asList(reportDto.getClass().getDeclaredFields()));
		return fieldsList;
	}
	
	private static Map<String, Annotation> getMixinFields(BaseReportDtoMixin reportDtoMixin) {
		LinkedList<Field> fieldsList = new LinkedList<>(Arrays.asList(reportDtoMixin.getClass().getSuperclass().getDeclaredFields()));
		fieldsList.addAll(Arrays.asList(reportDtoMixin.getClass().getDeclaredFields()));
		Map<String, Annotation> fieldsMap = new LinkedHashMap<>();
		for (Field field : fieldsList) {
			field.setAccessible(true);
			fieldsMap.put(field.getName(),field.getAnnotation(JsonProperty.class));
		}
		return fieldsMap;
	}
	
	private static String[] asArray(Map<Field,Boolean> headerMap, BaseReportDto reportDto, Locale locale) throws IllegalArgumentException, IllegalAccessException, SecurityException {
		List<Field> fieldsList = getFields(reportDto);
		String[] fields = new String[fieldsList.size()];
		DateFormat df;
		for (Field field : fieldsList) {
			if(field.get(reportDto) != null) {
				int i = 0;
				for(Entry<Field,Boolean> entry : headerMap.entrySet()) {
					if(entry.getKey().getName().equals(field.getName())) {
						if(field.getType().equals(Date.class)) {
							df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
							fields[i] = df.format(field.get(reportDto));
							break;
						} else {
							fields[i] = field.get(reportDto).toString();
							break;
						}
					}
					i++;
				}
			}
		}
		return fields;
	}
	
	private static LinkedList<Field> getFields(BaseReportDto reportDto) throws IllegalArgumentException, IllegalAccessException {
		LinkedList<Field> fieldsList = new LinkedList<>(Arrays.asList(reportDto.getClass().getSuperclass().getDeclaredFields()));
		fieldsList.addAll(Arrays.asList(reportDto.getClass().getDeclaredFields()));
		for (Iterator<Field> iterator = fieldsList.iterator(); iterator.hasNext();) {
			Field field = iterator.next();
			field.setAccessible(true);
			if(field.getName().equals("serialVersionUID") || field.get(reportDto) == null) {
				iterator.remove();
			}
		}
		return fieldsList;
	}
}
