package com.hps.mayo.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Configuration
public class Hl7CodesMappingConfig {

	private Logger logger = LogManager.getLogger(this.getClass());
	private static HashMap<String, String> codesMap = new HashMap<String, String>();
	private static List<String> codesList;

	public Hl7CodesMappingConfig() {

	}

	public Hl7CodesMappingConfig(Map<String, String> sourceFieldMap) {

		if (!sourceFieldMap.isEmpty()) {
			codesMap.putAll(sourceFieldMap);
			codesList = new ArrayList<String>(sourceFieldMap.values());
		}

	}

	public HashMap<String, String> getcodesMap() {
		return codesMap;
	}

	public void setcodesMap(HashMap<String, String> codesMap) {
		this.codesMap = codesMap;
	}

	public String getCodeValueByKey(String key) {

		String value = codesMap.get(key);
		return value != null ? value : "UNKNOWN";

	}

	public List<String> getCodeValueListByKey(String key) {

		Iterator<Map.Entry<String, String>> entries = codesMap.entrySet().iterator();
		List<String> tmpList = new ArrayList<String>();

		while (entries.hasNext()) {

			Map.Entry<String, String> entry = entries.next();

			if (entry.getKey().trim().toUpperCase().startsWith(key.trim().toUpperCase())) {
				int indx = entry.getKey().indexOf(".") + 1;
				tmpList.add(entry.getKey().trim().substring(indx) + "=" + entry.getValue().trim());

			}

		}

		return tmpList;

	}

}
