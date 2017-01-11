import com.optionscity.freeway.api.AbstractJob;
import com.optionscity.freeway.api.IContainer;
import com.optionscity.freeway.api.IJobSetup;

/**
 * Created by demo01 on 1/11/2017.
 */
public class DemoJob extends AbstractJob{
    @Override
    public void install(IJobSetup iJobSetup) {

    }
    public void begin(IContainer container) {
        super.begin(container);
        log("Hello world!");
    }
    public void onTimer(){
        debug("timer invoked");
    }
}
