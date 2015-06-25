package tk.thebrightstuff.blindtale.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Created by niluje on 24/06/15.
 *
 */
public class StringUtils {

    public static String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String removeAccents(String in){
        String nfdNormalizedString = Normalizer.normalize(in, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

}
