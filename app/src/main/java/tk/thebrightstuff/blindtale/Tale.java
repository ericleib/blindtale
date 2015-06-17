package tk.thebrightstuff.blindtale;

import java.io.Serializable;

/**
 * Created by niluje on 17/06/15.
 */
public class Tale implements Serializable {

    private String title;

    private Scene scene;

    public String toString() {
        return title;
    }

    public Scene getScene() {
        return scene;
    }

    public static Tale getDummyTale() {
        Tale t = new Tale();
        t.title = "Toto's awesome adventure";
        t.scene = new Scene();
        return t;
    }

}
