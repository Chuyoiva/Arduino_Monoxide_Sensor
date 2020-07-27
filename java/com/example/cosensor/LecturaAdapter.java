package com.example.cosensor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LecturaAdapter extends RecyclerView.Adapter<LecturaAdapter.MyViewHolder> {

    private List<Lectura> lecturaList;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView _id, _ppm, _temperature, _humidity, _coordinates, _time, _date;

        public MyViewHolder(View view){
            super(view);
            _id = (TextView) view.findViewById(R.id.idN);
            _ppm = (TextView) view.findViewById(R.id.ppm);
            _temperature = (TextView) view.findViewById(R.id.temperature);
            _humidity = (TextView) view.findViewById(R.id.humidity);
            _coordinates = (TextView) view.findViewById(R.id.coordinates);
            _time = (TextView) view.findViewById(R.id.time);
            _date = (TextView) view.findViewById(R.id.date);

        }

    }

    public LecturaAdapter(List<Lectura> lecturaList) {
        this.lecturaList = lecturaList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lectura_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Lectura lectura = lecturaList.get(position);
        holder._id.setText(String.valueOf(lectura.getId()));
        holder._ppm.setText(String.format("%.02f", lectura.getPPMValue()) +"ppm");
        holder._temperature.setText(String.valueOf(lectura.getTemperature()) + "°C");
        holder._humidity.setText(String.valueOf(lectura.getHumidity()) + "%");
        holder._coordinates.setText("X:" + lectura.getLongitudeX() + "  Y:" + lectura.getLatitudeY());
        holder._time.setText(lectura.getTime());
        holder._date.setText(lectura.getDate());

    }

    @Override
    public int getItemCount() {
        return lecturaList.size();
    }
}
