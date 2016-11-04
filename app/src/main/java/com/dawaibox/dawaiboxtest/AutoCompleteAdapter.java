package com.dawaibox.dawaiboxtest;

import android.content.Context;
import android.database.DataSetObserver;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Darshan on 03-11-2016.
 */
public class AutoCompleteAdapter extends ArrayAdapter implements Filterable {
    ArrayList<DrugData> drugList;
    String ServerURL = "http://ec2-54-179-164-134.ap-southeast-1.compute.amazonaws.com:8080/DawaiBoxSmartPrescription/searchdrugs?Id=14120&SearchText=";
    public AutoCompleteAdapter(Context context, int resource) {
        super(context, resource);
        drugList = new ArrayList<DrugData>();
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    try {
                        String term = constraint.toString();
                        drugList = new DownloadDrugList().execute(term).get();
                    } catch (Exception e) {

                    }
                    filterResults.values = drugList;
                    filterResults.count = drugList.size();
                }
                return filterResults;
            }
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if(results != null && results.count > 0){
                        notifyDataSetChanged();
                    }else{
                        notifyDataSetInvalidated();
                    }
                }
            };
            return myFilter;
    }


    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return drugList.size();
    }

    @Override
    public DrugData getItem(int position) {
        return drugList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.auto_complete_layout, parent, false);
        DrugData drugData = drugList.get(position);
        TextView drugName = (TextView) view.findViewById(R.id.drugName);
        drugName.setText(drugData.getDrugName());
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
    private class DownloadDrugList extends AsyncTask<String,Void,ArrayList> {

        @Override
        protected ArrayList doInBackground(String... params) {
            try {
                String NEW_URL = ServerURL + URLEncoder.encode(params[0], "UTF-8") + "&start=0&limit=10&role=Doctor";
                URL url = new URL(NEW_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null){
                    sb.append(line).append("\n");
                }
                String jsonString =  sb.toString();
                ArrayList<DrugData> drugListTemp = new ArrayList();
                JSONObject json_obj = new JSONObject(jsonString);
                if(json_obj.has("drugList")) {
                    JSONArray jsonArray = json_obj.getJSONArray("drugList");

                    //JSONArray jsonArray = new JSONArray(jsonString);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jo = jsonArray.getJSONObject(i);
                        DrugData drugData = new DrugData();
                        drugData.setDrugId(jo.getString("drugId"));
                        drugData.setDrugName(jo.getString("drugName"));
                        drugData.setDrugType(jo.getString("drugType"));
                        drugData.setPharmaCompName(jo.getString("pharmaCompName"));
                        drugData.setCompound(jo.getString("compound"));
                        drugData.setDrugInteractions(jo.getString("drugInteractions"));
                        drugListTemp.add(drugData);
                    }
                }
                return drugListTemp;
            }catch (Exception e){
                return null;
            }

        }


    }
}
