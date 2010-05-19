import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorState;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

public class TextExtractor extends PDFTextStripper {
	
	public TextExtractor() throws IOException {
		super();
	}

	private ArrayList<TextRun> textRuns = new ArrayList<TextRun>();

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
			textRuns.add(TextRun.newFor(tp, gs));
		}
	}
	
	public String toString() {
		String s = "";
		for (TextRun tr : textRuns) {
			s += tr.run + " @ " + tr.x + "," + tr.y 
						+ " : " + tr.font.getBaseFont()
						+ " C " + tr.strokeColor.toString()
						+ "\n";
		}
		return s;
	}
}

class TextRun {
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
	
	boolean isIncidentToLeft(TextPosition tp) {
		final float mostAcceptableLeft = x - looseness(font);
		final float mostAcceptableRight = x;
		
		return y == tp.getY() 
				&& tp.getX() >= mostAcceptableLeft
				&& tp.getX() <= mostAcceptableRight;
	}
	
	boolean isIncidentToRight(TextPosition tp) {
		final float mostAcceptableLeft = x + width;
		final float mostAcceptableRight = x + width + looseness(font);
		
		return y == tp.getY()
				&& tp.getX() >= mostAcceptableLeft
				&& tp.getY() <= mostAcceptableRight;
	}
	
	boolean hasMatchingStyle(TextPosition tp, PDGraphicsState gs) {
		return tp.getFont() == font
				&& gs.getStrokingColor() == strokeColor
				&& gs.getNonStrokingColor() == nonStrokeColor;
	}
}
