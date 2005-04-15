/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * SAX2DOMEx.java
 *
 * Created on February 22, 2002, 1:55 PM
 */

package com.sun.xml.bind.marshaller;

import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.sun.xml.bind.util.Which;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;

/**
 * Builds a DOM tree from SAX2 events.
 * 
 * @author  Vivek Pandey
 * @since 1.0
 */
public class SAX2DOMEx implements ContentHandler {

    private Node _node=null;
    private final Stack _nodeStk = new Stack();
    
    public final Element getCurrentElement() {
        return (Element) _nodeStk.peek();
    }
    
    /**
     * Document object that owns the specified node.
     */
    private final Document _document;
    
    /**
     * @param   node
     *      Nodes will be created and added under this object.
     */
    public SAX2DOMEx(Node node)
    {
        _node = node;
        _nodeStk.push(_node);

        if( node instanceof Document )
            this._document = (Document)node;
        else
            this._document = node.getOwnerDocument();
    }
    
    /**
     * Creates a fresh empty DOM document and adds nodes under this document.
     */
    public SAX2DOMEx() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        
        _document = factory.newDocumentBuilder().newDocument();
        _node = _document;
        _nodeStk.push( _document );
    }

    public Node getDOM() {
        return _node;
    }

    public void startDocument() {
    }

    public void endDocument(){
    }

    public void startElement(String namespace, String localName, String qName, Attributes attrs){
        Node parent = (Node) _nodeStk.peek();
        
        // some broken DOM implementatino (we confirmed it with SAXON)
        // return null from this method.
        Element element = _document.createElementNS(namespace, qName);
        
        if( element==null ) {
            // if so, report an user-friendly error message,
            // rather than dying mysteriously with NPE.
            throw new AssertionError(
                Messages.format(Messages.DOM_IMPL_DOESNT_SUPPORT_CREATELEMENTNS,
                    _document.getClass().getName(),
                    Which.which(_document.getClass())));
        }
        
        // process namespace bindings
        for( int i=0; i<unprocessedNamespaces.size(); i+=2 ) {
            String prefix = (String)unprocessedNamespaces.get(i+0);
            String uri = (String)unprocessedNamespaces.get(i+1);
            
            String qname;
            if( "".equals(prefix) || prefix==null )
                qname = "xmlns";
            else
                qname = "xmlns:"+prefix;
            
            // older version of Xerces (I confirmed that the bug is gone with Xerces 2.4.0)
            // have a problem of re-setting the same namespace attribute twice.
            // work around this bug removing it first.
            if( element.hasAttributeNS("http://www.w3.org/2000/xmlns/",qname) ) {
                // further workaround for an old Crimson bug where the removeAttribtueNS
                // method throws NPE when the element doesn't have any attribute.
                // to be on the safe side, check the existence of attributes before
                // attempting to remove it.
                // for details about this bug, see org.apache.crimson.tree.ElementNode2
                // line 540 or the following message:
                // https://jaxb.dev.java.net/servlets/ReadMsg?list=users&msgNo=2767
                element.removeAttributeNS("http://www.w3.org/2000/xmlns/",qname);
            }
            // workaround until here
            
            element.setAttributeNS("http://www.w3.org/2000/xmlns/",qname, uri);
        }
        unprocessedNamespaces.clear();
        
        
        int length = attrs.getLength();
        for(int i=0;i<length;i++){
            String namespaceuri = attrs.getURI(i);
            String value = attrs.getValue(i);
            String qname = attrs.getQName(i);
            element.setAttributeNS(namespaceuri, qname, value);
        }
        // append this new node onto current stack node
        parent.appendChild(element);
        // push this node onto stack
        _nodeStk.push(element);        
    }
    
    public void endElement(String namespace, String localName, String qName){
        _nodeStk.pop();
    }


    public void characters(char[] ch, int start, int length) {
        Node parent = (Node) _nodeStk.peek();
        Text text = _document.createTextNode(new String(ch, start, length));
        parent.appendChild(text);
    }



    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    public void processingInstruction(String target, String data) throws org.xml.sax.SAXException{
        Node parent = (Node) _nodeStk.peek();
        Node node = _document.createProcessingInstruction(target, data);
        parent.appendChild(node);	
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void skippedEntity(String name) {
    }

    private ArrayList unprocessedNamespaces = new ArrayList();
    
    public void startPrefixMapping(String prefix, String uri) {
        unprocessedNamespaces.add(prefix);
        unprocessedNamespaces.add(uri);
    }

    public void endPrefixMapping(String prefix) {
    }
}
