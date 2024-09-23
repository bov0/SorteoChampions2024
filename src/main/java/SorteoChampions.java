import java.util.*;
import java.util.stream.Collectors;

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
        Map<Integer, List<Equipo>> equiposPorBombo = new HashMap<>();

        // Inicializar encuentros y agrupar equipos por bombo
        for (Equipo equipo : equipos) {
            Encuentro encuentro = new Encuentro(equipo);
            encuentrosMap.put(equipo, encuentro);
            equiposPorBombo.computeIfAbsent(equipo.getBombo(), k -> new ArrayList<>()).add(equipo);
        }

        // Trabajar solo con el bombo 1
        int bombo = 1;
        List<Equipo> equiposDelBombo = equiposPorBombo.get(bombo);

        if (equiposDelBombo == null) return;

        System.out.println("Generando encuentros para el bombo: " + bombo);

        // Obtener teamsByCountry para el bombo actual
        LinkedHashMap<String, Integer> teamsByCountry = obtenerTeamsByCountry(equiposDelBombo);
        System.out.println("Estado inicial de equipos por país: " + teamsByCountry);

        // Mapas para controlar los partidos asignados
        Map<Equipo, Integer> partidosAsignados = new HashMap<>();
        Set<String> partidosJugados = new HashSet<>(); // Para evitar partidos repetidos

        for (Equipo equipo : equiposDelBombo) {
            partidosAsignados.put(equipo, 0);
        }

        while (true) {
            // Obtener la lista de ligas ordenada por cantidad de equipos
            List<Map.Entry<String, Integer>> listaLigas = new ArrayList<>(teamsByCountry.entrySet());
            listaLigas.sort((a, b) -> b.getValue().compareTo(a.getValue())); // Ordenar de mayor a menor

            // Buscar liga con equipos disponibles
            String ligaConMasEquipos = null;
            for (Map.Entry<String, Integer> entry : listaLigas) {
                if (entry.getValue() > 0) {
                    ligaConMasEquipos = entry.getKey();
                    break;
                }
            }

            if (ligaConMasEquipos == null) break; // Salir si no hay países disponibles

            // Elegir un equipo del país con más equipos
            List<Equipo> equiposLigaConMayoresContendientes = new ArrayList<>();
            for (Equipo equipo : equiposDelBombo) {
                if (equipo.getLiga().equals(ligaConMasEquipos) && partidosAsignados.get(equipo) < 2) {
                    equiposLigaConMayoresContendientes.add(equipo);
                }
            }

            if (equiposLigaConMayoresContendientes.isEmpty()) continue; // Continuar si no hay equipos disponibles en la liga

            Equipo equipoLocal = equiposLigaConMayoresContendientes.get(new Random().nextInt(equiposLigaConMayoresContendientes.size()));

            // Buscar un equipo visitante en otras ligas
            List<Equipo> equiposVisitantes = new ArrayList<>();
            for (Equipo equipo : equiposDelBombo) {
                // Verificar si no es el mismo que el local y que no ha disputado más de 2 partidos
                if (!equipo.equals(equipoLocal) && partidosAsignados.get(equipo) < 2 && !encuentrosMap.get(equipo).yaHaJugadoComoLocal(equipo)) {
                    System.out.println(equipo.getNombre() + " ha jugado como visitante " + encuentrosMap.get(equipo).yaHaJugadoComoVisitante(equipo));
                    equiposVisitantes.add(equipo);
                }
            }

            // Buscar un equipo visitante en otras ligas
            List<Equipo> equiposLocales = new ArrayList<>();
            for (Equipo equipo : equiposDelBombo) {
                // Verificar si no es el mismo que el local y que no ha disputado más de 2 partidos
                if (!equipo.equals(equipoLocal) && partidosAsignados.get(equipo) < 2 && !encuentrosMap.get(equipo).yaHaJugadoComoLocal(equipo)) {
                    System.out.println(equipo.getNombre() + " ha jugado como local " + encuentrosMap.get(equipo).yaHaJugadoComoLocal(equipo));
                    equiposLocales.add(equipo);
                }
            }

            // Filtrar para que el visitante no sea de la misma liga
            equiposVisitantes.removeIf(equipo -> equipo.getLiga().equals(equipoLocal.getLiga()));

            // Filtrar los que ya han jugado como visitantes en sus encuentros
            equiposVisitantes.removeIf(equipo -> {
                Encuentro encuentro = encuentrosMap.get(equipo);
                return encuentro != null && encuentro.yaHaJugadoComoVisitante(equipo);
                //tampoco funciona return partidosJugados.contains("-" + equipo);
            });

            if (equiposVisitantes.isEmpty()) continue; // Continuar si no hay equipos visitantes válidos


            // Elegir un equipo visitante de una liga con más equipos
            String ligaVisitante = equiposVisitantes.stream()
                    .map(Equipo::getLiga)
                    .distinct()
                    .min((a, b) -> Integer.compare(teamsByCountry.get(b), teamsByCountry.get(a)))
                    .orElse(null);

            if (ligaVisitante == null) continue; // Si no hay ligas visitantes, continuar

            List<Equipo> posiblesVisitantes = equiposVisitantes.stream()
                    .filter(equipo -> equipo.getLiga().equals(ligaVisitante))
                    .toList();

            if (posiblesVisitantes.isEmpty()) continue; // Si no hay visitantes válidos, continuar

            Equipo equipoVisitante = posiblesVisitantes.get(new Random().nextInt(posiblesVisitantes.size()));

            // Verificar si ya se han jugado este partido
            String partidoId = equipoLocal.getNombre() + "-" + equipoVisitante.getNombre();
            if (partidosJugados.contains(partidoId)) continue; // Si ya se jugó, saltar

            // Comprobar que ambos equipos no superen el límite de 2 partidos
            if (partidosAsignados.get(equipoLocal) < 2 && partidosAsignados.get(equipoVisitante) < 2) {
                // Asignar el partido
                Partido partido = new Partido(equipoLocal, equipoVisitante, true);
                Encuentro encuentroLocal = encuentrosMap.get(equipoLocal);
                Encuentro encuentroVisitante = encuentrosMap.get(equipoVisitante);

                encuentroLocal.agregarPartido(partido);
                encuentroVisitante.agregarPartido(new Partido(equipoVisitante, equipoLocal, false));

                // Actualizar los conteos
                partidosAsignados.put(equipoLocal, partidosAsignados.get(equipoLocal) + 1);
                partidosAsignados.put(equipoVisitante, partidosAsignados.get(equipoVisitante) + 1);

                // Marcar el partido como jugado
                partidosJugados.add(partidoId);

                // Actualizar el conteo de equipos por país
                teamsByCountry.put(equipoLocal.getLiga(), teamsByCountry.get(equipoLocal.getLiga()) - 1);
                teamsByCountry.put(equipoVisitante.getLiga(), teamsByCountry.get(equipoVisitante.getLiga()) - 1);

                // Mostrar estado actualizado de teamsByCountry
                System.out.println("Partido asignado: " + equipoLocal.getNombre() + " vs " + equipoVisitante.getNombre());
                System.out.println("Estado actualizado de equipos por país: " + teamsByCountry);
            }
        }

        // Mostrar todos los encuentros
        for (Encuentro encuentro : encuentrosMap.values()) {
            if (encuentro.getEquipo().getBombo() == 1) {
                System.out.println(encuentro);
                System.out.println();
            }
        }

        // Verificar que todos los equipos tengan exactamente 2 partidos asignados
        for (Equipo equipo : equiposDelBombo) {
            if (equipo.getBombo() == 1) {
                Encuentro encuentro = encuentrosMap.get(equipo);
                if (encuentro.getPartidos().size() != 2) {
                    System.out.println("El equipo " + equipo.getNombre() + " no tiene exactamente 2 partidos asignados.");
                }
            }
        }
    }

    // Método para obtener el conteo de equipos por liga en función del bombo
    private LinkedHashMap<String, Integer> obtenerTeamsByCountry(List<Equipo> equiposDelBombo) {
        LinkedHashMap<String, Integer> teamsByCountry = new LinkedHashMap<>();
        for (Equipo equipo : equiposDelBombo) {
            teamsByCountry.put(equipo.getLiga(), teamsByCountry.getOrDefault(equipo.getLiga(), 0) + 2);
        }
        return teamsByCountry;
    }

    public List<Equipo> getEquipos() { return equipos; }
    public void setEquipos(List<Equipo> equipos) { this.equipos = equipos; }
}