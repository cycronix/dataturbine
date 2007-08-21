/* $Id: UsageMarker.java,v 1.23 2003/12/06 22:15:38 eric Exp $
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
package proguard.shrink;

import proguard.classfile.*;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;


/**
 * This ClassFileVisitor and MemberInfoVisitor recursively marks all
 * classes and class elements that are being used.
 *
 * @see ClassFileShrinker
 *
 * @author Eric Lafortune
 */
public class UsageMarker
  implements ClassFileVisitor,
             MemberInfoVisitor,
             CpInfoVisitor,
             AttrInfoVisitor,
             InstructionVisitor,
             InnerClassesInfoVisitor,
             ExceptionInfoVisitor,
             LocalVariableInfoVisitor
{
    // A visitor info flag to indicate the ProgramMemberInfo object is being used,
    // if its ClassFile can be determined as being used as well.
    private static final Object POSSIBLY_USED = new Object();
    // A visitor info flag to indicate the visitor accepter is being used.
    private static final Object USED          = new Object();


    // A field acting as a parameter to the visitMemberInfo method.
    private boolean processing = false;

    private MyInterfaceUsageMarker interfaceUsageMarker = new MyInterfaceUsageMarker();


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        if (!isUsed(programClassFile))
        {
            // Mark this class.
            markAsUsed(programClassFile);

            // Mark this class's name.
            markCpEntry(programClassFile, programClassFile.u2thisClass);

            // Mark the superclass.
            if (programClassFile.u2superClass != 0)
            {
                markCpEntry(programClassFile, programClassFile.u2superClass);
            }

            // Give the interfaces preliminary marks.
            programClassFile.accept(
                new ClassFileUpDownTraveler(false, false, true, false,
                                            interfaceUsageMarker));

            // Note that the <clinit> method and the parameterless <init> method
            // are 'overridden' from the ones in java.lang.Object, and therefore
            // always marked as being used. Well, not always, unfortunately.
            // The MIDP run-time jar midpapi.zip has a version of java.lang.Object
            // without a <clinit> method. So we'll explicitly mark it before
            // processing the methods.
            programClassFile.methodAccept(this,
                                          ClassConstants.INTERNAL_METHOD_NAME_CLINIT,
                                          ClassConstants.INTERNAL_METHOD_TYPE_CLINIT);

            // Process all fields and methods that have already been marked as
            // possibly used.
            processing = true;
            programClassFile.fieldsAccept(this);
            programClassFile.methodsAccept(this);
            processing = false;

            // Mark the attributes.
            programClassFile.attributesAccept(this);
        }
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        if (!isUsed(libraryClassFile))
        {
            markAsUsed(libraryClassFile);

            // We're not going to analyze all library code. We're assuming that
            // if this class is being used, all of its methods will be used as
            // well. We'll mark them as such (here and in all subclasses).

            // Mark the superclass.
            ClassFile superClass = libraryClassFile.superClass;
            if (superClass != null)
            {
                superClass.accept(this);
            }

            // Mark the interfaces.
            ClassFile[] interfaceClasses = libraryClassFile.interfaceClasses;
            if (interfaceClasses != null)
            {
                for (int i = 0; i < interfaceClasses.length; i++)
                {
                    if (interfaceClasses[i] != null)
                    {
                        interfaceClasses[i].accept(this);
                    }
                }
            }

            // Mark all methods.
            libraryClassFile.methodsAccept(this);
        }
    }


    /**
     * This ClassFileVisitor marks ProgramClassFile objects as possibly used,
     * and it visits LibraryClassFile objects with its outer UsageMarker.
     */
    private class MyInterfaceUsageMarker implements ClassFileVisitor
    {
        public void visitProgramClassFile(ProgramClassFile programClassFile)
        {
            if (!isUsed(programClassFile))
            {
                // We can't process the interface yet, because it might not
                // be required. Give it a preliminary mark.
                markAsPossiblyUsed(programClassFile);
            }
        }

        public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
        {
            // Make sure all library interface methods are marked.
            UsageMarker.this.visitLibraryClassFile(libraryClassFile);
        }
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        visitMemberInfo(programClassFile, programFieldInfo);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        visitMemberInfo(programClassFile, programMethodInfo);
    }


    private void visitMemberInfo(ProgramClassFile programClassFile, ProgramMemberInfo programMemberInfo)
    {
        if (!isUsed(programMemberInfo))
        {
            if (processing ? isPossiblyUsed(programMemberInfo) :
                             isUsed(programClassFile))
            {
                boolean oldProcessing = processing;
                processing = false;

                markAsUsed(programMemberInfo);

                // Mark the name and descriptor.
                markCpEntry(programClassFile, programMemberInfo.u2nameIndex);
                markCpEntry(programClassFile, programMemberInfo.u2descriptorIndex);

                // Mark the attributes.
                programMemberInfo.attributesAccept(programClassFile, this);

                // Mark the classes referenced in the descriptor string.
                programMemberInfo.referencedClassesAccept(this);

                // Restore the processing flag.
                processing = oldProcessing;
            }
            else
            {
                // We can't process the class member yet, because the class
                // file isn't marked as being used (yet). Give it a
                // preliminary mark.
                markAsPossiblyUsed(programMemberInfo);
            }
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}

    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        if (!isUsed(libraryMethodInfo))
        {
            markAsUsed(libraryMethodInfo);

            String name = libraryMethodInfo.getName(libraryClassFile);
            String type = libraryMethodInfo.getDescriptor(libraryClassFile);

            // Mark all implementations of the method.
            // Library class methods are supposed to be used by
            // default, so we don't want to lose their redefinitions.
            //
            // For an abstract method:
            //   First go to  all concrete classes of the interface.
            //   From there, travel up and down the class hierarchy to mark
            //   the method.
            //
            //   This way, we're also catching retro-fitted interfaces,
            //   where a class's implementation of an interface method is
            //   hiding higher up its class hierarchy.
            //
            // For a concrete method:
            //   Simply mark all overriding implementations down the
            //   class hierarchy.
            libraryClassFile.accept(
                (libraryMethodInfo.getAccessFlags() &
                 ClassConstants.INTERNAL_ACC_ABSTRACT) != 0 ?

                (ClassFileVisitor)
                new ConcreteClassFileDownTraveler(
                new ClassFileUpDownTraveler(true, true, false, true,
                new NamedMethodVisitor(this, name, type))) :

                (ClassFileVisitor)
                new ClassFileUpDownTraveler(false, false, false, true,
                new NamedMethodVisitor(this, name, type)));
        }
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo)
    {
        if (!isUsed(integerCpInfo))
        {
            markAsUsed(integerCpInfo);
        }
    }


    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo)
    {
        if (!isUsed(longCpInfo))
        {
            markAsUsed(longCpInfo);
        }
    }


    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo)
    {
        if (!isUsed(floatCpInfo))
        {
            markAsUsed(floatCpInfo);
        }
    }


    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo)
    {
        if (!isUsed(doubleCpInfo))
        {
            markAsUsed(doubleCpInfo);
        }
    }


    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        if (!isUsed(stringCpInfo))
        {
            markAsUsed(stringCpInfo);

            markCpEntry(classFile, stringCpInfo.u2stringIndex);

            // Mark the referenced class, if the string is being used in
            // a Class.forName construct.
            stringCpInfo.referencedClassAccept(this);
        }
    }


    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo)
    {
        if (!isUsed(utf8CpInfo))
        {
            markAsUsed(utf8CpInfo);
        }
    }


    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        if (!isUsed(fieldrefCpInfo))
        {
            markAsUsed(fieldrefCpInfo);

            markCpEntry(classFile, fieldrefCpInfo.u2classIndex);
            markCpEntry(classFile, fieldrefCpInfo.u2nameAndTypeIndex);

            // When compiled with "-target 1.2", the class actually containing
            // the referenced field may be higher up the hierarchy. It should
            // be marked as one of the super classes, but we'll mark it here
            // as well, as we do for method references.
            fieldrefCpInfo.referencedClassAccept(this);

            // Mark the referenced field itself.
            fieldrefCpInfo.referencedMemberInfoAccept(this);
        }
    }


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        if (!isUsed(interfaceMethodrefCpInfo))
        {
            markAsUsed(interfaceMethodrefCpInfo);

            markCpEntry(classFile, interfaceMethodrefCpInfo.u2classIndex);
            markCpEntry(classFile, interfaceMethodrefCpInfo.u2nameAndTypeIndex);

            // When compiled with "-target 1.2", the interface actually
            // containing the referenced method may be higher up the
            // hierarchy. Make sure it's marked, in case it isn't
            // used elsewhere.
            interfaceMethodrefCpInfo.referencedClassAccept(this);

            // Mark the referenced interface method itself.
            interfaceMethodrefCpInfo.referencedMemberInfoAccept(this);

            String name = interfaceMethodrefCpInfo.getName(classFile);
            String type = interfaceMethodrefCpInfo.getType(classFile);

            // Mark all implementations of the method.
            // First go to  all concrete classes of the interface.
            // From there, travel up and down the class hierarchy to mark
            // the method.
            //
            // This way, we're also catching retro-fitted interfaces, where
            // a class's implementation of an interface method is hiding
            // higher up its class hierarchy.
            interfaceMethodrefCpInfo.referencedClassAccept(
                new ConcreteClassFileDownTraveler(
                new ClassFileUpDownTraveler(true, true, false, true,
                new NamedMethodVisitor(this, name, type))));
        }
    }


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        if (!isUsed(methodrefCpInfo))
        {
            markAsUsed(methodrefCpInfo);

            markCpEntry(classFile, methodrefCpInfo.u2classIndex);
            markCpEntry(classFile, methodrefCpInfo.u2nameAndTypeIndex);

            // When compiled with "-target 1.2", the class or interface
            // actually containing the referenced method may be higher up
            // the hierarchy. Make sure it's marked, in case it isn't
            // used elsewhere.
            methodrefCpInfo.referencedClassAccept(this);

            // Mark the referenced method itself.
            methodrefCpInfo.referencedMemberInfoAccept(this);

            String name = methodrefCpInfo.getName(classFile);
            String type = methodrefCpInfo.getType(classFile);

            // Mark all overriding implementations of the method,
            // down the class hierarchy.
            methodrefCpInfo.referencedClassAccept(
                new ClassFileUpDownTraveler(false, false, false, true,
                new NamedMethodVisitor(this, name, type)));
        }
    }


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        if (!isUsed(classCpInfo))
        {
            markAsUsed(classCpInfo);

            markCpEntry(classFile, classCpInfo.u2nameIndex);

            // Mark the referenced class itself.
            classCpInfo.referencedClassAccept(this);
        }
    }


    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo)
    {
        if (!isUsed(nameAndTypeCpInfo))
        {
            markAsUsed(nameAndTypeCpInfo);

            markCpEntry(classFile, nameAndTypeCpInfo.u2nameIndex);
            markCpEntry(classFile, nameAndTypeCpInfo.u2descriptorIndex);

            // Mark the classes referenced in the descriptor string.
            nameAndTypeCpInfo.referencedClassesAccept(this);
        }
    }


    // Implementations for AttrInfoVisitor.
    // Note that attributes are typically only referenced once, so we don't
    // test if they have been marked already.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo)
    {
        // This is the best we can do for unknown attributes.
        markAsUsed(unknownAttrInfo);

        markCpEntry(classFile, unknownAttrInfo.u2attrNameIndex);
    }


    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo)
    {
        // Don't mark the attribute and its name yet. We may mark it later, in
        // InnerUsageMarker.
        //markAsUsed(innerClassesAttrInfo);

        //markCpEntry(classFile, innerClassesAttrInfo.u2attrNameIndex);
        innerClassesAttrInfo.innerClassEntriesAccept(classFile, this);
    }


    public void visitConstantValueAttrInfo(ClassFile classFile, ConstantValueAttrInfo constantValueAttrInfo)
    {
        markAsUsed(constantValueAttrInfo);

        markCpEntry(classFile, constantValueAttrInfo.u2attrNameIndex);
        markCpEntry(classFile, constantValueAttrInfo.u2constantValueIndex);
    }


    public void visitExceptionsAttrInfo(ClassFile classFile, ExceptionsAttrInfo exceptionsAttrInfo)
    {
        markAsUsed(exceptionsAttrInfo);

        markCpEntry(classFile, exceptionsAttrInfo.u2attrNameIndex);

        exceptionsAttrInfo.exceptionEntriesAccept((ProgramClassFile)classFile, this);
    }


    public void visitCodeAttrInfo(ClassFile classFile, CodeAttrInfo codeAttrInfo)
    {
        markAsUsed(codeAttrInfo);

        markCpEntry(classFile, codeAttrInfo.u2attrNameIndex);

        codeAttrInfo.instructionsAccept(classFile, this);
        codeAttrInfo.exceptionsAccept(classFile, this);
        codeAttrInfo.attributesAccept(classFile, this);
    }


    public void visitLineNumberTableAttrInfo(ClassFile classFile, LineNumberTableAttrInfo lineNumberTableAttrInfo)
    {
        markAsUsed(lineNumberTableAttrInfo);

        markCpEntry(classFile, lineNumberTableAttrInfo.u2attrNameIndex);
    }


    public void visitLocalVariableTableAttrInfo(ClassFile classFile, LocalVariableTableAttrInfo localVariableTableAttrInfo)
    {
        markAsUsed(localVariableTableAttrInfo);

        markCpEntry(classFile, localVariableTableAttrInfo.u2attrNameIndex);

        localVariableTableAttrInfo.localVariablesAccept(classFile, this);
    }


    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo)
    {
        markAsUsed(sourceFileAttrInfo);

        markCpEntry(classFile, sourceFileAttrInfo.u2attrNameIndex);
        markCpEntry(classFile, sourceFileAttrInfo.u2sourceFileIndex);
    }


    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo)
    {
        markAsUsed(sourceDirAttrInfo);

        markCpEntry(classFile, sourceDirAttrInfo.u2attrNameIndex);
        markCpEntry(classFile, sourceDirAttrInfo.u2sourceDirIndex);
    }


    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo)
    {
        markAsUsed(deprecatedAttrInfo);

        markCpEntry(classFile, deprecatedAttrInfo.u2attrNameIndex);
    }


    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo)
    {
        markAsUsed(syntheticAttrInfo);

        markCpEntry(classFile, syntheticAttrInfo.u2attrNameIndex);
    }


    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo)
    {
        markAsUsed(signatureAttrInfo);

        markCpEntry(classFile, signatureAttrInfo.u2attrNameIndex);
        markCpEntry(classFile, signatureAttrInfo.u2signatureIndex);
    }


    // Implementations for InstructionVisitor.

    public void visitInstruction(ClassFile classFile, Instruction instruction)
    {
        // Just ignore generic instructions.
    }


    public void visitCpInstruction(ClassFile classFile, CpInstruction cpInstruction)
    {
        markCpEntry(classFile, cpInstruction.getCpIndex());
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(ClassFile classFile, ExceptionInfo exceptionInfo)
    {
        markAsUsed(exceptionInfo);

        if (exceptionInfo.u2catchType != 0)
        {
            markCpEntry(classFile, exceptionInfo.u2catchType);
        }
    }


    // Implementations for InnerClassesInfoVisitor.

    public void visitInnerClassesInfo(ClassFile classFile, InnerClassesInfo innerClassesInfo)
    {
        // For now, only make sure we mark outer classes of this class.
        if (innerClassesInfo.u2innerClassInfoIndex != 0 ||
            !classFile.getName().equals(classFile.getCpClassNameString(innerClassesInfo.u2innerClassInfoIndex)))
        {
            // Skip any other InnerClassesInfo. We may mark it later, in
            // InnerUsageMarker.
            return;
        }

        markAsUsed(innerClassesInfo);

        if (innerClassesInfo.u2innerClassInfoIndex != 0)
        {
            markCpEntry(classFile, innerClassesInfo.u2innerClassInfoIndex);
        }

        if (innerClassesInfo.u2outerClassInfoIndex != 0)
        {
            markCpEntry(classFile, innerClassesInfo.u2outerClassInfoIndex);
        }

        if (innerClassesInfo.u2innerNameIndex != 0)
        {
            markCpEntry(classFile, innerClassesInfo.u2innerNameIndex);
        }
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(ClassFile classFile, LocalVariableInfo localVariableInfo)
    {
        markCpEntry(classFile, localVariableInfo.u2nameIndex);
        markCpEntry(classFile, localVariableInfo.u2descriptorIndex);
    }


    // Small utility methods.

    /**
     * Marks the given constant pool entry of the given class. This includes
     * visiting any referenced objects.
     */
    private void markCpEntry(ClassFile classFile, int index)
    {
         classFile.constantPoolEntryAccept(this, index);
    }


    static void markAsUnused(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(null);
    }


    static void markAsPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(POSSIBLY_USED);
    }


    static boolean isPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == POSSIBLY_USED;
    }


    static void markAsUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(USED);
    }


    static boolean isUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == USED;
    }
}
