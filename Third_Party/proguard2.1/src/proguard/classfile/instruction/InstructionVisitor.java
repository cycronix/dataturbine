/* $Id: InstructionVisitor.java,v 1.7 2003/02/09 15:22:28 eric Exp $
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
package proguard.classfile.instruction;

import proguard.classfile.*;


/**
 * This interface specifies the methods for a visitor of
 * <code>Instruction</code> objects. Currently, only two types of instructions
 * are considered: instructions that refer to the constant pool, and all the
 * rest.
 *
 * @author Eric Lafortune
 */
public interface InstructionVisitor
{
    public void visitInstruction(ClassFile classFile, Instruction instruction);
    public void visitCpInstruction(ClassFile classFile, CpInstruction cpInstruction);
}
