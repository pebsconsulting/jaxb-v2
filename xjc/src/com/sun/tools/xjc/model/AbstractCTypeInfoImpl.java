package com.sun.tools.xjc.model;

import javax.activation.MimeType;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.xml.bind.v2.model.core.ID;

import org.relaxng.datatype.ValidationContext;

/**
 * Partial implementation of {@link CTypeInfo}.
 *
 * <p>
 * The inheritance of {@link TypeUse} by {@link CTypeInfo}
 * isn't a normal inheritance (see {@link CTypeInfo} for more.)
 * This class implments methods on {@link TypeUse} for {@link CTypeInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractCTypeInfoImpl implements CTypeInfo {

    private final CCustomizations customizations;

    protected AbstractCTypeInfoImpl(Model model, CCustomizations customizations) {
        if(customizations==null)
            customizations = CCustomizations.EMPTY;
        else
            customizations.setParent(model,this);
        this.customizations = customizations;
    }

    public final boolean isCollection() {
        return false;
    }

    public final CAdapter getAdapterUse() {
        return null;
    }

    public final CTypeInfo getInfo() {
        return this;
    }

    public final ID idUse() {
        return ID.NONE;
    }

    /**
     * No default {@link MimeType}.
     */
    public MimeType getExpectedMimeType() {
        return null;
    }

    public CCustomizations getCustomizations() {
        return customizations;
    }

    // this is just a convenient default
    public JExpression createConstant(JCodeModel codeModel, String lexical, ValidationContext context) {
        return null;
    }
}
