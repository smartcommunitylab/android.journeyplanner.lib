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
package eu.trentorise.smartcampus.jp.custom.data;

import it.sayservice.platform.smartplanner.data.message.journey.RecurrentJourneyParameters;

import java.io.Serializable;

public class BasicRecurrentJourneyParameters implements Serializable {
	private static final long serialVersionUID = -4995063946056861304L;

	private String clientId;
	private String name;
	private RecurrentJourneyParameters data;
	private boolean monitor;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public RecurrentJourneyParameters getData() {
		return data;
	}

	public void setData(RecurrentJourneyParameters content) {
		this.data = content;
	}

	public boolean isMonitor() {
		return monitor;
	}

	public void setMonitor(boolean monitored) {
		this.monitor = monitored;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
