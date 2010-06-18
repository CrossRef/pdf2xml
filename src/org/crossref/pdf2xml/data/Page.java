package org.crossref.pdf2xml.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class Page {
	private static final ArrayList<Text> NOTHING = new ArrayList<Text>();
	
	private ArrayList<Text> texts;
	private HashMap<Float, ArrayList<Text>> yPosMap;
	private PDRectangle clipBox;
	private int number;
	
	public Page(PDRectangle newClipBox, int newNumber) {
		texts = new ArrayList<Text>();
		yPosMap = new HashMap<Float, ArrayList<Text>>();
		clipBox = newClipBox;
		number = newNumber;
	}
	
	public void addText(Text t) {
		texts.add(t);
		
		Float yPos = new Float(t.getBaseline());
		if (yPosMap.containsKey(yPos)) {
			ArrayList<Text> l = yPosMap.get(yPos);
			l.add(t);
		} else {
			ArrayList<Text> l = new ArrayList<Text>();
			l.add(t);
			yPosMap.put(yPos, l);
		}
	}
	
	public void removeText(Text t) {
		if (texts.contains(t)) {
			texts.remove(t);
			yPosMap.get(t.getBaseline()).remove(t);
		}
	}
	
	public PDRectangle getClipBox() {
		return clipBox;
	}
	
	public List<Text> getText() {
		return texts;
	}
	
	public int getNumber() {
	    return number;
	}
	
	public List<Text> getTextAtY(float y) {
		Float fObj = new Float(y);
		if (yPosMap.containsKey(fObj)) {
			return yPosMap.get(fObj);
		}
		return NOTHING;
	}
	
	/** 
	 * @return Answers a float for every y position that is incident with
	 * the start of a Text.
	 */
	public Set<Float> getYPosWithText() {
		return yPosMap.keySet();
	}
}