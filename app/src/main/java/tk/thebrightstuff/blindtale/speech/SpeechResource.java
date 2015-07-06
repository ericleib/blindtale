package tk.thebrightstuff.blindtale.speech;

import java.util.Locale;
import java.util.Set;

/**
 * Created by niluje on 02/07/15.
 *
 */
public interface SpeechResource {
    Locale getLocale();
    Set<String> getKeywords();
}
