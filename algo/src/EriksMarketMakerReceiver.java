import com.optionscity.freeway.api.*;
import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IJobSetup;

/**
 * Created by evoit on 2/28/2017.
 */
public class EriksMarketMakerReceiver extends AbstractJob {
    // Declare global variables
    String instrumentId;
    String signalInstrumentId;
    double theo;

    @Override
    public void install(IJobSetup iJobSetup) {
        iJobSetup.setDefaultDescription("Receives instrument and theo. Manages market bracket");
        iJobSetup.addVariable("Instrument", "Instrument", "instrument", "");
    }

    public void begin(IContainer container) {
        super.begin(container);
        container.subscribeToSignals();
    }
    public void onSignal(FairValueSignal signal) {
        instrumentId = signal.instrumentId;
        theo = signal.theo;
        log(signal.sender+" says "+ signal.instrumentId + " new theo value is " + signal.theo);
    }
}
