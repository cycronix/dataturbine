/* $Id: AttrInfoVisitor.java,v 1.7 2003/11/10 16:50:56 eric Exp $
 *
 * ProGuard -- obfuscation and shrinking package for Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.classfile.visitor;

import proguard.classfile.*;


/**
 * This interface specifies the methods for a visitor of <code>AttrInfo</code>
 * objects.
 *
 * @author Eric Lafortune
 */
public interface AttrInfoVisitor
{
    public void visitUnknownAttrInfo(           ClassFile classFile, UnknownAttrInfo            unknownAttrInfo);
    public void visitInnerClassesAttrInfo(      ClassFile classFile, InnerClassesAttrInfo       innerClassesAttrInfo);
    public void visitConstantValueAttrInfo(     ClassFile classFile, ConstantValueAttrInfo      constantValueAttrInfo);
    public void visitExceptionsAttrInfo(        ClassFile classFile, ExceptionsAttrInfo         exceptionsAttrInfo);
    public void visitCodeAttrInfo(              ClassFile classFile, CodeAttrInfo               codeAttrInfo);
    public void visitLineNumberTableAttrInfo(   ClassFile classFile, LineNumberTableAttrInfo    lineNumberTableAttrInfo);
    public void visitLocalVariableTableAttrInfo(ClassFile classFile, LocalVariableTableAttrInfo localVariableTableAttrInfo);
    public void visitSourceFileAttrInfo(        ClassFile classFile, SourceFileAttrInfo         sourceFileAttrInfo);
    public void visitSourceDirAttrInfo(         ClassFile classFile, SourceDirAttrInfo          sourceDirAttrInfo);
    public void visitDeprecatedAttrInfo(        ClassFile classFile, DeprecatedAttrInfo         deprecatedAttrInfo);
    public void visitSyntheticAttrInfo(         ClassFile classFile, SyntheticAttrInfo          syntheticAttrInfo);
    public void visitSignatureAttrInfo(         ClassFile classFile, SignatureAttrInfo          syntheticAttrInfo);
}
