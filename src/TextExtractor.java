import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorState;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

public class TextExtractor extends PDFTextStripper {
	
	private ArrayList<TextRun> textRuns;
	
	private HashMap<Float, ArrayList<TextRun>> yPosMap;
	
	public TextExtractor() throws IOException {
		super();
		textRuns = new ArrayList<TextRun>();
		yPosMap = new HashMap<Float, ArrayList<TextRun>>();
	}
	
	@Override
	public void processStream(PDPage aPage, PDResources resources,
			COSStream cosStream) throws IOException {
		super.processStream(aPage, resources, cosStream);
		coalesceRows();
	}

	protected void processTextPosition(TextPosition tp) {
		PDGraphicsState gs = getGraphicsState();
		
		// try to find an existing text run that fits
		boolean added = false;
		
		for (TextRun tr : textRuns) {
			if (tr.hasMatchingStyle(tp, gs)) {
				if (tr.isIncidentToLeft(tp)) {
					tr.addBefore(tp.getCharacter());
					added = true;
					break;
				} else if (tr.isIncidentToRight(tp)) {
					tr.addAfter(tp.getCharacter());
					added = true;
					break;
				}
			}
		}
		
		if (!added) {
			TextRun newTr = TextRun.newFor(tp, gs);
			textRuns.add(newTr);
			
			ArrayList<TextRun> withSameY = yPosMap.get(newTr.y);
			if (withSameY == null) {
				ArrayList<TextRun> newWithSameY = new ArrayList<TextRun>();
				newWithSameY.add(newTr);
				yPosMap.put(newTr.y, newWithSameY);
			} else {
				withSameY.add(newTr);
			}
		}
	}
	
	public void coalesceRows() {
		for (Float f : yPosMap.keySet()) {
			ArrayList<TextRun> runs = yPosMap.get(f);
			
			Collections.sort(runs);
			
			int i=0;
			while (i+1 < runs.size()) {
				TextRun first = runs.get(i);
				TextRun snd = runs.get(i+1);
				
				if (first.hasMatchingStyle(snd) 
						&& first.isIncidentToRight(snd)) {
					first.addAfter(snd);
					runs.remove(i+1);
					textRuns.remove(snd);
				} else {
					i++;
				}
			}
		}
	}
	
	public String toString() {
		String s = "";
		for (TextRun tr : textRuns) {
			try {
				s += tr.run + " @ " + tr.x + "," + tr.y 
							+ " : " + tr.font.getBaseFont()
							+ " C " + tr.strokeColor.getJavaColor()
							+ "\n";
			} catch (IOException e) {
				
			}
		}
		return s;
	}
}

class TextRun implements Comparable<TextRun> {
	float x;
	float y;
	float width;
	String run;
	PDFont font;
	PDColorState strokeColor;
	PDColorState nonStrokeColor;
	
	static TextRun newFor(TextPosition tp, PDGraphicsState gs) {
		TextRun tr = new TextRun();
		tr.x = tp.getX();
		tr.y = tp.getY();
		tr.font = tp.getFont();
		tr.strokeColor = gs.getStrokingColor();
		tr.nonStrokeColor = gs.getNonStrokingColor();
		tr.run = tp.getCharacter();
		
		try {
			tr.width = tr.font.getStringWidth(tr.run);
		} catch (IOException e) {
			Logger.getAnonymousLogger()
			      .log(Level.WARNING, "Can't get string width for new TextRun.");
		}
		 
		return tr;
	}
	
	private static float looseness(PDFont font) {
		try {
			float spaceWidth = font.getStringWidth(" ");
			return spaceWidth * 2;
		} catch (IOException e) {
			Logger.getAnonymousLogger()
				  .log(Level.WARNING, "Can't get font space width.");
			return 0;
		}
	}
	
	TextRun addBefore(TextRun tr) {
		run = tr.run + run;
		return this;
	}
	
	TextRun addAfter(TextRun tr) {
		run += tr.run;
		return this;
	}
	
	TextRun addBefore(String s) {
		run = s + run;
		
		try {
			width = font.getStringWidth(run);
		} catch (IOException e) {
			Logger.getAnonymousLogger()
			      .log(Level.WARNING, "Can't update TextRun width.");
		}
		
		return this;
	}
	
	TextRun addAfter(String s) {
		run += s;
		
		try {
			width = font.getStringWidth(run);
		} catch (IOException e) {
			Logger.getAnonymousLogger()
				  .log(Level.WARNING, "Can't update TextRun width.");
		}
		
		return this;
	}
	
	boolean isIncidentToLeft(TextRun tr) {
		final float runRightX = tr.x + tr.width;
		
		return y == tr.y
				&& runRightX >= (x - looseness(font))
				&& runRightX <= x;
	}
	
	boolean isIncidentToRight(TextRun tr) {
		return y == tr.y
				&& tr.x >= (x + width)
				&& tr.x <= (x + width + looseness(font));
	}
	
	boolean isIncidentToLeft(TextPosition tp) {
		final float mostAcceptableLeft = x - looseness(font);
		final float mostAcceptableRight = x;
		final float charRightX = tp.getX() + tp.getWidth();
		
		return y == tp.getY() 
				&& charRightX >= mostAcceptableLeft
				&& charRightX <= mostAcceptableRight;
	}
	
	boolean isIncidentToRight(TextPosition tp) {
		final float mostAcceptableLeft = x + width;
		final float mostAcceptableRight = x + width + looseness(font);
		
		return y == tp.getY()
				&& tp.getX() >= mostAcceptableLeft
				&& tp.getX() <= mostAcceptableRight;
	}
	
	boolean hasMatchingStyle(TextPosition tp, PDGraphicsState gs) {
		return tp.getFont() == font
				&& gs.getStrokingColor() == strokeColor
				&& gs.getNonStrokingColor() == nonStrokeColor;
	}
	
	boolean hasMatchingStyle(TextRun tr) {
		return tr.font == font
				&& tr.strokeColor == strokeColor
				&& tr.nonStrokeColor == nonStrokeColor;
	}

	@Override
	public int compareTo(TextRun other) {
		if (this.x < other.x) {
			return -1;
		} else if (this.x == other.x) {
			return 0;
		} else {
			return 1;
		}
	}
}
