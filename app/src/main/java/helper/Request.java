package helper;

import android.location.Address;
import android.os.AsyncTask;
import android.util.Log;

import com.example.falldetectionsystem.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import adapters.PatientRecyclerAdapter;

/**
 * @author Antoine Sauray
 * @version 0.5
 * Licensed under the Apache2 license
 */

public class Request {

    private static MapView map = null;
    private static User user = null;
    private static Location location = null;
    private static double latitude = 0.0;
    private static double longitude = 0.0;

    public static void getPlaces(Action a, MapView m, User u, Location l, ArrayList<Pair> parameters) {
        try {
            new GetPlaces(a, parameters).execute();
            map = m;
            user = u;
            location = l;
            Log.d("getPlaces", "getPlaces");
        } catch (IllegalStateException e) {
            Log.e(e.getMessage(), "exception");
        }
    }


    private static class GetPlaces extends AsyncTask<Pair, Pair, Location> {

    /*
        wiki : http://wiki.openstreetmap.org/wiki/Nominatim

        street=<housenumber> <streetname>
        city=<city>
        county=<county>
        state=<state>
        country=<country>
        postalcode=<postalcode>

        use q= if you don't know whether the user type an address, a city a county or whatever
    */

//        private final String QUERY = "https://nominatim.openstreetmap.org//search?";
//        private final String QUERY = "https://nominatim.openstreetmap.org//reverse?";
        private String QUERY = "";

        private Action action;
        private ArrayList<Pair>[] parameters;
        String display_name="";

        /**
         * @param action     The method to apply on each Place which is returned by nominatim
         * @param parameters A set of keys and values to provide to the request. Each map will be triggered in a different request
         * @see Action
         */
        public GetPlaces(Action action, ArrayList<Pair>... parameters) {
            this.action = action;
            this.parameters = parameters;
        }

        @Override
        protected Location doInBackground(Pair... params) {
            if(location.getAddress() != null){
                QUERY = "https://nominatim.openstreetmap.org//reverse?";
            }else{
                QUERY = "https://nominatim.openstreetmap.org//search?";
            }

            StringBuilder jsonResult = new StringBuilder();
            StringBuilder sb = new StringBuilder(QUERY);
            sb.append("format=json&");

            double lat = 30.0;
            double lon = 30.0;

            for (ArrayList<Pair> pairs : parameters) {
                Log.d("size=" + pairs.size(), "arraylist found");
                for (Pair p : pairs) {
//                    if(p.third != null && p.fourth != null){
//                        sb.append(p.first + "=" + p.second + "&" + p.third + "=" + p.fourth);
//                        Log.d(p.first + "+" + p.second + "+" + p.third + "+" + p.fourth, "pairs");
//                    }else{
                        sb.append(p.first + "=" + p.second + "&");
                        Log.d("p.first=" + p.first + " & p.second" + p.second, "pairs");
//                    }
                }
                try {
                    URL url = new URL(sb.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    InputStreamReader in = new InputStreamReader(conn.getInputStream());

                    BufferedReader jsonReader = new BufferedReader(in);
                    String lineIn;
                    while ((lineIn = jsonReader.readLine()) != null) {
                        jsonResult.append(lineIn);
                    }

                    try {
                        JSONArray jsonArray = new JSONArray(jsonResult.toString());
                        int length = jsonArray.length();
                        if (length > 0) {
                            for (int i = 0; i < length; i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                                JSONArray boundingArray = jsonObject.getJSONArray("boundingbox");
//                                BoundingBox boundingBox = new BoundingBox();
//                                for (int j = 0; i < boundingArray.length(); i++) {
//                                    boundingBox.setBound(i, boundingArray.optDouble(i));
//                                }
                                lat = jsonObject.getDouble("lat");
                                lon = jsonObject.getDouble("lon");
                                display_name = jsonObject.optString("display_name");
//                                String entityClass = jsonObject.optString("class");
//                                String type = jsonObject.optString("type");
//                                float importance = (float) jsonObject.optDouble("importance");
//                                String[] split = display_name.split(",");
                            }
                        }

                        if (location.getLatitude() == 0.0 && location.getLongitude() == 0.0) {
                            location = new Location(lat, lon);
                            location.setAddress(display_name);
                        }
                        String latlon = location.getLatitude() + "+" + location.getLongitude();
                        Log.d("Location", latlon);

                        return location;
//                            publishProgress(new Address(split[0]+","+split[1], R.mipmap.ic_launcher, lat, lon));
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        Log.d("Error Json Background",e.toString());
                    }
                } catch (IOException e) {
                    Log.d("Error IO Background",e.toString());
                }

            }
            return location;
        }


        @Override
        protected void onPostExecute(Location loc) {
            super.onPostExecute(loc);
            try{
                IMapController mapController = map.getController();
                mapController.setZoom(18.0);
                GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.setCenter(startPoint);

                Marker startMarker = new Marker(map);
                startMarker.setPosition(startPoint);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setTitle(user.getStreet() + ", " + user.getCity() + "\n" + user.getCountry() + ", " + user.getPostalCode());
                startMarker.setDraggable(true);
                map.getOverlays().add(startMarker);
            }catch(Exception e){
                Log.d("Exception post execute",e.toString());
            }
        }

    }

    public static void getDataFromSQL(DatabaseHelper databaseHelper, List<User> patientList, PatientRecyclerAdapter patientRecyclerAdapter) {
        try {
            new GetDataFromSQLite(databaseHelper, patientList, patientRecyclerAdapter).execute();
            Log.d("getDataFromSQLite", "getDataSQLite");
        } catch (IllegalStateException e) {
            Log.e(e.getMessage(), "exception");
        }

    }

    private static class GetDataFromSQLite extends AsyncTask<Void, Void, Void> {

        DatabaseHelper databaseHelper;
        List<User> patientList;
        PatientRecyclerAdapter patientRecyclerAdapter;

        public GetDataFromSQLite(DatabaseHelper databaseHelper, List<User> patientList, PatientRecyclerAdapter patientRecyclerAdapter) {
            this.databaseHelper = databaseHelper;
            this.patientList = patientList;
            this.patientRecyclerAdapter = patientRecyclerAdapter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            patientList.clear();
            patientList.addAll(databaseHelper.getAllPatient());
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            patientRecyclerAdapter.notifyDataSetChanged();

        }
    }

}

