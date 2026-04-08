package app.dissension.demo.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.Hibernate;

@Entity // JPA Entity (maps instances of this class to records in a database table)
@Table(name = "app_users") // speaks for itself
public class AppUser {

  @Id // identifies primary key
  @GeneratedValue(strategy = GenerationType.IDENTITY) // pk generation strategy
  private Long id;

  @Column(nullable = false, unique = true, length = 50) // configs the username column
  private String username;

  @Column(nullable = false)
  private String passwordHash;

  @Column(length = 2048)
  private String imageUrl;

  // an empty constructor required by JPA (should be public or protected)
  protected AppUser() {
    //! Required by JPA to create instances of the entity using reflection.
  }

  public AppUser(String username, String passwordHash) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.imageUrl = null;
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  @Override
  public final boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (object == null || Hibernate.getClass(this) != Hibernate.getClass(object)) {
      return false;
    }

    AppUser other = (AppUser) object;
    return id != null && id.equals(other.id);
  }

  @Override
  public final int hashCode() {
    return Hibernate.getClass(this).hashCode();
  }
}
