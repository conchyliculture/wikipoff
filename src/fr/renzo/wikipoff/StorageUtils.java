/**
 * This has been salvaged from
 * http://stackoverflow.com/questions/5694933/find-an-external-sd-card-location#answer-19982451
 */

package fr.renzo.wikipoff;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

public class StorageUtils {

    @SuppressWarnings("unused")
	private static final String TAG = "StorageUtils";

    public static class StorageInfo {

        public final String path;
        public final boolean readonly;
        public final boolean removable;     
        public final int number;

        StorageInfo(String path, boolean readonly, boolean removable, int number) {
        	this.path = path;
            this.readonly = readonly;
            this.removable = removable;         
            this.number = number;
        }
        
        public String getDisplayName(Context c) {
            StringBuilder res = new StringBuilder();
            if (!removable) {
                res.append(c.getString(R.string.message_internal_sd_card));
            } else if (number > 1) {
                res.append(c.getString(R.string.message_sd_card_n,number));
            } else if (number == -1 ) {
            	res.append(c.getString(R.string.message_external_files_dir,-1));
            } else {
                res.append(c.getString(R.string.message_sd_card));
            }
            if (readonly) {
                res.append(c.getString(R.string.message_read_only_sd_card));
            }
            return res.toString();
        }
        
//        public String toString() {
//        	String res=this.getDisplayName()+" ("+path+", ro:"+readonly+", rm:"+removable+", num:"+number+")";
//        	return res;
//        }
    }
    

    public static String getDefaultStorage() {
    	return Environment.getExternalStorageDirectory().getAbsolutePath();
    }
    
    public static List<StorageInfo> getStorageList() {

        List<StorageInfo> list = new ArrayList<StorageInfo>();
        String def_path = Environment.getExternalStorageDirectory().getPath();
        boolean def_path_removable = Environment.isExternalStorageRemovable();
        String def_path_state = Environment.getExternalStorageState();
        boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
                                    || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);

        HashSet<String> paths = new HashSet<String>();
        int cur_removable_number = 1;

        if (def_path_available) {
            paths.add(def_path);
            list.add(0, new StorageInfo(def_path, def_path_readonly, def_path_removable, def_path_removable ? cur_removable_number++ : -1));
        }

        BufferedReader buf_reader = null;
        try {
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
           // Log.d(TAG, "/proc/mounts");
            while ((line = buf_reader.readLine()) != null) {
              // Log.d(TAG, line);
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    @SuppressWarnings("unused")
					String unused = tokens.nextToken(); //device
                    String mount_point = tokens.nextToken(); //mount point
                    if (paths.contains(mount_point)) {
                        continue;
                    }
                    unused = tokens.nextToken(); //file system
                    List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                    boolean readonly = flags.contains("ro");

                    if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure")
                            && !line.contains("/mnt/asec")
                            && !line.contains("/mnt/obb")
                            && !line.contains("/dev/mapper")
                            && !line.contains("tmpfs")) {
                            paths.add(mount_point);
                            list.add(new StorageInfo(mount_point, readonly, true, cur_removable_number++));
                        }
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {}
            }
        }
        return list;
    }
    
    public static String getAvailableXmlPath(Context context){
    	SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
    	String storage = config.getString(context.getString(R.string.config_key_storage), StorageUtils.getDefaultStorage());
    	String res = "";
    	if (storage.contains(context.getApplicationContext().getPackageName())) {
    		res = storage+"/"+context.getString(R.string.available_xml_file); 
    	} else {
    		res = storage+"/"+context.getApplicationContext().getPackageName()+"/"+context.getString(R.string.available_xml_file);
    	}
    	return res;
    }
    
    public static String getDBDirPath(Context context){
    	SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
    	String storage = config.getString(context.getString(R.string.config_key_storage), StorageUtils.getDefaultStorage());
    	String res = "";
    	if (storage.contains(context.getApplicationContext().getPackageName())) {
    		res = storage+"/"+context.getString(R.string.DBDir); 
    	} else {
    		res = storage+"/"+context.getApplicationContext().getPackageName()+"/"+context.getString(R.string.DBDir);
    	}
    	return res;
    }
}
