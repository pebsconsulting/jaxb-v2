/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.XMLReader;

import simple.*;

/*
 * @(#)$Id: Main.java,v 1.1 2005-04-15 20:06:28 kohsuke Exp $
 *
 * Copyright 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

public class Main {
    public static void main( String[] args ) throws Exception {
        
        // create JAXBContext for the primer.xsd
        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
        ObjectFactory of = new ObjectFactory();
        
        // \u00F6 is o with diaeresis
        JAXBElement<String> e = of.createE("G\u00F6del & his friends");
        
        
        // set up a normal marshaller
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty( "jaxb.encoding", "US-ASCII" );
        
        // check out the console output
        marshaller.marshal( e, System.out );
        
        
        // set up a marshaller with a custom character encoding handler
        marshaller = context.createMarshaller();
        marshaller.setProperty( "jaxb.encoding", "US-ASCII" );
        marshaller.setProperty(
          "com.sun.xml.bind.characterEscapeHandler",
          new CustomCharacterEscapeHandler() );
        
        // check out the console output
        marshaller.marshal( e, System.out );
    }

}
