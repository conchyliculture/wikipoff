/*

Copyright 2014 "Renzokuken" (pseudonym, first committer of WikipOff project) at
https://github.com/conchyliculture/wikipoff

This file is part of WikipOff.

    WikipOff is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    WikipOff is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with WikipOff.  If not, see <http://www.gnu.org/licenses/>.

*/
package fr.renzo.wikipoff;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import fr.renzo.wikipoff.Database.DatabaseException;

public class SearchCursorAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	private Database[] dbhs;
	private Context context;

	
	public SearchCursorAdapter(Context context, Cursor c, Database dbh) {
		this(context,c, new Database[]{dbh} );
		
	}
	@SuppressWarnings("deprecation")
	public SearchCursorAdapter(Context context, Cursor c, Database[] dbhs) {
		super(context, c);
		this.context=context;
		this.dbhs=dbhs;
	}

	@Override
	public void bindView(View view, Context ctx, Cursor cursor) {
		String t = cursor.getString(1);
	    ((TextView) view).setText(t);
	}

	@Override
	public View newView(Context ctx, Cursor cursor, ViewGroup parent) {
		inflater = LayoutInflater.from(ctx);
	    TextView view = (TextView) inflater.inflate(android.R.layout.select_dialog_item, parent, false);
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
			ArrayList<Cursor> cursors = new ArrayList<Cursor>();
			for (int i = 0; i < this.dbhs.length; i++) {
				Database dbh=dbhs[i];
			
				Cursor c = dbh.myRawQuery("SELECT _id,title FROM searchTitles WHERE title MATCH ? ORDER BY length(title), title limit 500 ", (String) constraint);
				cursors.add(c);
			}
			MatrixCursor extras = new MatrixCursor(new String[] { "_id", "title" });
			extras.addRow(new String[] { "-1", (String) constraint });
			
			Cursor[] arraycursors = new Cursor[cursors.size()+1];

			for (int i = 0; i < cursors.size(); i++) {
				arraycursors[i] = cursors.get(i);
			}
			arraycursors[cursors.size()] = extras;
			
			return (Cursor) new MergeCursor(arraycursors);
		} catch (DatabaseException e) {
			 e.alertUser(context);
		}
		return null;
	}
}
