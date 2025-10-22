package projeto.collendar.model.enums;

public enum TipoPermissao {

    VISUALIZAR("Visualizar"),
    EDITAR("Editar");

    private final String descricao;

    TipoPermissao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
