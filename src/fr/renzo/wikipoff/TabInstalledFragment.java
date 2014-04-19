package fr.renzo.wikipoff;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;


public class TabInstalledFragment extends Fragment implements OnItemClickListener {

    private static final String TAG = "TabInstalledFragment";
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        return inflater.inflate(R.layout.fragment_tab_installed,null);

    }
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

	
}