package tk.thebrightstuff.blindtale.utils;

/**
 * Created by niluje on 08/07/15.
 *
 */
public interface Log {
    void info(String tag, String message);
    void error(String tag, String message, Exception e);
}
