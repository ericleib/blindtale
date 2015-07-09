package tk.thebrightstuff.blindtale.tale;

import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.stream.HyphenStyle;
import org.simpleframework.xml.stream.Style;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.io.File;

/**
 * Created by niluje on 08/07/15.
 *
 */
public class TaleParser {

    private final Serializer serializer;

    public TaleParser(){
        Style style = new HyphenStyle();
        Format format = new Format(style);
        this.serializer = new Persister(new TaleMatcher(), format);
    }

    public Tale parse(File file) throws Exception {
        return serializer.read(Tale.class, file);
    }

    public class TaleMatcher implements Matcher {
        @Override
        public Transform<?> match(Class type) throws Exception {
            if(Condition.class.isAssignableFrom(type)){
                return new Transform<Condition>() {
                    @Override
                    public Condition read(String s) throws Exception {
                        return Condition.makeCondition(s);
                    }

                    @Override
                    public String write(Condition condition) throws Exception {
                        return condition.toString();
                    }
                };
            }
            return null;
        }
    }


}
