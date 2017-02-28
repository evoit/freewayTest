import com.optionscity.freeway.api.*;
import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IJobSetup;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by evoit on 2/28/2017.
 */
public class EriksMarketMakerReceiver extends AbstractJob {
    // Declare global variables
    String instrumentId;
    String signalInstrumentId;
    double theo;
    double minTick;
    double minEdge;
    int numberOfLayers;
    int orderQty;
    Map<Double, Long> buyOrderIds = new TreeMap<>();
    Map<Double, Long> sellOrderIds = new TreeMap<>();

    @Override
    public void install(IJobSetup iJobSetup) {
        iJobSetup.setDefaultDescription("Receives instrument and theo. Manages market bracket");
        iJobSetup.addVariable("Instrument", "Instrument", "instrument", "");
        iJobSetup.addVariable("minTick", "Min Tick", "double", ".25");
        iJobSetup.addVariable("minEdge", "Min Edge", "double", ".25");
        iJobSetup.addVariable("numberOfLayers", "Number Of Layers", "int", "3");
        iJobSetup.addVariable("orderQty", "order Quantity", "int", "3");
    }

    public void begin(IContainer container) {
        super.begin(container);
        container.subscribeToSignals();
        minTick = getDoubleVar("minTick");
        minEdge = getDoubleVar("minEdge");
        numberOfLayers = getIntVar("numberOfLayers");
        orderQty = getIntVar("orderQty");

    }
    public void onSignal(FairValueSignal signal) {
        instrumentId = signal.instrumentId;
        theo = signal.theo;
        log(signal.sender+" says "+ signal.instrumentId + " new theo value is " + signal.theo);
        double baseOffer = Math.ceil((theo + minEdge)/minTick) * minTick;
        double baseBid = Math.floor((theo - minEdge)/minTick) * minTick;
        //declare local tree maps
        Map<Double, Long> buyOrderIds = new TreeMap<>();
        Map<Double, Long> sellOrderIds = new TreeMap<>();
        // logic to place all layers of orders
        for (int i=0; i< numberOfLayers; i++){
            double bidPrice = baseBid - (i * minTick);
            if (!this.buyOrderIds.containsKey(bidPrice)){
                log("Placing order at bid price " + bidPrice);
                Long orderId = orders().submit(new OrderRequest(Order.Type.LIMIT, Order.Side.BUY, instrumentId, bidPrice, orderQty));
                buyOrderIds.put(bidPrice, orderId);
            }
            double offerPrice = baseOffer + (i * minTick);
            if (!this.sellOrderIds.containsKey(offerPrice)){
                log("Placing order at offer price " + offerPrice);
                Long orderId = orders().submit(new OrderRequest(Order.Type.LIMIT, Order.Side.SELL, instrumentId, offerPrice, orderQty));
                sellOrderIds.put(offerPrice, orderId);
            }
        }
        this.buyOrderIds = buyOrderIds;
        this.sellOrderIds = sellOrderIds;
    }
}
