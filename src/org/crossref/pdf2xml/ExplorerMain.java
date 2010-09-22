package org.crossref.pdf2xml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.pdfbox.PDFReader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * A class that provides an interface to explore the COS objects of a
 * document.
 */
public class ExplorerMain {

    public static void main(String[] args) {
        for (String filename : args) {
            try {
                PDDocument doc = PDDocument.load(new File(filename));
                JFrame explorer = createExplorer(doc);
                explorer.setSize(new Dimension(400, 600));
                explorer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                explorer.setVisible(true);
                doc.close();
            } catch (IOException e) {
                System.err.println("Couldn't open '" + filename + "':");
                System.err.println(e);
            }
        }
    }
    
    public static JFrame createExplorer(PDDocument doc) throws IOException {
        JFrame frame = new JFrame();
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Doc");
        JTree tree = new JTree(root);
        frame.add(new JScrollPane(tree), BorderLayout.CENTER);
        
        COSDocument cosDoc = doc.getDocument();
        
        DefaultMutableTreeNode dId = 
            new DefaultMutableTreeNode("Document Id");
        addCOSBase(dId, cosDoc.getDocumentID());
        root.add(dId);
        
        DefaultMutableTreeNode catalog = 
            new DefaultMutableTreeNode("Catalog");
        addCOSBase(catalog, cosDoc.getCatalog());
        root.add(catalog);
        
        if (cosDoc.getEncryptionDictionary() != null) {
            DefaultMutableTreeNode encDict = 
                new DefaultMutableTreeNode("EncryptionDictionary");
            addCOSBase(encDict, cosDoc.getEncryptionDictionary());
            root.add(encDict);
        }
        
        DefaultMutableTreeNode trailer = 
            new DefaultMutableTreeNode("Trailer");
        addCOSBase(trailer, cosDoc.getTrailer());
        root.add(trailer);
        
        DefaultMutableTreeNode everything = 
            new DefaultMutableTreeNode("Everything");
        
        for (COSObject obj : cosDoc.getObjects()) {
            COSBase base = obj.getObject();
            addCOSBase(everything, base);
        }
        
        root.add(everything);
        
        return frame;
    }
    
    private static void addCOSBase(DefaultMutableTreeNode root, COSBase base) {
        if (base instanceof COSArray) {
            COSArray ary = (COSArray) base;
            DefaultMutableTreeNode nestedTn = new DefaultMutableTreeNode("#Array");
            
            for (int i=0; i<ary.size(); i++) {
                COSBase valueObj = ary.get(i);
                addCOSBase(nestedTn, valueObj);
            }
            
            root.add(nestedTn);
            
        } else if (base instanceof COSDictionary) {
            COSDictionary dict = (COSDictionary) base;
            
            // Look for a Type key, add it to the display name of the dict.
            final String typeStr = dict.getNameAsString("Type");
            final String nodeName = "#Dict" + (typeStr == null ? "" : (" " + typeStr));
            DefaultMutableTreeNode nestedTn = new DefaultMutableTreeNode(nodeName);
            
            for (java.util.Map.Entry<COSName, COSBase> e : dict.entrySet()) {
                String keyName = e.getKey().getName();
                DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(keyName);
                
                addCOSBase(keyNode, e.getValue());
                nestedTn.add(keyNode);
            }
            
            root.add(nestedTn);
            
        } else {
            root.add(new DefaultMutableTreeNode(getCOSName(base)));
        }
    }
    
    private static String getCOSName(COSBase base) {
        if (base instanceof COSBoolean) {
            return String.valueOf(((COSBoolean) base).getValue());
        } else if (base instanceof COSInteger) {
            return String.valueOf(((COSInteger) base).doubleValue());
        } else if (base instanceof COSName) {
            return ((COSName) base).getName();
        } else if (base instanceof COSString) {
            return "\"" + ((COSString) base).getString() + "\"";
        } else if (base instanceof COSFloat) {
            return String.valueOf(((COSFloat) base).floatValue());
        } else if (base instanceof COSNull) {
            return "#Null";
        } else if (base instanceof COSObject) {
            COSObject cosObj = (COSObject) base;
            return "COSObject Gen: " + cosObj.getGenerationNumber().intValue()
                    + " Obj: " + cosObj.getObjectNumber().intValue();
        }
        return base.toString();
    }

}
