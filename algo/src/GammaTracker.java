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
    SortedMap<Double, Double> strikeToMidDeltaMap;
    SortedMap<Double, Double> strikeToMidGammaMap;
    SortedMap<Double, Double> strikeToBidDeltaMap;
    SortedMap<Double, Double> strikeToBidGammaMap;
    SortedMap<Double, Double> strikeToAskDeltaMap;
    SortedMap<Double, Double> strikeToAskGammaMap;
    Set<String> instrumentIds;

    @Override
    public void install(IJobSetup iJobSetup) {
        iJobSetup.addVariable("Instrument Month", "Instrument Month","String","" );
    }
    public void begin(IContainer container) {
        super.begin(container);
        loadInstrumentIds();
        strikeToMidDeltaMap = new TreeMap<>();
        strikeToMidGammaMap = new TreeMap<>();
        strikeToBidDeltaMap = new TreeMap<>();
        strikeToBidGammaMap = new TreeMap<>();
        strikeToAskDeltaMap = new TreeMap<>();
        strikeToAskGammaMap = new TreeMap<>();
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
                strikeToMidDeltaMap.put(strike, delta);
                strikeToMidGammaMap.put(strike, gamma);
                double bidGamma = theos().getBidGreeks(instrumentId).gamma;
                double bidDelta = theos().getBidGreeks(instrumentId).delta;
                strikeToBidDeltaMap.put(strike, bidDelta);
                strikeToBidGammaMap.put(strike, bidGamma);
                double askGamma = theos().getAskGreeks(instrumentId).gamma;
                double askDelta = theos().getAskGreeks(instrumentId).delta;;
                strikeToAskDeltaMap.put(strike, askDelta);
                strikeToAskGammaMap.put(strike, askGamma);

                log("The bid delta is " + bidDelta + " gamma is " + bidGamma);
                log("The mid delta is " + delta + " gamma is " + gamma);
                log("The ask delta is " + askDelta + " gamma is " + askGamma);
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
    private void loadInstrumentIds(){
        instrumentMonth = container.getVariable("Instrument Month");
        instrumentIds = new HashSet<>();

        for (String symbol : instruments().getAllSymbols()) {
            for (String instrumentId : instruments().getInstrumentIds(symbol + ";;;;;;;")) {
                InstrumentDetails instrumentDetails = instruments().getInstrumentDetails(instrumentId);
                if (instrumentMonth.equals(instrumentDetails.instrumentMonth)) {
                    instrumentIds.add(instrumentId);
                }
            }
        }
    }

    public void onTimer(){
        gammaPop();
        //TODO make another method to dump the contents of the maps into grids
    }
}
