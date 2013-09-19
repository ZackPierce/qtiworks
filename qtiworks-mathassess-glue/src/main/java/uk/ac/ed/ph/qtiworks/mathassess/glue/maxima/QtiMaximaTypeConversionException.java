/* Copyright (c) 2012-2013, University of Edinburgh.
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
package uk.ac.ed.ph.qtiworks.mathassess.glue.maxima;

import uk.ac.ed.ph.qtiworks.mathassess.glue.MathAssessCasAuthoringException;
import uk.ac.ed.ph.qtiworks.mathassess.glue.types.ValueWrapper;

/**
 * This Exception is thrown when attempting to extract and convert a Maxima
 * output into a QTI value having an incompatible type.
 *
 * @author David McKain
 */
public final class QtiMaximaTypeConversionException extends MathAssessCasAuthoringException {

    private static final long serialVersionUID = 5940754810506417594L;

    private final String maximaInput;
    private final String maximaOutput;
    private final Class<? extends ValueWrapper> badTypeClass;

    public QtiMaximaTypeConversionException(final String maximaInput, final String maximaOutput,
            final Class<? extends ValueWrapper> badTypeClass) {
        super("Could not convert Maxima output to type " + badTypeClass
                + "\nMaxima input was: " + maximaInput
                + "\nMaxima output was: " + maximaOutput);
        this.maximaInput = maximaInput;
        this.maximaOutput = maximaOutput;
        this.badTypeClass = badTypeClass;
    }

    public String getMaximaInput() {
        return maximaInput;
    }

    public String getMaximaOutput() {
        return maximaOutput;
    }

    public Class<? extends ValueWrapper> getBadTypeClass() {
        return badTypeClass;
    }
}
