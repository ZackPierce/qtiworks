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
package uk.ac.ed.ph.qtiworks.web.controller.legacy;

import uk.ac.ed.ph.qtiworks.QtiWorksRuntimeException;
import uk.ac.ed.ph.qtiworks.domain.PrivilegeException;
import uk.ac.ed.ph.qtiworks.domain.entities.Assessment;
import uk.ac.ed.ph.qtiworks.domain.entities.AssessmentPackage;
import uk.ac.ed.ph.qtiworks.domain.entities.CandidateSession;
import uk.ac.ed.ph.qtiworks.domain.entities.Delivery;
import uk.ac.ed.ph.qtiworks.services.AssessmentDataService;
import uk.ac.ed.ph.qtiworks.services.AssessmentManagementService;
import uk.ac.ed.ph.qtiworks.services.CandidateSessionStarter;
import uk.ac.ed.ph.qtiworks.services.domain.AssessmentPackageFileImportException;
import uk.ac.ed.ph.qtiworks.services.domain.AssessmentPackageFileImportException.APFIFailureReason;
import uk.ac.ed.ph.qtiworks.services.domain.EnumerableClientFailure;
import uk.ac.ed.ph.qtiworks.web.GlobalRouter;
import uk.ac.ed.ph.qtiworks.web.domain.StandaloneRunCommand;

import uk.ac.ed.ph.jqtiplus.validation.AssessmentObjectValidationResult;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This controller is used by the current version of Uniqurate only.
 * <p>
 * DO NOT USE this legacy controller for any new systems!
 *
 * @author David McKain
 */
@Controller
public class UniqurateStandaloneRunner {

    @Resource
    private AssessmentManagementService assessmentManagementService;

    @Resource
    private CandidateSessionStarter candidateSessionStarter;

    @Resource
    private AssessmentDataService assessmentDataService;

    //--------------------------------------------------------------------

    @RequestMapping(value="/standalonerunner", method=RequestMethod.POST)
    public String uniqurateUploadAndRun(final Model model,
            @Valid @ModelAttribute final StandaloneRunCommand command,
            final BindingResult errors) {
        /* Catch any binding errors */
        if (errors.hasErrors()) {
            return "standalonerunner/uploadForm";
        }
        try {
            final Assessment assessment = assessmentManagementService.importAssessment(command.getFile(), false);
            final AssessmentObjectValidationResult<?> validationResult = assessmentDataService.validateAssessment(assessment);
            final AssessmentPackage assessmentPackage = assessmentDataService.ensureSelectedAssessmentPackage(assessment);
            if (!assessmentPackage.isLaunchable()) {
                /* Assessment isn't launchable */
                model.addAttribute("validationResult", validationResult);
                return "standalonerunner/invalidUpload";
            }
            final Delivery delivery = assessmentManagementService.createDemoDelivery(assessment, null);
            final String exitUrl = "/web/anonymous/standalonerunner";
            final CandidateSession candidateSession = candidateSessionStarter.createCandidateSession(delivery, true, exitUrl, null, null);

            /* Redirect to candidate dispatcher */
            return GlobalRouter.buildSessionStartRedirect(candidateSession);
        }
        catch (final AssessmentPackageFileImportException e) {
            final EnumerableClientFailure<APFIFailureReason> failure = e.getFailure();
            failure.registerErrors(errors, "assessmentPackageUpload");
            return "standalonerunner/uploadForm";
        }
        catch (final PrivilegeException e) {
            /* This should not happen if access control logic has been done correctly */
            throw QtiWorksRuntimeException.unexpectedException(e);
        }
    }
}