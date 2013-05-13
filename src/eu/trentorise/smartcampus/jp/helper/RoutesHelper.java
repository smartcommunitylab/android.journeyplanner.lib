package eu.trentorise.smartcampus.jp.helper;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Id;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.SparseArray;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.model.RouteDescriptor;

public class RoutesHelper {

	public static final int AGENCYID_BUS = 12;
	public static final int AGENCYID_TRAIN_TM = 10;
	public static final int AGENCYID_TRAIN_BZVR = 5;
	public static final int AGENCYID_TRAIN_TNBDG = 6;

	public static List<Route> getRoutesList(Context ctx, int[] agencyIds) {
		List<Route> list = new ArrayList<Route>();
		for (RouteDescriptor r : getRouteDescriptorsList(agencyIds)) {
			list.add(routeDescriptor2route(ctx, r));
		}
		return list;
	}

	public static List<RouteDescriptor> getRouteDescriptorsList(int[] agencyIds) {
		// if agencyIds are not provided use all
		if (agencyIds == null || agencyIds.length == 0) {
			agencyIds = new int[] { AGENCYID_BUS, AGENCYID_TRAIN_BZVR, AGENCYID_TRAIN_TM, AGENCYID_TRAIN_TNBDG };
		}

		List<RouteDescriptor> list = new ArrayList<RouteDescriptor>();
		for (int i = 0; i < agencyIds.length; i++) {
			for (RouteDescriptor r : ROUTES.get(agencyIds[i])) {
				list.add(r);
			}
		}
		return list;
	}

	public static RouteDescriptor getRouteDescriptorByRouteId(String routeId) {
		RouteDescriptor routeDescriptor = null;

		for (RouteDescriptor rd : getRouteDescriptorsList(null)) {
			if (rd.getRouteId().equalsIgnoreCase(routeId)) {
				routeDescriptor = rd;
				break;
			}
		}
		
		return routeDescriptor;
	}

	public static List<SmartLine> getSmartLines(Context ctx, String agencyId) {
		List<Route> list = new ArrayList<Route>();
		Resources resources = ctx.getResources();
		String[] lines = resources.getStringArray(R.array.smart_checks_bus_number);
		TypedArray icons = resources.obtainTypedArray(R.array.smart_checks_bus_icons);
		TypedArray colors = resources.obtainTypedArray(R.array.smart_checks_bus_color);

		// get info from result (busRoutes)
		Map<String, List<String>> singleRoutesShorts = new HashMap<String, List<String>>();
		Map<String, List<String>> singleRoutesLong = new HashMap<String, List<String>>();
		Map<String, List<String>> singleRoutesId = new HashMap<String, List<String>>();
		ArrayList<SmartLine> busLines = new ArrayList<SmartLine>();

		list = RoutesHelper.getRoutesList(ctx, new int[] { Integer.parseInt(agencyId) });
		Collections.sort(list, Utils.getRouteComparator());

		// get all-the-routes for a smartline
		for (int index = 0; index < lines.length; index++) {
			// put them in the array
			for (Route route : list) {
				//
				if ((route.getId().getId().toUpperCase().compareTo(lines[index].toUpperCase()) == 0)
						|| route.getId().getId().toUpperCase().compareTo(lines[index].toUpperCase() + "R") == 0
						|| route.getId().getId().toUpperCase().compareTo(lines[index].toUpperCase() + "A") == 0) {
					if (singleRoutesShorts.get(lines[index]) == null) {
						singleRoutesShorts.put(lines[index], new ArrayList<String>());
						singleRoutesLong.put(lines[index], new ArrayList<String>());
						singleRoutesId.put(lines[index], new ArrayList<String>());

					}
					singleRoutesShorts.get(lines[index]).add(route.getRouteShortName());
					singleRoutesLong.get(lines[index]).add(route.getRouteLongName());
					singleRoutesId.get(lines[index]).add(route.getId().getId());

				}
			}
			SmartLine singleLine = new SmartLine(icons.getDrawable(index), lines[index], colors.getColor(index, 0),
					singleRoutesShorts.get(lines[index]), singleRoutesLong.get(lines[index]), singleRoutesId.get(lines[index]));
			busLines.add(singleLine);
		}
		return busLines;
	}

	private static Route routeDescriptor2route(Context ctx, RouteDescriptor rd) {
		Route route = new Route();
		Id id = new Id();
		id.setAgency(Integer.toString(rd.getAgencyId()));
		id.setId(rd.getRouteId());
		route.setId(id);
		route.setRouteShortName(rd.getShortNameResource());
		route.setRouteLongName(ctx.getString(rd.getNameResource()));
		return route;
	}

	private static final List<RouteDescriptor> RoutesDescriptorsList_12 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_BUS, "_A", R.string.agency_12_route__A, "A"),
			new RouteDescriptor(AGENCYID_BUS, "_B", R.string.agency_12_route__B, "B"),
			new RouteDescriptor(AGENCYID_BUS, "C", R.string.agency_12_route_C, "C"),
			new RouteDescriptor(AGENCYID_BUS, "Da", R.string.agency_12_route_Da, "D"),
			new RouteDescriptor(AGENCYID_BUS, "Dr", R.string.agency_12_route_Dr, "D"),
			new RouteDescriptor(AGENCYID_BUS, "NPA", R.string.agency_12_route_NPA, "NP"),
			new RouteDescriptor(AGENCYID_BUS, "01", R.string.agency_12_route_01, "1"),
			new RouteDescriptor(AGENCYID_BUS, "02", R.string.agency_12_route_02, "2"),
			new RouteDescriptor(AGENCYID_BUS, "03A", R.string.agency_12_route_03A, "3"),
			new RouteDescriptor(AGENCYID_BUS, "03R", R.string.agency_12_route_03R, "3"),
			new RouteDescriptor(AGENCYID_BUS, "04A", R.string.agency_12_route_04A, "4"),
			new RouteDescriptor(AGENCYID_BUS, "04R", R.string.agency_12_route_04R, "4"),
			new RouteDescriptor(AGENCYID_BUS, "05A", R.string.agency_12_route_05A, "5"),
			new RouteDescriptor(AGENCYID_BUS, "05R", R.string.agency_12_route_05R, "5"),
			new RouteDescriptor(AGENCYID_BUS, "06A", R.string.agency_12_route_06A, "6"),
			new RouteDescriptor(AGENCYID_BUS, "06R", R.string.agency_12_route_06R, "6"),
			new RouteDescriptor(AGENCYID_BUS, "07A", R.string.agency_12_route_07A, "7"),
			new RouteDescriptor(AGENCYID_BUS, "07R", R.string.agency_12_route_07R, "7"),
			new RouteDescriptor(AGENCYID_BUS, "08A", R.string.agency_12_route_08A, "8"),
			new RouteDescriptor(AGENCYID_BUS, "08R", R.string.agency_12_route_08R, "8"),
			new RouteDescriptor(AGENCYID_BUS, "09A", R.string.agency_12_route_09A, "9"),
			new RouteDescriptor(AGENCYID_BUS, "09R", R.string.agency_12_route_09R, "9"),
			new RouteDescriptor(AGENCYID_BUS, "10A", R.string.agency_12_route_10A, "10"),
			new RouteDescriptor(AGENCYID_BUS, "10R", R.string.agency_12_route_10R, "10"),
			new RouteDescriptor(AGENCYID_BUS, "11A", R.string.agency_12_route_11A, "11"),
			new RouteDescriptor(AGENCYID_BUS, "11R", R.string.agency_12_route_11R, "11"),
			new RouteDescriptor(AGENCYID_BUS, "12A", R.string.agency_12_route_12A, "12"),
			new RouteDescriptor(AGENCYID_BUS, "12R", R.string.agency_12_route_12R, "12"),
			new RouteDescriptor(AGENCYID_BUS, "13A", R.string.agency_12_route_13A, "13"),
			new RouteDescriptor(AGENCYID_BUS, "13R", R.string.agency_12_route_13R, "13"),
			new RouteDescriptor(AGENCYID_BUS, "14A", R.string.agency_12_route_14A, "14"),
			new RouteDescriptor(AGENCYID_BUS, "14R", R.string.agency_12_route_14R, "14"),
			new RouteDescriptor(AGENCYID_BUS, "15A", R.string.agency_12_route_15A, "15"),
			new RouteDescriptor(AGENCYID_BUS, "15R", R.string.agency_12_route_15R, "15"),
			new RouteDescriptor(AGENCYID_BUS, "16A", R.string.agency_12_route_16A, "16"),
			new RouteDescriptor(AGENCYID_BUS, "16R", R.string.agency_12_route_16R, "16"),
			new RouteDescriptor(AGENCYID_BUS, "17A", R.string.agency_12_route_17A, "17"),
			new RouteDescriptor(AGENCYID_BUS, "17R", R.string.agency_12_route_17R, "17") });

	private static final List<RouteDescriptor> RoutesDescriptorsList_10 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_TRAIN_TM, "555", R.string.agency_10_route_555, "RG"),
			new RouteDescriptor(AGENCYID_TRAIN_TM, "556", R.string.agency_10_route_556, "RG") });

	private static final List<RouteDescriptor> RoutesDescriptorsList_5 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_TRAIN_BZVR, "BV_R1_G", R.string.agency_5_route_BV_R1_G, "RG"),
			new RouteDescriptor(AGENCYID_TRAIN_BZVR, "BV_R1_R", R.string.agency_5_route_BV_R1_R, "RG") });

	private static final List<RouteDescriptor> RoutesDescriptorsList_6 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_TRAIN_TNBDG, "TB_R2_G", R.string.agency_6_route_TB_R2_G, "RG"),
			new RouteDescriptor(AGENCYID_TRAIN_TNBDG, "TB_R2_R", R.string.agency_6_route_TB_R2_R, "RG") });

	public static final SparseArray<List<RouteDescriptor>> ROUTES = new SparseArray<List<RouteDescriptor>>() {
		{
			put(AGENCYID_BUS, RoutesDescriptorsList_12);
			put(AGENCYID_TRAIN_TM, RoutesDescriptorsList_10);
			put(AGENCYID_TRAIN_BZVR, RoutesDescriptorsList_5);
			put(AGENCYID_TRAIN_TNBDG, RoutesDescriptorsList_6);
		}
	};

}
