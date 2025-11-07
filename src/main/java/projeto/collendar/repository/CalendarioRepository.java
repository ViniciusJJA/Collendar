package projeto.collendar.repository;

import projeto.collendar.model.Calendario;
import projeto.collendar.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarioRepository extends JpaRepository<Calendario, UUID> {

    List<Calendario> findByUsuario(Usuario usuario);

    Page<Calendario> findByUsuario(Usuario usuario, Pageable pageable);

    List<Calendario> findByUsuarioId(UUID usuarioId);

    Page<Calendario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}