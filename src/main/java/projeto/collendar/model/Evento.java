package projeto.collendar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import projeto.collendar.model.enums.TipoRecorrencia;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título do evento é obrigatório")
    @Size(min = 3, max = 200, message = "Título deve ter entre 3 e 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "Data de início é obrigatória")
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    @Size(max = 200, message = "Local deve ter no máximo 200 caracteres")
    @Column(length = 200)
    private String local;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato hexadecimal #RRGGBB")
    @Column(length = 7)
    private String cor;

    @Column(name = "dia_inteiro", nullable = false)
    @Builder.Default
    private Boolean diaInteiro = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean recorrente = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recorrencia", length = 20)
    private TipoRecorrencia tipoRecorrencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendario_id", nullable = false)
    @ToString.Exclude
    private Calendario calendario;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (diaInteiro == null) {
            diaInteiro = false;
        }
        if (recorrente == null) {
            recorrente = false;
        }
        validarDatas();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validarDatas();
    }

    private void validarDatas() {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim deve ser maior ou igual à data de início");
        }
    }
}