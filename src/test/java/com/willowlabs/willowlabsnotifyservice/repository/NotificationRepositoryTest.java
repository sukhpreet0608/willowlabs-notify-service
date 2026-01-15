package com.willowlabs.willowlabsnotifyservice.repository;

import com.willowlabs.willowlabsnotifyservice.model.ChannelType;
import com.willowlabs.willowlabsnotifyservice.model.Notification;
import com.willowlabs.willowlabsnotifyservice.model.NotificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should find pending notifications scheduled before now")
    void testFindByStatusAndChannelAndScheduledAtBefore() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Notification pendingPast = createNotification("Past", NotificationStatus.PENDING, now.minusMinutes(10));
        Notification pendingFuture = createNotification("Future", NotificationStatus.PENDING, now.plusMinutes(10));
        Notification sentPast = createNotification("Sent", NotificationStatus.SENT, now.minusMinutes(10));

        entityManager.persist(pendingPast);
        entityManager.persist(pendingFuture);
        entityManager.persist(sentPast);
        entityManager.flush();

        // When
        List<Notification> results = repository.findByStatusAndChannelAndScheduledAtBefore(
                NotificationStatus.PENDING,
                ChannelType.PUSH,
                now
        );

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContent()).isEqualTo("Past");
    }

    @Test
    @DisplayName("Should find notifications with multiple statuses scheduled before now")
    void testFindAllByStatusInAndScheduledAtBefore() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Notification pending = createNotification("Pending", NotificationStatus.PENDING, now.minusMinutes(5));
        Notification scheduled = createNotification("Scheduled", NotificationStatus.SCHEDULED, now.minusMinutes(5));
        Notification failedFuture = createNotification("Failed Future", NotificationStatus.FAILED, now.plusMinutes(5));

        entityManager.persist(pending);
        entityManager.persist(scheduled);
        entityManager.persist(failedFuture);
        entityManager.flush();

        // When
        List<Notification> results = repository.findAllByStatusInAndScheduledAtBefore(
                Set.of(NotificationStatus.PENDING, NotificationStatus.SCHEDULED),
                now
        );

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Notification::getStatus)
                .containsExactlyInAnyOrder(NotificationStatus.PENDING, NotificationStatus.SCHEDULED);
    }

    // Helper method to build test data
    private Notification createNotification(String content, NotificationStatus status, LocalDateTime scheduledAt) {
        Notification n = new Notification();
        n.setMobileUserName("TestUser");
        n.setRecipient("token123");
        n.setContent(content);
        n.setChannel(ChannelType.PUSH);
        n.setStatus(status);
        n.setScheduledAt(scheduledAt);
        return n;
    }
}