import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IContainer;
import com.optionscity.freeway.api.IJobSetup;
import com.optionscity.freeway.api.messages.MarketLastMessage;

/**
 * Created by demo01 on 1/23/2017.
 */
public class TrackerJob extends AbstractJob {
    String instrumentID;
    String choice;
    double lastPrice;


    @Override
    public void install(IJobSetup iJobSetup) {
        iJobSetup.addVariable("Instrument", "Instrument to Track", "instrument", "");
        iJobSetup.addVariable("TrackedMetric", "Implied Vol or Last Trade", "choice:Implied Vol;Last Trade", "Implied Vol");

    }
    public void begin (IContainer container){
        super.begin(container);
        instrumentID = container.getVariable("Instrument");
        choice = container.getVariable("TrackedMetric");
        container.subscribeToMarketLastMessages();
        container.filterMarketMessages(instrumentID);
        lastPrice = instruments().getMarketPrices(instrumentID).last;
        log("last price is: " + lastPrice);
    }

    public void onMarketLast(MarketLastMessage msg){
        if ("Last Trade".equals(choice)) {
            lastPrice = msg.price;
            log("last price is: " + lastPrice);
        }
    }
}
