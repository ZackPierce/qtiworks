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
package uk.ac.ed.ph.qtiworks.services;

import uk.ac.ed.ph.qtiworks.QtiWorksLogicException;
import uk.ac.ed.ph.qtiworks.QtiWorksRuntimeException;
import uk.ac.ed.ph.qtiworks.base.services.Auditor;
import uk.ac.ed.ph.qtiworks.domain.DomainConstants;
import uk.ac.ed.ph.qtiworks.domain.DomainEntityNotFoundException;
import uk.ac.ed.ph.qtiworks.domain.IdentityContext;
import uk.ac.ed.ph.qtiworks.domain.Privilege;
import uk.ac.ed.ph.qtiworks.domain.PrivilegeException;
import uk.ac.ed.ph.qtiworks.domain.dao.AssessmentDao;
import uk.ac.ed.ph.qtiworks.domain.dao.AssessmentPackageDao;
import uk.ac.ed.ph.qtiworks.domain.entities.Assessment;
import uk.ac.ed.ph.qtiworks.domain.entities.AssessmentPackage;
import uk.ac.ed.ph.qtiworks.domain.entities.User;
import uk.ac.ed.ph.qtiworks.services.domain.AssessmentPackageFileImportException;
import uk.ac.ed.ph.qtiworks.services.domain.AssessmentStateException;
import uk.ac.ed.ph.qtiworks.services.domain.AssessmentStateException.APSFailureReason;
import uk.ac.ed.ph.qtiworks.services.domain.OutputStreamer;

import uk.ac.ed.ph.jqtiplus.exception2.QtiLogicException;
import uk.ac.ed.ph.jqtiplus.internal.util.Assert;
import uk.ac.ed.ph.jqtiplus.internal.util.StringUtilities;
import uk.ac.ed.ph.jqtiplus.node.AssessmentObjectType;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlObjectReader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.AssessmentObjectManager;
import uk.ac.ed.ph.jqtiplus.validation.AssessmentObjectValidationResult;
import uk.ac.ed.ph.jqtiplus.xmlutils.XmlReadResult;
import uk.ac.ed.ph.jqtiplus.xmlutils.XmlResourceNotFoundException;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;
import uk.ac.ed.ph.jqtiplus.xperimental.ToRefactor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

/**
 * Services for managing {@link Assessment}s
 *
 * @author David McKain
 */
@Service
@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
public class AssessmentManagementService {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentManagementService.class);

    public static final String DEFAULT_IMPORT_TITLE = "My Assessment";

    @Resource
    private Auditor auditor;

    @Resource
    private IdentityContext identityContext;

    @Resource
    private FilespaceManager filespaceManager;

    @Resource
    private AssessmentPackageFileService assessmentPackageFileService;

    @Resource
    private AssessmentPackageFileImporter assessmentPackageFileImporter;

    @Resource
    private AssessmentDao assessmentDao;

    @Resource
    private AssessmentPackageDao assessmentPackageDao;

    @Resource
    private QtiXmlReader qtiXmlReader;

    //-------------------------------------------------
    // Assessment access

    /**
     * Looks up the {@link Assessment} having the given ID (aid) and checks that
     * the caller may access it.
     *
     * @param aid
     * @return
     * @throws DomainEntityNotFoundException
     * @throws PrivilegeException
     */
    public Assessment lookupAssessment(final long aid)
            throws DomainEntityNotFoundException, PrivilegeException {
        Assert.ensureNotNull(aid, "aid");
        final Assessment result = assessmentDao.requireFindById(aid);
        ensureCallerMayAccess(result);
        return result;
    }

    /**
     * TODO: Currently only permitting people to see either public Assessments, or
     * their own Assessments.
     */
    private User ensureCallerMayAccess(final Assessment assessment)
            throws PrivilegeException {
        final User caller = identityContext.getCurrentThreadEffectiveIdentity();
        if (!assessment.isPublic() && !assessment.getOwner().equals(caller)) {
            throw new PrivilegeException(caller, Privilege.VIEW_ASSESSMENT, assessment);
        }
        return caller;
    }

    //-------------------------------------------------
    // AssessmentPackage access

    public AssessmentPackage lookupAssessmentPackage(final long apid)
            throws DomainEntityNotFoundException, PrivilegeException {
        final AssessmentPackage result = assessmentPackageDao.requireFindById(apid);
        ensureCallerMayAccess(result.getAssessment());
        return result;
    }

    //-------------------------------------------------

    public void streamPackageSource(final AssessmentPackage assessmentPackage, final OutputStreamer outputStreamer)
            throws PrivilegeException, IOException {
        Assert.ensureNotNull(assessmentPackage, "assessmentPackage");
        Assert.ensureNotNull(outputStreamer, "outputStreamer");
        ensureCallerMayViewSource(assessmentPackage.getAssessment());
        assessmentPackageFileService.streamAssessmentPackageSource(assessmentPackage, outputStreamer);
    }

    public AssessmentPackage getCurrentAssessmentPackage(final Assessment assessment) {
        Assert.ensureNotNull(assessment, "assessment");
        final AssessmentPackage result = assessmentPackageDao.getCurrentAssessmentPackage(assessment);
        if (result==null) {
            throw new QtiWorksLogicException("Expected to always find at least 1 AssessmentPackage associated with an Assessment. Check the JPA-QL query and the logic in this class");
        }
        return result;
    }

    public List<Assessment> getCallerAssessments() {
        return assessmentDao.getForOwner(identityContext.getCurrentThreadEffectiveIdentity());
    }

    /**
     * Creates and persists a new {@link Assessment} and initial {@link AssessmentPackage}
     * from the data provided by the given {@link InputStream} and having the given content type.
     * <p>
     * Success post-conditions:
     * - the {@link InputStream} is left open
     * - a new {@link AssessmentPackage} is persisted, and its data is safely stored in a sandbox
     *
     * @param multipartFile
     * @param contentType
     * @param name for the resulting package. A default will be chosen if one is not provided.
     *   The name will be silently truncated if it is too large for the underlying DB field.
     *
     * @throws PrivilegeException if the caller is not allowed to perform this action
     * @throws AssessmentPackageFileImportException
     * @throws QtiWorksRuntimeException
     */
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public Assessment importAssessment(final MultipartFile multipartFile)
            throws PrivilegeException, AssessmentPackageFileImportException {
        Assert.ensureNotNull(multipartFile, "multipartFile");
        final User caller = ensureCallerMayCreateAssessment();

        /* First, upload the data into a sandbox */
        final AssessmentPackage assessmentPackage = importPackageFiles(multipartFile);

        /* Create resulting Assessment entity */
        final Assessment assessment = new Assessment();
        assessment.setAssessmentType(assessmentPackage.getAssessmentType());
        assessment.setOwner(caller);

        /* Try to create Assessment name from the name of the MultipartFile */
        final String fileName = multipartFile.getOriginalFilename();
        String assessmentName;
        if (StringUtilities.isNullOrBlank(fileName)) {
            assessmentName = assessmentPackage.getAssessmentType()==AssessmentObjectType.ASSESSMENT_ITEM ? "Item" : "Test";
        }
        else {
            assessmentName = ServiceUtilities.trimString(fileName, DomainConstants.ASSESSMENT_NAME_MAX_LENGTH);
        }
        assessment.setName(assessmentName);

        /* Guess a title */
        final String guessedTitle = guessAssessmentTitle(assessmentPackage);
        final String resultingTitle = !StringUtilities.isNullOrEmpty(guessedTitle) ? guessedTitle : DEFAULT_IMPORT_TITLE;
        assessment.setTitle(ServiceUtilities.trimSentence(resultingTitle, DomainConstants.ASSESSMENT_TITLE_MAX_LENGTH));

        /* Relate Assessment & AssessmentPackage */
        assessmentPackage.setAssessment(assessment);
        assessmentPackage.setImportVersion(Long.valueOf(1L));
        assessment.setPackageImportVersion(Long.valueOf(1L));

        /* Persist entities */
        try {
            assessmentDao.persist(assessment);
            assessmentPackageDao.persist(assessmentPackage);
        }
        catch (final Exception e) {
            logger.warn("Persistence of AssessmentPackage failed - deleting its sandbox", assessmentPackage);
            deleteAssessmentPackageSandbox(assessmentPackage);
            throw new QtiWorksRuntimeException("Failed to persist AssessmentPackage " + assessmentPackage, e);
        }
        logger.debug("Created new Assessment #{} with package #{}", assessment.getId(), assessmentPackage.getId());
        auditor.recordEvent("Created Assessment #" + assessment.getId() + " and AssessmentPackage #" + assessmentPackage.getId());
        return assessment;
    }


    /**
     * NOTE: Not allowed to go item->test or test->item.
     *
     * @throws AssessmentStateException
     * @throws PrivilegeException
     * @throws AssessmentPackageFileImportException
     * @throws DomainEntityNotFoundException
     */
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public Assessment updateAssessmentPackageFiles(final long aid,
            final MultipartFile multipartFile)
            throws AssessmentStateException, PrivilegeException,
            AssessmentPackageFileImportException, DomainEntityNotFoundException {
        Assert.ensureNotNull(multipartFile, "multipartFile");
        final Assessment assessment = assessmentDao.requireFindById(aid);
        ensureCallerMayChange(assessment);

        /* Upload data into a new sandbox */
        final AssessmentPackage newAssessmentPackage = importPackageFiles(multipartFile);

        /* Make sure we haven't gone item->test or test->item */
        if (newAssessmentPackage.getAssessmentType()!=assessment.getAssessmentType()) {
            throw new AssessmentStateException(APSFailureReason.CANNOT_CHANGE_ASSESSMENT_TYPE,
                    assessment.getAssessmentType(), newAssessmentPackage.getAssessmentType());
        }

        /* Join together */
        final long newPackageVersion = assessment.getPackageImportVersion().longValue() + 1;
        newAssessmentPackage.setImportVersion(newPackageVersion);
        newAssessmentPackage.setAssessment(assessment);
        assessment.setPackageImportVersion(newPackageVersion);

        /* Finally update DB */
        try {
            assessmentDao.update(assessment);
            assessmentPackageDao.persist(newAssessmentPackage);
        }
        catch (final Exception e) {
            logger.warn("Failed to update state of AssessmentPackage {} after file replacement - deleting new sandbox", e);
            deleteAssessmentPackageSandbox(newAssessmentPackage);
            throw new QtiWorksRuntimeException("Failed to update AssessmentPackage entity " + assessment, e);
        }
        logger.debug("Updated Assessment #{} to have package #{}", assessment.getId(), newAssessmentPackage.getId());
        auditor.recordEvent("Updated Assessment #" + assessment.getId() + " with AssessmentPackage #" + newAssessmentPackage.getId());
        return assessment;
    }

    //-------------------------------------------------
    // Not implemented

    @Transactional(propagation=Propagation.REQUIRED)
    @SuppressWarnings("unused")
    @ToRefactor
    public void deleteAssessment(final Assessment assessment)
            throws AssessmentStateException, PrivilegeException {
        /* In order to do this correctly, we need to delete all state that might have
         * been associated with this assessment as well, so we'll come back to this...
         */
        throw new QtiLogicException("Not yet implemented!");
    }

    /**
     * DEV NOTES:
     *
     * - Forbid deletion of the only remaining package, as that ensures there's always a most
     *   recent package
     */
    @Transactional(propagation=Propagation.REQUIRED)
    @SuppressWarnings("unused")
    @ToRefactor
    public void deleteAssessmentPackage(final AssessmentPackage assessmentPackage)
            throws AssessmentStateException, PrivilegeException {
        /* In order to do this correctly, we need to delete all state that might have
         * been associated with this package as well, so we'll come back to this...
         */
        throw new QtiLogicException("Not yet implemented!");
    }

    //-------------------------------------------------
    // Validation

    @Transactional(propagation=Propagation.REQUIRED)
    public AssessmentObjectValidationResult<?> validateAssessment(final long aid)
            throws PrivilegeException, DomainEntityNotFoundException {
        final Assessment assessment = lookupAssessment(aid);
        final AssessmentPackage currentAssessmentPackage = getCurrentAssessmentPackage(assessment);

        /* Run the validation process */
        final AssessmentObjectValidationResult<?> validationResult = validateAssessment(currentAssessmentPackage);

        /* Persist results */
        currentAssessmentPackage.setValidated(true);
        currentAssessmentPackage.setValid(validationResult.isValid());
        assessmentPackageDao.update(currentAssessmentPackage);

        return validationResult;
    }

    /**
     * (This is not private as it is also called by the anonymous upload/validate action featured
     * in the first iteration of QTI Works. TODO: Decide whether we'll keep this kind of
     * functionality.)
     *
     * @param assessmentPackage
     * @return
     */
    @SuppressWarnings("unchecked")
    <E extends AssessmentObjectValidationResult<?>> AssessmentObjectValidationResult<?>
    validateAssessment(final AssessmentPackage assessmentPackage) {
        final ResourceLocator inputResourceLocator = assessmentPackageFileService.createResolvingResourceLocator(assessmentPackage);
        final URI assessmentObjectSystemId = assessmentPackageFileService.createAssessmentObjectUri(assessmentPackage);
        final QtiXmlObjectReader objectReader = qtiXmlReader.createQtiXmlObjectReader(inputResourceLocator);
        final AssessmentObjectManager objectManager = new AssessmentObjectManager(objectReader);
        E result;
        final AssessmentObjectType assessmentObjectType = assessmentPackage.getAssessmentType();
        if (assessmentObjectType==AssessmentObjectType.ASSESSMENT_ITEM) {
            result = (E) objectManager.resolveAndValidateItem(assessmentObjectSystemId);
        }
        else if (assessmentObjectType==AssessmentObjectType.ASSESSMENT_TEST) {
            result = (E) objectManager.resolveAndValidateTest(assessmentObjectSystemId);
        }
        else {
            throw new QtiWorksLogicException("Unexpected branch " + assessmentObjectType);
        }
        return result;
    }

    //-------------------------------------------------

    /**
     * @throws QtiWorksLogicException if sandboxPath is already null
     */
    private void deleteAssessmentPackageSandbox(final AssessmentPackage assessmentPackage) {
        final String sandboxPath = assessmentPackage.getSandboxPath();
        if (sandboxPath==null) {
            throw new QtiWorksLogicException("AssessmentPackage sandbox is null");
        }
        filespaceManager.deleteSandbox(new File(sandboxPath));
        assessmentPackage.setSandboxPath(null);
    }

    /**
     * @throws PrivilegeException
     * @throws AssessmentPackageFileImportException
     * @throws QtiWorksRuntimeException
     */
    private AssessmentPackage importPackageFiles(final MultipartFile multipartFile)
            throws PrivilegeException, AssessmentPackageFileImportException {
        final User owner = identityContext.getCurrentThreadEffectiveIdentity();
        final File packageSandbox = filespaceManager.createAssessmentPackageSandbox(owner);
        final InputStream inputStream = ServiceUtilities.ensureInputSream(multipartFile);
        final String contentType = multipartFile.getContentType();
        try {
            final AssessmentPackage assessmentPackage = assessmentPackageFileImporter.importAssessmentPackageData(packageSandbox, inputStream, contentType);
            assessmentPackage.setImporter(owner);
            return assessmentPackage;
        }
        catch (final AssessmentPackageFileImportException e) {
            filespaceManager.deleteSandbox(packageSandbox);
            throw e;
        }
    }

    /**
     * Attempts to extract the title from an {@link AssessmentItem} or {@link AssessmentTest} for
     * bootstrapping the initial state of the resulting {@link AssessmentPackage}.
     * <p>
     * This performs a low level XML parse to save time; proper read/validation using JQTI+
     * is expected to happen later on.
     *
     * @param assessmentPackage
     * @return guessed title, or an empty String if nothing could be guessed.
     */
    public String guessAssessmentTitle(final AssessmentPackage assessmentPackage) {
        Assert.ensureNotNull(assessmentPackage, "assessmentPackage");
        final ResourceLocator inputResourceLocator = assessmentPackageFileService.createResolvingResourceLocator(assessmentPackage);
        final URI assessmentSystemId = assessmentPackageFileService.createAssessmentObjectUri(assessmentPackage);
        XmlReadResult xmlReadResult;
        try {
            xmlReadResult = qtiXmlReader.read(assessmentSystemId, inputResourceLocator, false);
        }
        catch (final XmlResourceNotFoundException e) {
            throw new QtiWorksLogicException("Assessment resource for package " + assessmentPackage, e);
        }
        /* Let's simply extract the title attribute from the document element, and not worry about
         * anything else at this point.
         */
        final Document document = xmlReadResult.getDocument();
        return document!=null ? document.getDocumentElement().getAttribute("title") : "";
    }

    //-------------------------------------------------

    /**
     * TODO: Currently allowing people to view the source of public Assessments, or
     * their own Assessments.
     *
     * @param assessment
     * @return
     * @throws PrivilegeException
     */
    private User ensureCallerMayViewSource(final Assessment assessment)
            throws PrivilegeException {
        final User caller = identityContext.getCurrentThreadEffectiveIdentity();
        if (!assessment.isPublic() && !assessment.getOwner().equals(caller)) {
            throw new PrivilegeException(caller, Privilege.VIEW_ASSESSMENT_SOURCE, assessment);
        }
        return caller;
    }

    private User ensureCallerMayChange(final Assessment assessment)
            throws PrivilegeException {
        final User caller = identityContext.getCurrentThreadEffectiveIdentity();
        if (!assessment.getOwner().equals(caller)) {
            throw new PrivilegeException(caller, Privilege.CHANGE_ASSESSMENT, assessment);
        }
        return caller;
    }

    /**
     * TODO: Currently we are only allowing instructors to create Assessments.
     * We may choose to let anonymous users do the same in future.
     */
    private User ensureCallerMayCreateAssessment() throws PrivilegeException {
        final User caller = identityContext.getCurrentThreadEffectiveIdentity();
        if (!caller.isInstructor()) {
            throw new PrivilegeException(caller, Privilege.CREATE_ASSESSMENT);
        }
        return caller;
    }

}
