import com.optionscity.freeway.api.*;
import com.optionscity.freeway.api.messages.MarketBidAskMessage;
import com.optionscity.freeway.api.messages.MarketLastMessage;

/**
 * Created by demo01 on 1/23/2017.
 * This freeway Class extends AbstractJob
 */
public class TrackerJob extends AbstractJob {
    /**
     *
     */
    String instrumentID;
    private String underlyingInstrumentId;
    private String choice;
    private double lastPrice;
    private double optionMid;
    private double futureMid;
    private double impliedVol;
    // Question I think we did this to avoid a Null value for futurePrices. What is the value of futurePrices now that it's initialized?
    private Prices futurePrices = new Prices();
    private Prices optionPrices = new Prices();
    IGrid volGrid;

    /*
    every new freeway algo needs to override the abstract method install()
    this is where we declare job defined variables accessible in the algo's Configure pain.
    */
    @Override
    public void install(IJobSetup iJobSetup) {
        iJobSetup.addVariable("Instrument", "Instrument to Track", "instrument", "");
        iJobSetup.addVariable("TrackedMetric", "Implied Vol or Last Trade", "choice:Implied Vol;Last Trade", "Implied Vol");
    }

    // begin( ) tells the job how it should start and is invoked before anything else once the job is started.
    public void begin(IContainer container) {
        // allows job to access superclass convenience methods. Actor model implementing event-handler/event call back methods on a single thread.
        super.begin(container);
        // assign global variable instrumentID to job variable Instrument.
        instrumentID = container.getVariable("Instrument");
        underlyingInstrumentId = instruments().getInstrumentDetails(instrumentID).underlyingId;
        choice = container.getVariable("TrackedMetric");
        // Add grid configured in Freeway
        container.addGrid("VolGrid",new String[]{"AtmVol"});
        volGrid = container.getGrid("VolGrid");
        /* Logic to decide what to do if job is configured to track Last Trade or Implied Vol
         * we use "Implied Vol".equals(choice) to avoid null pointer exception if choice is not configured in the job */
        if (underlyingInstrumentId == null && "Implied Vol".equals(choice)) {
            container.stopJob("No Underlying for "  + instrumentID);
        } else if (!"Last Trade".equals(choice)){
            // if our job is not configured to Last Trade and not Null for underlyingInstrumentId then it must be tracking ATM Vol.
            container.filterMarketMessages(underlyingInstrumentId);
            futureMid = getCleanMidMkt(underlyingInstrumentId);
            optionMid = getCleanMidMkt(instrumentID);
            updateVol();
        }
        // Once we subscribe to container.subcribeToMarketLastMessages() we must now implement onMarketLast()
        container.subscribeToMarketLastMessages();
        // Limit messages received by the job to those that pertain only to the selected instrumentID
        container.filterMarketMessages(instrumentID);

        // Update initial tracker variables
        lastPrice = instruments().getMarketPrices(instrumentID).last;
        log("last price is: " + lastPrice);
    }
    // implementation of onMarketLast() because we call container.subscribeToMarketLastMessages() in begin()
    public void onMarketLast(MarketLastMessage msg) {
        if ("Last Trade".equals(choice)) {
            lastPrice = msg.price;
            log("last price is: " + lastPrice);
        }
        //TODO update grids
    }
    // Question though we have implemented instances of Prices class we never called subscribeToMarketBidAskMessages()
    public void onMarketBidAsk(MarketBidAskMessage m) {
        if ("Implied Vol".equals(choice) && !isPricesTheSame(m.instrumentId)) {
            if (m.instrumentId.equals(instrumentID)) {
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
            }
            updateVol();
        }
    }

    private void updateVol() {
        impliedVol = theos().calculateImpliedVolatility(instrumentID, optionMid, futureMid);

        //TODO update grid with last implied vol
        volGrid.set(instrumentID, "AtmVol", impliedVol);
    }

    private double getCleanMidMkt (String instrumentID) {
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

    private boolean isPricesTheSame(String instrumentId){
        Prices lastTrackedPrices;
        if (instrumentId.equals(instrumentID)) {
            lastTrackedPrices = optionPrices;
        } else if (instrumentId.equals(underlyingInstrumentId)) {
            lastTrackedPrices = futurePrices;
        } else {
            lastTrackedPrices = new Prices();
        }

        Prices prices=instruments().getMarketPrices(instrumentId);
        return prices.bid == lastTrackedPrices.bid && prices.ask == lastTrackedPrices.ask;
    }
}
