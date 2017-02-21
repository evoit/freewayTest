import com.optionscity.freeway.api.*;

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
    IGrid bidGammaGrid;
    IGrid bidDeltaGrid;
    IGrid midGammaGrid;
    IGrid midDeltaGrid;
    IGrid askGammaGrid;
    IGrid askDeltaGrid;
    static final double DELTA_MULTIPLIER = 100;
    static final double GAMMA_MULTIPLIER = 100000;

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
        // Add grid configured in Freeway
        container.addGrid("BidGammaGrid",new String[]{"BidGamma"});
        bidGammaGrid = container.getGrid("BidGammaGrid");
        container.addGrid("MidGammaGrid",new String[]{"MidGamma"});
        midGammaGrid = container.getGrid("MidGammaGrid");
        container.addGrid("AskGammaGrid",new String[]{"AskGamma"});
        askGammaGrid = container.getGrid("AskGammaGrid");        
        // Add grid configured in Freeway
        container.addGrid("BidDeltaGrid",new String[]{"BidDelta"});
        bidDeltaGrid = container.getGrid("BidDeltaGrid");
        container.addGrid("MidDeltaGrid",new String[]{"MidDelta"});
        midDeltaGrid = container.getGrid("MidDeltaGrid");
        container.addGrid("AskDeltaGrid",new String[]{"AskDelta"});
        askDeltaGrid = container.getGrid("AskDeltaGrid");
    }

    public void gammaPop() {
        for (String instrumentId : instrumentIds) {
            // only use outrights
            InstrumentDetails instrumentDetails = instruments().getInstrumentDetails(instrumentId);
            double atmPrice = getCleanMidMktPrice(instrumentDetails.underlyingId);
            double strike = instrumentDetails.strikePrice;
            InstrumentDetails.Type type = instrumentDetails.type;
            boolean isOtm = (strike > atmPrice && InstrumentDetails.Type.CALL.equals(type)) || (strike <= atmPrice && InstrumentDetails.Type.PUT.equals(type));
            if (isOtm) {
                double gamma = theos().getGreeks(instrumentId).gamma * GAMMA_MULTIPLIER;
                double delta = theos().getGreeks(instrumentId).delta * DELTA_MULTIPLIER;
                strikeToMidDeltaMap.put(strike, delta);
                strikeToMidGammaMap.put(strike, gamma);
                double bidGamma = theos().getBidGreeks(instrumentId).gamma * GAMMA_MULTIPLIER;
                double bidDelta = theos().getBidGreeks(instrumentId).delta * DELTA_MULTIPLIER;
                strikeToBidDeltaMap.put(strike, bidDelta);
                strikeToBidGammaMap.put(strike, bidGamma);
                double askGamma = theos().getAskGreeks(instrumentId).gamma * GAMMA_MULTIPLIER;
                double askDelta = theos().getAskGreeks(instrumentId).delta * DELTA_MULTIPLIER;
                strikeToAskDeltaMap.put(strike, askDelta);
                strikeToAskGammaMap.put(strike, askGamma);

                log("For " + instrumentId + "The bid delta is " + bidDelta + " gamma is " + bidGamma);
                log("For " + instrumentId + "The mid delta is " + delta + " gamma is " + gamma);
                log("For " + instrumentId + "The ask delta is " + askDelta + " gamma is " + askGamma);

                // TODO Update the grids
                bidGammaGrid.set(instrumentId, "BidGamma", bidGamma);
                midGammaGrid.set(instrumentId, "MidGamma", gamma);
                askGammaGrid.set(instrumentId, "AskGamma", askGamma);
                bidDeltaGrid.set(instrumentId, "BidDelta", bidDelta);
                midDeltaGrid.set(instrumentId, "MidDelta", delta);
                askDeltaGrid.set(instrumentId, "AskDelta", askDelta);
            }
        }
    }

    private double getCleanMidMktPrice(String instrumentId) {
        Prices prices = instruments().getMarketPrices(instrumentId);
        if (Double.isNaN(prices.bid) && Double.isNaN(prices.ask)) {
            return Double.NaN;
        } else if (Double.isNaN(prices.ask)) {
            return prices.bid;
        } else if (Double.isNaN(prices.bid)) {
            return prices.ask;
        } else {
            return 0.5*(prices.ask + prices.bid);
        }
    }

    // Load all instrument ids for the desired instrument month
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
