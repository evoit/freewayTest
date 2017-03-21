import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IContainer;
import com.optionscity.freeway.api.IJobSetup;
import com.optionscity.freeway.api.InstrumentDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by evoit on 3/21/2017.
 */
public class RfqTest extends AbstractJob {
    // Global variables
    public String instrument1 = "";
    public String instrument2 = "";
    public Collection<InstrumentDetails.LegDetails> instrumentLegs;

    @Override
    public void install(IJobSetup IJobSetup) {
        IJobSetup.addVariable("Instrument1","First Instrument leg of spread","String","LO-20170417-45C");
        IJobSetup.addVariable("Instrument2","Second Instrument leg of spread","String","LO-20170417-45P");
        IJobSetup.addVariable("Quantity1","Quantity of first leg","double","1");
        IJobSetup.addVariable("Quantity2", "Quantity of second leg" , "double" , "1" );
    }

    public void begin(IContainer container) {
        super.begin(container);
        // grab instruments for legs
        instrument1 = container.getVariable("Instrument1");
        instrument2 = container.getVariable("Instrument2");
        double qty1 = getDoubleVar("Quantity1");
        double qty2 = getDoubleVar("Quantity2");

        log("instrument Id 1 is " + instrument1 + "quantity 1 is " + qty1);
        log("instrument Id 2 is " + instrument2  + "quantity 2 is " + qty1);

        // Create leg details objects
        InstrumentDetails.LegDetails leg1 = new InstrumentDetails.LegDetails();
        leg1.instrumentId = instrument1;
        leg1.quantity = qty1;
        leg1.underlyingId = instruments().getInstrumentDetails(instrument1).underlyingId;

        InstrumentDetails.LegDetails leg2 = new InstrumentDetails.LegDetails();
        leg2.instrumentId = instrument2;
        leg2.quantity = qty2;
        leg2.underlyingId = instruments().getInstrumentDetails(instrument2).underlyingId;

        // implement instrumentLges as arrayList<>
        instrumentLegs = new ArrayList<InstrumentDetails.LegDetails>();
        // add legs
        instrumentLegs.add(leg1);
        instrumentLegs.add(leg2);


        String strategyId = instruments().findStrategy(instrumentLegs, null);
        log("The strategy Id is " + strategyId);

        /*
        // Added logic for testing createStrategy() without the null comparison
        strategyId = instruments().createStrategy(instrumentLegs, instruments().getInstrumentDetails(instrument1).exchange);
        log("The strategy Id is " + strategyId);
        */
        quotes().requestForQuote("LO-VRTCL LO-20170417-48P:-1|LO-20170417-55P:1", 10, "FREEWAY");

        if ( strategyId == null){
            strategyId = instruments().createStrategy(instrumentLegs, null);
            /* instruments().createStrategy(instrumentLegs, null); IS THE CALL THAT IS THROWING THE EXCEPTION

            RfqTest.1 job died:java.security.AccessControlException: access denied ("java.io.FilePermission" "var/requests.lastid" "read")

            An exception occurred in job 'RfqTest.1'
            At Tue Mar 21 13:42:44 CDT 2017

            java.security.AccessControlException: access denied ("java.io.FilePermission" "var/requests.lastid" "read")
                at java.security.AccessControlContext.checkPermission(AccessControlContext.java:372)
                at java.security.AccessController.checkPermission(AccessController.java:559)
                at java.lang.SecurityManager.checkPermission(SecurityManager.java:549)
                at java.lang.SecurityManager.checkRead(SecurityManager.java:888)
                at java.io.FileInputStream.<init>(FileInputStream.java:135)
                at java.io.FileInputStream.<init>(FileInputStream.java:101)
                at java.io.FileReader.<init>(FileReader.java:58)
                at com.optionscity.citylibrary.persistence.IDManager.readIDFile(IDManager.java:323)
                at com.optionscity.citylibrary.persistence.IDManager.init(IDManager.java:238)
                at com.optionscity.citylibrary.persistence.IDManager.getNextID(IDManager.java:308)
                at com.optionscity.citylibrary.exchange.StrategyCreationRequest.<init>(StrategyCreationRequest.java:37)
                at com.optionscity.instrumentcenter.InstrumentCenter.createStrategy(InstrumentCenter.java:2614)
                at com.optionscity.freeway.InstrumentService.createStrategy(InstrumentService.java:809)
                at RfqTest.begin(RfqTest.java:57)
                at com.optionscity.freeway.container.JobInstance$JobRunnerRunnable.run(JobInstance.java:548)
                at com.optionscity.freeway.container.JobInstance$JobRunnerThread.doRun(JobInstance.java:507)
                at com.optionscity.citylibrary.scheduling.ManagedThread.run(ManagedThread.java:289)
            */
        }

        // log("Request for quote on " + strategyId + " on the " + instruments().getInstrumentDetails(strategyId).exchange + " exchange.");
        //quotes().requestForQuote(strategyId, 10, instruments().getInstrumentDetails(strategyId).exchange);

    }

    /*
    public void onTimer() {
        for (InstrumentDetails.LegDetails temp : instrumentLegs) {
            log("The instrument is " + temp.instrumentId + " Qty is " + temp.quantity + " underlyingId is " + temp.underlyingId);
        }
    }
    */
}
