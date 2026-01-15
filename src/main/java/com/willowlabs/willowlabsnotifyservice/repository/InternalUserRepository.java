package com.willowlabs.willowlabsnotifyservice.repository;

import com.willowlabs.willowlabsnotifyservice.model.InternalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Internal user repository  to find designated users.
 * @author Sukhpreet Khurana
 */

@Repository
public interface InternalUserRepository extends JpaRepository<InternalUser, Long> {

    /**
     * Fetches all internal users who are designated to receive
     * alerts for new mobile user subscriptions.
     */
    List<InternalUser> findAllByIsDesignatedRecipientTrue();
}