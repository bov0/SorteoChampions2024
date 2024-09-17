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

        // Mapas para controlar los partidos en casa, como visitante y los encuentros por bombo
        Map<Equipo, Integer> partidosEnCasa = new HashMap<>();
        Map<Equipo, Integer> partidosComoVisitante = new HashMap<>();
        Map<Equipo, Map<Integer, Integer>> encuentrosPorBombo = new HashMap<>();

        for (Equipo equipo : equipos) {
            partidosEnCasa.put(equipo, 0);
            partidosComoVisitante.put(equipo, 0);
            encuentrosPorBombo.put(equipo, new HashMap<>());
            for (int i = 1; i <= 4; i++) {
                encuentrosPorBombo.get(equipo).put(i, 0);
            }
        }

        List<Partido> partidosAsignados = new ArrayList<>();

        for (Equipo equipoLocal : equipos) {
            for (Equipo equipoVisitante : equipos) {
                if (!equipoLocal.equals(equipoVisitante)) {
                    partidosAsignados.add(new Partido(equipoLocal, equipoVisitante, true));
                }
            }
        }

        // Ordenar los partidos de manera estratégica, los comparamos teniendo el bombo del que pertenecen.
        partidosAsignados.sort(Comparator.comparingInt((Partido partido) -> partido.getLocal().getBombo())
                .thenComparingInt(partido -> partido.getVisitante().getBombo()));

        // Proceso iterativo de asignación
        for (Partido partido : partidosAsignados) {
            Equipo equipoLocal = partido.getLocal();
            Equipo equipoVisitante = partido.getVisitante();

            int bomboLocal = equipoLocal.getBombo();
            int bomboVisitante = equipoVisitante.getBombo();

            // Verificar si se puede asignar el partido sin violar las restricciones
            if (partidosEnCasa.get(equipoLocal) < PARTIDOS_EN_CASA &&
                    partidosComoVisitante.get(equipoVisitante) < PARTIDOS_COMO_VISITANTE &&
                    encuentrosPorBombo.get(equipoLocal).get(bomboVisitante) < 2 &&
                    encuentrosPorBombo.get(equipoVisitante).get(bomboLocal) < 2) {

                // Asignar el partido
                Encuentro encuentroLocal = encuentrosMap.get(equipoLocal);
                Encuentro encuentroVisitante = encuentrosMap.get(equipoVisitante);

                encuentroLocal.agregarPartido(partido);
                encuentroVisitante.agregarPartido(new Partido(equipoVisitante, equipoLocal, false));

                // Actualizar conteo
                partidosEnCasa.put(equipoLocal, partidosEnCasa.get(equipoLocal) + 1);
                partidosComoVisitante.put(equipoVisitante, partidosComoVisitante.get(equipoVisitante) + 1);

                encuentrosPorBombo.get(equipoLocal).put(bomboVisitante, encuentrosPorBombo.get(equipoLocal).get(bomboVisitante) + 1);
                encuentrosPorBombo.get(equipoVisitante).put(bomboLocal, encuentrosPorBombo.get(equipoVisitante).get(bomboLocal) + 1);
            }
        }

        // Verificar que todos los equipos tengan 8 partidos
        for (Equipo equipo : equipos) {
            Encuentro encuentro = encuentrosMap.get(equipo);
            if (encuentro.getPartidos().size() != 8) {
                System.out.println("El equipo " + equipo.getNombre() + " no tiene exactamente 8 partidos asignados.");
            }
        }

        // Mostrar todos los encuentros
        for (Encuentro encuentro : encuentrosMap.values()) {
            System.out.println(encuentro);
        }
    }

    private boolean verificarBomboExacto(Map<Integer, Integer> encuentrosPorBombo, int bomboRival) {
        return encuentrosPorBombo.get(bomboRival) < 2;
    }

    public List<Equipo> getEquipos() { return equipos; }
    public void setEquipos(List<Equipo> equipos) { this.equipos = equipos; }
}