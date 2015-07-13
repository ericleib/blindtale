package tk.thebrightstuff.blindtale.tale;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.Validate;

import java.io.Serializable;

/**
 * Created by niluje on 08/07/15.
 *
 */
public class Audio extends AbstractConditional implements Serializable {

    @Attribute(required=false)
    private String voice;
    @Text(required=false)
    private String text;
    @Attribute(required=false)
    private String file;

    @Validate
    public void validate() throws Exception {
        if((text==null || text.trim().equals("")) && file==null)
            throw new Exception("Audio must have either some text or a link to a file");

        if((text!=null && (!text.trim().equals(""))) && file!=null)
            throw new Exception("Audio cannot have both text and a link to a file");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voiceId) {
        this.voice = voiceId;
    }
}
