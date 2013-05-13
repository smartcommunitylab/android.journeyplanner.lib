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

package eu.trentorise.smartcampus.jp.custom.map;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;

import java.util.Collection;

import android.app.Activity;

import com.google.android.maps.MapView;

import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public abstract class StopsMapLoadProcessor extends AbstractAsyncTaskProcessor<Void, Collection<SmartCheckStop>> {

	protected StopsItemizedOverlay overlay = null;
	protected MapView mapView = null;

	public StopsMapLoadProcessor(Activity activity, StopsItemizedOverlay overlay, MapView mapView) {
		super(activity);
		this.overlay = overlay;
		this.mapView = mapView;
	}

	@Override
	public Collection<SmartCheckStop> performAction(Void... params) throws SecurityException, Exception {
		return getObjects();
	}

	@Override
	public void handleResult(Collection<SmartCheckStop> objects) {
		if (objects != null) {
			for (SmartCheckStop o : objects) {
				overlay.addOverlay(o);
			}
			overlay.populateAll();
			mapView.invalidate();
		}
	}

	protected abstract Collection<SmartCheckStop> getObjects() throws SecurityException, Exception;

}
