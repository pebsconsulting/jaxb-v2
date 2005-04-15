/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// import java content classes generated by binding compiler
import org.example.*;

/*
 * $Id: Main.java,v 1.1 2005-04-15 20:06:35 kohsuke Exp $
 *
 * Copyright 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
 
public class Main {
    
    // This sample application demonstrates how to modify a java content
    // tree and marshal it back to a xml data
    
    public static void main( String[] args ) {
        try {
            // create a JAXBContext capable of handling classes generated into
            // the org.example package
            JAXBContext jc = JAXBContext.newInstance( "org.example" );
            
            // create an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();
            
            // unmarshal a po instance document into a tree of Java content
            // objects composed of classes from the primer.po package.
	    Object folderE = u.unmarshal( new FileInputStream( "folder.xml" ) );

	    // get XML content.
	    // normalize that unmarshal returns either JAXBElement OR 
	    // class annotated with @XmlRootElement.
	    Folder folder = (Folder)(folderE instanceof JAXBElement ?
				     ((JAXBElement)folderE).getValue() :
				     folderE);

	    JAXBIntrospector inspect = jc.createJAXBIntrospector();
	    System.out.println("Unmarshalled xml element tag is:" + inspect.getElementName(folderE));

            System.out.println("Processing headers...");
            ObjectFactory of = new ObjectFactory();
	    for( JAXBElement<? extends Header> hdrE : folder.getHeaderE()) {
		Header hdr = hdrE.getValue();
		if (hdr instanceof OrderHeader) {
	           OrderHeader oh= (OrderHeader)hdr;
	           System.out.println("OrderHeader info:" + 
				      oh.getOrderInfo());
                } else if (hdr instanceof InvoiceHeader) {
	           InvoiceHeader ih = (InvoiceHeader)hdr;
	           System.out.println("InvoiceHeader info:" + 
				      ih.getInvoiceInfo());
                } else if (hdr instanceof BidHeader ) {
	           BidHeader bh= (BidHeader)hdr;
	           System.out.println("BidHeader info:" + 
				      bh.getBidInfo());
                }
            }

	    InvoiceHeader ih = of.createInvoiceHeader();
	    ih.setGeneralComment("New Comment");
	    ih.setInvoiceInfo("New Invoice Info");
	    JAXBElement newHdr = of.createInvoiceHeaderE(ih);
	    folder.getHeaderE().add(newHdr);

            // create a Marshaller and marshal to a file
            Marshaller m = jc.createMarshaller();
            m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            m.marshal( folderE, System.out );
            
        } catch( JAXBException je ) {
            je.printStackTrace();
        } catch( IOException ioe ) {
            ioe.printStackTrace();
        }
    }
}
