package de.ruu.app.pragma.client;

import de.ruu.app.pragma.bean.UserBean;
import de.ruu.lib.junit.DisabledOnServerNotListening;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledOnServerNotListening(propertyNameHost = "pragma.rest-api.host", propertyNamePort = "pragma.rest-api.port")
class AdminClientIT
{
    private AdminClient client;
    private final List<UserBean> createdUsers = new ArrayList<>();

    @BeforeEach
    void setUp()
    {
        client = new AdminClient();
        client.postConstruct();
    }

    @AfterEach
    void tearDown()
    {
        for (int i = createdUsers.size() - 1; i >= 0; i--)
        {
            UserBean user = createdUsers.get(i);
            if (user.id() != null) client.deleteUser(user);
        }
        client.preDestroy();
    }

    @Test
    void testUsersAndDuplicateEmailUpdate()
    {
        assertThat(client.users()).isNotNull();

        String suffix = String.valueOf(System.currentTimeMillis());
        String email = "it-duplicate-" + suffix + "@example.com";

        UserBean first = client.createUser(new UserBean(
            "it-user-a-" + suffix,
            "It User A",
            email));
        createdUsers.add(first);

        UserBean second = client.createUser(new UserBean(
            "it-user-b-" + suffix,
            "It User B",
            "it-user-b-" + suffix + "@example.com"));
        createdUsers.add(second);

        second.email(email);
        UserBean updated = client.updateUser(second);

        assertThat(updated.email()).isEqualTo(email);
        assertThat(client.users())
            .extracting(UserBean::email)
            .contains(email);
    }
}
