import java.util.*;

public class SorteoChampions {
    private List<Equipo> equipos;
    private static final int PARTIDOS_EN_CASA = 4;
    private static final int PARTIDOS_COMO_VISITANTE = 4;

    // Constructor predeterminado necesario para Jackson
    public SorteoChampions() {
        this.equipos = new ArrayList<>();
    }

    public SorteoChampions(List<Equipo> equipos) {
        this.equipos = equipos;
    }

    public void realizarSorteo() {
        Map<Equipo, Encuentro> encuentrosMap = new HashMap<>();
        for (Equipo equipo : equipos) {
            Encuentro encuentro = new Encuentro(equipo);
            encuentrosMap.put(equipo, encuentro);
        }

        List<Partido> partidosDisponibles = new ArrayList<>();

        // Generar todos los partidos posibles
        for (Equipo equipoLocal : equipos) {
            for (Equipo equipoVisitante : equipos) {
                if (!equipoLocal.equals(equipoVisitante)) {
                    partidosDisponibles.add(new Partido(equipoLocal, equipoVisitante, true));
                }
            }
        }

        Collections.shuffle(partidosDisponibles);

        Map<Equipo, Integer> partidosEnCasa = new HashMap<>();
        Map<Equipo, Integer> partidosComoVisitante = new HashMap<>();

        Map<Equipo, Map<Integer, Integer>> encuentrosPorBombo = new HashMap<>();

        for (Equipo equipo : equipos) {
            partidosEnCasa.put(equipo, 0);
            partidosComoVisitante.put(equipo, 0);
            encuentrosPorBombo.put(equipo, new HashMap<>());
            for (int i = 1; i <= 4; i++) {
                encuentrosPorBombo.get(equipo).put(i, 0);  // Cada bombo con 0 encuentros iniciales
            }
        }

        // Asignar partidos respetando las reglas de enfrentamientos por bombo
        for (Partido partido : partidosDisponibles) {
            Equipo equipoLocal = partido.getLocal();
            Equipo equipoVisitante = partido.getVisitante();

            int bomboLocal = equipoLocal.getBombo();
            int bomboVisitante = equipoVisitante.getBombo();

            // Condiciones: No exceder 4 partidos en casa y visitante
            if (partidosEnCasa.get(equipoLocal) < PARTIDOS_EN_CASA &&
                    partidosComoVisitante.get(equipoVisitante) < PARTIDOS_COMO_VISITANTE &&
                    verificarBomboExacto(encuentrosPorBombo.get(equipoLocal), bomboVisitante) &&
                    verificarBomboExacto(encuentrosPorBombo.get(equipoVisitante), bomboLocal)) {

                Encuentro encuentroLocal = encuentrosMap.get(equipoLocal);
                Encuentro encuentroVisitante = encuentrosMap.get(equipoVisitante);

                encuentroLocal.agregarPartido(partido);
                encuentroVisitante.agregarPartido(new Partido(equipoVisitante, equipoLocal, false));

                // Actualizar conteo de partidos en casa, visitante y por bombo
                partidosEnCasa.put(equipoLocal, partidosEnCasa.get(equipoLocal) + 1);
                partidosComoVisitante.put(equipoVisitante, partidosComoVisitante.get(equipoVisitante) + 1);

                encuentrosPorBombo.get(equipoLocal).put(bomboVisitante, encuentrosPorBombo.get(equipoLocal).get(bomboVisitante) + 1);
                encuentrosPorBombo.get(equipoVisitante).put(bomboLocal, encuentrosPorBombo.get(equipoVisitante).get(bomboLocal) + 1);
            }
        }

        // Verificar si todos los equipos tienen 8 partidos asignados
        for (Equipo equipo : equipos) {
            Encuentro encuentro = encuentrosMap.get(equipo);
            if (encuentro.getPartidos().size() != 8) {
                System.out.println("El equipo " + equipo.getNombre() + " no tiene exactamente 8 partidos asignados.");
            }
        }

        for (Encuentro encuentro : encuentrosMap.values()) {
            System.out.println(encuentro);
        }
    }

    // Método para verificar si un equipo tiene exactamente 2 partidos por bombo
    private boolean verificarBomboExacto(Map<Integer, Integer> encuentrosPorBombo, int bomboRival) {
        return encuentrosPorBombo.get(bomboRival) < 2;
    }

    public List<Equipo> getEquipos() { return equipos; }
    public void setEquipos(List<Equipo> equipos) { this.equipos = equipos; }
}