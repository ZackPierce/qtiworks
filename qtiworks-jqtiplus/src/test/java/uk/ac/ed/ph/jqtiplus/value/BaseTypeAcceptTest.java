/* Copyright (c) 2012, University of Edinburgh.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *
 * * Neither the name of the University of Edinburgh nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * This software is derived from (and contains code from) QTItools and MathAssessEngine.
 * QTItools is (c) 2008, University of Southampton.
 * MathAssessEngine is (c) 2010, University of Edinburgh.
 */
package uk.ac.ed.ph.jqtiplus.value;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests parsing <code>BaseType</code> from <code>String</code> representation.
 * <p>
 * This test contains only valid <code>String</code> representations.
 *
 * @see uk.ac.ed.ph.jqtiplus.value.BaseType
 */
@RunWith(Parameterized.class)
public class BaseTypeAcceptTest {

    /**
     * Creates test data for this test.
     *
     * @return test data for this test
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "identifier", BaseType.IDENTIFIER }, { "boolean", BaseType.BOOLEAN }, { "integer", BaseType.INTEGER },
                { "float", BaseType.FLOAT }, { "string", BaseType.STRING }, { "point", BaseType.POINT }, { "pair", BaseType.PAIR },
                { "directedPair", BaseType.DIRECTED_PAIR }, { "duration", BaseType.DURATION }, { "file", BaseType.FILE }, { "uri", BaseType.URI },
        });
    }

    private final String string;

    private final BaseType expectedBaseType;

    /**
     * Constructs this test.
     *
     * @param string <code>String</code> representation of <code>BaseType</code>
     * @param expectedBaseType expected parsed <code>BaseType</code>
     */
    public BaseTypeAcceptTest(final String string, final BaseType expectedBaseType) {
        this.string = string;
        this.expectedBaseType = expectedBaseType;
    }

    /**
     * Tests parsing <code>BaseType</code> from <code>String</code> representation.
     */
    @Test
    public void testParseBaseType() {
        assertEquals(expectedBaseType, BaseType.parseBaseType(string));
    }
}
