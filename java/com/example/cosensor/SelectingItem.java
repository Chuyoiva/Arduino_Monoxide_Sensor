package com.example.cosensor;

import android.view.View;
import android.widget.AdapterView;

public class SelectingItem implements AdapterView.OnItemSelectedListener {
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        /*Toast.makeText(parent.getContext(),
                "Selecting Item : " + parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();*/
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }
}
