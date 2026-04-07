package app.dissension.demo.chat.entity;

import app.dissension.demo.auth.entity.AppUser;
import app.dissension.demo.channel.entity.AppChannel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "channel_id", nullable = false)
  private AppChannel channel;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  private AppUser author;

  @Column(nullable = false, length = 2000)
  private String content;

  @Column(nullable = false)
  private Instant createdAt;

  protected ChatMessage() {
    // Required by JPA.
  }

  public ChatMessage(AppChannel channel, AppUser author, String content, Instant createdAt) {
    this.channel = channel;
    this.author = author;
    this.content = content;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public AppChannel getChannel() {
    return channel;
  }

  public AppUser getAuthor() {
    return author;
  }

  public String getContent() {
    return content;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
