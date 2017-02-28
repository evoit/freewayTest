import com.optionscity.freeway.api.*;
import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IContainer;
import com.optionscity.freeway.api.IJobSetup;
import com.optionscity.freeway.api.messages.MarketBidAskMessage;

/**
 * Created by evoit on 2/28/2017.
 */
public class FairValueSender extends AbstractJob {
    // Declare global variables
    String instrumentId;
    private String underlyingInstrumentId;
    private double optionMid;
    private double futureMid;
    private Prices futurePrices = new Prices();
    private Prices optionPrices = new Prices();
    double optionTheoValue;
    double currentOptionTheoValue;

    @Override
    public void install(IJobSetup iJobSetup) {
        iJobSetup.addVariable("Instrument", "Instrument", "instrument", "");
    }
    public void begin(IContainer container) {
        super.begin(container);
        instrumentId = container.getVariable("Instrument");
        underlyingInstrumentId = instruments().getInstrumentDetails(instrumentId).underlyingId;
        container.subscribeToMarketBidAskMessages();

        if (underlyingInstrumentId == null) {
            container.stopJob("No Underlying for "  + instrumentId);
        } else {
            // filter only for selected instrument
            container.filterMarketMessages(underlyingInstrumentId);
            // can make the signal more robust int he future but just going to pull an option theo for now
            futureMid = getCleanMidMkt(underlyingInstrumentId);
            optionMid = getCleanMidMkt(instrumentId);
            currentOptionTheoValue = theos().getGreeks(instrumentId).theo;
            optionTheoValue = theos().getGreeks(instrumentId).theo;
        }
        container.subscribeToMarketBidAskMessages();
        // Limit messages received by the job to those that pertain only to the selected instrumentID
        container.filterMarketMessages(instrumentId);
    }


    public void onMarketBidAsk(MarketBidAskMessage m) {
        if (!isPricesTheSame(m.instrumentId)) {
            /*if (m.instrumentId.equals(instrumentId)) {
                double mid = getCleanMidMkt(m.instrumentId);
                if (!Double.isNaN(mid)) {
                    optionMid = mid;
                }
                optionPrices = instruments().getAllPrices(m.instrumentId);
            } else {
                double mid = getCleanMidMkt(m.instrumentId);
                if (!Double.isNaN(mid)){
                    futureMid = mid;
                }
                futurePrices = instruments().getAllPrices(m.instrumentId);
            } */
            updateTheo();
        }
    }

    private void updateTheo() {
        optionTheoValue = theos().getGreeks(instrumentId).theo;
        if (optionTheoValue != currentOptionTheoValue) {
            log("The new theo value for " + instrumentId + " is " + optionTheoValue);
            container.signal(new FairValueSignal(instrumentId, optionTheoValue));
            currentOptionTheoValue = optionTheoValue;
        }
    }

    // get a clean mid market price for theo value calculation
    private double getCleanMidMkt(String instrumentID) {
        Prices prices = instruments().getMarketPrices(instrumentID);
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

    // only update if prices have changed
    private boolean isPricesTheSame(String instrument){
        Prices lastTrackedPrices;
        if (instrument.equals(instrumentId)) {
            lastTrackedPrices = optionPrices;
        } else if (instrument.equals(underlyingInstrumentId)) {
            lastTrackedPrices = futurePrices;
        } else {
            lastTrackedPrices = new Prices();
        }
        Prices prices=instruments().getMarketPrices(instrumentId);
        return prices.bid == lastTrackedPrices.bid && prices.ask == lastTrackedPrices.ask;
    }
}
