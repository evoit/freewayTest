import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IContainer;
import com.optionscity.freeway.api.IJobSetup;
import com.optionscity.freeway.api.InstrumentDetails;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by demo01 on 2/16/2017.
 */
public class GammaTracker extends AbstractJob {

    String instrumentMonth;
    SortedMap<Double, Double> strikeToDeltaMap;
    SortedMap<Double, Double> strikeToGammaMap;
    Set<String> instrumentIds;

    @Override
    public void install(IJobSetup iJobSetup) {
        iJobSetup.addVariable("Instrument Month", "Instrument Month","String","" );
    }
    public void begin(IContainer container) {
        super.begin(container);
        loadInstrumentIds();
        strikeToDeltaMap = new TreeMap<>();
        strikeToGammaMap = new TreeMap<>();
    }


    // TODO add two more maps--one for bid and offer instead of mid
    public void gammaPop() {
        for (String instrumentId : instrumentIds) {
            // only use outrights
            InstrumentDetails instrumentDetails = instruments().getInstrumentDetails(instrumentId);
            double atmPrice = getCleanMidMktPrice(instrumentDetails.underlyingId);
            double strike = instrumentDetails.strikePrice;
            InstrumentDetails.Type type = instrumentDetails.type;
            boolean isOtm = (strike > atmPrice && InstrumentDetails.Type.CALL.equals(type)) || (strike <= atmPrice && InstrumentDetails.Type.PUT.equals(type));
            if (isOtm) {
                //theos().getAskGreeks()
                double gamma = theos().getGreeks(instrumentId).gamma;
                double delta = theos().getGreeks(instrumentId).delta;
                strikeToDeltaMap.put(strike, delta);
                strikeToGammaMap.put(strike, gamma);
            }
        }
    }

    // TODO
    /**
     * Returns mid market price if both sides are available. If one side is missing, return the other side
     *
     * @param instrumentId
     * @return
     */
    private double getCleanMidMktPrice(String instrumentId) {
        return 0d;
    }

    // Load all instruemnt ids for the desired instrument moonth
    public void loadInstrumentIds(){
        instrumentMonth = container.getVariable("Instrument Month");
        instrumentIds = new HashSet<>();
        for (String instrumentId : instruments().getInstrumentIds(";;;;;;;")){
            InstrumentDetails instrumentDetails = instruments().getInstrumentDetails(instrumentId);
            if (instrumentMonth.equals(instrumentDetails.instrumentMonth)) {
                instrumentIds.add(instrumentId);
            }
        }
    }
    public void onTimer(){
        gammaPop();
        //TODO make another method to dump the contents of the maps into grids
    }
}
