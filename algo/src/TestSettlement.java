import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IContainer;
import com.optionscity.freeway.api.IJobSetup;
import com.optionscity.freeway.api.services.IInstrumentService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by demo01 on 2/9/2017.
 */


public class TestSettlement extends AbstractJob{

    String variableName1, variableName2;



    public void install(IJobSetup setup) {
    }

    public void begin (IContainer beginVar) {

        super.begin(beginVar);
        String instrumentID = "OZB-20170324-120C";
        String settlementDateStr = "20170209";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date settlementDate;
        try {
            settlementDate = simpleDateFormat.parse(settlementDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }


        //IInstrumentService instrumentService = instruments();
        log ("Is instrument service null " + (instruments()== null));


        double settlePrice = instruments().getSettlementPrice(instrumentID, settlementDate);
        log("Settlement price is " + settlePrice);

        instruments().setSettlementPrice(instrumentID, settlementDate, 12.0);

        settlePrice = instruments().getSettlementPrice(instrumentID, settlementDate);
        log("New Settlement price is " + settlePrice);
    }


}
