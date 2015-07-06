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
    public boolean end = false;
    public List<Audio> audio = new ArrayList<>();
    public List<Action> actions = new ArrayList<>();

}
