package app.dissension.demo.channel.entity;

import app.dissension.demo.channel.model.ChannelType;
import app.dissension.demo.server.entity.AppServer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "channels")
public class AppChannel {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private AppServer server;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelType type;

    @Column(nullable = false)
    private int position;

    protected AppChannel() {
        // Required by JPA.
    }

    public AppChannel(AppServer server, String name, ChannelType type, int position) {
        this.server = server;
        this.name = name;
        this.type = type;
        this.position = position;
    }

    public UUID getId() {
        return id;
    }

    public AppServer getServer() {
        return server;
    }

    public String getName() {
        return name;
    }

    public ChannelType getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }
}
