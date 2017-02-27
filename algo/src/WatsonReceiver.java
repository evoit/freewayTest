import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IContainer;
import com.optionscity.freeway.api.IJobSetup;

/**
 * Created by demo01 on 2/27/2017.
 */
public class WatsonReceiver extends AbstractJob {
    @Override
    public void install(IJobSetup iJobSetup) {

    }
    public void begin(IContainer container){
        super.begin(container);
        container.subscribeToSignals();
    }

    public void onSignal(TelephoneSignal signal){
        log("Recieved signal from " + signal.sender + " with message " + signal.message );
    }
}
