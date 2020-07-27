package com.example.cosensor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TemperatureFragment extends Fragment {

    private TextView txtTemperatura;
    private TextView txtHumedad;
    private Thread tempThread = null;
    private boolean active = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_temperature, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        txtTemperatura = getView().findViewById(R.id.txtTemperatura);
        txtHumedad = getView().findViewById(R.id.txtHumedad);
        active = true;

        tempThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (active){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                            getTemperature();
                            getHumidity();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
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

        tempThread.start();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        active = true;

    }

    private void getTemperature(){
       int intT = ((MainActivity) getActivity()).getTemperature();
       String tempText = intT + "°C";
        txtTemperatura.setText(tempText);
        if(intT > 0){
            if(intT > 10){
                if(intT > 20){
                    if(intT > 30){
                        if(intT > 40){
                            txtTemperatura.setTextColor(Color.parseColor("#FFED4502"));
                            return;
                        }
                        txtTemperatura.setTextColor(Color.parseColor("#FFEDAE02"));
                        return;
                    }
                    txtTemperatura.setTextColor(Color.parseColor("#FF7DBE03"));
                    return;
                }
                txtTemperatura.setTextColor(Color.parseColor("#FF03BE7C"));
                return;
            }
            txtTemperatura.setTextColor(Color.parseColor("#FF039EBE"));
            return;
        }
        txtTemperatura.setTextColor(Color.parseColor("#FF0076AD"));
        return;
    }

    private void getHumidity(){
        int intH = ((MainActivity)getActivity()).getHumidity();
        String tempText =  intH + "%";
        txtHumedad.setText(tempText);
    }

    @Override
    public void onPause() {
        super.onPause();
        active = false;
        tempThread.interrupt();
    }
}
