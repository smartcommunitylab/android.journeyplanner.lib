package eu.trentorise.smartcampus.jp.helper;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Id;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
	public static final String AGENCYID_BUS = "12";
	public static final String AGENCYID_BUS_EXTRAURBAN = "17";
	public static final String AGENCYID_BUS_EXTRAURBAN_ZONE_1 = "17_1";
	public static final String AGENCYID_BUS_EXTRAURBAN_ZONE_2 = "17_2";
	public static final String AGENCYID_BUS_EXTRAURBAN_ZONE_3 = "17_3";
	public static final String AGENCYID_BUS_EXTRAURBAN_ZONE_4 = "17_4";
	public static final String AGENCYID_BUS_EXTRAURBAN_ZONE_5 = "17_5";
	public static final String AGENCYID_BUS_EXTRAURBAN_ZONE_6 = "17_6";

	public static final String[] AGENCYIDS = new String[] { AGENCYID_BUS, AGENCYID_TRAIN_BZVR, AGENCYID_TRAIN_TM,
			AGENCYID_TRAIN_TNBDG, AGENCYID_BUS_EXTRAURBAN };

	public static Map<String, List<SmartLine>> smartLines = new HashMap<String, List<SmartLine>>();

	public static List<Route> getRoutesList(Context ctx, String[] agencyIds) {
		List<Route> list = new ArrayList<Route>();
		for (RouteDescriptor r : getRouteDescriptorsList(agencyIds)) {
			list.add(routeDescriptor2route(ctx, r));
		}
		return list;
	}

	public static List<RouteDescriptor> getRouteDescriptorsList(String[] agencyIds) {
		// if agencyIds are not provided use all
		if (agencyIds == null || agencyIds.length == 0) {
			agencyIds = AGENCYIDS;
		}

		List<RouteDescriptor> list = new ArrayList<RouteDescriptor>();
		for (int i = 0; i < agencyIds.length; i++) {
			if (ROUTES.get(agencyIds[i]) != null) {
				for (RouteDescriptor r : ROUTES.get(agencyIds[i])) {
					list.add(r);
				}
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
		List<SmartLine> cachedSmartLine = smartLines.get(agencyId);

		if (cachedSmartLine != null) {
			return cachedSmartLine;
		}

		Resources resources = ctx.getResources();

		String[] lines = null;
		TypedArray icons = null;
		TypedArray colors = null;

		if (agencyId == AGENCYID_BUS) {
			lines = resources.getStringArray(R.array.smart_check_12_numbers);
			icons = resources.obtainTypedArray(R.array.smart_check_12_icons);
			colors = resources.obtainTypedArray(R.array.smart_check_12_colors);
		} else if (agencyId == AGENCYID_BUS_EXTRAURBAN) {
			lines = resources.getStringArray(R.array.smart_check_17_zones);
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

		List<Route> list = RoutesHelper.getRoutesList(ctx, new String[] { agencyId });
		Collections.sort(list, Utils.getRouteComparator());

		// get all-the-routes for a smartline
		for (int index = 0; index < lines.length; index++) {
			// put them in the array
			for (Route route : list) {
				//
				if ((route.getId().getId().toUpperCase(Locale.getDefault()).compareTo(lines[index].toUpperCase()) == 0)
						|| route.getId().getId().toUpperCase(Locale.getDefault()).compareTo(lines[index].toUpperCase() + "R") == 0
						|| route.getId().getId().toUpperCase(Locale.getDefault()).compareTo(lines[index].toUpperCase() + "A") == 0) {
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
			SmartLine singleLine = new SmartLine((icons != null ? icons.getDrawable(index) : null), lines[index],
					colors.getColor(index, 0), singleRoutesShorts.get(lines[index]), singleRoutesLong.get(lines[index]),
					singleRoutesId.get(lines[index]));
			busLines.add(singleLine);
		}

		smartLines.put(agencyId, busLines);

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

	/*
	 * Trento urbano
	 */
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

	/*
	 * Trento - Male'
	 */
	private static final List<RouteDescriptor> RoutesDescriptorsList_10 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_TRAIN_TM, "555", R.string.agency_10_route_555, "RG"),
			new RouteDescriptor(AGENCYID_TRAIN_TM, "556", R.string.agency_10_route_556, "RG") });

	/*
	 * Bolzano - Verona
	 */
	private static final List<RouteDescriptor> RoutesDescriptorsList_5 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_TRAIN_BZVR, "BV_R1_G", R.string.agency_5_route_BV_R1_G, "RG"),
			new RouteDescriptor(AGENCYID_TRAIN_BZVR, "BV_R1_R", R.string.agency_5_route_BV_R1_R, "RG") });

	/*
	 * Trento - Bassano D.G.
	 */
	private static final List<RouteDescriptor> RoutesDescriptorsList_6 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_TRAIN_TNBDG, "TB_R2_G", R.string.agency_6_route_TB_R2_G, "RG"),
			new RouteDescriptor(AGENCYID_TRAIN_TNBDG, "TB_R2_R", R.string.agency_6_route_TB_R2_R, "RG") });

	/*
	 * Trento extraurbano
	 */
	private static final List<RouteDescriptor> RoutesDescriptorsList_17_zone_1 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "243_ExUr", R.string.agency_17_route_243_ExUr, "101"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "244_ExUr", R.string.agency_17_route_244_ExUr, "101"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "6_ExUr", R.string.agency_17_route_6_ExUr, "102"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "3_ExUr", R.string.agency_17_route_3_ExUr, "102"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "121_ExUr", R.string.agency_17_route_121_ExUr, "103"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "120_ExUr", R.string.agency_17_route_120_ExUr, "103"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "55_ExUr", R.string.agency_17_route_55_ExUr, "104"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "56_ExUr", R.string.agency_17_route_56_ExUr, "104"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "62_ExUr", R.string.agency_17_route_62_ExUr, "105"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "60_ExUr", R.string.agency_17_route_60_ExUr, "105"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "343_ExUr", R.string.agency_17_route_343_ExUr, "106"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "344_ExUr", R.string.agency_17_route_344_ExUr, "106"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "342_ExUr", R.string.agency_17_route_342_ExUr, "107"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "340_ExUr", R.string.agency_17_route_340_ExUr, "107"),
			// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "242_ExUr",
			// R.string.agency_17_route_242_ExUr, "108"),
			// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "241_ExUr",
			// R.string.agency_17_route_241_ExUr, "108"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "65_ExUr", R.string.agency_17_route_65_ExUr, "109"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "66_ExUr", R.string.agency_17_route_66_ExUr, "109"),
			// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "240_ExUr",
			// R.string.agency_17_route_240_ExUr, "110"),
			// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "239_ExUr",
			// R.string.agency_17_route_239_ExUr, "110"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "67_ExUr", R.string.agency_17_route_67_ExUr, "111"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "68_ExUr", R.string.agency_17_route_68_ExUr, "111"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "117_ExUr", R.string.agency_17_route_117_ExUr, "112"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "118_ExUr", R.string.agency_17_route_118_ExUr, "112"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "8_ExUr", R.string.agency_17_route_8_ExUr, "114"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "7_ExUr", R.string.agency_17_route_7_ExUr, "114"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "300_ExUr", R.string.agency_17_route_300_ExUr, "115"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "307_ExUr", R.string.agency_17_route_307_ExUr, "115"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "151_ExUr", R.string.agency_17_route_151_ExUr, "116"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "152_ExUr", R.string.agency_17_route_152_ExUr, "116"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "611_ExUr", R.string.agency_17_route_611_ExUr, "120"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "613_ExUr", R.string.agency_17_route_613_ExUr, "120"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "64_ExUr", R.string.agency_17_route_64_ExUr, "121"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "63_ExUr", R.string.agency_17_route_63_ExUr, "121"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "289_ExUr", R.string.agency_17_route_289_ExUr, "122"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "288_ExUr", R.string.agency_17_route_288_ExUr, "122"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "252_ExUr", R.string.agency_17_route_252_ExUr, "123"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "253_ExUr", R.string.agency_17_route_253_ExUr, "123"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "638_ExUr", R.string.agency_17_route_638_ExUr, "140"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "639_ExUr", R.string.agency_17_route_639_ExUr, "140") });

	private static final List<RouteDescriptor> RoutesDescriptorsList_17_zone_2 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "71_ExUr", R.string.agency_17_route_71_ExUr, "201"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "72_ExUr", R.string.agency_17_route_72_ExUr, "201"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "238_ExUr", R.string.agency_17_route_238_ExUr, "202"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "237_ExUr", R.string.agency_17_route_237_ExUr, "202"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "79_ExUr", R.string.agency_17_route_79_ExUr, "203"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "78_ExUr", R.string.agency_17_route_78_ExUr, "203"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "175_ExUr", R.string.agency_17_route_175_ExUr, "204"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "176_ExUr", R.string.agency_17_route_176_ExUr, "204"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "134_ExUr", R.string.agency_17_route_134_ExUr, "205"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "135_ExUr", R.string.agency_17_route_135_ExUr, "205"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "80_ExUr", R.string.agency_17_route_80_ExUr, "206"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "81_ExUr", R.string.agency_17_route_81_ExUr, "206"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "287_ExUr", R.string.agency_17_route_287_ExUr, "208"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "286_ExUr", R.string.agency_17_route_286_ExUr, "208"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "159_ExUr", R.string.agency_17_route_159_ExUr, "209"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "160_ExUr", R.string.agency_17_route_160_ExUr, "209"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "155_ExUr", R.string.agency_17_route_155_ExUr, "210"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "156_ExUr", R.string.agency_17_route_156_ExUr, "210"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "153_ExUr", R.string.agency_17_route_153_ExUr, "211"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "154_ExUr", R.string.agency_17_route_154_ExUr, "211"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "168_ExUr", R.string.agency_17_route_168_ExUr, "212"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "167_ExUr", R.string.agency_17_route_167_ExUr, "212"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "247_ExUr", R.string.agency_17_route_247_ExUr, "214"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "245_ExUr", R.string.agency_17_route_245_ExUr, "214"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "158_ExUr", R.string.agency_17_route_158_ExUr, "215"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "157_ExUr", R.string.agency_17_route_157_ExUr, "215"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "74_ExUr", R.string.agency_17_route_74_ExUr, "216"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "73_ExUr", R.string.agency_17_route_73_ExUr, "216"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "321_ExUr", R.string.agency_17_route_321_ExUr, "217"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "320_ExUr", R.string.agency_17_route_320_ExUr, "217"),
			// 218?
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "229_ExUr", R.string.agency_17_route_229_ExUr, "230"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "230_ExUr", R.string.agency_17_route_230_ExUr, "230"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "637_ExUr", R.string.agency_17_route_637_ExUr, "231"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "636_ExUr", R.string.agency_17_route_636_ExUr, "231"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "565_ExUr", R.string.agency_17_route_565_ExUr, "236"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "566_ExUr", R.string.agency_17_route_566_ExUr, "236"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "420_ExUr", R.string.agency_17_route_420_ExUr, "861"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "423_ExUr", R.string.agency_17_route_423_ExUr, "862"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "417_ExUr", R.string.agency_17_route_417_ExUr, "863"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "583_ExUr", R.string.agency_17_route_583_ExUr, "863")
	// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "418_ExUr",
	// R.string.agency_17_route_418_ExUr, "864")
			});

	private static final List<RouteDescriptor> RoutesDescriptorsList_17_zone_3 = Arrays.asList(new RouteDescriptor[] {

			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "110_ExUr", R.string.agency_17_route_110_ExUr, "301"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "111_ExUr", R.string.agency_17_route_111_ExUr, "301"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "181_ExUr", R.string.agency_17_route_181_ExUr, "302"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "182_ExUr", R.string.agency_17_route_182_ExUr, "302"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "187_ExUr", R.string.agency_17_route_187_ExUr, "303"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "186_ExUr", R.string.agency_17_route_186_ExUr, "303"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "231_ExUr", R.string.agency_17_route_231_ExUr, "305"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "232_ExUr", R.string.agency_17_route_232_ExUr, "305"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "196_ExUr", R.string.agency_17_route_196_ExUr, "306"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "197_ExUr", R.string.agency_17_route_197_ExUr, "306"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "201_ExUr", R.string.agency_17_route_201_ExUr, "307"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "200_ExUr", R.string.agency_17_route_200_ExUr, "307"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "314_ExUr", R.string.agency_17_route_314_ExUr, "310"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "315_ExUr", R.string.agency_17_route_315_ExUr, "310"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "194_ExUr", R.string.agency_17_route_194_ExUr, "311"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "195_ExUr", R.string.agency_17_route_195_ExUr, "311"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "215_ExUr", R.string.agency_17_route_215_ExUr, "312"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "216_ExUr", R.string.agency_17_route_216_ExUr, "312"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "203_ExUr", R.string.agency_17_route_203_ExUr, "314"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "204_ExUr", R.string.agency_17_route_204_ExUr, "314"),
			// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "198_ExUr",
			// R.string.agency_17_route_198_ExUr, "315"),
			// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "199_ExUr",
			// R.string.agency_17_route_199_ExUr, "315"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "191_ExUr", R.string.agency_17_route_191_ExUr, "316"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "190_ExUr", R.string.agency_17_route_190_ExUr, "316"),
			// 318?
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "233_ExUr", R.string.agency_17_route_233_ExUr, "319"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "234_ExUr", R.string.agency_17_route_234_ExUr, "319"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "205_ExUr", R.string.agency_17_route_205_ExUr, "321"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "634_ExUr", R.string.agency_17_route_634_ExUr, "332"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "635_ExUr", R.string.agency_17_route_635_ExUr, "332"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "507_ExUr", R.string.agency_17_route_507_ExUr, "334"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "503_ExUr", R.string.agency_17_route_503_ExUr, "335"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "581_ExUr", R.string.agency_17_route_581_ExUr, "336"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "580_ExUr", R.string.agency_17_route_580_ExUr, "336") });

	private static final List<RouteDescriptor> RoutesDescriptorsList_17_zone_4 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "520_ExUr", R.string.agency_17_route_520_ExUr, "401"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "521_ExUr", R.string.agency_17_route_521_ExUr, "401"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "69_ExUr", R.string.agency_17_route_69_ExUr, "402"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "70_ExUr", R.string.agency_17_route_70_ExUr, "402"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "494_ExUr", R.string.agency_17_route_494_ExUr, "403"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "495_ExUr", R.string.agency_17_route_495_ExUr, "403"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "290_ExUr", R.string.agency_17_route_290_ExUr, "404"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "291_ExUr", R.string.agency_17_route_291_ExUr, "404"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "523_ExUr", R.string.agency_17_route_523_ExUr, "405"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "522_ExUr", R.string.agency_17_route_522_ExUr, "405"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "169_ExUr", R.string.agency_17_route_169_ExUr, "406"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "170_ExUr", R.string.agency_17_route_170_ExUr, "406"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "184_ExUr", R.string.agency_17_route_184_ExUr, "407"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "185_ExUr", R.string.agency_17_route_185_ExUr, "407"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "86_ExUr", R.string.agency_17_route_86_ExUr, "408"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "87_ExUr", R.string.agency_17_route_87_ExUr, "408"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "133_ExUr", R.string.agency_17_route_133_ExUr, "409"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "132_ExUr", R.string.agency_17_route_132_ExUr, "409"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "98_ExUr", R.string.agency_17_route_98_ExUr, "410"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "99_ExUr", R.string.agency_17_route_99_ExUr, "410"),
			// 413?
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "90_ExUr", R.string.agency_17_route_90_ExUr, "415"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "91_ExUr", R.string.agency_17_route_91_ExUr, "415"),
			// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "306_ExUr",
			// R.string.agency_17_route_306_ExUr, "416"),
			// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "305_ExUr",
			// R.string.agency_17_route_305_ExUr, "416"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "179_ExUr", R.string.agency_17_route_179_ExUr, "417"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "180_ExUr", R.string.agency_17_route_180_ExUr, "417"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "178_ExUr", R.string.agency_17_route_178_ExUr, "418"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "177_ExUr", R.string.agency_17_route_177_ExUr, "418"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "189_ExUr", R.string.agency_17_route_189_ExUr, "423"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "188_ExUr", R.string.agency_17_route_188_ExUr, "423"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "561_ExUr", R.string.agency_17_route_561_ExUr, "425"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "309_ExUr", R.string.agency_17_route_309_ExUr, "425"),
			// 428 ?
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "84_ExUr", R.string.agency_17_route_84_ExUr, "429"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "85_ExUr", R.string.agency_17_route_85_ExUr, "429"),
			// 431 ?
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "564_ExUr", R.string.agency_17_route_564_ExUr, "433"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "550_ExUr", R.string.agency_17_route_550_ExUr, "461"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "549_ExUr", R.string.agency_17_route_549_ExUr, "461"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "551_ExUr", R.string.agency_17_route_551_ExUr, "462"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "560_ExUr", R.string.agency_17_route_560_ExUr, "462"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "556_ExUr", R.string.agency_17_route_556_ExUr, "463"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "557_ExUr", R.string.agency_17_route_557_ExUr, "463"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "555_ExUr", R.string.agency_17_route_555_ExUr, "464"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "554_ExUr", R.string.agency_17_route_554_ExUr, "464"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "563_ExUr", R.string.agency_17_route_563_ExUr, "465"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "562_ExUr", R.string.agency_17_route_562_ExUr, "465"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "552_ExUr", R.string.agency_17_route_552_ExUr, "466"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "553_ExUr", R.string.agency_17_route_553_ExUr, "466"),
			// 467 ?
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "640_ExUr", R.string.agency_17_route_640_ExUr, "468") });

	private static final List<RouteDescriptor> RoutesDescriptorsList_17_zone_5 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "102_ExUr", R.string.agency_17_route_102_ExUr, "501"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "103_ExUr", R.string.agency_17_route_103_ExUr, "501"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "619_ExUr", R.string.agency_17_route_619_ExUr, "502"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "618_ExUr", R.string.agency_17_route_618_ExUr, "502"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "248_ExUr", R.string.agency_17_route_248_ExUr, "503"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "249_ExUr", R.string.agency_17_route_249_ExUr, "503"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "107_ExUr", R.string.agency_17_route_107_ExUr, "504"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "106_ExUr", R.string.agency_17_route_106_ExUr, "504"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "251_ExUr", R.string.agency_17_route_251_ExUr, "506"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "250_ExUr", R.string.agency_17_route_250_ExUr, "506"),
			// 507 ?
			// 511 ?
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "108_ExUr", R.string.agency_17_route_108_ExUr, "512"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "109_ExUr", R.string.agency_17_route_109_ExUr, "512"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "123_ExUr", R.string.agency_17_route_123_ExUr, "514"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "124_ExUr", R.string.agency_17_route_124_ExUr, "514")
	// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "439_ExUr",
	// R.string.agency_17_route_439_ExUr, "543")
			});

	private static final List<RouteDescriptor> RoutesDescriptorsList_17_zone_6 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "346_ExUr", R.string.agency_17_route_346_ExUr, "611"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "345_ExUr", R.string.agency_17_route_345_ExUr, "611"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "395_ExUr", R.string.agency_17_route_395_ExUr, "615"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "396_ExUr", R.string.agency_17_route_396_ExUr, "615"),
			// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "387_ExUr",
			// R.string.agency_17_route_387_ExUr, "616"),
			// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "386_ExUr",
			// R.string.agency_17_route_386_ExUr, "616"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "367_ExUr", R.string.agency_17_route_367_ExUr, "620"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "366_ExUr", R.string.agency_17_route_366_ExUr, "620"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "350_ExUr", R.string.agency_17_route_350_ExUr, "623"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "349_ExUr", R.string.agency_17_route_349_ExUr, "623"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "356_ExUr", R.string.agency_17_route_356_ExUr, "624"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "354_ExUr", R.string.agency_17_route_354_ExUr, "624"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "384_ExUr", R.string.agency_17_route_384_ExUr, "625"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "383_ExUr", R.string.agency_17_route_383_ExUr, "625"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "368_ExUr", R.string.agency_17_route_368_ExUr, "627"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "369_ExUr", R.string.agency_17_route_369_ExUr, "627"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "375_ExUr", R.string.agency_17_route_375_ExUr, "630"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "376_ExUr", R.string.agency_17_route_376_ExUr, "630"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "365_ExUr", R.string.agency_17_route_365_ExUr, "632"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "364_ExUr", R.string.agency_17_route_364_ExUr, "632"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "379_ExUr", R.string.agency_17_route_379_ExUr, "633"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "378_ExUr", R.string.agency_17_route_378_ExUr, "633"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "358_ExUr", R.string.agency_17_route_358_ExUr, "634"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "357_ExUr", R.string.agency_17_route_357_ExUr, "634"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "362_ExUr", R.string.agency_17_route_362_ExUr, "635"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "363_ExUr", R.string.agency_17_route_363_ExUr, "635"),
			// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "382_ExUr",
			// R.string.agency_17_route_382_ExUr, "636"),
			// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "381_ExUr",
			// R.string.agency_17_route_381_ExUr, "636"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "325_ExUr", R.string.agency_17_route_325_ExUr, "640"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "324_ExUr", R.string.agency_17_route_324_ExUr, "640"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "330_ExUr", R.string.agency_17_route_330_ExUr, "641"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "331_ExUr", R.string.agency_17_route_331_ExUr, "641"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "327_ExUr", R.string.agency_17_route_327_ExUr, "642"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "326_ExUr", R.string.agency_17_route_326_ExUr, "642"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "335_ExUr", R.string.agency_17_route_335_ExUr, "643"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "334_ExUr", R.string.agency_17_route_334_ExUr, "643"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "329_ExUr", R.string.agency_17_route_329_ExUr, "644"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "328_ExUr", R.string.agency_17_route_328_ExUr, "644"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "336_ExUr", R.string.agency_17_route_336_ExUr, "645"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "337_ExUr", R.string.agency_17_route_337_ExUr, "645"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "332_ExUr", R.string.agency_17_route_332_ExUr, "646"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "333_ExUr", R.string.agency_17_route_333_ExUr, "646"),
	// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "391_ExUr",
	// R.string.agency_17_route_391_ExUr, "701"),
	// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "392_ExUr",
	// R.string.agency_17_route_392_ExUr, "701")
	// new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "418_ExUr",
	// R.string.agency_17_route_418_ExUr, "864")
			});

	private static final List<RouteDescriptor> RoutesDescriptorsList_17 = Arrays.asList(new RouteDescriptor[] {
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "243_ExUr", R.string.agency_17_route_243_ExUr, "101"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "244_ExUr", R.string.agency_17_route_244_ExUr, "101"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "6_ExUr", R.string.agency_17_route_6_ExUr, "102"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "3_ExUr", R.string.agency_17_route_3_ExUr, "102"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "121_ExUr", R.string.agency_17_route_121_ExUr, "103"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "120_ExUr", R.string.agency_17_route_120_ExUr, "103"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "55_ExUr", R.string.agency_17_route_55_ExUr, "104"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "56_ExUr", R.string.agency_17_route_56_ExUr, "104"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "62_ExUr", R.string.agency_17_route_62_ExUr, "105"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "60_ExUr", R.string.agency_17_route_60_ExUr, "105"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "343_ExUr", R.string.agency_17_route_343_ExUr, "106"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "344_ExUr", R.string.agency_17_route_344_ExUr, "106"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "342_ExUr", R.string.agency_17_route_342_ExUr, "107"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "340_ExUr", R.string.agency_17_route_340_ExUr, "107"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "242_ExUr", R.string.agency_17_route_242_ExUr, "108"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "241_ExUr", R.string.agency_17_route_241_ExUr, "108"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "65_ExUr", R.string.agency_17_route_65_ExUr, "109"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "66_ExUr", R.string.agency_17_route_66_ExUr, "109"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "240_ExUr", R.string.agency_17_route_240_ExUr, "110"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "239_ExUr", R.string.agency_17_route_239_ExUr, "110"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "67_ExUr", R.string.agency_17_route_67_ExUr, "111"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "68_ExUr", R.string.agency_17_route_68_ExUr, "111"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "117_ExUr", R.string.agency_17_route_117_ExUr, "112"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "118_ExUr", R.string.agency_17_route_118_ExUr, "112"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "8_ExUr", R.string.agency_17_route_8_ExUr, "114"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "7_ExUr", R.string.agency_17_route_7_ExUr, "114"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "300_ExUr", R.string.agency_17_route_300_ExUr, "115"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "307_ExUr", R.string.agency_17_route_307_ExUr, "115"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "151_ExUr", R.string.agency_17_route_151_ExUr, "116"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "152_ExUr", R.string.agency_17_route_152_ExUr, "116"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "611_ExUr", R.string.agency_17_route_611_ExUr, "120"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "613_ExUr", R.string.agency_17_route_613_ExUr, "120"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "64_ExUr", R.string.agency_17_route_64_ExUr, "121"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "63_ExUr", R.string.agency_17_route_63_ExUr, "121"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "289_ExUr", R.string.agency_17_route_289_ExUr, "122"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "288_ExUr", R.string.agency_17_route_288_ExUr, "122"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "252_ExUr", R.string.agency_17_route_252_ExUr, "123"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "253_ExUr", R.string.agency_17_route_253_ExUr, "123"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "638_ExUr", R.string.agency_17_route_638_ExUr, "140"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "639_ExUr", R.string.agency_17_route_639_ExUr, "140"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "71_ExUr", R.string.agency_17_route_71_ExUr, "201"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "72_ExUr", R.string.agency_17_route_72_ExUr, "201"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "238_ExUr", R.string.agency_17_route_238_ExUr, "202"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "237_ExUr", R.string.agency_17_route_237_ExUr, "202"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "79_ExUr", R.string.agency_17_route_79_ExUr, "203"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "78_ExUr", R.string.agency_17_route_78_ExUr, "203"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "175_ExUr", R.string.agency_17_route_175_ExUr, "204"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "176_ExUr", R.string.agency_17_route_176_ExUr, "204"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "134_ExUr", R.string.agency_17_route_134_ExUr, "205"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "135_ExUr", R.string.agency_17_route_135_ExUr, "205"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "80_ExUr", R.string.agency_17_route_80_ExUr, "206"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "81_ExUr", R.string.agency_17_route_81_ExUr, "206"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "287_ExUr", R.string.agency_17_route_287_ExUr, "208"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "286_ExUr", R.string.agency_17_route_286_ExUr, "208"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "159_ExUr", R.string.agency_17_route_159_ExUr, "209"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "160_ExUr", R.string.agency_17_route_160_ExUr, "209"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "155_ExUr", R.string.agency_17_route_155_ExUr, "210"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "156_ExUr", R.string.agency_17_route_156_ExUr, "210"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "153_ExUr", R.string.agency_17_route_153_ExUr, "211"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "154_ExUr", R.string.agency_17_route_154_ExUr, "211"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "168_ExUr", R.string.agency_17_route_168_ExUr, "212"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "167_ExUr", R.string.agency_17_route_167_ExUr, "212"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "247_ExUr", R.string.agency_17_route_247_ExUr, "214"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "245_ExUr", R.string.agency_17_route_245_ExUr, "214"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "158_ExUr", R.string.agency_17_route_158_ExUr, "215"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "157_ExUr", R.string.agency_17_route_157_ExUr, "215"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "74_ExUr", R.string.agency_17_route_74_ExUr, "216"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "73_ExUr", R.string.agency_17_route_73_ExUr, "216"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "321_ExUr", R.string.agency_17_route_321_ExUr, "217"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "320_ExUr", R.string.agency_17_route_320_ExUr, "217"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "229_ExUr", R.string.agency_17_route_229_ExUr, "230"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "230_ExUr", R.string.agency_17_route_230_ExUr, "230"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "637_ExUr", R.string.agency_17_route_637_ExUr, "231"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "636_ExUr", R.string.agency_17_route_636_ExUr, "231"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "565_ExUr", R.string.agency_17_route_565_ExUr, "236"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "566_ExUr", R.string.agency_17_route_566_ExUr, "236"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "110_ExUr", R.string.agency_17_route_110_ExUr, "301"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "111_ExUr", R.string.agency_17_route_111_ExUr, "301"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "181_ExUr", R.string.agency_17_route_181_ExUr, "302"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "182_ExUr", R.string.agency_17_route_182_ExUr, "302"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "187_ExUr", R.string.agency_17_route_187_ExUr, "303"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "186_ExUr", R.string.agency_17_route_186_ExUr, "303"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "231_ExUr", R.string.agency_17_route_231_ExUr, "305"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "232_ExUr", R.string.agency_17_route_232_ExUr, "305"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "196_ExUr", R.string.agency_17_route_196_ExUr, "306"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "197_ExUr", R.string.agency_17_route_197_ExUr, "306"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "201_ExUr", R.string.agency_17_route_201_ExUr, "307"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "200_ExUr", R.string.agency_17_route_200_ExUr, "307"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "314_ExUr", R.string.agency_17_route_314_ExUr, "310"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "315_ExUr", R.string.agency_17_route_315_ExUr, "310"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "194_ExUr", R.string.agency_17_route_194_ExUr, "311"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "195_ExUr", R.string.agency_17_route_195_ExUr, "311"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "215_ExUr", R.string.agency_17_route_215_ExUr, "312"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "216_ExUr", R.string.agency_17_route_216_ExUr, "312"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "203_ExUr", R.string.agency_17_route_203_ExUr, "314"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "204_ExUr", R.string.agency_17_route_204_ExUr, "314"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "198_ExUr", R.string.agency_17_route_198_ExUr, "315"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "199_ExUr", R.string.agency_17_route_199_ExUr, "315"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "191_ExUr", R.string.agency_17_route_191_ExUr, "316"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "190_ExUr", R.string.agency_17_route_190_ExUr, "316"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "233_ExUr", R.string.agency_17_route_233_ExUr, "319"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "234_ExUr", R.string.agency_17_route_234_ExUr, "319"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "205_ExUr", R.string.agency_17_route_205_ExUr, "321"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "634_ExUr", R.string.agency_17_route_634_ExUr, "332"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "635_ExUr", R.string.agency_17_route_635_ExUr, "332"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "507_ExUr", R.string.agency_17_route_507_ExUr, "334"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "503_ExUr", R.string.agency_17_route_503_ExUr, "335"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "581_ExUr", R.string.agency_17_route_581_ExUr, "336"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "580_ExUr", R.string.agency_17_route_580_ExUr, "336"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "520_ExUr", R.string.agency_17_route_520_ExUr, "401"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "521_ExUr", R.string.agency_17_route_521_ExUr, "401"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "69_ExUr", R.string.agency_17_route_69_ExUr, "402"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "70_ExUr", R.string.agency_17_route_70_ExUr, "402"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "494_ExUr", R.string.agency_17_route_494_ExUr, "403"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "495_ExUr", R.string.agency_17_route_495_ExUr, "403"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "290_ExUr", R.string.agency_17_route_290_ExUr, "404"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "291_ExUr", R.string.agency_17_route_291_ExUr, "404"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "523_ExUr", R.string.agency_17_route_523_ExUr, "405"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "522_ExUr", R.string.agency_17_route_522_ExUr, "405"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "169_ExUr", R.string.agency_17_route_169_ExUr, "406"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "170_ExUr", R.string.agency_17_route_170_ExUr, "406"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "184_ExUr", R.string.agency_17_route_184_ExUr, "407"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "185_ExUr", R.string.agency_17_route_185_ExUr, "407"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "86_ExUr", R.string.agency_17_route_86_ExUr, "408"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "87_ExUr", R.string.agency_17_route_87_ExUr, "408"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "133_ExUr", R.string.agency_17_route_133_ExUr, "409"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "132_ExUr", R.string.agency_17_route_132_ExUr, "409"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "98_ExUr", R.string.agency_17_route_98_ExUr, "410"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "99_ExUr", R.string.agency_17_route_99_ExUr, "410"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "90_ExUr", R.string.agency_17_route_90_ExUr, "415"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "91_ExUr", R.string.agency_17_route_91_ExUr, "415"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "306_ExUr", R.string.agency_17_route_306_ExUr, "416"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "305_ExUr", R.string.agency_17_route_305_ExUr, "416"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "179_ExUr", R.string.agency_17_route_179_ExUr, "417"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "180_ExUr", R.string.agency_17_route_180_ExUr, "417"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "178_ExUr", R.string.agency_17_route_178_ExUr, "418"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "177_ExUr", R.string.agency_17_route_177_ExUr, "418"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "189_ExUr", R.string.agency_17_route_189_ExUr, "423"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "188_ExUr", R.string.agency_17_route_188_ExUr, "423"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "561_ExUr", R.string.agency_17_route_561_ExUr, "425"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "309_ExUr", R.string.agency_17_route_309_ExUr, "425"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "84_ExUr", R.string.agency_17_route_84_ExUr, "429"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "85_ExUr", R.string.agency_17_route_85_ExUr, "429"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "564_ExUr", R.string.agency_17_route_564_ExUr, "433"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "550_ExUr", R.string.agency_17_route_550_ExUr, "461"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "549_ExUr", R.string.agency_17_route_549_ExUr, "461"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "551_ExUr", R.string.agency_17_route_551_ExUr, "462"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "560_ExUr", R.string.agency_17_route_560_ExUr, "462"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "556_ExUr", R.string.agency_17_route_556_ExUr, "463"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "557_ExUr", R.string.agency_17_route_557_ExUr, "463"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "555_ExUr", R.string.agency_17_route_555_ExUr, "464"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "554_ExUr", R.string.agency_17_route_554_ExUr, "464"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "563_ExUr", R.string.agency_17_route_563_ExUr, "465"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "562_ExUr", R.string.agency_17_route_562_ExUr, "465"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "552_ExUr", R.string.agency_17_route_552_ExUr, "466"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "553_ExUr", R.string.agency_17_route_553_ExUr, "466"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "640_ExUr", R.string.agency_17_route_640_ExUr, "468"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "102_ExUr", R.string.agency_17_route_102_ExUr, "501"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "103_ExUr", R.string.agency_17_route_103_ExUr, "501"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "619_ExUr", R.string.agency_17_route_619_ExUr, "502"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "618_ExUr", R.string.agency_17_route_618_ExUr, "502"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "248_ExUr", R.string.agency_17_route_248_ExUr, "503"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "249_ExUr", R.string.agency_17_route_249_ExUr, "503"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "107_ExUr", R.string.agency_17_route_107_ExUr, "504"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "106_ExUr", R.string.agency_17_route_106_ExUr, "504"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "251_ExUr", R.string.agency_17_route_251_ExUr, "506"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "250_ExUr", R.string.agency_17_route_250_ExUr, "506"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "108_ExUr", R.string.agency_17_route_108_ExUr, "512"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "109_ExUr", R.string.agency_17_route_109_ExUr, "512"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "123_ExUr", R.string.agency_17_route_123_ExUr, "514"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "124_ExUr", R.string.agency_17_route_124_ExUr, "514"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "439_ExUr", R.string.agency_17_route_439_ExUr, "543"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "346_ExUr", R.string.agency_17_route_346_ExUr, "611"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "345_ExUr", R.string.agency_17_route_345_ExUr, "611"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "395_ExUr", R.string.agency_17_route_395_ExUr, "615"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "396_ExUr", R.string.agency_17_route_396_ExUr, "615"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "387_ExUr", R.string.agency_17_route_387_ExUr, "616"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "386_ExUr", R.string.agency_17_route_386_ExUr, "616"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "367_ExUr", R.string.agency_17_route_367_ExUr, "620"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "366_ExUr", R.string.agency_17_route_366_ExUr, "620"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "350_ExUr", R.string.agency_17_route_350_ExUr, "623"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "349_ExUr", R.string.agency_17_route_349_ExUr, "623"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "356_ExUr", R.string.agency_17_route_356_ExUr, "624"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "354_ExUr", R.string.agency_17_route_354_ExUr, "624"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "384_ExUr", R.string.agency_17_route_384_ExUr, "625"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "383_ExUr", R.string.agency_17_route_383_ExUr, "625"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "368_ExUr", R.string.agency_17_route_368_ExUr, "627"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "369_ExUr", R.string.agency_17_route_369_ExUr, "627"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "375_ExUr", R.string.agency_17_route_375_ExUr, "630"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "376_ExUr", R.string.agency_17_route_376_ExUr, "630"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "365_ExUr", R.string.agency_17_route_365_ExUr, "632"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "364_ExUr", R.string.agency_17_route_364_ExUr, "632"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "379_ExUr", R.string.agency_17_route_379_ExUr, "633"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "378_ExUr", R.string.agency_17_route_378_ExUr, "633"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "358_ExUr", R.string.agency_17_route_358_ExUr, "634"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "357_ExUr", R.string.agency_17_route_357_ExUr, "634"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "362_ExUr", R.string.agency_17_route_362_ExUr, "635"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "363_ExUr", R.string.agency_17_route_363_ExUr, "635"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "382_ExUr", R.string.agency_17_route_382_ExUr, "636"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "381_ExUr", R.string.agency_17_route_381_ExUr, "636"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "325_ExUr", R.string.agency_17_route_325_ExUr, "640"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "324_ExUr", R.string.agency_17_route_324_ExUr, "640"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "330_ExUr", R.string.agency_17_route_330_ExUr, "641"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "331_ExUr", R.string.agency_17_route_331_ExUr, "641"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "327_ExUr", R.string.agency_17_route_327_ExUr, "642"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "326_ExUr", R.string.agency_17_route_326_ExUr, "642"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "335_ExUr", R.string.agency_17_route_335_ExUr, "643"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "334_ExUr", R.string.agency_17_route_334_ExUr, "643"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "329_ExUr", R.string.agency_17_route_329_ExUr, "644"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "328_ExUr", R.string.agency_17_route_328_ExUr, "644"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "336_ExUr", R.string.agency_17_route_336_ExUr, "645"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "337_ExUr", R.string.agency_17_route_337_ExUr, "645"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "332_ExUr", R.string.agency_17_route_332_ExUr, "646"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "333_ExUr", R.string.agency_17_route_333_ExUr, "646"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "391_ExUr", R.string.agency_17_route_391_ExUr, "701"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "392_ExUr", R.string.agency_17_route_392_ExUr, "701"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "420_ExUr", R.string.agency_17_route_420_ExUr, "861"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "423_ExUr", R.string.agency_17_route_423_ExUr, "862"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "417_ExUr", R.string.agency_17_route_417_ExUr, "863"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "583_ExUr", R.string.agency_17_route_583_ExUr, "863"),
			new RouteDescriptor(AGENCYID_BUS_EXTRAURBAN, "418_ExUr", R.string.agency_17_route_418_ExUr, "864") });

	public static final Map<String, List<RouteDescriptor>> ROUTES = new HashMap<String, List<RouteDescriptor>>() {
		private static final long serialVersionUID = 8472504007546826470L;
		{
			put(AGENCYID_BUS, RoutesDescriptorsList_12);
			put(AGENCYID_TRAIN_TM, RoutesDescriptorsList_10);
			put(AGENCYID_TRAIN_BZVR, RoutesDescriptorsList_5);
			put(AGENCYID_TRAIN_TNBDG, RoutesDescriptorsList_6);
			// put(AGENCYID_BUS_EXTRAURBAN, RoutesDescriptorsList_17);
			put(AGENCYID_BUS_EXTRAURBAN_ZONE_1, RoutesDescriptorsList_17_zone_1);
			put(AGENCYID_BUS_EXTRAURBAN_ZONE_2, RoutesDescriptorsList_17_zone_2);
			put(AGENCYID_BUS_EXTRAURBAN_ZONE_3, RoutesDescriptorsList_17_zone_3);
			put(AGENCYID_BUS_EXTRAURBAN_ZONE_4, RoutesDescriptorsList_17_zone_4);
			put(AGENCYID_BUS_EXTRAURBAN_ZONE_5, RoutesDescriptorsList_17_zone_5);
			put(AGENCYID_BUS_EXTRAURBAN_ZONE_6, RoutesDescriptorsList_17_zone_6);
		}
	};

}
