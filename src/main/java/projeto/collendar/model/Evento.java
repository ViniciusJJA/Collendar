package projeto.collendar.model;

import projeto.collendar.enums.TipoRecorrencia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "eventos")
@Getter
@Setter
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String titulo;

    private String descricao;

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    private String local;

    private String cor;

    private Boolean diaInteiro;

    private Boolean recorrente;

    @Enumerated(EnumType.STRING)
    private TipoRecorrencia tipoRecorrencia;

    @ManyToOne
    private Calendario calendario;

    private LocalDateTime createdAt;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}