package com.sun.xml.bind.v2.runtime.reflect;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.runtime.IDHandler;
import com.sun.xml.bind.v2.runtime.XMLSerializer;

/**
 * Used to list individual values of a multi-value property, and
 * to pack individual values into a multi-value property.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public abstract class Lister<BeanT,PropT,ItemT,PackT> {

    protected Lister() {}

    /**
     * Iterates values of a multi-value property.
     *
     * @param context
     *      This parameter is used to support ID/IDREF handling.
     */
    public abstract ListIterator<ItemT> iterator(PropT multiValueProp, XMLSerializer context);

    /**
     * Setting values to a multi-value property starts by creating
     * a transient object called "pack" from the current field.
     */
    public abstract PackT startPacking(BeanT bean, Accessor<BeanT, PropT> acc) throws AccessorException;

    /**
     * Once the {@link #startPacking} is called, you can
     * add values to the pack by using this method.
     */
    public abstract void addToPack( PackT pack, ItemT newValue );

    /**
     * Finally, call this method to
     * wraps up the {@code pack}. This method may update the field of
     * the given bean.
     */
    public abstract void endPacking( PackT pack, BeanT bean, Accessor<BeanT,PropT> acc ) throws AccessorException;

    /**
     * Clears the values of the property.
     */
    public abstract void reset(BeanT o,Accessor<BeanT,PropT> acc) throws AccessorException;


    /**
     * Gets a reference to the appropriate {@link Lister} object
     * if the field is a multi-value field. Otherwise null.
     *
     * @param fieldType
     *      the type of the field that stores the collection
     * @param idness
     *      ID-ness of the property.
     */
    public static final <BeanT,PropT,ItemT,PackT> Lister<BeanT,PropT,ItemT,PackT> create(Class fieldType,ID idness) {
        Lister l;
        if( fieldType.isArray() )
            l = getArrayLister(fieldType.getComponentType());
        else
        if( Collection.class.isAssignableFrom(fieldType) )
            l = new CollectionLister(getImplClass(fieldType));
        else
            return null;

        if(idness==ID.IDREF)
            l = new IDHandler.IDREFS(l);

        return l;
    }

    private static final Class[] implClasses = new Class[] {
        ArrayList.class,
        HashSet.class,
        Stack.class,
    };

    private static Class getImplClass(Class fieldType) {
        TODO.checkSpec();       // TODO: semantics?
        if(!fieldType.isInterface())
            // instanciable class?
            return fieldType;

        for( Class impl : implClasses ) {
            if(fieldType.isAssignableFrom(impl))
                return impl;
        }

        // if we can't find an implementation class,
        // let's just hope that we will never need to create a new object,
        // and returns null
        return null;
    }

    /**
     * Cache instances of {@link ArrayLister}s.
     */
    private static final Map<Class,Lister> arrayListerCache =
        Collections.synchronizedMap(new WeakHashMap<Class,Lister>());
    
    /**
     * Creates a lister for array type.
     */
    private static Lister getArrayLister( Class componentType ) {
        Lister l;
        if(componentType.isPrimitive())
            l = primitiveArrayListers.get(componentType);
        else {
            l = arrayListerCache.get(componentType);
            if(l==null) {
                l = new ArrayLister(componentType);
                arrayListerCache.put(componentType,l);
            }
        }
        assert l!=null;
        return l;
    }

    /**
     * {@link Lister} for an array.
     *
     * <p>
     * Array packing is slower, but we expect this to be used less frequently than
     * the {@link CollectionLister}.
     */
    private static final class ArrayLister<BeanT,ItemT> extends Lister<BeanT,ItemT[],ItemT,Pack<ItemT>> {

        private final Class<ItemT> itemType;

        public ArrayLister(Class<ItemT> itemType) {
            this.itemType = itemType;
        }

        public ListIterator<ItemT> iterator(final ItemT[] objects, XMLSerializer context) {
            return new ListIterator<ItemT>() {
                int idx=0;
                public boolean hasNext() {
                    return idx<objects.length;
                }

                public ItemT next() {
                    return objects[idx++];
                }
            };
        }

        public Pack startPacking(BeanT current, Accessor<BeanT, ItemT[]> acc) {
            return new Pack<ItemT>(itemType);
        }

        public void addToPack(Pack<ItemT> objects, ItemT o) {
            objects.add(o);
        }

        public void endPacking( Pack<ItemT> pack, BeanT bean, Accessor<BeanT,ItemT[]> acc ) throws AccessorException {
            acc.set(bean,pack.build());
        }

        public void reset(BeanT o,Accessor<BeanT,ItemT[]> acc) throws AccessorException {
            acc.set(o,(ItemT[])Array.newInstance(itemType,0));
        }

    };

    public static final class Pack<ItemT> extends ArrayList<ItemT> {
        private final Class<ItemT> itemType;

        public Pack(Class<ItemT> itemType) {
            this.itemType = itemType;
        }

        public ItemT[] build() {
            return super.toArray( (ItemT[])Array.newInstance(itemType,size()) );
        }
    }

    /**
     * Listers for the primitive type arrays, keyed by their primitive Class object.
     */
    /*package*/ static final Map<Class,Lister> primitiveArrayListers = new HashMap<Class,Lister>();

    static {
        // register primitive array listers
        PrimitiveArrayListerBoolean.register();
        PrimitiveArrayListerByte.register();
        PrimitiveArrayListerCharacter.register();
        PrimitiveArrayListerDouble.register();
        PrimitiveArrayListerFloat.register();
        PrimitiveArrayListerInteger.register();
        PrimitiveArrayListerLong.register();
        PrimitiveArrayListerShort.register();
    }

    /**
     * {@link Lister} for a collection
     */
    public static final class CollectionLister<BeanT,T extends Collection> extends Lister<BeanT,T,Object,T> {

        /**
         * Sometimes we need to create a new instance of a collection.
         * This is such an implementation class.
         */
        private final Class<? extends T> implClass;

        public CollectionLister(Class<? extends T> implClass) {
            this.implClass = implClass;
        }

        public ListIterator iterator(T collection, XMLSerializer context) {
            final Iterator itr = collection.iterator();
            return new ListIterator() {
                public boolean hasNext() {
                    return itr.hasNext();
                }
                public Object next() {
                    return itr.next();
                }
            };
        }

        public T startPacking(BeanT bean, Accessor<BeanT, T> acc) throws AccessorException {
            T collection = acc.get(bean);
            if(collection==null) {
                collection = ClassFactory.create(implClass);
                acc.set(bean,collection);
            }
            collection.clear();
            return collection;
        }

        public void addToPack(T collection, Object o) {
            collection.add(o);
        }

        public void endPacking( T pack, BeanT bean, Accessor<BeanT,T> acc ) {
        }

        public void reset(BeanT bean, Accessor<BeanT, T> acc) throws AccessorException {
            acc.get(bean).clear();
        }
    };

    /**
     * Special {@link Lister} used to recover from an error.
     */
    public static final Lister ERROR = new Lister() {
        public ListIterator iterator(Object o, XMLSerializer context) {
            return EMPTY_ITERATOR;
        }

        public Object startPacking(Object o, Accessor accessor) {
            return null;
        }

        public void addToPack(Object o, Object o1) {
        }

        public void endPacking(Object o, Object o1, Accessor accessor) {
        }

        public void reset(Object o, Accessor accessor) {
        }
    };

    private static final ListIterator EMPTY_ITERATOR = new ListIterator() {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            throw new IllegalStateException();
        }
    };
}
