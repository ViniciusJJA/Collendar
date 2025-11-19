package projeto.collendar.model;

import projeto.collendar.enums.TipoPermissao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "compartilhamentos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"calendario_id", "usuario_id"}))
@Getter
@Setter
public class Compartilhamento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    private Calendario calendario;

    @ManyToOne
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private TipoPermissao permissao;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}