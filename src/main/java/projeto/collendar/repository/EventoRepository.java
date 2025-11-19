package projeto.collendar.repository;

import projeto.collendar.model.Calendario;
import projeto.collendar.model.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventoRepository extends JpaRepository<Evento, UUID>, JpaSpecificationExecutor<Evento> {

    List<Evento> findByCalendario(Calendario calendario);

    Page<Evento> findByCalendario(Calendario calendario, Pageable pageable);

    List<Evento> findByCalendarioId(UUID calendarioId);

    List<Evento> findByDataInicioBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    @Query("SELECT e FROM Evento e WHERE e.calendario.id = :calendarioId " +
            "AND e.dataInicio BETWEEN :dataInicio AND :dataFim")
    List<Evento> findByCalendarioAndDataBetween(
            @Param("calendarioId") UUID calendarioId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    Page<Evento> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    List<Evento> findByRecorrente(Boolean recorrente);
}