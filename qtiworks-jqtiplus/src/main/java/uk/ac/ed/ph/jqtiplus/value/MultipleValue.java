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

/**
 * Implementation of "bag-type" container.
 * <p>
 * This container can be multiple or NULL (if empty).
 *
 * @see uk.ac.ed.ph.jqtiplus.value.Cardinality
 * @author Jiri Kajaba
 */
public final class MultipleValue extends ListValue {

    private static final long serialVersionUID = 717217375026655181L;

    /**
     * Constructs empty (NULL) <code>MultipleValue</code> container.
     */
    public MultipleValue() {
        super();
    }

    /**
     * Constructs <code>MultipleValue</code> containing the given value.
     *
     * @param value added <code>SingleValue</code>
     */
    public MultipleValue(final SingleValue value) {
        super(value);
    }

    /**
     * Constructs <code>MultipleValue</code> containing the given values.
     *
     * @param values added <code>SingleValue</code>s
     */
    public MultipleValue(final Iterable<? extends SingleValue> values) {
        super(values);
    }

    @Override
    public Cardinality getCardinality() {
        if (isNull()) {
            return null;
        }

        return Cardinality.MULTIPLE;
    }

    @Override
    public boolean isOrdered() {
        return false;
    }

    /**
     * Returns true if this container contains given <code>MultipleValue</code>; false otherwise.
     *
     * @param multipleValue given <code>MultipleValue</code>
     * @return true if this container contains given <code>MultipleValue</code>; false otherwise
     */
    public boolean contains(final MultipleValue multipleValue) {
        if (multipleValue.isNull()) {
            return false;
        }

        for (final SingleValue singleValue : multipleValue.container) {
            if (multipleValue.count(singleValue) > count(singleValue)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof MultipleValue)) {
            return false;
        }

        final MultipleValue other = (MultipleValue) object;
        if (container.size() != other.container.size()) {
            return false;
        }
        return container.containsAll(other.container);
    }

    @Override
    public final int hashCode() {
        /* Need an alternative calculation from usual in order to be compatible with equals().
         * So let's add up the hashCode of each element, taking advantage of the commutativity
         * of addition
         */
        int sum = 0;
        for (final SingleValue singleValue : container) {
            sum += singleValue.hashCode();
        }
        return sum;
    }
}
