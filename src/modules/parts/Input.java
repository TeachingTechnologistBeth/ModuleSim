package modules.parts;

import util.BinData;

/**
 * Input port for a module
 * @author aw12700
 *
 */
public class Input extends Port {

    public BinData pull = new BinData(0);

    @Override
    public BinData getVal() {
        if (link != null) {
            BinData v = super.getVal();
            if (!bidir) v.resolvePull(pull);
            return v;
        }
        else {
            if (bidir) return new BinData();
            else return pull;
        }
    }

}
