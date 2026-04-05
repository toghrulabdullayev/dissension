package app.dissension.demo.server.repository;

import app.dissension.demo.server.entity.AppServer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppServerRepository extends JpaRepository<AppServer, UUID> {

	List<AppServer> findAllByOrderByIdAsc();

	@Query(
		"""
		select s
		from AppServer s
		where lower(s.name) like lower(concat('%', :query, '%'))
		   or lower(coalesce(s.description, '')) like lower(concat('%', :query, '%'))
		order by s.id asc
		"""
	)
	List<AppServer> searchByQuery(@Param("query") String query);
}
