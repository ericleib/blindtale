package tk.thebrightstuff.blindtale.tale;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

import java.io.Serializable;

/**
 * Created by niluje on 08/07/15.
 *
 */

public class Credit implements Serializable {

    @Attribute
    private String role;
    @Text
    private String name;


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}