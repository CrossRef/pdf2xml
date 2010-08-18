package org.crossref.pdf2xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.exceptions.WrappedIOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageNode;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Main {
    
    @Option(name="-p", usage="Specify an optional decryption password.",
            required=false, multiValued=false, metaVar="PASSWD")
    private String password = "";
    
    @Argument
    private List<String> filenames = new ArrayList<String>();
	
	private TextExtractor parsePdf(File f) throws IOException {
		PDDocument doc = PDDocument.load(f);
		
		if(doc.isEncrypted()) {
            // Some documents are encrypted with the empty password. Try
		    // to decrypt with this password, or the one passed in on the
		    // command line (if any), and fail if we can't.
            try {
                doc.decrypt(password); // Defaults to the empty string.
            } catch (CryptographyException e) {
                throw new WrappedIOException("Can't decrypt document: ", e);
            } catch (InvalidPasswordException e) {
                throw new WrappedIOException("Document is encrypted: ", e);
            }
        }
		
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
	
	private void doMain() {
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
        }
	}
	
	public static void main(String[] args) {
	    Main m = new Main();
        CmdLineParser parser = new CmdLineParser(m);
        
        if (args.length == 0) {
            System.err.println("Usage: pdf2xml [options] <FILEs>");
            parser.printUsage(System.err);
        } else {
            try {
                parser.parseArgument(args);
                m.doMain();
            } catch (CmdLineException e) {
                parser.printUsage(System.err);
            }
        }
	}

}
