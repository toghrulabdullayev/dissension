package app.dissension.demo.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.Hibernate;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "app_servers")
public class AppServer {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 150)
  private String description;

  protected AppServer() {
    // Required by JPA.
  }

  public AppServer(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public final boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (object == null || Hibernate.getClass(this) != Hibernate.getClass(object)) {
      return false;
    }

    AppServer other = (AppServer) object;
    return id != null && id.equals(other.id);
  }

  @Override
  public final int hashCode() {
    return Hibernate.getClass(this).hashCode();
  }
}
