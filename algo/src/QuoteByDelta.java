import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IContainer;
import com.optionscity.freeway.api.IJobSetup;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by demo01 on 2/15/2017.
 */
public class QuoteByDelta extends AbstractJob {

    double minDelta, maxDelta, minDTE, maxDTE;
    Set<String> instrumentIds;
    static final double DELTA_MULTIPLIER = 100;

    @Override
    public void install(IJobSetup iJobSetup) {
        iJobSetup.addVariable("Symbol", "Option symbol","String","" );
        iJobSetup.addVariable("Min Delta", "Min Delta","double","" );
        iJobSetup.addVariable("Max Delta", "Max Delta","double","" );
        iJobSetup.addVariable("Min DTE", "Min Days to Expiration","double","" );
        iJobSetup.addVariable("Max DTE", "Max Days to Expiration","double","" );
    }

    public void begin(IContainer container) {
        super.begin(container);
        getDelta();
        getDTE();
        getInstrumentIds();
        // TODO start quoting update HashSet on price update
    }

    private void getInstrumentIds() {
        String symbol = getStringVar("Symbol").toUpperCase();
        if (!instruments().getAllSymbols().contains(symbol)){
            container.stopJob(symbol + " is not a valid symbol");
        }
        instrumentIds = new HashSet<>();
        for (String instrumentId : instruments().getInstrumentIds(symbol+";;;;;;;")){
            double dte = instruments().getDaysToExpiration(instrumentId);
            if (minDTE <= dte && dte <= maxDTE){
                double delta = Math.abs(theos().getGreeks(instrumentId).delta) * DELTA_MULTIPLIER;
                if (minDelta <= delta && delta <= maxDelta){
                    instrumentIds.add(instrumentId);
                }
            }
        }
    }

    public void getDelta(){
        minDelta = getDoubleVar("Min Delta");
        maxDelta = getDoubleVar("Max Delta");
        if (minDelta < 0 || maxDelta > 100) {
            container.stopJob("Delta must be between 0 and 100.");
        }
        if (maxDelta < minDelta) {
            container.stopJob("Max must be greater than min!");
        }
    }
    
    public void getDTE(){
        minDTE = getDoubleVar("Min DTE");
        maxDTE = getDoubleVar("Max DTE");
        if (minDTE < 0) {
            container.stopJob("DTE must be greater than 0.");
        }
        if (maxDTE < minDTE) {
            container.stopJob("Hey knuckle head max must be greater than min!");
        }
    }
}
