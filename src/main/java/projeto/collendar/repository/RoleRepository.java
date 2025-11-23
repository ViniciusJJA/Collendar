package projeto.collendar.repository;

import projeto.collendar.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    @Query("SELECT r FROM Role r WHERE UPPER(r.nome) = UPPER(:nome)")
    Optional<Role> findByNome(@Param("nome") String nome);
}