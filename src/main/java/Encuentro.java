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

    public boolean yaHaJugadoComoVisitante(Equipo equipo) {
        for (Partido partido : this.partidos) {
            if (partido.getVisitante().equals(equipo)) {
                return true; // El equipo ya ha jugado como visitante
            }
        }
        return false; // El equipo no ha jugado como visitante
    }

    public Equipo getEquipo() { return equipo; }
    public List<Partido> getPartidos() { return partidos; }

    @Override
    public String toString() {
        return "Equipo: " + equipo + "\nPartidos:\n" + partidos + "\n Número de partidos: " + partidos.size();
    }
}