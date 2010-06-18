package org.crossref.pdf2xml.visual;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.crossref.pdf2xml.data.Page;
import org.crossref.pdf2xml.data.Text;

public class PageCanvas extends JComponent {
    
    private Page currentPage;
    
    private int pageIndex;
    
    private List<Page> pages;
    
    public PageCanvas(Page newPage) {
        currentPage = newPage;
        
        addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            @Override
            public void mousePressed(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (pages != null && pages.size() > 1) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        currentPage = pages.get(++pageIndex % pages.size());
                    }
                    repaint();
                }
            }
        });
    }
    
    public PageCanvas(List<Page> newPages) {
        this(newPages.get(0));
        pages = newPages;
        pageIndex = 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        final float pageWidth = currentPage.getClipBox().getWidth();
        final float scalingScalar = getWidth() / pageWidth;
        
        for (Text t : currentPage.getText()) {
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
