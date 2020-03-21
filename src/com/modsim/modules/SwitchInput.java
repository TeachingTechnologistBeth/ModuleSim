package com.modsim.modules;

import java.awt.*;
import java.util.HashMap;

import com.modsim.res.Colors.LEDColour;
import com.modsim.modules.parts.Port;
import com.modsim.res.Colors;
import com.modsim.util.BinData;
import com.modsim.modules.ports.Output;
import com.modsim.modules.parts.Switch;

import javax.swing.*;

/**
 * Multi-switch input module
 * @author aw12700
 *
 */
public class SwitchInput extends BaseModule {

	private Switch s1, s2, s3, s4;
	private Output data;

	SwitchInput() {
		w = 100;
		h = 50;

		// Add the one output
		data = addOutput("Data", 0, Output.GENERIC);

		// Add the switches
		s1 = new Switch(-30, 5);
		s2 = new Switch(-10, 5);
		s3 = new Switch( 10, 5);
		s4 = new Switch( 30, 5);

		addPart(s1);
		addPart(s2);
		addPart(s3);
		addPart(s4);

		propagate();
	}

    @Override
    public BaseModule createNew() {
        return new SwitchInput();
    }

	@Override
	public void paintStatic(Graphics2D g) {
		// Fill in polygon
		g.setColor(Colors.moduleFill);
		drawTrapezoid(g, 10);

		// Show output
		g.setColor(Colors.modulePorts);
		drawOutputs(g);
	}

	@Override
	public void paintDynamic(Graphics2D g) {
		// Draw switches
		Colors.LEDColour switchCol = LEDColour.RED;
		if (data.link != null) {
			switch (data.link.targ.type) {
				case Port.CTRL:
					switchCol = LEDColour.BLUE;
					break;
				case Port.CLOCK:
					switchCol = LEDColour.GREEN;
					break;
			}
		}

		s1.setColour(switchCol);
		s2.setColour(switchCol);
		s3.setColour(switchCol);
		s4.setColour(switchCol);

		drawDynamicParts(g);
	}

	@Override
    public void propagate() {
		// Generate output value
		BinData out = new BinData(0);

		if (s1.getEnabled()) out.setBit(3, 1);
		if (s2.getEnabled()) out.setBit(2, 1);
		if (s3.getEnabled()) out.setBit(1, 1);
		if (s4.getEnabled()) out.setBit(0, 1);

		// Output
		data.setVal(out);
	}

	@Override
	public void dataIn(HashMap<String, String> data) {
        super.dataIn(data);

		if (data.containsKey("switch_set")) {
			// Parse switch setting
			String str = data.get("switch_set");
			try {
				if (str.length() != 4) throw new Exception("bad string length");
				// Who needs loops right?
				String b0 = str.substring(0,1);
				s1.setEnabled(Integer.parseInt(b0) == 1);
				String b1 = str.substring(1,2);
				s2.setEnabled(Integer.parseInt(b1) == 1);
				String b2 = str.substring(2,3);
				s3.setEnabled(Integer.parseInt(b2) == 1);
				String b3 = str.substring(3);
				s4.setEnabled(Integer.parseInt(b3) == 1);
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Corrupt/unrecognized SwitchInput data: "+e.getMessage());
			}
		}
	}

	@Override
	public HashMap<String, String> dataOut() {
		HashMap<String, String> data = super.dataOut();
		String setting;
		setting  = s1.getEnabled() ? "1" : "0";
		setting += s2.getEnabled() ? "1" : "0";
		setting += s3.getEnabled() ? "1" : "0";
		setting += s4.getEnabled() ? "1" : "0";
		data.put("switch_set", setting);

		return data;
	}

    @Override
    public AvailableModules getModType() {
        return AvailableModules.SWITCH;
    }

}
