package app.dissension.api.domain.game.valueobject;

public enum GameSessionStatus {
    WAITING,
    ACTIVE,
    FINISHED;

    public boolean isTerminal() {
        return this == FINISHED;
    }
}
