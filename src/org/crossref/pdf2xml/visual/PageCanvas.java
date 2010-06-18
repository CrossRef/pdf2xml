package org.crossref.pdf2xml.visual;

import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JComponent;

import org.crossref.pdf2xml.data.Page;
import org.crossref.pdf2xml.data.Text;

public class PageCanvas extends JComponent {
    
    private Page page;
    
    public PageCanvas(Page newPage) {
        page = newPage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        final float pageWidth = page.getClipBox().getWidth();
        final float pageHeight = page.getClipBox().getHeight();
        final float scalingScalar = getWidth() / pageWidth;
        
        for (Text t : page.getText()) {
            int style = Font.PLAIN;
            if (t.getFontFace().equals("Bold")) {
                style = Font.BOLD;
            } else if (t.getFontFace().equals("Italic")
                    || t.getFontFace().equals("Oblique")) {
                style = Font.ITALIC;
            }
            Font f = new Font(Font.SANS_SERIF, 
                              style, 
                              (int) (t.getPointSize() * scalingScalar));
            
            int x = (int) (scalingScalar * t.getX());
            int y = (int) (scalingScalar * t.getTop());
            g.setFont(f);
            g.drawString(t.getRun(), x, y);
        }
    }
    
}
