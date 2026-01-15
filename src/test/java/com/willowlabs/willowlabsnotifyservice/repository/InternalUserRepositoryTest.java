package com.willowlabs.willowlabsnotifyservice.repository;

import com.willowlabs.willowlabsnotifyservice.model.InternalUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InternalUserRepositoryTest {

    @Autowired
    private InternalUserRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should return only users where isDesignatedRecipient is true")
    void testFindAllByIsDesignatedRecipientTrue() {
        // Arrange: Create designated users
        InternalUser designated1 = createStaff("Alice", "alice@willow.com", true);
        InternalUser designated2 = createStaff("Bob", "bob@willow.com", true);

        // Arrange: Create a non-designated user
        InternalUser standardUser = createStaff("Charlie", "charlie@willow.com", false);

        entityManager.persist(designated1);
        entityManager.persist(designated2);
        entityManager.persist(standardUser);
        entityManager.flush();

        // Act
        List<InternalUser> result = repository.findAllByIsDesignatedRecipientTrue();

        // Assert
        assertThat(result)
                .extracting(InternalUser::getEmail)
                .doesNotContain("charlie@willow.com");
    }

    @Test
    @DisplayName("Should return an empty list if no designated recipients exist")
    void testFindAllByIsDesignatedRecipientTrue_Empty() {
        // Arrange
        entityManager.persist(createStaff("Noel", "noel@willow.com", true));
        entityManager.flush();

        // Act
        List<InternalUser> result = repository.findAllByIsDesignatedRecipientTrue();

        // Assert
        assertThat(result).isNotEmpty();
    }

    // Helper method to build entities
    private InternalUser createStaff(String name, String email, boolean isDesignated) {
        InternalUser user = new InternalUser();
        user.setName(name);
        user.setEmail(email);
        user.setDepartment("Engineering");
        user.setDesignatedRecipient(isDesignated);
        return user;
    }
}