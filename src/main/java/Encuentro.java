import java.util.ArrayList;
import java.util.List;

public class Encuentro {
    private Equipo equipo;
    private List<Partido> partidos;

    public Encuentro(Equipo equipo) {
        this.equipo = equipo;
        this.partidos = new ArrayList<>();
    }

    public void agregarPartido(Partido partido) {
        partidos.add(partido);
    }

    public boolean compararEncuentro(Partido partido1, Partido partido2) {
        return partido1.equals(partido2);
    }

    public Boolean yaHaJugadoComoLocal(Equipo equipo) {
        for (Partido partido : this.partidos) {
            // Si el equipo ha jugado como local, devolver true
            if (partido.getLocal().equals(equipo)) {
                return true;
            }
        }
        return false;
    }

    public Boolean yaHaJugadoComoVisitante(Equipo equipo) {
        boolean haJugadoComoLocal = false;

        for (Partido partido : this.partidos) {
            // Si el equipo ha jugado como visitante, devolver true
            if (partido.getVisitante().equals(equipo)) {
                return true;
            }
        }
        return false;
    }

    public Equipo getEquipo() { return equipo; }
    public List<Partido> getPartidos() { return partidos; }

    @Override
    public String toString() {
        return "Equipo: " + equipo + "\nPartidos:\n" + partidos + "\n Número de partidos: " + partidos.size();
    }
}