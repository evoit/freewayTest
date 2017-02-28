import com.optionscity.freeway.api.messages.Signal;

/**
 * Created by evoit on 2/28/2017.
 */
public class FairValueSignal extends Signal {
    public final String instrumentId;
    public final double theo;

    public FairValueSignal(String instrumentId, double theo){
        //super(null, "Theo value changed");
        this.instrumentId = instrumentId;
        this.theo = theo;
    }
}
