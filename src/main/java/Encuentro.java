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
        boolean haJugadoComoVisitante = false;

        for (Partido partido : this.partidos) {
            // Si el equipo ha jugado como local, devolver true
            if (partido.getLocal().equals(equipo)) {
                return true;
            }
            // Si el equipo ha jugado como visitante, marcar que ha jugado
            if (partido.getVisitante().equals(equipo)) {
                haJugadoComoVisitante = true;
            }
        }
        // Si ha jugado como visitante pero no como local, devolver false
        if (haJugadoComoVisitante) {
            return false;
        }
        // Si no ha jugado ningún partido, devolver null
        return null;
    }

    public Boolean yaHaJugadoComoVisitante(Equipo equipo) {
        boolean haJugadoComoLocal = false;

        for (Partido partido : this.partidos) {
            // Si el equipo ha jugado como visitante, devolver true
            if (partido.getVisitante().equals(equipo)) {
                return true;
            }
            // Si el equipo ha jugado como local, marcar que ha jugado
            if (partido.getLocal().equals(equipo)) {
                haJugadoComoLocal = true;
            }
        }
        // Si ha jugado como local pero no como visitante, devolver false
        if (haJugadoComoLocal) {
            return false;
        }
        // Si no ha jugado ningún partido, devolver null
        return null;
    }

    public Equipo getEquipo() { return equipo; }
    public List<Partido> getPartidos() { return partidos; }

    @Override
    public String toString() {
        return "Equipo: " + equipo + "\nPartidos:\n" + partidos + "\n Número de partidos: " + partidos.size();
    }
}