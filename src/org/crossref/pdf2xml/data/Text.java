package org.crossref.pdf2xml.data;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorState;
import org.apache.pdfbox.util.TextPosition;

public class Text implements Comparable<Text> {
	private float x, y, width, height, pointSize;
	private String run;
	private PDFont font;
	private PDColorState strokeColor;
	private PDColorState nonStrokeColor;
	
	public static Text newFor(TextPosition tp, PDGraphicsState gs) {
		Text t = new Text();
		t.x = tp.getX();
		t.y = tp.getY();
		t.font = tp.getFont();
		t.strokeColor = gs.getStrokingColor();
		t.nonStrokeColor = gs.getNonStrokingColor();
		t.run = tp.getCharacter();
		t.width = tp.getWidth();
		t.height = tp.getHeight();
		t.pointSize = tp.getFontSizeInPt();
		 
		return t;
	}
	
	private static float looseness() {
		return 1;
	}
	
	private static float looseness(TextPosition tp, PDFont font) {
		return tp.getWidth() / 2;
	}
	
	private static String getColorString(PDColorState c) {
		String colorString = "";
		
		float[] rgb = c.getColorSpaceValue();
		if (rgb.length == 1) {
		    int grey = (int) rgb[0] * 255;
		    colorString = Integer.toHexString(grey);
		    if (colorString.length() == 1) {
		        colorString = "0" + colorString;
		    }
		    colorString = colorString + colorString + colorString;
		} else {
    		for (int colorIndex=0; colorIndex<3; colorIndex++) {
    		    int color = (int) rgb[colorIndex] * 255;
    		    String s = Integer.toHexString(color);
    		    if (s.length() == 1) {
    		        s = "0" + s;
    		    }
    		    colorString += s;
    		}
		}
		
		return "#" + colorString;
	}
	
	public String getForegroundColor() {
		return getColorString(nonStrokeColor);
	}
	
	public String getFontFamily() {
		String fontName = font.getBaseFont();
		if (fontName.endsWith("MT")) {
		    fontName = fontName.substring(0, fontName.length() - 2);
		}
		String[] bits = fontName.split("\\+|-");
		if (bits.length > 1) {
			return bits[1];
		}
		return "";
	}
	
	public String getFontFace() {
		String fontName = font.getBaseFont();
		if (fontName.endsWith("MT")) {
            fontName = fontName.substring(0, fontName.length() - 2);
        }
		String[] bits = fontName.split("\\+|-");
		if (bits.length > 2) {
			return bits[2];
		}
		return "Normal";
	}
	
	public String getBaseFontName() {
		return font.getBaseFont();
	}
	
	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public float getPointSize() {
		return pointSize;
	}

	public String getRun() {
		return run;
	}

	public Text addBefore(Text t) {
		run = t.run + run;
		width += t.width;
		height = Math.max(height, t.height);
		return this;
	}
	
	public Text addAfter(Text t) {
		run += t.run;
		width += t.width;
		height = Math.max(height, t.height);
		return this;
	}
	
	public Text addBefore(TextPosition tp) {
		run = tp.getCharacter() + run;
		width += tp.getWidth();
		height = Math.max(height, tp.getHeight());
		return this;
	}
	
	public Text addAfter(TextPosition tp) {
		run += tp.getCharacter();
		width += tp.getWidth();
		height = Math.max(height, tp.getHeight());
		return this;
	}
	
	public boolean isIncidentToLeft(Text t) {
		final float runRightX = t.x + t.width;
		
		return y == t.y
				&& runRightX >= (x - looseness())
				&& runRightX <= x + looseness();
	}
	
	public boolean isIncidentToRight(Text t) {
		return y == t.y
				&& t.x >= (x + width - looseness())
				&& t.x <= (x + width + looseness());
	}
	
	public boolean isIncidentToLeft(TextPosition tp) {
		final float mostAcceptableLeft = x - looseness(tp, font);
		final float mostAcceptableRight = x;
		final float charRightX = tp.getX() + tp.getWidth();
		
		return y == tp.getY() 
				&& charRightX >= mostAcceptableLeft
				&& charRightX <= mostAcceptableRight;
	}
	
	public boolean isIncidentToRight(TextPosition tp) {
		final float mostAcceptableLeft = x + width;
		final float mostAcceptableRight = x + width + looseness(tp, font);
		
		return y == tp.getY()
				&& tp.getX() >= mostAcceptableLeft
				&& tp.getX() <= mostAcceptableRight;
	}
	
	public boolean hasMatchingStyle(TextPosition tp, PDGraphicsState gs) {
		return tp.getFont() == font
				&& gs.getStrokingColor() == strokeColor
				&& gs.getNonStrokingColor() == nonStrokeColor;
	}
	
	public boolean hasMatchingStyle(Text t) {
		return t.font == font
				&& t.strokeColor == strokeColor
				&& t.nonStrokeColor == nonStrokeColor;
	}

	@Override
	public int compareTo(Text other) {
		if (this.x < other.x) {
			return -1;
		} else if (this.x == other.x) {
			return 0;
		} else {
			return 1;
		}
	}
}