package tk.thebrightstuff.blindtale;

import android.net.Uri;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by niluje on 17/06/15.
 */
public class Scene implements Serializable {

    public Tale tale;
    public String id;
    public String soundPath;
    public List<Action> actions = new ArrayList<Action>();

    public File getSoundFile() {
        return new File(new File(tale.getTaleFolder(),"sounds"),soundPath);
    }
}
