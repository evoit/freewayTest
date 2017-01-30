import com.optionscity.freeway.api.*;
import com.optionscity.freeway.api.messages.MarketBidAskMessage;
import com.optionscity.freeway.api.messages.MarketLastMessage;

import java.util.Collection;

/**
 * Created by demo01 on 1/30/2017.
 * This freeway Class extends AbstractJob
 */
public class MultiTrackerJob extends AbstractJob {
    /**
     *
     */
    private Prices futurePrices = new Prices();
    private Prices optionPrices = new Prices();
    IGrid volGrid;
    Collection<String> instrumentIds;

    /*
    every new freeway algo needs to override the abstract method install()
    this is where we declare job defined variables accessible in the algo's Configure pain.
    */
    @Override
    public void install(IJobSetup iJobSetup) {
        iJobSetup.addVariable("Instruments", "Instrument to Track", "instruments", "");
    }

    // begin( ) tells the job how it should start and is invoked before anything else once the job is started.
    public void begin(IContainer container) {
        // allows job to access superclass convenience methods. Actor model implementing event-handler/event call back methods on a single thread.
        super.begin(container);
        // assign global variable instrumentID to job variable Instrument.
        instrumentIds = instruments().getInstrumentIds(container.getVariable("Instruments"));
        container.subscribeToMarketBidAskMessages();

        for (String instrumentId : instrumentIds) {
            String underlyingInstrumentId = instruments().getInstrumentDetails(instrumentId).underlyingId;
            if (underlyingInstrumentId == null) {
                debug("No Underlying for "  + instrumentId);
            }
            else {
                container.filterMarketMessages(instrumentId);
                container.filterMarketMessages(underlyingInstrumentId);
                debug("Subscribing to " + instrumentId + " with the underlying " + underlyingInstrumentId);
                updateVol(instrumentId);

            }
        }

        // Add grid configured in Freeway
        container.addGrid("VolGrid",new String[]{"AtmVol"});
        volGrid = container.getGrid("VolGrid");
        /* Logic to decide what to do if job is configured to track Last Trade or Implied Vol
         * we use "Implied Vol".equals(choice) to avoid null pointer exception if choice is not configured in the job */

        // if our job is not configured to Last Trade and not Null for underlyingInstrumentId then it must be tracking ATM Vol.
    }
    public void onMarketBidAsk(MarketBidAskMessage m) {
        //TODO make this work aka fix instrument
        if (!isPricesTheSame(m.instrumentId)) {
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

    private void updateVol(String instrumentId) {
        String underlyingInstrumentId = instruments().getInstrumentDetails(instrumentId).underlyingId;
        double futureMid = getCleanMidMkt(underlyingInstrumentId);
        double optionMid = getCleanMidMkt(instrumentId);
        double impliedVol = theos().calculateImpliedVolatility(instrumentId, optionMid, futureMid);
        //TODO update grid with last implied vol
        volGrid.set(instrumentId, "AtmVol", impliedVol);
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
        //TODO Fix dis
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
