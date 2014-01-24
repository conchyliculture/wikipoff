package fr.renzo.wikipoff;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import fr.renzo.wikipoff.Database.DatabaseException;

public class SearchCursorAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	private Database dbh;
	private Context context;

	@SuppressWarnings("deprecation")
	public SearchCursorAdapter(Context context, Cursor c, Database dbh) {
		super(context, c);
		this.context=context;
		this.dbh=dbh;
	}

	@Override
	public void bindView(View view, Context ctx, Cursor cursor) {
		String t = cursor.getString(1);
	    ((TextView) view).setText(t);
	}

	@Override
	public View newView(Context ctx, Cursor cursor, ViewGroup parent) {
		inflater = LayoutInflater.from(ctx);
	    final TextView view = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
	    return view;
	}
	@Override
	public String convertToString(Cursor cursor) {
	    return cursor.getString(1);
	}
	
	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		if (constraint == null)
			return null;
		try {
			return dbh.myRawQuery("SELECT _id,title FROM searchTitles WHERE title MATCH ? ORDER BY title", (String) constraint);
		} catch (DatabaseException e) {
			 e.alertUser(context);
		}
		return null;
	}
}
