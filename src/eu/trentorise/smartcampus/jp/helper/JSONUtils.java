/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.jp.helper;

import java.util.Map;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.NopAnnotationIntrospector;
import org.codehaus.jackson.type.TypeReference;

public class JSONUtils {
	
	private static ObjectMapper fullMapper = new ObjectMapper();
	
	static {
		fullMapper.setAnnotationIntrospector(NopAnnotationIntrospector.nopInstance());
		fullMapper.configure(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true);
		fullMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		fullMapper.configure(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true);

		fullMapper.configure(SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING, true);
		fullMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> convertJSONToData(String o) {
		try {
			return fullMapper.readValue(o, Map.class);
		} catch (Exception e) {
			return null;
		}
	}

	public static <T> T convert(Object o, TypeReference<T> type) {
		try {
			return fullMapper.convertValue(o, type);
		} catch (Exception e) {
			return null;
		}
	}

	public static <T> T convertJSON(String s, TypeReference<T> type) {
		try {
			return fullMapper.readValue(s, type);
		} catch (Exception e) {
			return null;
		}
	}

	public static String convertToJSON(Object data) {
		try {
			return fullMapper.writeValueAsString(data);
		} catch (Exception e) {
			return "";
		}
	}

	public static ObjectMapper getFullMapper() {
		return fullMapper;
	}
}
