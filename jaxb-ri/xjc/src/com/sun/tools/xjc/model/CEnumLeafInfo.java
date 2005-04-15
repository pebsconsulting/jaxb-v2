/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.model;

import java.util.Collection;
import java.util.List;
import java.util.Collections;

import javax.xml.namespace.QName;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.EnumLeafInfo;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.NonElement;

import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Locator;

/**
 * Transducer that converts a string into an "enumeration class."
 * 
 * The structure of the generated class needs to precisely
 * follow the JAXB spec.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public final class CEnumLeafInfo implements EnumLeafInfo<NType,NClass>, NClass, CNonElement
{
    /**
     * The parent into which the enum class should be generated.
     */
    public final CClassInfoParent parent;

    /**
     * Short name of the generated type-safe enum.
     */
    public final String shortName;

    private final QName typeName;

    /**
     * {@link Model} that owns this object.
     */
    /*package*/ final Model model;

    /**
     * Represents the underlying type of this enumeration
     * and its conversion.
     *
     * <p>
     * To parse XML into a constant, we use the base type
     * to do lexical -> value, then use a map to pick up the right one.
     *
     * <p>
     * Hence this also represents the type of the Java value.
     * For example, if this is an enumeration of xs:int,
     * then this field will be Java int.
     */
    public final CNonElement base;


    /**
     * List of enum members.
     */
    public final Collection<CEnumConstant> members;

    private final List<CPluginCustomization> customizations;
    /**
     * Source line information that points to the place
     * where this type-safe enum is defined.
     * Used to report error messages.
     */
    public final Locator sourceLocator;

    public String javadoc;

    /**
     * @param _members
     */
    public CEnumLeafInfo(Model model,
                         QName typeName,
                         CClassInfoParent container,
                         String shortName,
                         CNonElement base,
                         Collection<CEnumConstant> _members,
                         List<CPluginCustomization> customizations,
                         Locator _sourceLocator) {
        this.model = model;
        this.parent = container;
        this.shortName = shortName;
        this.base = base;
        this.members = _members;
        if(customizations==null)
            customizations = Collections.emptyList();
        this.customizations = customizations;
        this.sourceLocator = _sourceLocator;
        this.typeName = typeName;

        for( CEnumConstant mem : members )
            mem.setParent(this);

        model.add(this);
    }


    public QName getTypeName() {
        return typeName;
    }

    public NType getType() {
        return this;
    }

    public NClass getClazz() {
        return this;
    }

    public JClass toType(Outline o, Aspect aspect) {
        return o.getEnum(this).clazz;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isBoxedType() {
        return false;
    }

    public String fullName() {
        return parent.fullName()+'.'+shortName;
    }

    public boolean isPrimitive() {
        return false;
    }

    /**
     * The spec says the value field in the enum class will be generated
     * only under certain circumstances.
     *
     * @return
     *      true if the generated enum class should have the value field.
     */
    public boolean needsValueField() {
        for (CEnumConstant cec : members) {
            if(!cec.getName().equals(cec.getLexicalValue()))
                return true;
        }
        return false;
    }

    public JExpression createConstant(JCodeModel codeModel, String literal, ValidationContext context) {
        // it is difficult to generate constants for enums,
        // because when this method is called we still haven't generated the enum class yet,
        // which seems to point to a general problem that default value expression computation
        // is done too early.
        return null;
    }

    public boolean isCollection() {
        return false;
    }

    public CAdapter getAdapterUse() {
        return null;
    }

    public CTypeInfo getInfo() {
        return this;
    }

    public ID idUse() {
        return ID.NONE;
    }

    public Collection<CEnumConstant> getConstants() {
        return members;
    }

    public NonElement<NType,NClass> getBaseType() {
        return base;
    }

    public List<CPluginCustomization> getCustomizations() {
        return customizations;
    }
}
