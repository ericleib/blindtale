package tk.thebrightstuff.blindtale;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by niluje on 17/06/15.
 *
 */
public class Scene implements Serializable {

    public Tale tale;
    public String id;
    public String title;
    public String soundPath;
    public List<Action> actions = new ArrayList<>();

    public File getSoundFile() {
        return new File(new File(tale.getTaleFolder(),"sounds"),soundPath);
    }
}
