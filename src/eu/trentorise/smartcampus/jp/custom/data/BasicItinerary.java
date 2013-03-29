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

import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.Position;

import java.io.Serializable;

public class BasicItinerary implements Serializable {
	
	private static final long serialVersionUID = -6831267767135882489L;

	private String clientId;
	private Itinerary data;
	private boolean monitor;
	private Position originalFrom;
	private Position originalTo;
	private String name;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Itinerary getData() {
		return data;
	}

	public void setData(Itinerary content) {
		this.data = content;
	}

	public boolean isMonitor() {
		return monitor;
	}

	public void setMonitor(boolean monitored) {
		this.monitor = monitored;
	}

	public Position getOriginalFrom() {
		return originalFrom;
	}

	public void setOriginalFrom(Position originalFrom) {
		this.originalFrom = originalFrom;
	}

	public Position getOriginalTo() {
		return originalTo;
	}

	public void setOriginalTo(Position originalTo) {
		this.originalTo = originalTo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
