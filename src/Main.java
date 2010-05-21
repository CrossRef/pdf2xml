import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageNode;


public class Main {
	
	public static void parsePdf(File f) throws IOException {
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
		
		te.coalesceRows();
		System.out.println(te.toString());
	}
	
	public static void main(String[] filenames) {
		for (String filename : filenames) {
			try {
				parsePdf(new File(filename));
			} catch (IOException e) {
				Logger.getAnonymousLogger().log(Level.WARNING,
												e.toString());
			}
		}
	}

}
