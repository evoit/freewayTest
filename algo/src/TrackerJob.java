import com.optionscity.freeway.api.*;
import com.optionscity.freeway.api.messages.MarketBidAskMessage;
import com.optionscity.freeway.api.messages.MarketLastMessage;

/**
 * Created by demo01 on 1/23/2017.
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
    private Prices futurePrices = new Prices();
    private Prices optionPrices = new Prices();
    IGrid volGrid;


    @Override
    public void install(IJobSetup iJobSetup) {
        iJobSetup.addVariable("Instrument", "Instrument to Track", "instrument", "");
        iJobSetup.addVariable("TrackedMetric", "Implied Vol or Last Trade", "choice:Implied Vol;Last Trade", "Implied Vol");
    }

    public void begin(IContainer container) {
        super.begin(container);
        instrumentID = container.getVariable("Instrument");
        underlyingInstrumentId = instruments().getInstrumentDetails(instrumentID).underlyingId;
        choice = container.getVariable("TrackedMetric");
        container.addGrid("VolGrid",new String[]{"AtmVol"});
        volGrid = container.getGrid("VolGrid");
        if (underlyingInstrumentId == null && "Implied Vol".equals(choice)) {
            container.stopJob("No Underlying for "  + instrumentID);
        } else if (!"Last Trade".equals(choice)){
            container.filterMarketMessages(underlyingInstrumentId);
            futureMid = getCleanMidMkt(underlyingInstrumentId);
            optionMid = getCleanMidMkt(instrumentID);
            updateVol();
        }
        container.subscribeToMarketLastMessages();
        container.filterMarketMessages(instrumentID);

        // Update initial tracker variables
        lastPrice = instruments().getMarketPrices(instrumentID).last;
        log("last price is: " + lastPrice);

    }

    public void onMarketLast(MarketLastMessage msg) {
        if ("Last Trade".equals(choice)) {
            lastPrice = msg.price;
            log("last price is: " + lastPrice);
        }

        //TODO update grids

        /*else {
            lastPrice = msg.price;
            log("last price is: " + lastPrice);
            //Prices prices=instruments().getMarketPrices(instrumentID);
            //underlyingPrice= prices.getUnderlyingMidMarket();
            underlyingPrice = instruments().getMarketPrices(instrumentID).getUnderlyingMidMarket();
            log("underlying price is: " + underlyingPrice);
        }*/
    }

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

        /*else {
            lastPrice = msg.price;
            log("last price is: " + lastPrice);
            //Prices prices=instruments().getMarketPrices(instrumentID);
            //underlyingPrice= prices.getUnderlyingMidMarket();
            underlyingPrice = instruments().getMarketPrices(instrumentID).getUnderlyingMidMarket();
            log("underlying price is: " + underlyingPrice);
        }*/
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
