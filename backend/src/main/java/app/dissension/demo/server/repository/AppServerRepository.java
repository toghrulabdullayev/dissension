package app.dissension.demo.server.repository;

import app.dissension.demo.server.entity.AppServer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppServerRepository extends JpaRepository<AppServer, Long> {
}
