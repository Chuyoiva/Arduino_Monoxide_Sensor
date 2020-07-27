package com.example.cosensor;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity /*implements OnMapReadyCallback */{

    private BottomNavigationView bottomBar;
    private float ppmValues;
    private LineData data;
    private int spinnerSelected;
    private int temperature;
    private int humidity;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double longitude;
    private double latitude;
    private MapFragment mapFragment = null;
    private List<Lectura> lecturaList = new ArrayList<>();
    private RecyclerView recyclerView;
    private LecturaAdapter lecturaAdapter;
    private boolean runThread = false;
    private Thread runningThread;
    private int index;
    private int selectedTab = 0;
    private Fragment selectedFragment = null;
    private boolean scrollToBottom = true;
    private int prevPosition = -1;

    //-------------------------------------------
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;
    //-------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedFragment = new GraphFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        CreateBottomBar();
        runThread = true;
        index = 0;
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        lecturaAdapter = new LecturaAdapter(lecturaList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(lecturaAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Lectura lectura = lecturaList.get(position);
                if(prevPosition != position){
                    prevPosition = position;
                    recyclerView.scrollToPosition(position);
                    scrollToBottom = false;
                    if(selectedTab == 0){
                        GraphFragment gf = (GraphFragment) selectedFragment;
                        gf.HighlightValue(lectura.getId());

                    }
                    recyclerView.setItemAnimator(null);
                    Toast.makeText(getApplicationContext(), "Entry "+String.valueOf(lectura.getId()) + " is selected!", Toast.LENGTH_SHORT).show();
                }else{
                    if(selectedTab == 0){
                        GraphFragment gf = (GraphFragment) selectedFragment;
                        gf.HighlightValue(-1);
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        recyclerView.addOnScrollListener(new RecyclerScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(1)) {
                    scrollToBottom = true;
                }
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        System.out.println("The RecyclerView is not scrolling");
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        System.out.println("Scrolling now");
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        System.out.println("Scroll Settling");
                        break;

                }

            }
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dx > 0) {
                    System.out.println("Scrolled Right");
                } else if (dx < 0) {
                    System.out.println("Scrolled Left");
                } else {
                    System.out.println("No Horizontal Scrolled");
                }

                if (dy > 0) {
                    System.out.println("Scrolled Downwards");
                    //if reached the last item keep scrolling automatically
                } else if (dy < 0) {
                    System.out.println("Scrolled Upwards");
                    scrollToBottom = false;
                } else {
                    System.out.println("No Vertical Scrolled");
                }
            }
        });

        data = new LineData();
        data.setValueTextColor(Color.WHITE);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        boolean isGPSenabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGPSenabled) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            longitude = loc.getLongitude();
            latitude = loc.getLatitude();

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    LatLng position = new LatLng(latitude, longitude);
                    if(mapFragment != null){
                        mapFragment.UpdateMapPosition(position);
                    }

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            bluetoothIn = new Handler() {
                public void handleMessage(Message msg) {
                    if (msg.what == handlerState) {
                        String readMessage = (String) msg.obj;
                        DataStringIN.append(readMessage);

                        int endOfLineIndex = DataStringIN.indexOf("#");

                        if (endOfLineIndex > 0) {
                            String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                            String[]data = dataInPrint.split(",");
                            ppmValues = Float.parseFloat(data[0]);
                            temperature = Math.round(Float.parseFloat(data[1]));
                            humidity = Math.round(Float.parseFloat(data[2]));
                            
                            DataStringIN.delete(0, DataStringIN.length());
                        }
                    }
                }
            };
            btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
            VerificarEstadoBT();
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void BTStuff(){
        //Consigue la direccion MAC desde DeviceListActivity via intent
        Intent intent = getIntent();
        //Consigue la direccion MAC desde DeviceListActivity via EXTRA
        address = intent.getStringExtra(DispositivosBT.EXTRA_DEVICE_ADDRESS);//<-<- PARTE A MODIFICAR >->->
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }


    private void CreateBottomBar() {
        bottomBar = findViewById(R.id.btmNavigationBar);
        bottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mapFragment = null;
                switch (item.getItemId()) {
                    case R.id.navigation_Graph:
                        Toast.makeText(MainActivity.this, "CO Graph", Toast.LENGTH_SHORT).show();
                        selectedFragment = new GraphFragment();
                        selectedTab = 0;
                        break;
                    case R.id.navigation_Temperature:
                        Toast.makeText(MainActivity.this, "Temperature & Humidity", Toast.LENGTH_SHORT).show();
                        selectedFragment = new TemperatureFragment();
                        selectedTab = 1;
                        break;
                    case R.id.navigation_Map:
                        Toast.makeText(MainActivity.this, "CO Map", Toast.LENGTH_SHORT).show();
                        selectedFragment = new MapFragment();
                        mapFragment = (MapFragment) selectedFragment;
                        selectedTab = 2;
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        runThread = true;
        BTStuff();
        //mapView.onResume();
        runningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //for(int i = 0; i < 100; i++){
                while (runThread){
                    runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void run() {
                            index++;
                            GetSensorReading();
                            //ReadSensorTemperature();
                           // ReadSensorHumidity();
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat mdformat = new SimpleDateFormat("dd/MM/yyyy");
                            SimpleDateFormat tdformat = new SimpleDateFormat("HH:mm:ss");

                            String date = mdformat.format(calendar.getTime());
                            String time = tdformat.format(calendar.getTime());
                            Lectura lectura = new Lectura(index, ppmValues, temperature, humidity, latitude, longitude, time, date);

                            //Collections.reverse(lecturaList);
                            lecturaList.add(lectura);
                            //Collections.reverse(lecturaList);
                            lecturaAdapter.notifyDataSetChanged();
                            if(scrollToBottom){
                                recyclerView.scrollToPosition(lecturaList.size()-1);
                            }


                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        runningThread.start();
    }

    private void GetSensorReading() {
        //ppmValues = (float) Math.random() * 100;
        if (data != null) {
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            Entry e1 = new Entry(set.getEntryCount(), ppmValues);
            data.addEntry(e1, 0);
        }
    }



    public void setData (LineData d){
        data = d;
    }

    public LineData getData(){
        return  data;
    }

    public void setSpinnerSelected (int s){
        spinnerSelected = s;
    }

    public int getSpinnerSelected(){
        return spinnerSelected;
    }

    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null, "CO");
        set.setDrawCircles(true);
        set.setCubicIntensity(0.2f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(getResources().getColor(R.color.colorDataSet));
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.GREEN);
        set.setValueTextColor(Color.GRAY);
        set.setValueTextSize(10f);
        return set;
    }

    public void SelectItemOnRecycleView(float index){
        scrollToBottom = false;
        recyclerView.scrollToPosition((int)index);
    }

    private void ReadSensorTemperature(){
        float t = (float) Math.random()*50;
        temperature = (int)t;
    }

    private void ReadSensorHumidity(){
        float h = (float) Math.random()*100;
        humidity = (int)h;

    }

    public int getTemperature(){
        return  temperature;
    }

    public int getHumidity(){
        return  humidity;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

            }
        }
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    @Override
    protected void onPause() {
        super.onPause();
        runThread = false;
        runningThread.interrupt();
        try
        { // Cuando se sale de la aplicación esta parte permite
            // que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {}

    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //Envio de trama
        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

}
