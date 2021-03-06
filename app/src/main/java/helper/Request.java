package helper;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.falldetectionsystem.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
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
    private static TextView patientAddressTv;

    public static void getPlaces(Action a, MapView m, User u, Location l, TextView t, ArrayList<Pair> parameters) {
        try {
            new GetPlaces(a, parameters).execute();
            map = m;
            user = u;
            location = l;
            patientAddressTv = t;
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

        private String QUERY = "";

        private Action action;
        private ArrayList<Pair>[] parameters;
        String display_name="";
        double lat = -100000;
        double lon = -100000;

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
            if(location.getLatitude() != -30000.0 && location.getLongitude() != -30000.0){
                QUERY = "https://nominatim.openstreetmap.org//reverse?";
            }else{
                QUERY = "https://nominatim.openstreetmap.org//search?";
            }

            StringBuilder jsonResult = new StringBuilder();
            StringBuilder sb = new StringBuilder(QUERY);
            sb.append("format=json&limit=1&");

            for (ArrayList<Pair> pairs : parameters) {
                Log.d("size=" + pairs.size(), "arraylist found");
                for (Pair p : pairs) {
                    sb.append(p.first + "=" + p.second + "&");
                    Log.d("p.first=" + p.first + " & p.second=" + p.second, "pairs");
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

                    String result = jsonResult.toString().replace("Value ","");
                    Log.d("JSON Result",result);

                    try {
                        Object json = new JSONTokener(result).nextValue();
                        if (json instanceof JSONObject){
                            //you have an object
                            JSONObject jsonObject = new JSONObject(result);
                            if(!jsonObject.has("error")){
                                lat = jsonObject.getDouble("lat");
                                lon = jsonObject.getDouble("lon");
                                display_name = jsonObject.getString("display_name");
                            }else{
                                lat = -30.0;
                                lon = -30.0;
                                display_name = "Alamat tidak terdaftar";
                            }
                        }else if (json instanceof JSONArray){
                            //you have an array
                            JSONArray jsonArray = new JSONArray(result);
                            int length = jsonArray.length();
                            if (length > 0) {
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                if(!jsonObject.has("error")){
                                    lat = jsonObject.getDouble("lat");
                                    lon = jsonObject.getDouble("lon");
                                    display_name = jsonObject.getString("display_name");
                                }else{
                                    lat = -30.0;
                                    lon = -30.0;
                                    display_name = "Alamat tidak terdaftar";
                                }
                            }
                        }

                        location.setLatitude(lat);
                        location.setLongitude(lon);

                        String latlon = location.getLatitude() + "+" + location.getLongitude();
                        Log.d("Location", latlon);
                        Log.d("DisplayName", display_name);

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
                patientAddressTv.findViewById(R.id.patient_address_home_tv);

                IMapController mapController = map.getController();
                mapController.setZoom(18.0);
                GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.setCenter(startPoint);

                Marker startMarker = new Marker(map);
                startMarker.setPosition(startPoint);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                String address = user.getStreet() + ", " + user.getCity() + "\n" + user.getCountry() + ", " + user.getPostalCode();
                if(!display_name.contains("tidak ada")){
                    address = display_name;
                }
                startMarker.setTitle(address);
                patientAddressTv.setText(address);

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

