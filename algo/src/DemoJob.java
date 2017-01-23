import com.optionscity.freeway.api.*;
import com.optionscity.freeway.api.messages.MarketBidAskMessage;

import java.util.Collection;

/**
 * Created by demo01 on 1/11/2017.
 */
public class DemoJob extends AbstractJob{
    String instr;
    Collection<String> instrumentIds;
    @Override
    public void install(IJobSetup setup) {
        setup.addVariable("Instruments","Instrument to configure job on","instruments","");
    }
    public void begin(IContainer container) {
        super.begin(container);
        container.subscribeToMarketBidAskMessages();
        instr = container.getVariable("Instruments");
        log(instr);
        //container.filterMarketMessages(instr);
        instrumentIds = instruments().getInstrumentIds(instr);
        for (String instrumentId : instrumentIds) {
            Greeks greeks = theos().getGreeks(instrumentId);
            log("Delta for " + instrumentId + " is " + greeks.delta);
        }
    }
    public void onTimer(){
        debug("timer invoked every second");
    }
    public void onMarketBidAsk(MarketBidAskMessage msg) {
        /*Prices topOfBook = instruments().getTopOfBook(msg.instrumentId);
        log(msg.instrumentId);
        log("Bid is " + topOfBook.bid + " Qty is " + topOfBook.bid_size);
        log("Ask is " +topOfBook.ask + " Qty is " + topOfBook.ask_size);
    */}

}
