package projeto.collendar.enums;

public enum TipoRecorrencia {

    DIARIA("Diária"),
    SEMANAL("Semanal"),
    MENSAL("Mensal"),
    ANUAL("Anual");

    private final String descricao;

    TipoRecorrencia(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}