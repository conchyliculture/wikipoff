package fr.renzo.wikipoff;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TabCustomActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        TextView textview = new TextView(this);
        textview.setText("This is 3 tab");
        setContentView(textview);

    }
}