package projeto.collendar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import projeto.collendar.model.enums.TipoPermissao;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "compartilhamentos",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_compartilhamento",
                columnNames = {"calendario_id", "usuario_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Compartilhamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendario_id", nullable = false)
    @NotNull(message = "Calendário é obrigatório")
    @ToString.Exclude
    private Calendario calendario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "Usuário é obrigatório")
    @ToString.Exclude
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Permissão é obrigatória")
    private TipoPermissao permissao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}