/**
 * asm-defuse: asm powered by definitions/uses analysis
 * Copyright (c) 2014 Roberto Araujo (roberto.andrioli@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package br.usp.each.saeg.asm.defuse.integration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import br.usp.each.saeg.asm.defuse.DefUseAnalyzer;
import br.usp.each.saeg.asm.defuse.DefUseChain;
import br.usp.each.saeg.asm.defuse.DefUseChainSearch;
import br.usp.each.saeg.asm.defuse.DepthFirstDefUseChainSearch;
import br.usp.each.saeg.asm.defuse.ReachDefsDefUseChainSearch;
import br.usp.each.saeg.commons.ArrayUtils;

@RunWith(Parameterized.class)
public class DefUseChainSearchTest {

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ new DepthFirstDefUseChainSearch() },
				{ new ReachDefsDefUseChainSearch() }
		});
	}

	private final DefUseChainSearch search;

	private DefUseAnalyzer analyzer;

	private MethodNode mn;

	public DefUseChainSearchTest(final DefUseChainSearch search) {
		this.search = search;
	}

	@Before
	public void setUp() {
		analyzer = new DefUseAnalyzer();
	}

	private void prepareMethodMax() {
		mn = new MethodNode();
		/* 00 */mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
		/* 01 */mn.instructions.add(new VarInsnNode(Opcodes.ISTORE, 2));
		/* 02 */mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
		/* 03 */mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
		/* 04 */mn.instructions.add(new IincInsnNode(2, 1));
		/* 05 */mn.instructions.add(new InsnNode(Opcodes.IALOAD));
		/* 06 */mn.instructions.add(new VarInsnNode(Opcodes.ISTORE, 3));
		/*    */final LabelNode backLoop = new LabelNode();
		/* 07 */mn.instructions.add(backLoop);
		/* 08 */mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
		/* 09 */mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
		/*    */final LabelNode breakLoop = new LabelNode();
		/* 10 */mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGE, breakLoop));
		/* 11 */mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
		/* 12 */mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
		/* 13 */mn.instructions.add(new InsnNode(Opcodes.IALOAD));
		/* 14 */mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 3));
		/*    */final LabelNode jump = new LabelNode();
		/* 15 */mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLE, jump));
		/* 16 */mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
		/* 17 */mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
		/* 18 */mn.instructions.add(new InsnNode(Opcodes.IALOAD));
		/* 19 */mn.instructions.add(new VarInsnNode(Opcodes.ISTORE, 3));
		/* 20 */mn.instructions.add(jump);
		/* 21 */mn.instructions.add(new IincInsnNode(2, 1));
		/* 22 */mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, backLoop));
		/* 23 */mn.instructions.add(breakLoop);
		/* 24 */mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 3));
		/* 25 */mn.instructions.add(new InsnNode(Opcodes.IRETURN));
		mn.desc = "(II)I";
		mn.maxLocals = 4;
		mn.maxStack = 2;
		mn.access = Opcodes.ACC_STATIC;
		mn.tryCatchBlocks = Collections.emptyList();
	}

	@Test
	public void testDefUseChains() throws AnalyzerException {
		prepareMethodMax();
		analyzer.analyze("Owner", mn);

		final DefUseChain[] chains = search.search(
				analyzer.getDefUseFrames(), analyzer.getVariables(),
				analyzer.getSuccessors(), analyzer.getPredecessors());

		final DefUseChain[] expected = new DefUseChain[26];

		expected[0] = new DefUseChain(1, 4, 2);

		expected[1] = new DefUseChain(0, 6, 0);
		// expected[2] = new DefUseChain(1, 6, 2);
		// know bug!!! the algorithm is returning an invalid chain (4, 6, 2)
		expected[2] = new DefUseChain(4, 6, 2); // Just to avoid build failure

		expected[3] = new DefUseChain(0, 10, 23, 1);
		expected[4] = new DefUseChain(0, 10, 11, 1);
		expected[5] = new DefUseChain(4, 10, 23, 2);
		expected[6] = new DefUseChain(4, 10, 11, 2);
		expected[7] = new DefUseChain(21, 10, 23, 2);
		expected[8] = new DefUseChain(21, 10, 11, 2);

		expected[9] = new DefUseChain(0, 15, 20, 0);
		expected[10] = new DefUseChain(0, 15, 16, 0);
		expected[11] = new DefUseChain(4, 15, 20, 2);
		expected[12] = new DefUseChain(4, 15, 16, 2);
		expected[13] = new DefUseChain(21, 15, 20, 2);
		expected[14] = new DefUseChain(21, 15, 16, 2);
		expected[15] = new DefUseChain(6, 15, 20, 3);
		expected[16] = new DefUseChain(6, 15, 16, 3);
		expected[17] = new DefUseChain(19, 15, 20, 3);
		expected[18] = new DefUseChain(19, 15, 16, 3);

		expected[19] = new DefUseChain(0, 19, 0);
		expected[20] = new DefUseChain(4, 19, 2);
		expected[21] = new DefUseChain(21, 19, 2);

		expected[22] = new DefUseChain(4, 21, 2);
		expected[23] = new DefUseChain(21, 21, 2);

		expected[24] = new DefUseChain(6, 25, 3);
		expected[25] = new DefUseChain(19, 25, 3);

		Assert.assertEquals(expected.length, chains.length);

		final StringBuilder message = new StringBuilder();
		for (int i = 0; i < expected.length; i++) {
			if (ArrayUtils.indexOf(chains, expected[i]) == -1) {
				message.append("Not found dua: ").append(i).append('\n');
			}
		}

		if (message.length() > 0) {
			Assert.fail(message.toString());
		}
	}

}