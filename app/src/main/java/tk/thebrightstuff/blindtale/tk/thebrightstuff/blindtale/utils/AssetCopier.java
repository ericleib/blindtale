package tk.thebrightstuff.blindtale.tk.thebrightstuff.blindtale.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by niluje on 17/06/15.
 */
public class AssetCopier {

    private static String TAG = "Asset copier";

    public static boolean copyAssets(Context context){
        String dest = context.getDir("", Context.MODE_PRIVATE).getAbsolutePath();
        return copyFileOrDir(context, "", dest);
    }

    private static boolean copyFileOrDir(Context context, String path, String dest) {
        String assets[] = null;
        try {
            Log.i(TAG, "copyFileOrDir() " + path);
            assets = context.getAssets().list(path);
            if (assets.length == 0) {
                copyFile(context, path, dest);
            } else {
                File fullPath =  new File(dest + path);
                if(!fullPath.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit")){
                    Log.i(TAG, "Creating dir " + fullPath.getAbsolutePath());
                    fullPath.mkdir();
                    if(!fullPath.exists())
                        throw new Exception("Somehow failed to create the directory "+fullPath.getAbsolutePath());
                }

                for (int i = 0; i < assets.length; ++i) {
                    String p = path.equals("")? "" : path + File.separator;
                    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                        if(!copyFileOrDir(context, p + assets[i], dest))
                            return false;
                }
            }
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "I/O Exception", ex);
            return false;
        }
    }

    private static void copyFile(Context context, String filename, String dest) throws Exception {
        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            in = context.getAssets().open(filename);
            newFileName = dest + filename;
            Log.i(TAG, "copyFile() " + filename + " to " + newFileName);
            out = new FileOutputStream(newFileName);

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
