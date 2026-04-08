package app.dissension.demo.server.entity;

import app.dissension.demo.auth.entity.AppUser;
import app.dissension.demo.server.model.ServerRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.Hibernate;

// Association pattern (User X is a member of Server Y with role Z)
@Entity
@Table(
    name = "server_memberships",
    // prevents the same user from joining the same server more than once (uk = unique key)
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_server_user_membership", columnNames = {"server_id", "user_id"})
    }
)
public class ServerMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private AppServer server;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServerRole role;

    protected ServerMembership() {
        // Required by JPA.
    }

    public ServerMembership(AppServer server, AppUser user, ServerRole role) {
        this.server = server;
        this.user = user;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public AppServer getServer() {
        return server;
    }

    public AppUser getUser() {
        return user;
    }

    public ServerRole getRole() {
        return role;
    }

    public void setRole(ServerRole role) {
        this.role = role;
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || Hibernate.getClass(this) != Hibernate.getClass(object)) {
            return false;
        }

        ServerMembership other = (ServerMembership) object;
        return id != null && id.equals(other.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
