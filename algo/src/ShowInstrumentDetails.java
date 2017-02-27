import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IContainer;
import com.optionscity.freeway.api.IJobSetup;
import com.optionscity.freeway.api.InstrumentDetails;

/**
 * Created by demo01 on 2/23/2017.
 */
public class ShowInstrumentDetails extends AbstractJob {
    @Override
    public void install(IJobSetup iJobSetup) {
        iJobSetup.addVariable("Instrument Id", "Instrument Id","String","" );
    }

    public void begin(IContainer container) {
        super.begin(container);
        String instrumentId = getStringVar("Instrument Id");

        /*for (String symbol : instruments().getAllSymbols()) {
            log("Found " + instruments().getInstrumentIds(symbol + ";;;;;;;").size() + " instruemnts for symbol " + symbol);
            for (String id : instruments().getInstrumentIds(symbol + ";;;;;;;")) {
                log("Found instrument id of " + id);
            }
        }*/



        log("Instrument id equals " + instrumentId);
        InstrumentDetails instrumentDetails = instruments().getInstrumentDetails(instrumentId);
        log("instrument details equals " + instrumentDetails );



    }
}
