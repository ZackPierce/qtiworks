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
package uk.ac.ed.ph.qtiworks.services.dao;

import uk.ac.ed.ph.qtiworks.domain.entities.Delivery;
import uk.ac.ed.ph.qtiworks.domain.entities.LtiDomain;
import uk.ac.ed.ph.qtiworks.domain.entities.LtiUser;
import uk.ac.ed.ph.qtiworks.domain.entities.UserRole;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO implementation for the {@link LtiUser} entity.
 *
 * @author David McKain
 */
@Repository
@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class LtiUserDao extends GenericDao<LtiUser> {

    @PersistenceContext
    private EntityManager em;

    public LtiUserDao() {
        super(LtiUser.class);
    }

    public LtiUser findByLogicalKey(final String logicalKey) {
        final TypedQuery<LtiUser> query = em.createNamedQuery("LtiUser.findByLogicalKey", LtiUser.class);
        query.setParameter("logicalKey", logicalKey);
        return extractNullableFindResult(query);
    }

    public LtiUser findByLtiDomainLtiUserIdAndUserRole(final LtiDomain ltiDomain ,final String ltiUserId, final UserRole userRole) {
        final TypedQuery<LtiUser> query = em.createNamedQuery("LtiUser.findByLtiDomainLtiUserIdAndUserRole", LtiUser.class);
        query.setParameter("ltiDomain", ltiDomain);
        query.setParameter("ltiUserId", ltiUserId);
        query.setParameter("userRole", userRole);
        return extractNullableFindResult(query);
    }

    public LtiUser findByDeliveryAndLtiUserId(final Delivery delivery ,final String ltiUserId) {
        final TypedQuery<LtiUser> query = em.createNamedQuery("LtiUser.findByDeliveryAndLtiUserId", LtiUser.class);
        query.setParameter("delivery", delivery);
        query.setParameter("ltiUserId", ltiUserId);
        return extractNullableFindResult(query);
    }

    public List<LtiUser> getForUserRole(final UserRole userRole) {
        final TypedQuery<LtiUser> query = em.createNamedQuery("LtiUser.getForUserRole", LtiUser.class);
        query.setParameter("userRole", userRole);
        return query.getResultList();
    }
}
