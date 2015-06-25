package tk.thebrightstuff.blindtale.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by niluje on 17/06/15.
 *
 */
public class AssetCopier {

    private static String TAG = "Asset copier";

    public static boolean copyAssets(Context context){
        String dest = context.getDir("", Context.MODE_PRIVATE).getAbsolutePath();
        return copyFileOrDir(context, "", dest);
    }

    private static boolean copyFileOrDir(Context context, String path, String dest) {
        try {
            Log.i(TAG, "copyFileOrDir() " + path);
            String assets[] = context.getAssets().list(path);
            if (assets.length == 0) {   // It's a file
                copyFile(context, path, dest);
            } else if(!path.startsWith("sync") && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit")){                    // It's a folder
                File fullPath =  new File(dest + path);
                if(!fullPath.exists()){
                    Log.i(TAG, "Creating dir " + fullPath.getAbsolutePath());
                    fullPath.mkdir();
                    if(!fullPath.exists())
                        throw new Exception("Somehow failed to create the directory "+fullPath.getAbsolutePath());
                }
                String p = path.equals("")? "" : path + File.separator;
                for (String asset : assets)
                    if(!copyFileOrDir(context, p + asset, dest))
                        return false;
            }
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "I/O Exception", ex);
            return false;
        }
    }

    private static void copyFile(Context context, String filename, String dest) throws Exception {
        String newFileName = dest + filename;
        try {
            InputStream in = context.getAssets().open(filename);
            Log.i(TAG, "copyFile() " + filename + " to " + newFileName);
            OutputStream out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e(TAG, "Exception in copyFile() of "+newFileName);
            Log.e(TAG, "Exception in copyFile() "+e.toString());
            throw new Exception("Could not copy "+newFileName, e);
        }

    }
}
