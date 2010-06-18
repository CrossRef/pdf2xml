package org.crossref.pdf2xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageNode;
import org.crossref.pdf2xml.visual.PageCanvas;


public class Main {
	
	private static TextExtractor parsePdf(File f) throws IOException {
		PDDocument doc = PDDocument.load(f);
		PDDocumentCatalog docCat = doc.getDocumentCatalog();
		
		PDPageNode root = docCat.getPages();
		List pages = new ArrayList();
		root.getAllKids(pages);
		
		TextExtractor te = new TextExtractor();
		
		for (Object p : pages) {
			PDPage page = (PDPage) p;
			
			te.setShouldSeparateByBeads(false);
			te.processStream(page, 
							 page.getResources(), 
							 page.getContents().getStream());
		}
		
		doc.close();
		
		return te;
	}
	
	public static void main(String[] filenames) {
		for (String filename : filenames) {
			File inputFile = new File(filename);
			TextExtractor te = null;
			
			try {
				te = parsePdf(inputFile);
				System.out.println(te.toXml());
			} catch (IOException e) {
				System.err.println("Couldn't read file '" + inputFile +"'.");
				System.exit(1);
			}
			
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.add(new PageCanvas(te.getPages()));
			frame.setSize(600, 800);
			frame.setVisible(true);
		}
	}

}
