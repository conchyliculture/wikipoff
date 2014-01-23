package fr.renzo.wikipoff;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Article {
	public String title;
	public int id_;
	public String text;

	public  Article(int id_, String title, String text) {
		this.id_ = id_;
		this.title = title;
		this.text = text;
	}
	public  Article(int id_, String title, byte[] coded) {
		this.id_ = id_;
		this.title = title;
		this.text = decodeBlob(coded);
	}
	
	private String decodeBlob(byte[]coded) {
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
}
