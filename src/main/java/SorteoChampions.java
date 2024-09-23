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
        Map<Equipo, Encuentro> encuentrosMap = inicializarEncuentros();
        Map<Integer, List<Equipo>> equiposPorBombo = agruparEquiposPorBombo();

        int bombo = 1;
        List<Equipo> equiposDelBombo = equiposPorBombo.get(bombo);

        if (equiposDelBombo == null) return;

        System.out.println("Generando encuentros para el bombo: " + bombo);
        LinkedHashMap<String, Integer> teamsByCountry = obtenerTeamsByCountry(equiposDelBombo);
        System.out.println("Estado inicial de equipos por país: " + teamsByCountry);

        Map<Equipo, Integer> partidosAsignados = inicializarPartidosAsignados(equiposDelBombo);
        Set<String> partidosJugados = new HashSet<>();

        while (true) {
            String ligaConMasEquipos = obtenerLigaConMasEquipos(teamsByCountry);
            if (ligaConMasEquipos == null) break;

            Equipo equipoLocal = elegirEquipoLocal(equiposDelBombo, ligaConMasEquipos, partidosAsignados);
            if (equipoLocal == null) continue;

            Equipo equipoVisitante = elegirEquipoVisitante(equiposDelBombo, equipoLocal, partidosAsignados);
            if (equipoVisitante == null) continue;

            String partidoId = equipoLocal.getNombre() + "-" + equipoVisitante.getNombre();
            if (partidosJugados.contains(partidoId)) continue;

            if (asignarPartido(encuentrosMap, partidosAsignados, teamsByCountry, partidoId, equipoLocal, equipoVisitante)) {
                partidosJugados.add(partidoId);
            }
        }

        mostrarEncuentros(encuentrosMap, equiposDelBombo);
        verificarPartidosAsignados(encuentrosMap, equiposDelBombo);
    }

    private Map<Equipo, Encuentro> inicializarEncuentros() {
        Map<Equipo, Encuentro> encuentrosMap = new HashMap<>();
        for (Equipo equipo : equipos) {
            encuentrosMap.put(equipo, new Encuentro(equipo));
        }
        return encuentrosMap;
    }

    private Map<Integer, List<Equipo>> agruparEquiposPorBombo() {
        Map<Integer, List<Equipo>> equiposPorBombo = new HashMap<>();
        for (Equipo equipo : equipos) {
            equiposPorBombo.computeIfAbsent(equipo.getBombo(), k -> new ArrayList<>()).add(equipo);
        }
        return equiposPorBombo;
    }

    private Map<Equipo, Integer> inicializarPartidosAsignados(List<Equipo> equiposDelBombo) {
        Map<Equipo, Integer> partidosAsignados = new HashMap<>();
        for (Equipo equipo : equiposDelBombo) {
            partidosAsignados.put(equipo, 0);
        }
        return partidosAsignados;
    }

    private String obtenerLigaConMasEquipos(LinkedHashMap<String, Integer> teamsByCountry) {
        return teamsByCountry.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private Equipo elegirEquipoLocal(List<Equipo> equiposDelBombo, String ligaConMasEquipos, Map<Equipo, Integer> partidosAsignados) {
        List<Equipo> equiposDeLaLiga = equiposDelBombo.stream()
                .filter(equipo -> equipo.getLiga().equals(ligaConMasEquipos) && partidosAsignados.get(equipo) < 2)
                .toList();

        if (equiposDeLaLiga.isEmpty()) return null;
        return equiposDeLaLiga.get(new Random().nextInt(equiposDeLaLiga.size()));
    }

    private Equipo elegirEquipoVisitante(List<Equipo> equiposDelBombo, Equipo equipoLocal, Map<Equipo, Integer> partidosAsignados) {
        List<Equipo> equiposVisitantes = equiposDelBombo.stream()
                .filter(equipo -> !equipo.equals(equipoLocal) && partidosAsignados.get(equipo) < 2)
                .filter(equipo -> !equipo.getLiga().equals(equipoLocal.getLiga()))
                .toList();

        if (equiposVisitantes.isEmpty()) return null;

        return equiposVisitantes.get(new Random().nextInt(equiposVisitantes.size()));
    }

    private boolean asignarPartido(Map<Equipo, Encuentro> encuentrosMap, Map<Equipo, Integer> partidosAsignados,
                                   LinkedHashMap<String, Integer> teamsByCountry, String partidoId,
                                   Equipo equipoLocal, Equipo equipoVisitante) {

        if (partidosAsignados.get(equipoLocal) < 2 &&
                partidosAsignados.get(equipoVisitante) < 2) {

            Partido partido = new Partido(equipoLocal, equipoVisitante, true);
            Encuentro encuentroLocal = encuentrosMap.get(equipoLocal);
            Encuentro encuentroVisitante = encuentrosMap.get(equipoVisitante);

            encuentroLocal.agregarPartido(partido);
            encuentroVisitante.agregarPartido(new Partido(equipoVisitante, equipoLocal, false));

            partidosAsignados.put(equipoLocal, partidosAsignados.get(equipoLocal) + 1);
            partidosAsignados.put(equipoVisitante, partidosAsignados.get(equipoVisitante) + 1);

            teamsByCountry.put(equipoLocal.getLiga(), teamsByCountry.get(equipoLocal.getLiga()) - 1);
            teamsByCountry.put(equipoVisitante.getLiga(), teamsByCountry.get(equipoVisitante.getLiga()) - 1);

            System.out.println("Partido asignado: " + equipoLocal.getNombre() + " vs " + equipoVisitante.getNombre());
            System.out.println("Estado actualizado de equipos por país: " + teamsByCountry);
            return true;
        }
        return false;
    }

    private void mostrarEncuentros(Map<Equipo, Encuentro> encuentrosMap, List<Equipo> equiposDelBombo) {
        for (Encuentro encuentro : encuentrosMap.values()) {
            if (encuentro.getEquipo().getBombo() == 1) {
                System.out.println(encuentro);
                System.out.println();
            }
        }
    }

    private void verificarPartidosAsignados(Map<Equipo, Encuentro> encuentrosMap, List<Equipo> equiposDelBombo) {
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