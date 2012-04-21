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
package org.qtitools.mathassess.tools.glue.extras.pooling;

import org.qtitools.mathassess.tools.qticasbridge.maxima.QTIMaximaSession;
import org.qtitools.mathassess.tools.utilities.ConstraintUtilities;

import uk.ac.ed.ph.jacomax.MaximaConfiguration;
import uk.ac.ed.ph.jacomax.MaximaInteractiveProcess;
import uk.ac.ed.ph.jacomax.MaximaProcessLauncher;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetCache;

import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link PoolableObjectFactory} catering {@link QTIMaximaSession}s.
 *
 * @author David McKain
 */
class PoolableQTIMaximaSessionFactory implements PoolableObjectFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(PoolableQTIMaximaSessionFactory.class);
    
    private StylesheetCache stylesheetCache;
    private MaximaConfiguration maximaConfiguration;
    
    private MaximaProcessLauncher maximaProcessLauncher;
    
    public StylesheetCache getStylesheetCache() {
        return stylesheetCache;
    }
    
    public void setStylesheetCache(StylesheetCache stylesheetCache) {
        this.stylesheetCache = stylesheetCache;
    }

    
    public MaximaConfiguration getMaximaConfiguration() {
        return maximaConfiguration;
    }
    
    public void setMaximaConfiguration(MaximaConfiguration maximaConfiguration) {
        this.maximaConfiguration = maximaConfiguration;
    }
    
    //---------------------------------------------------------

    public void init() {
        ConstraintUtilities.ensureNotNull(maximaConfiguration, "maximaConfiguration");
        ConstraintUtilities.ensureNotNull(stylesheetCache, "stylesheetCache");
        maximaProcessLauncher = new MaximaProcessLauncher(maximaConfiguration);
    }

    public Object makeObject() {
        logger.debug("Creating new pooled Maxima process");
        MaximaInteractiveProcess maximaInteractiveProcess = maximaProcessLauncher.launchInteractiveProcess();
        QTIMaximaSession session = new QTIMaximaSession(maximaInteractiveProcess, stylesheetCache);
        session.init();
        return session;
    }
    
    public void activateObject(Object obj) {
        /* (Nothing to do here) */
    }
    
    public void passivateObject(Object obj) {
        logger.debug("Resetting Maxima session and passivating");
        QTIMaximaSession session = (QTIMaximaSession) obj;
        if (session.isTerminated()) {
            throw new IllegalStateException("Expected pool to verify Objects before passivation");
        }
        try {
            session.reset();
        }
        catch (Exception e) {
            logger.warn("Could not reset process - terminating so that it is no longer considered valid");
            session.terminate();
        }
    }
    
    public boolean validateObject(Object obj) {
        QTIMaximaSession session = (QTIMaximaSession) obj;
        return !session.isTerminated();
    }
    
    public void destroyObject(Object obj) {
        logger.debug("Terminating pooled Maxima session");
        QTIMaximaSession session = (QTIMaximaSession) obj;
        session.terminate();
    }
}
