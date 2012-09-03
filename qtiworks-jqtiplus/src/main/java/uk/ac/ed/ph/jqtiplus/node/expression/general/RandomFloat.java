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
package uk.ac.ed.ph.jqtiplus.node.expression.general;

import uk.ac.ed.ph.jqtiplus.attribute.value.FloatOrVariableRefAttribute;
import uk.ac.ed.ph.jqtiplus.node.expression.ExpressionParent;
import uk.ac.ed.ph.jqtiplus.node.expression.RandomExpression;
import uk.ac.ed.ph.jqtiplus.running.ProcessingContext;
import uk.ac.ed.ph.jqtiplus.types.FloatOrVariableRef;
import uk.ac.ed.ph.jqtiplus.validation.AttributeValidationError;
import uk.ac.ed.ph.jqtiplus.validation.ValidationContext;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.NullValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

import java.util.Random;

/**
 * Selects A random float from the specified range [min,max].
 * <p>
 * This implementation returns random double from range &lt;min, max).
 * <p>
 * Additional conditions: max &gt;= min
 *
 * @author Jiri Kajaba
 */
public class RandomFloat extends RandomExpression {

    private static final long serialVersionUID = 3837760238885597058L;

    /** Name of this class in xml schema. */
    public static final String QTI_CLASS_NAME = "randomFloat";

    /** Name of min attribute in xml schema. */
    public static final String ATTR_MIN_NAME = "min";

    /** Name of max attribute in xml schema. */
    public static final String ATTR_MAX_NAME = "max";

    public RandomFloat(final ExpressionParent parent) {
        this(parent, QTI_CLASS_NAME);
    }

    protected RandomFloat(final ExpressionParent parent, final String localName) {
        super(parent, localName);

        getAttributes().add(new FloatOrVariableRefAttribute(this, ATTR_MIN_NAME, true));
        getAttributes().add(new FloatOrVariableRefAttribute(this, ATTR_MAX_NAME, true));
    }

    public FloatOrVariableRef getMin() {
        return getAttributes().getFloatOrVariableRefAttribute(ATTR_MIN_NAME).getValue();
    }

    public void setMin(final FloatOrVariableRef minimum) {
        getAttributes().getFloatOrVariableRefAttribute(ATTR_MIN_NAME).setValue(minimum);
    }

    public FloatOrVariableRef getMax() {
        return getAttributes().getFloatOrVariableRefAttribute(ATTR_MAX_NAME).getValue();
    }

    public void setMax(final FloatOrVariableRef maximum) {
        getAttributes().getFloatOrVariableRefAttribute(ATTR_MAX_NAME).setValue(maximum);
    }

    @Override
    protected Long getSeedAttributeValue() {
        return null;
    }

    @Override
    protected void validateAttributes(final ValidationContext context) {
        super.validateAttributes(context);

        final FloatOrVariableRef maxComputer = getMax();
        final FloatOrVariableRef minComputer = getMin();

        if (maxComputer.isFloat() && minComputer.isFloat()) {
            final double max = maxComputer.getDouble();
            final double min = minComputer.getDouble();
            if (max < min) {
                context.add(new AttributeValidationError(getAttributes().get(ATTR_MAX_NAME),
                        "Attribute " + ATTR_MAX_NAME + " (" + max +
                        ") cannot be lower than " + ATTR_MIN_NAME + " (" + min + ")"));
            }

        }
    }

    @Override
    protected Value evaluateSelf(final ProcessingContext context, final Value[] childValues, final int depth) {
        final double computedMinimum = getMin().evaluate(context);
        final double computedMaximum = getMax().evaluate(context);

        if (computedMinimum > computedMaximum) {
            /* Bad computed values */
            return NullValue.INSTANCE;
        }

        final Random randomGenerator = getRandomGenerator(depth);
        final double randomNumber = randomGenerator.nextDouble();
        final double randomFloat = computedMinimum + (computedMaximum - computedMinimum) * randomNumber;

        return new FloatValue(randomFloat);
    }
}
