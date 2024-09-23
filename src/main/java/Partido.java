public class Partido {
    private Equipo local;
    private Equipo visitante;
    private boolean esLocal; // true si el equipo es el local, false si es visitante

    public Partido(Equipo local, Equipo visitante, boolean esLocal) {
        this.local = local;
        this.visitante = visitante;
        this.esLocal = esLocal;
    }

    public Equipo[] getEquipos() {
        return new Equipo[]{local, visitante};
    }

    public Equipo getLocal() {
        return local;
    }

    public Equipo getVisitante() {
        return visitante;
    }

    public boolean isLocal() {
        return esLocal;
    }

    @Override
    public String toString() {
        return (esLocal ? "Local: " : "Visitante: ") + visitante.getNombre() + " bombo " + visitante.getBombo();
    }

    public void setLocal(Equipo local) {
        this.local = local;
    }

    public void setVisitante(Equipo visitante) {
        this.visitante = visitante;
    }

    public void setEsLocal(boolean esLocal) {
        this.esLocal = esLocal;
    }
}