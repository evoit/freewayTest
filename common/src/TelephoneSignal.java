import com.optionscity.freeway.api.messages.Signal;

/**
 * Created by demo01 on 2/27/2017.
 */
public class TelephoneSignal extends Signal {
    public final String message;

    public TelephoneSignal(String message) {
        this.message = message;

    }
}
