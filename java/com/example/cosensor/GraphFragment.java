package com.example.cosensor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.highlight.Highlight;

public class GraphFragment extends Fragment {

    private RelativeLayout mainLayout;
    private LineChart mChart;
    private Spinner optionList;
    private boolean sensogram = false;
    private Thread runThread = null;
    private boolean active = false;

    LineData data;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_graph, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        active = true;
        CreateChart();
        CreateSpinnerList();
        runThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (active){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addEntry();
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        super.onViewCreated(view, savedInstanceState);

    }

    private void CreateSpinnerList() {
        optionList = getView().findViewById(R.id.spinnerGraph);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.GraphOptions, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        optionList.setAdapter(adapter);
        optionList.setSelection(((MainActivity)getActivity()).getSpinnerSelected());
        optionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Log.d("tagg", String.valueOf(optionList.getSelectedItemPosition()));
                switch (optionList.getSelectedItemPosition()){
                    case 0:
                        sensogram = false;
                        break;
                    case 1:
                        sensogram = true;
                        mChart.fitScreen();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void CreateChart() {
        mChart = getView().findViewById(R.id.mChartCO);

        Description description = mChart.getDescription();
        description.setEnabled(false);
        mChart.setNoDataText("No data");
        mChart.setHighlightPerTapEnabled(true);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(getResources().getColor(R.color.colorBG));
        mChart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                float x = e.getX();
                ((MainActivity)getActivity()).SelectItemOnRecycleView(x);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        mChart.setData(((MainActivity)getActivity()).getData());

        Legend l1 = mChart.getLegend();
        l1.setForm(Legend.LegendForm.LINE);
        l1.setTextColor(Color.BLACK);
        l1.setEnabled(false);

        XAxis x1 = mChart.getXAxis();
        x1.setDrawGridLines(false);
        x1.setAvoidFirstLastClipping(true);
        x1.setPosition(XAxis.XAxisPosition.BOTTOM);
        x1.setTextColor(Color.BLACK);
        x1.setAxisLineColor(Color.BLACK);
        x1.setAxisLineWidth(1);

        YAxis y1 = mChart.getAxisLeft();
        y1.setTextColor(Color.BLACK);
        y1.setDrawGridLines(true);
        y1.setDrawLabels(true);
        y1.setGridColor(Color.BLACK);
        y1.setAxisLineColor(Color.BLACK);
        y1.setAxisLineWidth(1);

        YAxis y2 = mChart.getAxisRight();
        y2.setEnabled(false);
    }

    private void addEntry(){
        data = mChart.getData();
        if(data != null){

            mChart.notifyDataSetChanged();
            if(sensogram){
                mChart.moveViewToX(0);
                mChart.setVisibleXRangeMaximum(data.getEntryCount());

            }else{
                mChart.moveViewToX(data.getEntryCount()-7);
                mChart.setVisibleXRangeMaximum(7);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(runThread != null){
            runThread.start();
        }else{
            active = true;
            runThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (active){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addEntry();
                            }
                        });
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            runThread.start();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).setData(mChart.getData());
        ((MainActivity)getActivity()).setSpinnerSelected(optionList.getSelectedItemPosition());
        active = false;
        runThread.interrupt();
        runThread = null;

    }

    public void HighlightValue(int index){
        mChart.highlightValue(index, 0);

    }

    public interface OnChartValueSelectedListener {
        /**
         * Called when a value has been selected inside the chart.
         *
         * @param e The selected Entry.
         * @param h The corresponding highlight object that contains information
         * about the highlighted position
         */
        public void onValueSelected(Entry e, Highlight h);
        /**
         * Called when nothing has been selected or an "un-select" has been made.
         */
        public void onNothingSelected();
    }
}
