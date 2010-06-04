import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
			File outputFile = new File(filename + ".xml");
			TextExtractor te = null;
			
			try {
				te = parsePdf(inputFile);
			} catch (IOException e) {
				System.err.println("Couldn't read file '" + inputFile +"'.");
			}
			
			try {
				if (outputFile.exists()) {
					outputFile.delete();
				}
				outputFile.createNewFile();
			} catch (IOException e) {
				System.err.println("Could not create output file '" + outputFile + "'.");
			}
			
			System.out.println(te.toXml());
		}
	}

}
