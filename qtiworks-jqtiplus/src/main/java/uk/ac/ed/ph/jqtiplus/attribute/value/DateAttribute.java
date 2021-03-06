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
package uk.ac.ed.ph.jqtiplus.attribute.value;

import uk.ac.ed.ph.jqtiplus.attribute.SingleAttribute;
import uk.ac.ed.ph.jqtiplus.exception.QtiParseException;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Attribute with Date value.
 *
 * @author Jiri Kajaba
 */
public final class DateAttribute extends SingleAttribute<Date> {

    private static final long serialVersionUID = 2736828962257037608L;

    /**
     * Date formatting pattern.
     * NB: DateFormat is not Thread safe, so don't cache the results of this across Threads!
     */
    public static final DateFormat createDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    }

    public DateAttribute(final QtiNode parent, final String localName, final boolean required) {
        super(parent, localName, required);
    }

    public DateAttribute(final QtiNode parent, final String localName, final Date defaultValue, final boolean required) {
        super(parent, localName, defaultValue, required);
    }

    @Override
    protected Date parseQtiString(final String value) {
        if (value == null || value.length() == 0) {
            throw new QtiParseException("Invalid datetime '" + value + "'. Length is not valid.");
        }

        try {
            return createDateFormat().parse(value);
        }
        catch (final ParseException ex) {
            throw new QtiParseException("Invalid datetime '" + value + "'.", ex);
        }
    }

    @Override
    protected String toQtiString(final Date value) {
        return createDateFormat().format(value);
    }
}
