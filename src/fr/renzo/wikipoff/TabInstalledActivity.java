package fr.renzo.wikipoff;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class TabInstalledActivity extends Activity {
	public TabInstalledActivity () {
		super();
	}
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        TextView textview = new TextView(this);
        textview.setText("This is 1 tab");
        setContentView(textview);
    }

}