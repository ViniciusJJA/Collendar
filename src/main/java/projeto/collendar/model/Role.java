package projeto.collendar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "role")
public class Role {

    private static final long serialVersionUID = 1L;

    @Column(name = "nome", unique = true, nullable = false, length = 50)
    @NotBlank(message = "O nome da role é obrigatório")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    private String nome;
}