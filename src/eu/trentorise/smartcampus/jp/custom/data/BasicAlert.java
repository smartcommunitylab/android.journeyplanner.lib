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

import it.sayservice.platform.smartplanner.data.message.alerts.Alert;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertType;

public class BasicAlert {

	private AlertType type;
	private Alert content;

	public BasicAlert(AlertType type, Alert content) {
		this.type = type;
		this.content = content;
	}
	
	public AlertType getType() {
		return this.type;
	}

	public void setType(AlertType type) {
		this.type = type;
	}

	public Alert getContent() {
		return this.content;
	}

	public void setContent(Alert content) {
		this.content = content;
	}

}
