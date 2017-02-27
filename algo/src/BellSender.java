import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IContainer;
import com.optionscity.freeway.api.IJobSetup;

/**
 * Created by demo01 on 2/27/2017.
 */
public class BellSender extends AbstractJob {
    String bellMessage;

    @Override
    public void install(IJobSetup iJobSetup) {
        iJobSetup.addVariable("BellMessage", "Bell Message","String","" );
    }

    public void begin(IContainer container) {
        super.begin(container);
        bellMessage = getStringVar("BellMessage");
    }
    public void onTimer(){
        container.signal(new TelephoneSignal(bellMessage));
    }
}
