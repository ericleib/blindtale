package tk.thebrightstuff.blindtale.tale;

import org.simpleframework.xml.Attribute;

/**
 * Created by niluje on 10/07/15.
 *
 */
public class Voice {

    @Attribute
    private String id;
    @Attribute
    private float pitch;
    @Attribute
    private float rate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
}
