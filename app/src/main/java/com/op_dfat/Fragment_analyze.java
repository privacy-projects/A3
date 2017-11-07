package com.op_dfat;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * Created by Arrigo Paterno, Dawid Wowniuk
 */

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_analyze.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_analyze#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_analyze extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OnFragmentInteractionListener mListener;

    // inflated view
    View v;
    // start stop button
    private Button startStop;
    // Progress bar
    private ProgressBar progressBar;
    private TextView text;
    private TextView sampleCount, sampleFound, scanDuration;
    private Chronometer elapsed;
    private ProgressDialog progressDialog;


    public Fragment_analyze() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_analyze.
     */
    public static Fragment_analyze newInstance(String param1, String param2) {
        Fragment_analyze fragment = new Fragment_analyze();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // get references to all view related objects
        v = inflater.inflate(R.layout.fragment_analyze, container, false);
        startStop = (Button) v.findViewById(R.id.startStopScan);
        text = (TextView) v.findViewById(R.id.textView3);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        sampleCount = (TextView) v.findViewById(R.id.sampleCount);
        elapsed = (Chronometer) v.findViewById(R.id.chronometer2);
        scanDuration = (TextView) v.findViewById(R.id.scanDuration);
        sampleFound = (TextView) v.findViewById(R.id.sampleFound);

        // apply current state to progressbar and start stop buttons
        changeStartStop(MainActivity.scanning);
        changeProgessBar(MainActivity.scanning);

        // set the clock and the sample count when fragment is created
        setClock((System.currentTimeMillis() - MyService.start_scan_time_ms));
        String sampleCountText = "" + Evaluation.sampleCount;
        sampleCount.setText(sampleCountText);

        return v;
    }

    private void changeStartStop(boolean scanning) {
        // apply the start stop button depending on the current analyzing state
        if (startStop != null) {
            if (scanning) {
                startStop.setText(R.string.stopScan);
            } else {
                startStop.setText(R.string.startScan);
            }
        }
    }

    private void changeProgessBar(boolean scanning) {
        // change the visibility of the progressbar depending on the current analyzing state
        if (scanning) {
            assert progressBar != null;
            progressBar.setVisibility(View.VISIBLE);
            text.setVisibility(View.VISIBLE);
            elapsed.setVisibility(View.VISIBLE);
            sampleCount.setVisibility(View.VISIBLE);
            String sampleCountText = "" + Evaluation.sampleCount;
            sampleCount.setText(sampleCountText);
            sampleFound.setVisibility(View.VISIBLE);
            scanDuration.setVisibility(View.VISIBLE);
        } else {
            assert progressBar != null;
            progressBar.setVisibility(View.INVISIBLE);
            text.setVisibility(View.INVISIBLE);
            elapsed.setVisibility(View.INVISIBLE);
            sampleCount.setVisibility(View.INVISIBLE);
            sampleFound.setVisibility(View.INVISIBLE);
            scanDuration.setVisibility(View.INVISIBLE);
        }
    }

    public void setClock (long elapsedTime) {
        elapsed.setBase(SystemClock.elapsedRealtime() - elapsedTime);
        elapsed.start();
    }

    public void stopClock () {
        elapsed.stop();
    }

    // button start analyzing
    public void applyChanges(boolean scanning) {
        changeStartStop(scanning);
        changeProgessBar(scanning);
    }

    public void showProgressDialog (int maxValue) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(maxValue);
        progressDialog.setMessage(getResources().getString(R.string.fragment_analyze_processing));
        progressDialog.show();
    }

    public void updateProgressDialog(int value) {
        if (progressDialog != null)
            progressDialog.setProgress(value);
    }

    public void dismissProgressDialog () {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    public void setSampleCount (long sampleCount) {
        String sampleCountText = "" + sampleCount;
        this.sampleCount.setText(sampleCountText);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
