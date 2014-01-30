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
			Cursor c = dbh.myRawQuery("SELECT _id,title FROM searchTitles WHERE title MATCH ? ORDER BY title", (String) constraint);
			MatrixCursor extras = new MatrixCursor(new String[] { "_id", "title" });
			extras.addRow(new String[] { "-1", (String) constraint });
			Cursor[] cursors = { extras, c };
			return (Cursor) new MergeCursor(cursors);
		} catch (DatabaseException e) {
			 e.alertUser(context);
		}
		return null;
	}
}
