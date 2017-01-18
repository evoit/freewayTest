import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IContainer;
import com.optionscity.freeway.api.IJobSetup;
import com.optionscity.freeway.api.Prices;
import com.optionscity.freeway.api.messages.MarketBidAskMessage;

/**
 * Created by demo01 on 1/11/2017.
 */
public class DemoJob extends AbstractJob{
    String instr;
    @Override
    public void install(IJobSetup setup) {
        setup.addVariable("Instrument","Instrument to configure job on","instrument","");
    }
    public void begin(IContainer container) {
        super.begin(container);
        container.subscribeToMarketBidAskMessages();
        instr = container.getVariable("Instrument");
        log(instr);
        container.filterMarketMessages(instr);
    }
    public void onTimer(){
        debug("timer invoked every second");
    }
    public void onMarketBidAsk(MarketBidAskMessage msg) {
        Prices topOfBook = instruments().getTopOfBook(msg.instrumentId);
        log(msg.instrumentId);
        log("Bid is " + topOfBook.bid + " Qty is " + topOfBook.bid_size);
        log("Ask is " +topOfBook.ask + " Qty is " + topOfBook.ask_size);
    }

}
