package com.op_dfat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Filter;
import android.support.v7.widget.SearchView;

import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import de.halfreal.googleplayscraper.api.GooglePlayApi;
import de.halfreal.googleplayscraper.api.HumanRequestBehavior;
import de.halfreal.googleplayscraper.model.App;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Dawid Wowniuk
 */

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_blacklist.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_blacklist#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_blacklist extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Filter filter;
    private ListView listView;
    private OnFragmentInteractionListener mListener;
    private SearchView sv;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ProgressDialog progressDialog;

    public Fragment_blacklist() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_blacklist.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_blacklist newInstance(String param1, String param2) {
        Fragment_blacklist fragment = new Fragment_blacklist();
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
        View view = inflater.inflate(R.layout.fragment_blacklist, container, false);
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

        // find the view components
        listView = (ListView) view.findViewById(R.id.listViewBlacklist);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_blacklist);

        // update black list on swipe down
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                if (Utility.networking(getActivity())) {
                    updateBlackList();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    public void showBlackList() {
        // get all the package names that were logged in the provided blacklist
        new AsyncTask<Void, Void, List<Apps>>() {

            @Override
            protected void onPreExecute() {
                if (!Utility.networking(getActivity())) {
                    return;
                }
                if (BlackListReceiver.isFetching()) {
                    progressDialog = ProgressDialog.show(getActivity(), getResources().getString(R.string.loadingInfo), getResources().getString(R.string.fragment_blackList_loadingBlackList));
                    progressDialog.setCancelable(true);
                    progressDialog.setCanceledOnTouchOutside(true);
                    progressDialog.show();
                }
            }

            @Override
            protected List<Apps> doInBackground(Void... voids) {

                while (!BlackListReceiver.isFetched()) {
                    try {
                        Thread.currentThread().sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return BlackListReceiver.getBlackList();
            }

            @Override
            protected void onPostExecute(List<Apps> app_data) {

                Adapter_Blacklist adapter = new Adapter_Blacklist(getActivity(), R.id.fragment_blacklist, app_data);

                // Assign adapter to ListView
                listView.setAdapter(adapter);

                // disables the default TextFilter
                listView.setTextFilterEnabled(false);
                // gets filter for the adapter
                filter = adapter.getFilter();

                // sets up the searchView
                sv = (android.support.v7.widget.SearchView) getView().findViewById(R.id.searchViewBlacklist);
                sv.clearFocus();
                setupSearchView();

                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void updateBlackList() {

        // get all the package names that were logged in the provided blacklist
        new AsyncTask<Void, Void, List<Apps>>() {

            @Override
            protected void onPreExecute() {
                // fetch the black list from server
                BlackListReceiver.setupBlackList(getActivity());
            }

            @Override
            protected List<Apps> doInBackground(Void... voids) {

                while (!BlackListReceiver.isFetched()) {
                    try {
                        Thread.currentThread().sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return BlackListReceiver.getBlackList();
            }

            @Override
            protected void onPostExecute(List<Apps> app_data) {

                Adapter_Blacklist adapter = new Adapter_Blacklist(getActivity(), R.id.fragment_blacklist, app_data);

                // Assign adapter to ListView
                listView.setAdapter(adapter);

                // disables the default TextFilter
                listView.setTextFilterEnabled(false);
                // gets filter for the adapter
                filter = adapter.getFilter();

                // sets up the searchView
                sv = (android.support.v7.widget.SearchView) getView().findViewById(R.id.searchViewBlacklist);
                sv.clearFocus();
                setupSearchView();

                // disable the refreshing symbol
                if (swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // sets up the SearchView
    private void setupSearchView(){
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
