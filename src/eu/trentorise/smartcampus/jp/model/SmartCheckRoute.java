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
package eu.trentorise.smartcampus.jp.model;

import java.util.List;
import java.util.Map;

import eu.trentorise.smartcampus.storage.BasicObject;

public class SmartCheckRoute extends BasicObject {
	private static final long serialVersionUID = -4144883474597868679L;

	private String route;
	private String name;
	private List<SmartCheckTime> times;
	private Map<String, Map<String, String>> delays;

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SmartCheckTime> getTimes() {
		return times;
	}

	public void setTimes(List<SmartCheckTime> times) {
		this.times = times;
	}

	public Map<String, Map<String, String>> getDelays() {
		return delays;
	}

	public void setDelays(Map<String, Map<String, String>> delays) {
		this.delays = delays;
	}

}
