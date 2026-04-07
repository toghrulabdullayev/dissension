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
  @UuidGenerator // 128-bit random identifier
  @Column(nullable = false, updatable = false) // cannot be null, cannot be changed
  private UUID id;

  // ManyToOne relationship between servers table, .LAZY - server object is loaded only when accessed, for performance improvements
  @ManyToOne(fetch = FetchType.LAZY, optional = false) // optional = false, relationship must NOT be null
  @JoinColumn(name = "server_id", nullable = false) // server_id cannot be NULL in the db (both enforce db integrity together)
  private AppServer server;

  @Column(nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING) // store the enum value as a string
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

  public void setName(String name) {
    this.name = name;
  }

  public ChannelType getType() {
    return type;
  }

  public void setType(ChannelType type) {
    this.type = type;
  }

  public int getPosition() {
    return position;
  }
}
