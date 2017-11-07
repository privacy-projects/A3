package com.op_dfat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SearchView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dawid Wowniuk
 */

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_history.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_history#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_history extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView lv;
    private SearchView sv;
    private Filter filter;
    private SqliteDBHelper sqliteDBHelper;

    public Fragment_history() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_history.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_history newInstance(String param1, String param2) {
        Fragment_history fragment = new Fragment_history();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        sv = (SearchView) view.findViewById(R.id.searchViewScans);
        lv = (ListView) view.findViewById(R.id.listViewScans);

        sqliteDBHelper = SqliteDBHelper.getInstance(getActivity());

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof OnFragmentInteractionListener)) {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    // sets up the list of scans that is to be displayed in the fragment
    public void getScanList(){

        new AsyncTask<Void, Void, List<String>>() {

            @Override
            @SuppressWarnings("unchecked")
            protected List<String> doInBackground(Void... voids) {
                return createList();
            }

            @Override
            protected void onPostExecute(final List<String> timestamps) {
                // convert the standard datetime format into a more readable format
                List<String> simpleFormatTimeStamps = new ArrayList<>();
                if (timestamps != null) {
                    for (int i = 0; i < timestamps.size(); i++) {
                        simpleFormatTimeStamps.add(Utility.formatStandardDateTimeToSimpleFormat(timestamps.get(i)));
                    }
                }
                // create an adapter and pass in the list of scan times
                Adapter_Scans adapter = new Adapter_Scans(getActivity(), R.id.fragment_history, simpleFormatTimeStamps, Fragment_history.this);
                lv.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                // when a scan has been press, show a screen with the scan details
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        assert timestamps != null;
                        String timeStamp = timestamps.get(position);
                        showAppDetails(timeStamp);
                    }
                });
                lv.setTextFilterEnabled(false);

                filter = adapter.getFilter();
                setupSearchView();
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressWarnings("unchecked")
    private List<String> createList() {
        return (List<String>) sqliteDBHelper.query_select_list("SELECT " + SqliteDBStructure.SCAN_TIME + " FROM " + SqliteDBStructure.SCAN_TIMES_LOGGED + " ORDER BY DATETIME(" + SqliteDBStructure.SCAN_TIME + ") DESC");
    }

    private void showAppDetails (String timeStamp) {
        Intent intent = new Intent(getActivity(), Activity_apps.class);
        intent.putExtra(MyService.STOP_SCAN_TIME_STRING, timeStamp);
        startActivity(intent);
    }

    // Sets up the searchView to be able to look up a specific scan(s)
    private void setupSearchView(){
        sv.clearFocus();
        sv.setIconifiedByDefault(false);
        sv.setOnQueryTextListener(
                new SearchView.OnQueryTextListener(){

                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filter.filter(newText);
                        return true;
                    }
                }
        );
        // deactivates a submit button at the end of the SearchView
        sv.setSubmitButtonEnabled(false);
        sv.setQueryHint("Search Here");
    }
}

