package tk.thebrightstuff.blindtale.utils;

/**
 * Created by niluje on 02/07/15.
 *
 */
public interface Callback<T> {
    void callback(T data, Exception e);
}
