package fr.renzo.pocketwiki;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;



public class Database   {
	private static final String TAG = "Database";
	public File seldatabasefile;
	private Context context;
	public SQLiteDatabase sqlh;
	
	public Database(Context context, File databasefile) {
		
	        this.context=context;
	        this.seldatabasefile = databasefile;
			if (this.seldatabasefile==null) {
				Log.d(TAG,"Merci de me donner un fichier qui existe");
			} else {
				Log.d(TAG,"Alors lisons "+this.seldatabasefile.getAbsolutePath());
			}

			SQLiteDatabase sqlh = SQLiteDatabase.openDatabase(this.seldatabasefile.getAbsolutePath(), null, 0);
		
			this.sqlh=sqlh;
			//openDatabase(this.selectedDb);
	}
	
	public String decodeBlob(byte[]coded) {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			int propertiesSize = 5;
			byte[] properties = new byte[propertiesSize];
			
			InputStream inStream = new ByteArrayInputStream(coded);
			
			OutputStream outStream = new BufferedOutputStream(baos,1024*1024);
			SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();

			if (inStream.read(properties, 0, propertiesSize) != propertiesSize)
				throw new Exception("input .lzma file is too short");
			if (!decoder.SetDecoderProperties(properties))
				throw new Exception("Incorrect stream properties");
			long outSize = 0;
			for (int i = 0; i < 8; i++)
			{
				int v = inStream.read();
				if (v < 0)
					throw new Exception("Can't read stream size");
				outSize |= ((long)v) << (8 * i);
			}
		
			if (!decoder.Code(inStream, outStream, outSize))
				throw new Exception("Error in data stream");
			outStream.flush();
			outStream.close();
			inStream.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baos.toString();
	}
	
	public String getRandomArticle() {
        String selectQuery = "SELECT  _id, url, title, text FROM "+context.getString(R.string.sql_table_name, "pute")+" ORDER BY RANDOM() LIMIT 1";
        Log.d(TAG,"On me demande "+selectQuery);
        Cursor cursor = this.sqlh.rawQuery(selectQuery, null);
        if (cursor.getCount()==1 && cursor.moveToFirst()) {
			byte[] coded = cursor.getBlob(3);
			return decodeBlob(coded);
		} else {
			return "No article not found";
		}
    }

	public String getArticle(String article) {
        String selectQuery = "SELECT  _id, url, title, text FROM "+context.getString(R.string.sql_table_name, "pute")+" WHERE title='"+article+"' LIMIT 1";
        Log.d(TAG,"On me demande "+selectQuery);
        Cursor cursor = this.sqlh.rawQuery(selectQuery, null);
        if (cursor.getCount()==1 && cursor.moveToFirst()) {
			byte[] coded = cursor.getBlob(3);
			return decodeBlob(coded);
		} else {
			return "Article '"+article+"' not found";
		}
	}

	public Cursor getAllTitles() {
		return this.sqlh.rawQuery("SELECT _id,title FROM titles ORDER BY title LIMIT 10 ", null);
	}

	public Cursor getAllTitles(String query) {
		return this.sqlh.rawQuery("SELECT _id,title FROM titles WHERE title LIKE '%"+query+"%' ORDER BY title LIMIT 10", null);
	}

	public List<String> getRandomTitles(int nb) {
		Log.d(TAG,"kikoo");
		ArrayList<String> res = new ArrayList<String>();
		Cursor c = this.sqlh.rawQuery("SELECT title FROM titles ORDER BY RANDOM() LIMIT 10", null);
		if (c.moveToFirst()) {
            do {
                String t = c.getString(0);
                Log.d(TAG,"kikoo "+t);
                res.add(t);
            } while (c.moveToNext());
        } else {
        	Log.d(TAG,"What");
        }

		return res;
	}


}
