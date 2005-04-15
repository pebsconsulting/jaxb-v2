/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;

import java.util.Iterator;

/**
 * {@link JType}s that belong the the type hierarchy rooted at
 * {@link Object}.
 * 
 * <p>
 * Those types are also called as "reference types".
 * To be exact, this object represents an "use" of a reference type,
 * not the declaration of it.
 */
public abstract class JClass extends JType
{
    protected JClass( JCodeModel _owner ) {
        this._owner = _owner;
    }

    /**
     * Gets the name of this class.
     *
     * @return
     *	name of this class, without any qualification.
     *	For example, this method returns "String" for
     *  <code>java.lang.String</code>.
     */
    abstract public String name();
	
	/**
     * Gets the package to which this class belongs.
     * TODO: shall we move move this down?
     */
    abstract public JPackage _package();
	
    private final JCodeModel _owner;
    /** Gets the JCodeModel object to which this object belongs. */
    public final JCodeModel owner() { return _owner; }
    
    /**
     * Gets the super class of this class.
     * 
     * @return
     *      Returns the JClass representing the superclass of the
     *      entity (class or interface) represented by this {@link JClass}.
     *      Even if no super class is given explicitly or this {@link JClass}
     *      is not a class, this method still returns
     *      {@link JClass} for {@link Object}.
     *      If this JClass represents {@link Object}, return null.
     */
    abstract public JClass _extends();
    
    /**
     * Iterates all super interfaces directly implemented by
     * this class/interface.
     * 
     * @return
     *		A non-null valid iterator that iterates all
     *		{@link JClass} objects that represents those interfaces
     *		implemented by this object.
     */
    abstract public Iterator _implements();
    
    /**
     * Iterates all the type parameters of this class/interface.
     * 
     * <p>
     * For example, if this {@link JClass} represents 
     * <code>Set&lt;T></code>, this method returns an array
     * that contains single {@link JTypeVar} for 'T'.
     */
    abstract public JTypeVar[] typeParams();
    
    /**
     * Checks if this object represents an interface.
     */
    abstract public boolean isInterface();

    /**
     * Checks if this class is an abstract class.
     */
    abstract public boolean isAbstract();

    /**
     * If this class represents one of the wrapper classes
     * defined in the java.lang package, return the corresponding
     * primitive type. Otherwise null.
     */
    public JPrimitiveType getPrimitiveType() { return null; }

    /**
     * @deprecated calling this method from {@link JClass}
     * would be meaningless, since it's always guaranteed to
     * return <tt>this</tt>.
     */
    public JClass boxify() { return this; }

    public JClass erasure() {
        return this;
    }

    /**
     * Checks the relationship between two classes.
     * <p>
     * This method works in the same way as {@link Class#isAssignableFrom(java.lang.Class)}
     * works. For example, baseClass.isAssignableFrom(derivedClass)==true.
     */
    public final boolean isAssignableFrom( JClass derived ) {
        // to avoid the confusion, always use "this" explicitly in this method.
        
        // null can be assigned to any type.
        if( derived instanceof JNullType )  return true;
        
        if( this==derived )     return true;
        
        // the only class that is assignable from an interface is
        // java.lang.Object
        if( this==_package().owner().ref(Object.class) )  return true;
        
        JClass b = derived._extends();
        if( b!=null && this.isAssignableFrom(b) )
            return true;
        
        if( this.isInterface() ) {
            Iterator itfs = derived._implements();
            while( itfs.hasNext() )
                if( this.isAssignableFrom((JClass)itfs.next()) )
                    return true;
        }
        
        return false;
    }

    /**
     * Gets the parameterization of the given base type.
     *
     * <p>
     * For example, given the following
     * <pre><xmp>
     * interface Foo<T> extends List<List<T>> {}
     * interface Bar extends Foo<String> {}
     * </xmp></pre>
     * This method works like this:
     * <pre><xmp>
     * getBaseClass( Bar, List ) = List<List<String>
     * getBaseClass( Bar, Foo  ) = Foo<String>
     * getBaseClass( Foo<? extends Number>, Collection ) = Collection<List<? extends Number>>
     * getBaseClass( ArrayList<? extends BigInteger>, List ) = List<? extends BigInteger>
     * </xmp></pre>
     *
     * @param baseType
     *      The class whose parameterization we are interested in.
     * @return
     *      The use of {@code baseType} in {@code this} type.
     *      or null if the type is not assignable to the base type.
     */
    public final JClass getBaseClass( JClass baseType ) {

        if( this.erasure()==baseType )
            return this;

        JClass b = _extends();
        if( b!=null ) {
            JClass bc = b.getBaseClass(baseType);
            if(bc!=null)
                return bc;
        }

        Iterator itfs = _implements();
        while( itfs.hasNext() ) {
            JClass bc = ((JClass)itfs.next()).getBaseClass(baseType);
            if(bc!=null)
                return bc;
        }

        return null;
    }

    public final JClass getBaseClass( Class baseType ) {
        return getBaseClass(owner().ref(baseType));
    }


    private JClass arrayClass;
    public JClass array() {
        if(arrayClass==null)
            arrayClass = new JArrayClass(owner(),this);
        return arrayClass;
    }

    /**
     * "Narrows" a generic class to a concrete class by specifying
     * a type argument.
     * 
     * <p>
     * <code>.narrow(X)</code> builds <code>Set&lt;X></code> from <code>Set</code>.
     */
    public JClass narrow( Class clazz ) {
        return narrow(owner().ref(clazz));
    }

    public JClass narrow( Class... clazz ) {
        JClass[] r = new JClass[clazz.length];
        for( int i=0; i<clazz.length; i++ )
            r[i] = owner().ref(clazz[i]);
        return narrow(r);
    }

    /**
     * "Narrows" a generic class to a concrete class by specifying
     * a type argument.
     * 
     * <p>
     * <code>.narrow(X)</code> builds <code>Set&lt;X></code> from <code>Set</code>.
     */
    public JClass narrow( JClass clazz ) {
        return new JNarrowedClass(this,clazz);
    }

    public JClass narrow( JClass... clazz ) {
        return new JNarrowedClass(this,clazz.clone());
    }

    /**
     * If this class is parameterized, return the type parameter of the given index.
     */
    public JClass getTypeParameter(int index) {
        throw new IllegalArgumentException();
    }

    /**
     * Returns true if this class is a parameterized class.
     */
    public final boolean isParameterized() {
        return erasure()!=this;
    }

    /**
     * Substitutes the type variables with their actual arguments.
     * 
     * <p>
     * For example, when this class is Map&lt;String,Map&lt;V>>,
     * (where V then doing
     * substituteParams( V, Integer ) returns a {@link JClass}
     * for <code>Map&lt;String,Map&lt;Integer>></code>.
     * 
     * <p>
     * This method needs to work recursively.
     */
    protected abstract JClass substituteParams( JTypeVar[] variables, JClass[] bindings );
    
    public String toString() {
        return this.getClass().getName() + "(" + name() + ")";
    }


    public final JExpression dotclass() {
        return JExpr.dotclass(this);
    }

    /** Generates a static method invocation. */
    public final JInvocation staticInvoke(JMethod method) {
        return staticInvoke(method.name());
    }
    
    /** Generates a static method invocation. */
    public final JInvocation staticInvoke(String method) {
        return new JInvocation(this,method);
    }
    
    /** Static field reference. */
    public final JFieldRef staticRef(String field) {
        return new JFieldRef(this, field);
    }

    /** Static field reference. */
    public final JFieldRef staticRef(JVar field) {
        return staticRef(field.name());
    }

    public void generate(JFormatter f) {
        f.t(this);
    }
}
