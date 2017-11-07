package com.op_dfat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marvin Duchmann and Arrigo Paterno
 */

public class SettingsPage extends AppCompatActivity {

    // view component variables
    private Spinner scanDurationSpinner, scanIntervalSpinner, scanDeletionSpinner;
    private Switch switcherWIFI;

    // array which holds scan durations converted into readable strings
    private String[] scanDuration;
    // array which holds scan intervals converted into readable strings
    private String[] scanInterval;
    // array which holds scan deletion times converted into readable strings
    private String[] scanDeletion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingspage);

        // get reference to the view components
        scanDurationSpinner = (Spinner) findViewById(R.id.scanDurationSpinner);
        scanIntervalSpinner = (Spinner) findViewById(R.id.scanIntervalSpinner);
        scanDeletionSpinner = (Spinner) findViewById(R.id.scanDeletionSpinner);
        switcherWIFI = (Switch) findViewById(R.id.switchWIFI);

        // load scan settings
        ScanSettings.loadSettings(getApplicationContext());
        // instantiate arrays responsible for showing scan setting descriptions
        instantiate();
        // setup view components
        setupViewComponents();

        scanDurationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // reset the index for the scan duration
                ScanSettings.indexScanDuration = i;
                scanDurationSpinner.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });

        scanIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // reset the index for the scan interval
                ScanSettings.indexScanInterval = i;
                scanIntervalSpinner.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });

        scanDeletionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // reset the index for the scan deletion
                ScanSettings.indexScanDeletion = i;
                scanDeletionSpinner.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });

        switcherWIFI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // reset if networking only on wifi is allowed
                ScanSettings.onlyWIFI = isChecked;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // save all the settings (indices)
        ScanSettings.saveSettings(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        // do stuff
        return id == R.id.impressum || super.onOptionsItemSelected(item);
    }

    private void instantiate () {
        scanDuration = new String[] {
                TimeUnit.MILLISECONDS.toMinutes(ScanSettings.scanDurationsInMS[0]) + " " + getString(R.string.minutes),
                TimeUnit.MILLISECONDS.toHours(ScanSettings.scanDurationsInMS[1]) + " " + getString(R.string.hour),
                TimeUnit.MILLISECONDS.toHours(ScanSettings.scanDurationsInMS[2]) + " " + getString(R.string.hours),
                TimeUnit.MILLISECONDS.toDays(ScanSettings.scanDurationsInMS[3]) + " " + getString(R.string.day),
                "Default (7 " + getString(R.string.days) + ")"
        };
        // array which holds scan intervals converted into readable strings
        scanInterval = new String[] {
                TimeUnit.MILLISECONDS.toSeconds(ScanSettings.scanIntervalInMS[0]) + " " + getString(R.string.seconds),
                TimeUnit.MILLISECONDS.toSeconds(ScanSettings.scanIntervalInMS[1]) + " " + getString(R.string.seconds),
                TimeUnit.MILLISECONDS.toSeconds(ScanSettings.scanIntervalInMS[2]) + " " + getString(R.string.seconds),
        };
        // array which holds scan deletion times converted into readable strings
        scanDeletion = new String[] {
                TimeUnit.MILLISECONDS.toDays(ScanSettings.scanDeletionInMS[0]) + " " + getString(R.string.days),
                TimeUnit.MILLISECONDS.toDays(ScanSettings.scanDeletionInMS[1]) + " " + getString(R.string.days),
                TimeUnit.MILLISECONDS.toDays(ScanSettings.scanDeletionInMS[2]) + " " + getString(R.string.days),
                "Default (" + getString(R.string.never) + ")"
        };
    }

    public void getHelp(MenuItem item) {
    }

    public void getAboutUs(MenuItem item) {
    }

    void setupViewComponents () {
        // setup the array adapters
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, scanDuration);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scanDurationSpinner.setAdapter(adapter);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, scanInterval);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scanIntervalSpinner.setAdapter(adapter1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, scanDeletion);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scanDeletionSpinner.setAdapter(adapter2);

        // set the selections to the corresponding indices
        scanDurationSpinner.setSelection(ScanSettings.indexScanDuration);
        scanIntervalSpinner.setSelection(ScanSettings.indexScanInterval);
        scanDeletionSpinner.setSelection(ScanSettings.indexScanDeletion);

        switcherWIFI.setChecked(ScanSettings.onlyWIFI);
    }
}
