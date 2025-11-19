package projeto.collendar.repository;

import projeto.collendar.enums.TipoPermissao;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Compartilhamento;
import projeto.collendar.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompartilhamentoRepository extends JpaRepository<Compartilhamento, UUID> {

    List<Compartilhamento> findByCalendario(Calendario calendario);

    List<Compartilhamento> findByUsuario(Usuario usuario);

    Optional<Compartilhamento> findByCalendarioAndUsuario(Calendario calendario, Usuario usuario);

    List<Compartilhamento> findByCalendarioId(UUID calendarioId);

    List<Compartilhamento> findByUsuarioId(UUID usuarioId);

    List<Compartilhamento> findByPermissao(TipoPermissao permissao);

    boolean existsByCalendarioAndUsuario(Calendario calendario, Usuario usuario);

    @Query("SELECT c.calendario FROM Compartilhamento c WHERE c.usuario.id = :usuarioId")
    List<Calendario> findCalendariosCompartilhadosComUsuario(@Param("usuarioId") UUID usuarioId);

    @Modifying
    @Query("DELETE FROM Compartilhamento c WHERE c.calendario.id = :calendarioId AND c.usuario.id = :usuarioId")
    void deleteByCalendarioIdAndUsuarioId(@Param("calendarioId") UUID calendarioId, @Param("usuarioId") UUID usuarioId);
}