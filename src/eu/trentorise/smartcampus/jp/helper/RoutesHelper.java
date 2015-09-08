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
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.model.RouteDescriptor;

public class RoutesHelper {

	public static final String AGENCYID_TRAIN_BZVR = "5";
	public static final String AGENCYID_TRAIN_TNBDG = "6";
	public static final String AGENCYID_TRAIN_TM = "10";
	public static final String AGENCYID_BUS_TRENTO = "12";
	public static final String AGENCYID_BUS_ROVERETO = "16";
	public static final String AGENCYID_BUS_SUBURBAN = "17";
	// public static final String AGENCYID_BUS_SUBURBAN_ZONE_1 = "17_1";
	// public static final String AGENCYID_BUS_SUBURBAN_ZONE_2 = "17_2";
	// public static final String AGENCYID_BUS_SUBURBAN_ZONE_3 = "17_3";
	// public static final String AGENCYID_BUS_SUBURBAN_ZONE_4 = "17_4";
	// public static final String AGENCYID_BUS_SUBURBAN_ZONE_5 = "17_5";
	// public static final String AGENCYID_BUS_SUBURBAN_ZONE_6 = "17_6";

	public static final List<String> AGENCYIDS = Arrays.asList(AGENCYID_BUS_TRENTO, AGENCYID_BUS_ROVERETO, AGENCYID_TRAIN_BZVR,
			AGENCYID_TRAIN_TM, AGENCYID_TRAIN_TNBDG, AGENCYID_BUS_SUBURBAN);

	public static final List<String> AGENCYIDS_BUSES = Arrays.asList(AGENCYID_BUS_TRENTO, AGENCYID_BUS_ROVERETO,
			AGENCYID_BUS_SUBURBAN);

	public static final List<String> AGENCYIDS_TRAINS = Arrays.asList(AGENCYID_TRAIN_BZVR, AGENCYID_TRAIN_TM,
			AGENCYID_TRAIN_TNBDG);

	public static final List<String> AGENCYIDS_TRAINS_TYPED = Arrays.asList(AGENCYID_TRAIN_BZVR, AGENCYID_TRAIN_TNBDG);

	public static Map<String, List<SmartLine>> smartLines = new HashMap<String, List<SmartLine>>();

	public static List<Route> getRoutesList(Context ctx, String[] agencyIds) {
		List<Route> list = new ArrayList<Route>();
		List<RouteDescriptor> routeDescriptorsList = getRouteDescriptorsListFiltered(ctx, agencyIds);
		for (RouteDescriptor r : routeDescriptorsList) {
			list.add(routeDescriptor2route(ctx, r));
		}

		return list;
	}

//	public static List<String> getRoutesIdsList(Context ctx, String[] agencyIds) {
//		List<String> list = new ArrayList<String>();
//		List<RouteDescriptor> routeDescriptorsList = getRouteDescriptorsListFiltered(ctx, agencyIds);
//		for (RouteDescriptor r : routeDescriptorsList) {
//			list.add(routeDescriptor2route(ctx, r).getId().getId());
//		}
//
//		return list;
//	}
	
	public static List<RouteDescriptor> getRouteDescriptorsListFiltered(Context ctx, String[] agencyIds) {
		// if agencyIds are not provided use all
//		if (agencyIds == null || agencyIds.length == 0) {
//			agencyIds = AGENCYIDS.toArray(new String[] {});
//		}
		if (agencyIds == null || agencyIds.length == 0) {
			agencyIds = JPParamsHelper.getAgencyID().toArray(new String[] {});
		}
		List<RouteDescriptor> list = new ArrayList<RouteDescriptor>();
		for (int i = 0; i < agencyIds.length; i++) {
			String agencyId = agencyIds[i];

			if (ROUTES.get(agencyId) == null) {
				continue;
			}

			String[] lines = new String[] {};
			if (AGENCYID_BUS_SUBURBAN.equals(agencyId)) {
				// filter for suburban zones in app parameters!
				lines = ctx.getResources().getStringArray(R.array.smart_check_17_zones);
				lines = filterSuburbanLines(lines);
			}
			//ritorna [null null 3 null null 6]
			List <RouteDescriptor> routes = null;
			//if param agencyid the prendi il parametro, altrimenti prendi tutto
			routes=JPParamsHelper.getRoutesIDByAgencyID(agencyId);
			
			for (RouteDescriptor r : routes) {
				if (AGENCYID_BUS_SUBURBAN.equals(agencyId)) {
					//solo se e' extraurbano
					for (int index = 0; index < lines.length; index++) {
						//per tutte le zone extraurbane
						if (lines[index] != null && validateRouteDescriptor(ctx, agencyId, r, lines[index], index)) {
							list.add(r);
						}
					}
				} else {
					list.add(r);
				}
			}
		}
		return list;
	}

	public static RouteDescriptor getRouteDescriptorByRouteGTFSId(Context context, String agencyId, String routeGTFSId) {
		RouteDescriptor routeDescriptor = null;

		String[] agencyIds = agencyId != null ? new String[] { agencyId } : null;

		for (RouteDescriptor rd : getRouteDescriptorsListFiltered(context, agencyIds)) {
			if (rd.getRouteGTFSId().equalsIgnoreCase(routeGTFSId)) {
				routeDescriptor = rd;
				break;
			}
		}

		return routeDescriptor;
	}

	public static RouteDescriptor getRouteDescriptorByRouteId(Context context, String agencyId, String routeId) {
		RouteDescriptor routeDescriptor = null;

		String[] agencyIds = agencyId != null ? new String[] { agencyId } : null;

		for (RouteDescriptor rd : getRouteDescriptorsListFiltered(context, agencyIds)) {
			if (rd.getRouteId().equalsIgnoreCase(routeId)) {
				routeDescriptor = rd;
				break;
			}
		}

		return routeDescriptor;
	}

//	public static RouteDescriptor getRouteDescriptorByRouteId(Context context, String routeId) {
//		return getRouteDescriptorByRouteId(context, null, routeId);
//	}
//
	public static String getShortNameByRouteIdAndAgencyID(String routeId, String AgencyId) {

		String returnShortName = "";
		List<RouteDescriptor> names = ROUTES.get(AgencyId);
		for (RouteDescriptor desc : names) {
			if (desc.getRouteId().equals(routeId))
				return desc.getShortNameResource();
		}
		return returnShortName;
	}

	public static List<SmartLine> getSmartLines(Context ctx, String agencyId) {
		List<SmartLine> cachedSmartLine = smartLines.get(agencyId);

		if (cachedSmartLine != null) {
			return cachedSmartLine;
		}

		Resources resources = ctx.getResources();

		String[] agencyIds = new String[] { agencyId };
		String[] lines = null;
		TypedArray linesNames = null;
		TypedArray icons = null;
		TypedArray colors = null;

		if (agencyId == AGENCYID_BUS_TRENTO) {
			lines = resources.getStringArray(R.array.smart_check_12_numbers);
			// linesNames =
			// resources.obtainTypedArray(R.array.smart_check_17_names);
			icons = resources.obtainTypedArray(R.array.smart_check_12_icons);
			colors = resources.obtainTypedArray(R.array.smart_check_12_colors);
		} else if (agencyId == AGENCYID_BUS_ROVERETO) {
			lines = resources.getStringArray(R.array.smart_check_16_numbers);
			// linesNames =
			// resources.obtainTypedArray(R.array.smart_check_17_names);
			icons = resources.obtainTypedArray(R.array.smart_check_16_icons);
			colors = resources.obtainTypedArray(R.array.smart_check_16_colors);
		} else if (agencyId == AGENCYID_BUS_SUBURBAN) {
			lines = resources.getStringArray(R.array.smart_check_17_zones);
			// filter for suburban zones in app parameters!
			lines = filterSuburbanLines(lines);
			linesNames = resources.obtainTypedArray(R.array.smart_check_17_names);
			// icons = resources.obtainTypedArray(R.array.smart_check_12_icons);
			colors = resources.obtainTypedArray(R.array.smart_check_17_colors);
		} else {
			return Collections.emptyList();
		}

		// get info from result (busRoutes)
		Map<String, List<String>> singleRoutesShorts = new HashMap<String, List<String>>();
		Map<String, List<String>> singleRoutesLong = new HashMap<String, List<String>>();
		Map<String, List<String>> singleRoutesId = new HashMap<String, List<String>>();
		ArrayList<SmartLine> busLines = new ArrayList<SmartLine>();

		List<Route> routes = RoutesHelper.getRoutesList(ctx, agencyIds);
		Collections.sort(routes, Utils.getRouteComparator());

		// get all-the-routes for a smartline
		for (int index = 0; index < lines.length; index++) {
			String line = lines[index];
			if (line == null) {
				// line not allowed
				continue;
			}

			// put them in the array
			for (Route route : routes) {
				if (validateRoute(ctx, agencyId, route, line, index)) {
					if (singleRoutesShorts.get(line) == null) {
						singleRoutesShorts.put(line, new ArrayList<String>());
						singleRoutesLong.put(line, new ArrayList<String>());
						singleRoutesId.put(line, new ArrayList<String>());
					}
					singleRoutesShorts.get(line).add(route.getRouteShortName());
					singleRoutesLong.get(line).add(route.getRouteLongName());
					singleRoutesId.get(line).add(route.getId().getId());
				}
			}
			SmartLine singleLine = new SmartLine((icons != null ? icons.getDrawable(index) : null),
					(linesNames != null ? linesNames.getString(index) : line), colors.getColor(index, 0),
					singleRoutesShorts.get(line), singleRoutesLong.get(line), singleRoutesId.get(line), agencyId);
			busLines.add(singleLine);
		}

		smartLines.put(agencyId, busLines);

		if (linesNames != null) {
			linesNames.recycle();
		}
		if (icons != null) {
			icons.recycle();
		}
		if (colors != null) {
			colors.recycle();
		}

		return busLines;
	}

	private static Route routeDescriptor2route(Context ctx, RouteDescriptor rd) {
		Route route = new Route();
		Id id = new Id();
		id.setAgency(rd.getAgencyId());
		id.setId(rd.getRouteId());
		route.setId(id);
		route.setRouteShortName(rd.getShortNameResource());
		route.setRouteLongName(ctx.getString(rd.getNameResource()));
		return route;
	}

	private static boolean validateRoute(Context mContext, String agencyId, Route route, String line, int index) {
		boolean contains = false;

		if (AGENCYID_BUS_TRENTO.equals(agencyId)) {
			contains = route.getId().getId().equalsIgnoreCase(line) || route.getId().getId().equalsIgnoreCase(line + "A")
					|| route.getId().getId().equalsIgnoreCase(line + "R");
		} else if (AGENCYID_BUS_ROVERETO.equals(agencyId)) {
			contains = route.getRouteShortName().equals(line);
		} else if (AGENCYID_BUS_SUBURBAN.equals(agencyId)) {
			TypedArray routesArrays = mContext.getResources().obtainTypedArray(R.array.agency_17_zones);
			CharSequence[] routesShortNames = routesArrays.getTextArray(index);
			contains = Arrays.asList(routesShortNames).contains(route.getRouteShortName());
			routesArrays.recycle();
		}

		return contains;
	}

	private static boolean validateRouteDescriptor(Context mContext, String agencyId, RouteDescriptor routeDescriptor,
			String line, int index) {
		boolean contains = false;

		if (AGENCYID_BUS_TRENTO.equals(agencyId)) {
			contains = routeDescriptor.getRouteId().equalsIgnoreCase(line)
					|| routeDescriptor.getRouteId().equalsIgnoreCase(line + "A")
					|| routeDescriptor.getRouteId().equalsIgnoreCase(line + "R");
		} else if (AGENCYID_BUS_ROVERETO.equals(agencyId)) {
			contains = routeDescriptor.getShortNameResource().equals(line);
		} else if (AGENCYID_BUS_SUBURBAN.equals(agencyId)) {
			TypedArray routesArrays = mContext.getResources().obtainTypedArray(R.array.agency_17_zones);
			CharSequence[] routesShortNames = routesArrays.getTextArray(index);
			contains = Arrays.asList(routesShortNames).contains(routeDescriptor.getShortNameResource());
			routesArrays.recycle();
		}

		return contains;
	}

	private static String[] filterSuburbanLines(String[] lines) {
		// lines not allowed are set as null
		List<String> optionsLines = JPParamsHelper.getSuburbanZones();
		if (optionsLines != null && optionsLines.size() > 0) {
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (!optionsLines.contains(line)) {
					lines[i] = null;
				}
			}
		}
		return lines;
	}

	/**
	 * ROUTEDESCRIPTORS!
	 */

	/*
	 * Trento urbano
	 */
	private static final List<RouteDescriptor> RoutesDescriptorsList_12 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "%20AC", R.string.agency_12_route__A, "A", "_A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "%20BC", R.string.agency_12_route__B, "B", "_B"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "Ca", R.string.agency_12_route_Ca, "C", "Ca"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "Cr", R.string.agency_12_route_Cr, "C", "Cr"),
			// new RouteDescriptor(AGENCYID_BUS_TRENTO, "Da",
			// R.string.agency_12_route_Da, "D"),
			// new RouteDescriptor(AGENCYID_BUS_TRENTO, "Dr",
			// R.string.agency_12_route_Dr, "D"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "NPC", R.string.agency_12_route_NPA, "NP", "NPA"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "01A", R.string.agency_12_route_1A, "1", "01A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "01R", R.string.agency_12_route_1R, "1", "01R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "02C", R.string.agency_12_route_02, "2", "02"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "03A", R.string.agency_12_route_03A, "3", "03A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "03R", R.string.agency_12_route_03R, "3", "03R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "04A", R.string.agency_12_route_04A, "4", "04A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "04R", R.string.agency_12_route_04R, "4", "04R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "05A", R.string.agency_12_route_05A, "5", "05A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "05R", R.string.agency_12_route_05R, "5", "05R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "06A", R.string.agency_12_route_06A, "6", "06A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "06R", R.string.agency_12_route_06R, "6", "06R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "07A", R.string.agency_12_route_07A, "7", "07A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "07R", R.string.agency_12_route_07R, "7", "07R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "08A", R.string.agency_12_route_08A, "8", "08A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "08R", R.string.agency_12_route_08R, "8", "08R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "09A", R.string.agency_12_route_09A, "9", "09A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "09R", R.string.agency_12_route_09R, "9", "09R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "10A", R.string.agency_12_route_10A, "10", "10A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "10R", R.string.agency_12_route_10R, "10", "10R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "11A", R.string.agency_12_route_11A, "11", "11A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "11R", R.string.agency_12_route_11R, "11", "11R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "12A", R.string.agency_12_route_12A, "12", "12A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "12R", R.string.agency_12_route_12R, "12", "12R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "13A", R.string.agency_12_route_13A, "13", "13A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "13R", R.string.agency_12_route_13R, "13", "13R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "14A", R.string.agency_12_route_14A, "14", "14A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "14R", R.string.agency_12_route_14R, "14", "14R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "15A", R.string.agency_12_route_15A, "15", "15A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "15R", R.string.agency_12_route_15R, "15", "15R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "16A", R.string.agency_12_route_16A, "16", "16A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "16R", R.string.agency_12_route_16R, "16", "16R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "17A", R.string.agency_12_route_17A, "17", "17A"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "17R", R.string.agency_12_route_17R, "17", "17R"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "FunA", R.string.agency_12_route_FUTSA, "Funivia", "FUTSA"),
			new RouteDescriptor(AGENCYID_BUS_TRENTO, "FunR", R.string.agency_12_route_FUTSR, "Funivia", "FUTSR") });

	/*
	 * Trento - Male'
	 */
	private static final List<RouteDescriptor> RoutesDescriptorsList_10 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_TRAIN_TM, "555", R.string.agency_10_route_555, "RG", "555"),
			new RouteDescriptor(AGENCYID_TRAIN_TM, "556", R.string.agency_10_route_556, "RG","556") });

	/*
	 * Bolzano - Verona
	 */
	private static final List<RouteDescriptor> RoutesDescriptorsList_5 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_TRAIN_BZVR, "BV_R1_G", R.string.agency_5_route_BV_R1_G, "RG", "BV_R1_G"),
			new RouteDescriptor(AGENCYID_TRAIN_BZVR, "BV_R1_R", R.string.agency_5_route_BV_R1_R, "RG", "BV_R1_R") });

	/*
	 * Trento - Bassano D.G.
	 */
	private static final List<RouteDescriptor> RoutesDescriptorsList_6 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_TRAIN_TNBDG, "TB_R2_G", R.string.agency_6_route_TB_R2_G, "RG", "TB_R2_G"),
			new RouteDescriptor(AGENCYID_TRAIN_TNBDG, "TB_R2_R", R.string.agency_6_route_TB_R2_R, "RG", "TB_R2_R") });

	/*
	 * Rovereto urban
	 */
	private static final List<RouteDescriptor> RoutesDescriptorsList_16 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "01A", R.string.agency_16_route_01A_Rov, "1", "01A_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "01R", R.string.agency_16_route_01R_Rov, "1", "01R_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "02A", R.string.agency_16_route_02A_Rov, "2", "02A_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "02R", R.string.agency_16_route_02R_Rov, "2", "02R_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "03A", R.string.agency_16_route_03A_Rov, "3", "03A_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "03R", R.string.agency_16_route_03R_Rov, "3", "03R_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "04A", R.string.agency_16_route_04A_Rov, "4", "04A_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "04R", R.string.agency_16_route_04R_Rov, "4", "04R_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "05R", R.string.agency_16_route_05_Rov, "5", "05A_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "06A", R.string.agency_16_route_06A_Rov, "6", "06A_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "06R", R.string.agency_16_route_06R_Rov, "6", "06R_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "07A", R.string.agency_16_route_07A_Rov, "7", "07A_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "07R", R.string.agency_16_route_07R_Rov, "7", "07R_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "AA", R.string.agency_16_route_AA_Rov, "A", "AA_Rov"),
			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "AR", R.string.agency_16_route_AR_Rov, "A", "AR_Rov"),
//			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "N1A_Rov", R.string.agency_16_route_N1A_Rov, "1"),
//			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "N1R_Rov", R.string.agency_16_route_N1R_Rov, "1"),
//			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "N2A_Rov", R.string.agency_16_route_N2A_Rov, "2"),
//			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "N2R_Rov", R.string.agency_16_route_N2R_Rov, "2"),
//			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "N3A_Rov", R.string.agency_16_route_N3A_Rov, "3"),
//			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "N3R_Rov", R.string.agency_16_route_N3R_Rov, "3"),
//			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "N5R_Rov", R.string.agency_16_route_N5R_Rov, "5"),
//			new RouteDescriptor(AGENCYID_BUS_ROVERETO, "N6_Rov", R.string.agency_16_route_N6_Rov, "6"), 
			});

	/*
	 * Suburban NEWEST: 01092015!!!
	 */
	private static final List<RouteDescriptor> RoutesDescriptorsList_17 = Arrays.asList(new RouteDescriptor[] {
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "110_17_0", R.string.agency_17_route_110_17_0, "301"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "110_17_1", R.string.agency_17_route_110_17_1, "301"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "181_17_0", R.string.agency_17_route_181_17_0, "302"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "181_17_1", R.string.agency_17_route_181_17_1, "302"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "186_17_0", R.string.agency_17_route_186_17_0, "303"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "186_17_1", R.string.agency_17_route_186_17_1, "303"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "231_17_0", R.string.agency_17_route_231_17_0, "305"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "231_17_1", R.string.agency_17_route_231_17_1, "305"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "196_17_0", R.string.agency_17_route_196_17_0, "306"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "196_17_1", R.string.agency_17_route_196_17_1, "306"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "200_17_0", R.string.agency_17_route_200_17_0, "307"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "200_17_1", R.string.agency_17_route_200_17_1, "307"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "220_17_0", R.string.agency_17_route_220_17_0, "310"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "220_17_1", R.string.agency_17_route_220_17_1, "310"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "194_17_0", R.string.agency_17_route_194_17_0, "311"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "194_17_1", R.string.agency_17_route_194_17_1, "311"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "215_17_0", R.string.agency_17_route_215_17_0, "312"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "215_17_1", R.string.agency_17_route_215_17_1, "312"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "203_17_0", R.string.agency_17_route_203_17_0, "314"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "203_17_1", R.string.agency_17_route_203_17_1, "314"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "198_17_0", R.string.agency_17_route_198_17_0, "315"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "198_17_1", R.string.agency_17_route_198_17_1, "315"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "190_17_0", R.string.agency_17_route_190_17_0, "316"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "190_17_1", R.string.agency_17_route_190_17_1, "316"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "235_17_0", R.string.agency_17_route_235_17_0, "318"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "235_17_1", R.string.agency_17_route_235_17_1, "318"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "233_17_0", R.string.agency_17_route_233_17_0, "319"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "233_17_1", R.string.agency_17_route_233_17_1, "319"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "205_17_0", R.string.agency_17_route_205_17_0, "321"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "205_17_1", R.string.agency_17_route_205_17_1, "321"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "634_17_0", R.string.agency_17_route_634_17_0, "332"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "634_17_1", R.string.agency_17_route_634_17_1, "332"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "507_17_0", R.string.agency_17_route_507_17_0, "334"),
////			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "507_17_1", R.string.agency_17_route_507_17_1, "334"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "503_17_0", R.string.agency_17_route_503_17_0, "335"),
////			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "503_17_1", R.string.agency_17_route_503_17_1, "335"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "580_17_0", R.string.agency_17_route_580_17_0, "336"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "580_17_1", R.string.agency_17_route_580_17_1, "336"),

	
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "301A", R.string.agency_17_route_110_17_0, "301", "110_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "301_17_0", R.string.agency_17_route_110_17_1,"301", "110_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "302A", R.string.agency_17_route_181_17_0, "302", "181_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "302R", R.string.agency_17_route_181_17_1, "302", "181_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "303A", R.string.agency_17_route_186_17_0, "303", "186_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "303R", R.string.agency_17_route_186_17_1, "303", "186_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "305A", R.string.agency_17_route_231_17_0, "305", "231_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "305R", R.string.agency_17_route_231_17_1, "305", "231_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "306A", R.string.agency_17_route_196_17_0, "306", "196_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "306R", R.string.agency_17_route_196_17_1, "306", "196_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "307A", R.string.agency_17_route_200_17_0, "307", "200_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "307R", R.string.agency_17_route_200_17_1, "307", "200_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "310INA", R.string.agency_17_route_220_17_0, "310", "220_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "310INR", R.string.agency_17_route_220_17_1, "310", "220_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "311A", R.string.agency_17_route_194_17_0, "311", "194_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "311R", R.string.agency_17_route_194_17_1, "311", "194_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "312A", R.string.agency_17_route_215_17_0, "312", "215_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "312R", R.string.agency_17_route_215_17_1, "312", "215_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "314A", R.string.agency_17_route_203_17_0, "314", "203_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "314R", R.string.agency_17_route_203_17_1, "314", "203_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "315A", R.string.agency_17_route_198_17_0, "315", "198_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "315R", R.string.agency_17_route_198_17_1, "315", "198_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "316AA", R.string.agency_17_route_190_17_0, "316", "190_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "316AR", R.string.agency_17_route_190_17_1, "316", "190_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "318INA", R.string.agency_17_route_235_17_0, "318", "235_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "318INR", R.string.agency_17_route_235_17_1, "318", "235_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "319A", R.string.agency_17_route_233_17_0, "319", "233_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "319R", R.string.agency_17_route_233_17_1, "319", "233_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "321A", R.string.agency_17_route_205_17_0, "321", "205_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "321R", R.string.agency_17_route_205_17_1, "321", "205_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "332A", R.string.agency_17_route_634_17_0, "332", "634_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "332R", R.string.agency_17_route_634_17_1, "332", "634_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "334A", R.string.agency_17_route_507_17_0, "334", "507_17_0"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "334R", R.string.agency_17_route_507_17_1, "334", "507_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "335A", R.string.agency_17_route_503_17_0, "335", "503_17_0"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "335R", R.string.agency_17_route_503_17_1, "335", "503_17_1"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "336AA", R.string.agency_17_route_580_17_0, "336", "580_17_0"),
			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "336AR", R.string.agency_17_route_580_17_1, "336", "580_17_1"),	
	}); 

	/*
	 * Suburban OLDER!!!
	 */
//	private static final List<RouteDescriptor> RoutesDescriptorsList_17 = Arrays.asList(new RouteDescriptor[] {
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "1_17_0", R.string.agency_17_route_1_17_0, "101"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "1_17_1", R.string.agency_17_route_1_17_1, "101"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "3_17_0", R.string.agency_17_route_3_17_0, "102"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "3_17_1", R.string.agency_17_route_3_17_1, "102"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "7_17_0", R.string.agency_17_route_7_17_0, "114"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "7_17_1", R.string.agency_17_route_7_17_1, "114"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "55_17_0", R.string.agency_17_route_55_17_0, "104"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "55_17_1", R.string.agency_17_route_55_17_1, "104"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "60_17_0", R.string.agency_17_route_60_17_0, "105"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "60_17_1", R.string.agency_17_route_60_17_1, "105"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "63_17_0", R.string.agency_17_route_63_17_0, "121"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "63_17_1", R.string.agency_17_route_63_17_1, "121"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "65_17_0", R.string.agency_17_route_65_17_0, "109"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "65_17_1", R.string.agency_17_route_65_17_1, "109"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "67_17_0", R.string.agency_17_route_67_17_0, "111"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "67_17_1", R.string.agency_17_route_67_17_1, "111"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "69_17_0", R.string.agency_17_route_69_17_0, "402"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "69_17_1", R.string.agency_17_route_69_17_1, "402"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "71_17_0", R.string.agency_17_route_71_17_0, "201"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "71_17_1", R.string.agency_17_route_71_17_1, "201"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "73_17_0", R.string.agency_17_route_73_17_0, "216"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "73_17_1", R.string.agency_17_route_73_17_1, "216"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "76_17_0", R.string.agency_17_route_76_17_0, "202"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "76_17_1", R.string.agency_17_route_76_17_1, "202"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "78_17_0", R.string.agency_17_route_78_17_0, "203"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "78_17_1", R.string.agency_17_route_78_17_1, "203"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "80_17_0", R.string.agency_17_route_80_17_0, "206"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "80_17_1", R.string.agency_17_route_80_17_1, "206"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "84_17_0", R.string.agency_17_route_84_17_0, "429"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "84_17_1", R.string.agency_17_route_84_17_1, "429"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "86_17_0", R.string.agency_17_route_86_17_0, "408"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "86_17_1", R.string.agency_17_route_86_17_1, "408"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "90_17_0", R.string.agency_17_route_90_17_0, "415"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "90_17_1", R.string.agency_17_route_90_17_1, "415"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "92_17_0", R.string.agency_17_route_92_17_0, "431"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "92_17_1", R.string.agency_17_route_92_17_1, "431"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "95_17_0", R.string.agency_17_route_95_17_0, "413"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "95_17_1", R.string.agency_17_route_95_17_1, "413"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "96_17_0", R.string.agency_17_route_96_17_0, "428"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "96_17_1", R.string.agency_17_route_96_17_1, "428"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "98_17_0", R.string.agency_17_route_98_17_0, "410"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "98_17_1", R.string.agency_17_route_98_17_1, "410"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "102_17_0", R.string.agency_17_route_102_17_0, "501"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "102_17_1", R.string.agency_17_route_102_17_1, "501"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "106_17_0", R.string.agency_17_route_106_17_0, "504"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "106_17_1", R.string.agency_17_route_106_17_1, "504"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "108_17_0", R.string.agency_17_route_108_17_0, "512"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "108_17_1", R.string.agency_17_route_108_17_1, "512"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "110_17_0", R.string.agency_17_route_110_17_0, "301"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "110_17_1", R.string.agency_17_route_110_17_1, "301"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "112_17_0", R.string.agency_17_route_112_17_0, "503"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "112_17_1", R.string.agency_17_route_112_17_1, "503"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "116_17_0", R.string.agency_17_route_116_17_0, "506"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "116_17_1", R.string.agency_17_route_116_17_1, "506"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "117_17_0", R.string.agency_17_route_117_17_0, "112"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "117_17_1", R.string.agency_17_route_117_17_1, "112"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "120_17_0", R.string.agency_17_route_120_17_0, "103"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "120_17_1", R.string.agency_17_route_120_17_1, "103"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "123_17_0", R.string.agency_17_route_123_17_0, "514"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "123_17_1", R.string.agency_17_route_123_17_1, "514"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "130_17_0", R.string.agency_17_route_130_17_0, "122"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "130_17_1", R.string.agency_17_route_130_17_1, "122"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "132_17_0", R.string.agency_17_route_132_17_0, "409"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "132_17_1", R.string.agency_17_route_132_17_1, "409"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "134_17_0", R.string.agency_17_route_134_17_0, "205"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "134_17_1", R.string.agency_17_route_134_17_1, "205"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "136_17_0", R.string.agency_17_route_136_17_0, "113"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "136_17_1", R.string.agency_17_route_136_17_1, "113"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "138_17_0", R.string.agency_17_route_138_17_0, "404"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "138_17_1", R.string.agency_17_route_138_17_1, "404"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "151_17_0", R.string.agency_17_route_151_17_0, "116"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "151_17_1", R.string.agency_17_route_151_17_1, "116"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "153_17_0", R.string.agency_17_route_153_17_0, "211"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "153_17_1", R.string.agency_17_route_153_17_1, "211"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "155_17_0", R.string.agency_17_route_155_17_0, "210"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "155_17_1", R.string.agency_17_route_155_17_1, "210"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "157_17_0", R.string.agency_17_route_157_17_0, "215"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "157_17_1", R.string.agency_17_route_157_17_1, "215"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "159_17_0", R.string.agency_17_route_159_17_0, "209"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "159_17_1", R.string.agency_17_route_159_17_1, "209"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "161_17_0", R.string.agency_17_route_161_17_0, "208"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "161_17_1", R.string.agency_17_route_161_17_1, "208"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "165_17_0", R.string.agency_17_route_165_17_0, "213"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "165_17_1", R.string.agency_17_route_165_17_1, "213"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "167_17_0", R.string.agency_17_route_167_17_0, "212"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "167_17_1", R.string.agency_17_route_167_17_1, "212"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "169_17_0", R.string.agency_17_route_169_17_0, "406"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "169_17_1", R.string.agency_17_route_169_17_1, "406"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "171_17_0", R.string.agency_17_route_171_17_0, "214"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "171_17_1", R.string.agency_17_route_171_17_1, "214"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "175_17_0", R.string.agency_17_route_175_17_0, "204"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "175_17_1", R.string.agency_17_route_175_17_1, "204"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "177_17_0", R.string.agency_17_route_177_17_0, "418"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "177_17_1", R.string.agency_17_route_177_17_1, "418"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "179_17_0", R.string.agency_17_route_179_17_0, "417"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "179_17_1", R.string.agency_17_route_179_17_1, "417"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "181_17_0", R.string.agency_17_route_181_17_0, "302"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "181_17_1", R.string.agency_17_route_181_17_1, "302"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "184_17_0", R.string.agency_17_route_184_17_0, "407"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "184_17_1", R.string.agency_17_route_184_17_1, "407"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "186_17_0", R.string.agency_17_route_186_17_0, "303"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "186_17_1", R.string.agency_17_route_186_17_1, "303"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "188_17_0", R.string.agency_17_route_188_17_0, "423"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "188_17_1", R.string.agency_17_route_188_17_1, "423"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "190_17_0", R.string.agency_17_route_190_17_0, "316"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "190_17_1", R.string.agency_17_route_190_17_1, "316"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "194_17_0", R.string.agency_17_route_194_17_0, "311"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "194_17_1", R.string.agency_17_route_194_17_1, "311"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "196_17_0", R.string.agency_17_route_196_17_0, "306"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "196_17_1", R.string.agency_17_route_196_17_1, "306"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "198_17_0", R.string.agency_17_route_198_17_0, "315"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "198_17_1", R.string.agency_17_route_198_17_1, "315"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "200_17_0", R.string.agency_17_route_200_17_0, "307"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "200_17_1", R.string.agency_17_route_200_17_1, "307"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "203_17_0", R.string.agency_17_route_203_17_0, "314"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "203_17_1", R.string.agency_17_route_203_17_1, "314"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "205_17_0", R.string.agency_17_route_205_17_0, "321"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "205_17_1", R.string.agency_17_route_205_17_1, "321"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "215_17_0", R.string.agency_17_route_215_17_0, "312"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "215_17_1", R.string.agency_17_route_215_17_1, "312"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "220_17_0", R.string.agency_17_route_220_17_0, "310"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "220_17_1", R.string.agency_17_route_220_17_1, "310"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "227_17_0", R.string.agency_17_route_227_17_0, "507"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "227_17_1", R.string.agency_17_route_227_17_1, "507"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "229_17_0", R.string.agency_17_route_229_17_0, "230"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "229_17_1", R.string.agency_17_route_229_17_1, "230"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "231_17_0", R.string.agency_17_route_231_17_0, "305"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "231_17_1", R.string.agency_17_route_231_17_1, "305"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "233_17_0", R.string.agency_17_route_233_17_0, "319"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "233_17_1", R.string.agency_17_route_233_17_1, "319"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "235_17_0", R.string.agency_17_route_235_17_0, "318"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "235_17_1", R.string.agency_17_route_235_17_1, "318"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "300_17_0", R.string.agency_17_route_300_17_0, "115"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "300_17_1", R.string.agency_17_route_300_17_1, "115"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "309_17_0", R.string.agency_17_route_309_17_0, "425"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "309_17_1", R.string.agency_17_route_309_17_1, "425"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "311_17_0", R.string.agency_17_route_311_17_0, "511"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "311_17_1", R.string.agency_17_route_311_17_1, "511"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "316_17_0", R.string.agency_17_route_316_17_0, "123"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "316_17_1", R.string.agency_17_route_316_17_1, "123"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "320_17_0", R.string.agency_17_route_320_17_0, "217"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "320_17_1", R.string.agency_17_route_320_17_1, "217"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "322_17_0", R.string.agency_17_route_322_17_0, "218"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "322_17_1", R.string.agency_17_route_322_17_1, "218"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "324_17_0", R.string.agency_17_route_324_17_0, "640"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "324_17_1", R.string.agency_17_route_324_17_1, "640"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "326_17_0", R.string.agency_17_route_326_17_0, "642"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "326_17_1", R.string.agency_17_route_326_17_1, "642"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "328_17_0", R.string.agency_17_route_328_17_0, "644"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "328_17_1", R.string.agency_17_route_328_17_1, "644"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "330_17_0", R.string.agency_17_route_330_17_0, "641"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "330_17_1", R.string.agency_17_route_330_17_1, "641"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "332_17_0", R.string.agency_17_route_332_17_0, "646"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "332_17_1", R.string.agency_17_route_332_17_1, "646"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "334_17_0", R.string.agency_17_route_334_17_0, "643"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "334_17_1", R.string.agency_17_route_334_17_1, "643"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "336_17_0", R.string.agency_17_route_336_17_0, "645"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "336_17_1", R.string.agency_17_route_336_17_1, "645"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "340_17_0", R.string.agency_17_route_340_17_0, "107"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "340_17_1", R.string.agency_17_route_340_17_1, "107"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "343_17_0", R.string.agency_17_route_343_17_0, "106"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "343_17_1", R.string.agency_17_route_343_17_1, "106"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "345_17_0", R.string.agency_17_route_345_17_0, "611"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "345_17_1", R.string.agency_17_route_345_17_1, "611"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "354_17_0", R.string.agency_17_route_354_17_0, "624"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "354_17_1", R.string.agency_17_route_354_17_1, "624"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "357_17_0", R.string.agency_17_route_357_17_0, "634"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "357_17_1", R.string.agency_17_route_357_17_1, "634"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "362_17_0", R.string.agency_17_route_362_17_0, "635"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "362_17_1", R.string.agency_17_route_362_17_1, "635"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "364_17_0", R.string.agency_17_route_364_17_0, "632"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "364_17_1", R.string.agency_17_route_364_17_1, "632"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "366_17_0", R.string.agency_17_route_366_17_0, "620"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "366_17_1", R.string.agency_17_route_366_17_1, "620"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "368_17_0", R.string.agency_17_route_368_17_0, "627"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "368_17_1", R.string.agency_17_route_368_17_1, "627"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "371_17_0", R.string.agency_17_route_371_17_0, "631"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "371_17_1", R.string.agency_17_route_371_17_1, "631"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "375_17_0", R.string.agency_17_route_375_17_0, "630"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "375_17_1", R.string.agency_17_route_375_17_1, "630"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "378_17_0", R.string.agency_17_route_378_17_0, "633"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "378_17_1", R.string.agency_17_route_378_17_1, "633"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "391_17_0", R.string.agency_17_route_391_17_0, "701"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "391_17_1", R.string.agency_17_route_391_17_1, "701"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "395_17_0", R.string.agency_17_route_395_17_0, "615"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "395_17_1", R.string.agency_17_route_395_17_1, "615"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "397_17_0", R.string.agency_17_route_397_17_0, "623"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "397_17_1", R.string.agency_17_route_397_17_1, "623"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "401_17_0", R.string.agency_17_route_401_17_0, "625"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "401_17_1", R.string.agency_17_route_401_17_1, "625"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "417_17_0", R.string.agency_17_route_417_17_0, "863"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "417_17_1", R.string.agency_17_route_417_17_1, "863"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "420_17_0", R.string.agency_17_route_420_17_0, "861"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "420_17_1", R.string.agency_17_route_420_17_1, "861"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "423_17_0", R.string.agency_17_route_423_17_0, "862"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "423_17_1", R.string.agency_17_route_423_17_1, "862"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "446_17_0", R.string.agency_17_route_446_17_0, "119"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "446_17_1", R.string.agency_17_route_446_17_1, "119"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "494_17_0", R.string.agency_17_route_494_17_0, "403"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "494_17_1", R.string.agency_17_route_494_17_1, "403"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "503_17_0", R.string.agency_17_route_503_17_0, "335"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "503_17_1", R.string.agency_17_route_503_17_1, "335"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "507_17_0", R.string.agency_17_route_507_17_0, "334"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "507_17_1", R.string.agency_17_route_507_17_1, "334"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "520_17_0", R.string.agency_17_route_520_17_0, "401"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "520_17_1", R.string.agency_17_route_520_17_1, "401"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "522_17_0", R.string.agency_17_route_522_17_0, "405"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "522_17_1", R.string.agency_17_route_522_17_1, "405"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "549_17_0", R.string.agency_17_route_549_17_0, "461"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "549_17_1", R.string.agency_17_route_549_17_1, "461"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "551_17_0", R.string.agency_17_route_551_17_0, "462"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "551_17_1", R.string.agency_17_route_551_17_1, "462"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "552_17_0", R.string.agency_17_route_552_17_0, "466"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "552_17_1", R.string.agency_17_route_552_17_1, "466"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "554_17_0", R.string.agency_17_route_554_17_0, "464"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "554_17_1", R.string.agency_17_route_554_17_1, "464"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "556_17_0", R.string.agency_17_route_556_17_0, "463"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "556_17_1", R.string.agency_17_route_556_17_1, "463"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "562_17_0", R.string.agency_17_route_562_17_0, "465"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "562_17_1", R.string.agency_17_route_562_17_1, "465"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "565_17_0", R.string.agency_17_route_565_17_0, "236"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "565_17_1", R.string.agency_17_route_565_17_1, "236"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "577_17_0", R.string.agency_17_route_577_17_0, "419"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "577_17_1", R.string.agency_17_route_577_17_1, "419"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "580_17_0", R.string.agency_17_route_580_17_0, "336"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "580_17_1", R.string.agency_17_route_580_17_1, "336"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "604_17_0", R.string.agency_17_route_604_17_0, "120"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "604_17_1", R.string.agency_17_route_604_17_1, "120"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "618_17_0", R.string.agency_17_route_618_17_0, "502"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "618_17_1", R.string.agency_17_route_618_17_1, "502"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "634_17_0", R.string.agency_17_route_634_17_0, "332"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "634_17_1", R.string.agency_17_route_634_17_1, "332"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "636_17_0", R.string.agency_17_route_636_17_0, "231"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "636_17_1", R.string.agency_17_route_636_17_1, "231"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "638_17_0", R.string.agency_17_route_638_17_0, "140"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "638_17_1", R.string.agency_17_route_638_17_1, "140"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "640_17_0", R.string.agency_17_route_640_17_0, "468"),
//			new RouteDescriptor(AGENCYID_BUS_SUBURBAN, "640_17_1", R.string.agency_17_route_640_17_1, "468") }); 	
	public static final Map<String, List<RouteDescriptor>> ROUTES = new HashMap<String, List<RouteDescriptor>>() {
		private static final long serialVersionUID = 8472504007546826470L;
		{
			put(AGENCYID_BUS_TRENTO, RoutesDescriptorsList_12);
			put(AGENCYID_TRAIN_TM, RoutesDescriptorsList_10);
			put(AGENCYID_TRAIN_BZVR, RoutesDescriptorsList_5);
			put(AGENCYID_TRAIN_TNBDG, RoutesDescriptorsList_6);
			put(AGENCYID_BUS_ROVERETO, RoutesDescriptorsList_16);
			put(AGENCYID_BUS_SUBURBAN, RoutesDescriptorsList_17);
		}
	};

}
