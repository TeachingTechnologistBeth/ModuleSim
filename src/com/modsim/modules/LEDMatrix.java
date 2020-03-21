package com.modsim.modules;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.modsim.modules.parts.LEDRow;
import com.modsim.modules.parts.Port;
import com.modsim.modules.ports.Input;
import com.modsim.res.Colors;
import com.modsim.simulator.PickableEntity;
import com.modsim.util.BinData;
import com.modsim.util.HexReader;
import com.modsim.util.HexWriter;

public class LEDMatrix extends BaseModule {

	private final List<Input> dIn;
    private final Input contIn;
    
    private final List<List<LEDRow>> matrix;
	private int previousRow = 0;
	private boolean persist = false;
	
	LEDMatrix(){
		w = 150;
        h = 150;

        dIn = Collections.unmodifiableList(Arrays.asList(new Input[]{
            addInput("Input A", -50, Port.DATA),
            addInput("Input B", -25, Port.DATA),
            addInput("Input C",  25, Port.DATA),
            addInput("Input D",  50, Port.DATA),
        }));
        contIn = addInput("Line Select", 0, Port.CTRL);

        int y=35;
     // Add display
        matrix = new ArrayList<List<LEDRow>>();
        for(int i=0; i<16; i++){
        	int x=-46;
        	List<LEDRow> row = new ArrayList<LEDRow>();
        	for(int j=0; j<4; j++){
        		row.add(new LEDRow(x,y));
        		x+=31;
        	}
        	y-=6;
        	matrix.add(row);
        }

        for (List<LEDRow> list : matrix){
        	for (LEDRow led : list){
        		addPart(led);
        	}
        }
        
        //addPart(new SSText(-45, 15, "LED Matrix", 40, Colors.moduleLabel));
        propagate();
	}
	
	
	@Override
	public AvailableModules getModType() {
		
		return AvailableModules.LEDMatrix;
	}

	@Override
	public void paintStatic(Graphics2D g) {
		// Fill in polygon
        g.setColor(Colors.moduleFill);
        drawBox(g, 10);
        g.setColor(Colors.moduleInset);
        drawTrapezoid(g, 5, 0, 55, 130, 20);

        // Show IO
        g.setColor(Colors.modulePorts);
        drawInputs(g);
        drawOutputs(g);

		drawStaticParts(g);
	}

	@Override
	public void propagate() {
		if(!persist){
			List<LEDRow> prevleds = matrix.get(previousRow);
			for(int i=0; i<4; i++){
				prevleds.get(i).setVal(new BinData(0));
			}
		}
		final int sel = contIn.getVal().getUInt() & 15;
		previousRow = sel;
		List<LEDRow> leds = matrix.get(sel);
		for(int i=0; i<4; i++){
			leds.get(i).setVal(dIn.get(i).getVal());
		}
	}
	
	@Override
	public PickableEntity createNew() {
		return new LEDMatrix();
	}
	
	public void turnOffPersist(){
		persist = false;
		for(List<LEDRow> row : matrix){
			for(LEDRow group : row){
				group.setVal(new BinData(0));
			}
		}
	}
	
	public void turnOnPersist(){
		persist = true;
	}
	
	public boolean isPersistEnabled(){
		return persist;
	}
	
	@Override
    public HashMap<String, String> dataOut() {
        if (!isPersistEnabled()) return super.dataOut();

        HashMap<String, String> data = super.dataOut();
        data.put("persist", "1");
        
        return data;
    }
	
	@Override
    public void dataIn(HashMap<String, String> data) {
        super.dataIn(data);

        if (data.containsKey("persist")) {
            String storeStr = data.get("persist");
            try{
            	if(Integer.parseInt(storeStr)==1)
            		turnOnPersist();
            }catch(NumberFormatException e){
            	//leave persist off
            }   
        }
    }

}
