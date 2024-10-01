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

    public void RealizarSorteo() {
        Map<Equipo, Encuentro> encuentrosMap = new HashMap<>();
        Map<Integer, List<Equipo>> equiposPorBombo = new HashMap<>();

        AgruparEquiposPorBombo(encuentrosMap, equiposPorBombo);

        // Trabajar solo con el bombo 1
        int bombo = 1;
        List<Equipo> equiposDelBombo = equiposPorBombo.get(bombo);

        if (equiposDelBombo == null) return;

        System.out.println("Generando encuentros para el bombo: " + bombo);

        // Obtener EquiposPorLiga para el bombo actual
        LinkedHashMap<String, Integer> equiposPorLiga = obtenerEquiposPorLiga(equiposDelBombo);
        List<List<Equipo>> equiposPorLigaOrdenadosDescendente = obtenerEquiposPorLigaOrdenadosDescendente(equiposDelBombo);
        imprimirSorteoOrdenadoDesc(equiposPorLigaOrdenadosDescendente);

        System.out.println("Estado inicial de equipos por país: " + equiposPorLiga);

        // Mapas para controlar los partidos asignados
        Map<Equipo, Integer> partidosAsignados = new HashMap<>();
        Set<String> partidosJugados = new HashSet<>(); // Para evitar partidos repetidos

        for (Equipo equipo : equiposDelBombo) {
            partidosAsignados.put(equipo, 0);
        }

        while (true) {

            //Mostrar todos los equipos que pueden jugar de local
            List<Equipo> equiposQuePuedenJugarDeLocal = EquiposQuePuedenJugar(equiposPorLigaOrdenadosDescendente, encuentrosMap, true);

            // Obtener la lista de ligas ordenada por cantidad de equipos
            List<Map.Entry<String, Integer>> listaLigas = new ArrayList<>(equiposPorLiga.entrySet());
            listaLigas.sort((a, b) -> b.getValue().compareTo(a.getValue())); // Ordenar de mayor a menor

            // Buscar liga con equipos disponibles
            String ligaConMasEquipos = listaLigas.stream().filter(entry -> entry.getValue() > 0).findFirst().map(Map.Entry::getKey).orElse(null);

            if (ligaConMasEquipos == null) {
                System.out.println("No hay mas ligas disponibles");
                break; // Salir si no hay ligas disponibles
            }

            // Elegir serie de equipos pertenecientes a la liga con más equipos
            List<Equipo> equiposLigaConMasEquipos = new ArrayList<>();
            for (Equipo equipo : equiposDelBombo) {
                if (equipo.getLiga().equals(ligaConMasEquipos) && partidosAsignados.get(equipo) < 2) {
                    equiposLigaConMasEquipos.add(equipo);
                }
            }
            System.out.println("1. Equipos en la liga con mas equipos disponibles que puedan jugar de local " + equiposLigaConMasEquipos);

            PartidosDisponiblesEnLigaConcreta(equiposLigaConMasEquipos, ligaConMasEquipos, encuentrosMap);

            if (equiposLigaConMasEquipos.isEmpty()) {
                System.out.println("No hay mas equipos en la liga con mas equipos disponibles");
                //Si no hay en esa liga abría que elegir la siguiente y asi sucesivamente.
                continue; // Continuar si no hay equipos disponibles en la liga
            }

            Equipo equipoPrincipal = equiposLigaConMasEquipos.get(new Random().nextInt(equiposLigaConMasEquipos.size()));

            System.out.println("EQUIPO PRINCIPAL SELECCIONADO ES: " + equipoPrincipal);
            System.out.println("HA JUGADO COMO LOCAL EL EQUIPO PRINCIPAL? " + encuentrosMap.get(equipoPrincipal).yaHaJugadoComoLocal(equipoPrincipal));
            System.out.println("HA JUGADO COMO VISITANTE EL EQUIPO PRINCIPAL? " + encuentrosMap.get(equipoPrincipal).yaHaJugadoComoVisitante(equipoPrincipal));

            Encuentro encuentroPrincipal = encuentrosMap.get(equipoPrincipal);
            boolean equipoPrincipalHaJugadoComoLocal = false;

            if (encuentroPrincipal != null && encuentroPrincipal.yaHaJugadoComoLocal(equipoPrincipal)) {
                equipoPrincipalHaJugadoComoLocal = true;
            }

            // Buscar un equipo contendiente en otras ligas
            List<Equipo> posiblesContendientes = new ArrayList<>();
            for (Equipo equipo : equiposDelBombo) {
                // Verificar si no es el mismo que el local y que no ha disputado más de 2 partidos
                if (!equipo.equals(equipoPrincipal) && partidosAsignados.get(equipo) < 2) {
                    posiblesContendientes.add(equipo);
                }
            }

            System.out.println("EQUIPOS CONTENDIENTES SIN FILTROS" + posiblesContendientes);

            // Filtrar para que el contendiente no sea de la misma liga
            posiblesContendientes.removeIf(equipo -> equipo.getLiga().equals(equipoPrincipal.getLiga()));
            for (int i = 0; i < posiblesContendientes.size(); i++) {
                System.out.println("HA JUGADO COMO LOCAL EL " + posiblesContendientes.get(i).getNombre() +  "? " + encuentrosMap.get(equipoPrincipal).yaHaJugadoComoLocal(equipoPrincipal));
                System.out.println("HA JUGADO COMO VISITANTE EL " + posiblesContendientes.get(i).getNombre() +  "? " + encuentrosMap.get(equipoPrincipal).yaHaJugadoComoVisitante(equipoPrincipal));
            }

            if (equipoPrincipalHaJugadoComoLocal) {
                // Mantener solo los equipos que NO hayan jugado como local, porque pueden ser locales
                posiblesContendientes.removeIf(equipo -> {
                    Encuentro encuentro = encuentrosMap.get(equipo);
                    return encuentro.yaHaJugadoComoLocal(equipo);
                });
            } else {
                // Si el equipo principal no ha jugado como local, buscamos un contendiente que pueda jugar como visitante
                posiblesContendientes.removeIf(equipo -> {
                    Encuentro encuentro = encuentrosMap.get(equipo);
                    return encuentro.yaHaJugadoComoVisitante(equipo);
                });
            }

            if (posiblesContendientes.isEmpty()) {
                System.out.println("NO HAY CONTENDIENTES VALIDOS, LES FALTA EL MISMO ENFRENTAMIENTO QUE EL EQUIPO PRINCIPAL!");
                System.out.println(equiposQuePuedenJugarDeLocal);
            } // Continuar si no hay equipos contendientes válidos

            System.out.println("EQUIPOS CONTENDIENTES DESPUES DE LOS FILTROS " + posiblesContendientes);

            // Elegir un equipo contendiente de una liga con más equipos
            String ligaContendiente = posiblesContendientes.stream()
                    .map(Equipo::getLiga)
                    .distinct()
                    .min((a, b) -> Integer.compare(equiposPorLiga.get(b), equiposPorLiga.get(a)))
                    .orElse(null);

            if (ligaContendiente == null) {
                System.out.println("No hay ligas contendientes");
                continue; // Si no hay ligas contendientes, continuar
            }

            List<Equipo> contendientes = posiblesContendientes.stream()
                    .filter(equipo -> equipo.getLiga().equals(ligaContendiente))
                    .toList();

            if (contendientes.isEmpty()) {
                System.out.println("No hay equipos contendientes validos");
                continue; // Si no hay visitantes válidos, continuar
            }

            Equipo equipoContendiente = contendientes.get(new Random().nextInt(contendientes.size()));

            // Verificar si ya se han jugado este partido
            String partidoId = equipoPrincipal.getNombre() + "-" + equipoContendiente.getNombre();
            String partidoId2 = equipoContendiente.getNombre() + "-" + equipoPrincipal.getNombre();
            if (partidosJugados.contains(partidoId) || partidosJugados.contains(partidoId2)) {
                System.out.println("Ya se jugó el partido " + partidoId);
                continue; // Si ya se jugó, saltar
            }

            // Comprobar que ambos equipos no superen el límite de 2 partidos
            if (partidosAsignados.get(equipoPrincipal) < 2 && partidosAsignados.get(equipoContendiente) < 2) {

                if (!equipoPrincipalHaJugadoComoLocal) {
                    asignarPartidos(equipoPrincipal, equipoContendiente, encuentrosMap);
                    ActualizarConteos(partidosAsignados, equipoPrincipal, equipoContendiente);
                } else {
                    asignarPartidos(equipoContendiente, equipoPrincipal, encuentrosMap);
                    ActualizarConteos(partidosAsignados, equipoContendiente, equipoPrincipal);
                }

                if (!equipoPrincipalHaJugadoComoLocal) {
                    System.out.println("PARTIDO REALIZADO: " + partidoId);
                } else {
                    System.out.println("PARTIDO REALIZADO " + partidoId2);
                }

                // Marcar el partido como jugado
                partidosJugados.add(partidoId);

                ActualizarConteoEquiposPorPais(equiposPorLiga, equipoPrincipal, equipoContendiente);
                ImprimirEstadoEquiposPorLiga(equipoPrincipal, equipoContendiente, equiposPorLiga);
            }
        }

        mostrarEncuentros(encuentrosMap);
        VerificarNumPartidosPorEquipo(equiposDelBombo, encuentrosMap);
    }

    private static void equiposQuePuedenJugarDeVisitante(List<Equipo> equiposQuePuedenJugarDeVisitante, Map<Equipo, Encuentro> encuentrosMap, String ligaConMasEquipos, Map<Equipo, Integer> partidosAsignados) {
        List<Equipo> equiposVisitantesConCondiciones = new ArrayList<>(equiposQuePuedenJugarDeVisitante);

        // Eliminar los equipos de la misma liga de la lista de visitantes
        equiposVisitantesConCondiciones.removeIf(equipo -> {
            Encuentro encuentro = encuentrosMap.get(equipo);

            // Verificar si ha jugado como visitante (manejar null)
            Boolean haJugadoComoVisitante = encuentro != null ? encuentro.yaHaJugadoComoVisitante(equipo) : null;

            // Condición para eliminar: de la misma liga, ya ha jugado como visitante, o ha jugado menos de 2 partidos
            return equipo.getLiga().equals(ligaConMasEquipos)
                    || (partidosAsignados.get(equipo) >= 2)
                    || (haJugadoComoVisitante != null && haJugadoComoVisitante);
        });
        System.out.println("EQUIPOS VISITANTES EXISTENTES REALES" + equiposVisitantesConCondiciones);
    }

    private static List<Equipo> EquiposQuePuedenJugar(List<List<Equipo>> equiposPorLigaOrdenadosDescendente,
                                                      Map<Equipo, Encuentro> encuentrosMap,
                                                      boolean esLocal) {
        List<Equipo> equiposDisponibles = new ArrayList<>();

        // Recorrer las ligas ordenadas por cantidad de equipos de forma descendente
        for (List<Equipo> liga : equiposPorLigaOrdenadosDescendente) {
            for (Equipo equipo : liga) {
                Encuentro encuentro = encuentrosMap.get(equipo);

                // Si no hay encuentros registrados para este equipo, puede jugar
                if (encuentro == null) {
                    equiposDisponibles.add(equipo);
                    continue;
                }

                // Verificar si el equipo ha jugado según el rol (local o visitante)
                boolean haJugado = esLocal
                        ? Boolean.TRUE.equals(encuentro.yaHaJugadoComoLocal(equipo))
                        : Boolean.TRUE.equals(encuentro.yaHaJugadoComoVisitante(equipo));

                // Si no ha jugado en ese rol, agregar a la lista de disponibles
                if (!haJugado) {
                    equiposDisponibles.add(equipo);
                }
            }
        }

        // Imprimir los equipos disponibles para el rol actual
        String rol = esLocal ? "local" : "visitante";
        System.out.println("Equipos que pueden jugar como " + rol + ":");
        for (Equipo equipo : equiposDisponibles) {
            System.out.println(equipo.getNombre());
        }

        // Retornar la lista de equipos disponibles
        return equiposDisponibles;
    }

    // Ver total de partidos que esa liga puede disputar
    private static void PartidosDisponiblesEnLigaConcreta(List<Equipo> equiposLigaConMasEquipos, String ligaConMasEquipos, Map<Equipo, Encuentro> encuentrosMap) {
        int partidosDispLigaConMasEquipos = 0;
        for (Equipo equipo : equiposLigaConMasEquipos) {
            if (equipo.getLiga().equals(ligaConMasEquipos)) {
                // Verificar si el equipo no ha jugado como local
                Boolean haJugadoLocal = encuentrosMap.get(equipo).yaHaJugadoComoLocal(equipo);
                if (haJugadoLocal == null || !haJugadoLocal) {
                    partidosDispLigaConMasEquipos++;
                    System.out.println("No ha jugado como local: " + equipo.getNombre());
                }

                // Verificar si el equipo no ha jugado como visitante
                Boolean haJugadoVisitante = encuentrosMap.get(equipo).yaHaJugadoComoVisitante(equipo);
                if (haJugadoVisitante == null || !haJugadoVisitante) {
                    partidosDispLigaConMasEquipos++;
                    System.out.println("No ha jugado como visitante: " + equipo.getNombre());
                }
            }
        }
        System.out.println("Partidos disponibles para la liga " + ligaConMasEquipos + ": " + partidosDispLigaConMasEquipos);
    }

    private static void ImprimirEstadoEquiposPorLiga(Equipo equipoPrincipal, Equipo equipoContendiente, LinkedHashMap<String, Integer> equiposPorLiga) {
        // Mostrar estado actualizado de EquiposPorLiga
        System.out.println("Partido asignado: " + equipoPrincipal.getNombre() + " vs " + equipoContendiente.getNombre());
        System.out.println("Estado actualizado de equipos por país: " + equiposPorLiga);
    }

    // Actualizar el conteo de equipos por país
    private static void ActualizarConteoEquiposPorPais(LinkedHashMap<String, Integer> equiposPorLiga, Equipo equipoPrincipal, Equipo equipoContendiente) {
        equiposPorLiga.put(equipoPrincipal.getLiga(), equiposPorLiga.get(equipoPrincipal.getLiga()) - 1);
        equiposPorLiga.put(equipoContendiente.getLiga(), equiposPorLiga.get(equipoContendiente.getLiga()) - 1);
    }

    // Inicializar encuentros y agrupar equipos por bombo
    private void AgruparEquiposPorBombo(Map<Equipo, Encuentro> encuentrosMap, Map<Integer, List<Equipo>> equiposPorBombo) {
        for (Equipo equipo : equipos) {
            Encuentro encuentro = new Encuentro(equipo);
            encuentrosMap.put(equipo, encuentro);
            equiposPorBombo.computeIfAbsent(equipo.getBombo(), k -> new ArrayList<>()).add(equipo);
        }
    }

    // Actualizar los conteos
    private static void ActualizarConteos(Map<Equipo, Integer> partidosAsignados, Equipo equipoPrincipal, Equipo equipoContendiente) {
        partidosAsignados.put(equipoPrincipal, partidosAsignados.get(equipoPrincipal) + 1);
        partidosAsignados.put(equipoContendiente, partidosAsignados.get(equipoContendiente) + 1);
    }

    // Mostrar todos los encuentros
    private static void mostrarEncuentros(Map<Equipo, Encuentro> encuentrosMap) {
        for (Encuentro encuentro : encuentrosMap.values()) {
            if (encuentro.getEquipo().getBombo() == 1) {
                System.out.println(encuentro);
                System.out.println();
            }
        }
    }

    // Asignar el partido
    private static void asignarPartidos(Equipo equipoPrincipal, Equipo equipoContendiente, Map<Equipo, Encuentro> encuentrosMap) {
        Partido partido = new Partido(equipoPrincipal, equipoContendiente, true);
        Encuentro encuentroLocal = encuentrosMap.get(equipoPrincipal);
        Encuentro encuentroVisitante = encuentrosMap.get(equipoContendiente);

        encuentroLocal.agregarPartido(partido);
        encuentroVisitante.agregarPartido(new Partido(equipoContendiente, equipoPrincipal, false));
    }

    // Método para obtener el conteo de equipos por liga en función del bombo
    private LinkedHashMap<String, Integer> obtenerEquiposPorLiga(List<Equipo> equiposDelBombo) {
        LinkedHashMap<String, Integer> equiposPorLiga = new LinkedHashMap<>();
        for (Equipo equipo : equiposDelBombo) {
            equiposPorLiga.put(equipo.getLiga(), equiposPorLiga.getOrDefault(equipo.getLiga(), 0) + 2);
        }
        return equiposPorLiga;
    }

    // Método para obtener un array con las listas de equipos por liga, ordenadas por número de equipos en orden descendente
    private List<List<Equipo>> obtenerEquiposPorLigaOrdenadosDescendente(List<Equipo> equiposDelBombo) {
        // Map para agrupar los equipos por liga
        Map<String, List<Equipo>> equiposPorLiga = new HashMap<>();

        // Agrupar los equipos por liga
        for (Equipo equipo : equiposDelBombo) {
            equiposPorLiga.computeIfAbsent(equipo.getLiga(), k -> new ArrayList<>()).add(equipo);
        }

        // Ordenar las ligas por el tamaño de su lista de equipos (descendente)
        List<Map.Entry<String, List<Equipo>>> ligasOrdenadas = new ArrayList<>(equiposPorLiga.entrySet());
        ligasOrdenadas.sort((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()));

        // Crear el array de listas de equipos en orden descendente
        List<List<Equipo>> equiposOrdenadosPorLiga = new ArrayList<>();

        for (Map.Entry<String, List<Equipo>> entry : ligasOrdenadas) {
            equiposOrdenadosPorLiga.add(entry.getValue()); // Añadir la lista de equipos de cada liga al resultado
        }

        return equiposOrdenadosPorLiga;
    }

    // Verificar que todos los equipos tengan exactamente 2 partidos asignados
    private static void VerificarNumPartidosPorEquipo(List<Equipo> equiposDelBombo, Map<Equipo, Encuentro> encuentrosMap) {
        for (Equipo equipo : equiposDelBombo) {
            if (equipo.getBombo() == 1) {
                Encuentro encuentro = encuentrosMap.get(equipo);
                if (encuentro.getPartidos().size() != 2) {
                    System.out.println("El equipo " + equipo.getNombre() + " no tiene exactamente 2 partidos asignados.");
                }
            }
        }
    }

    private static void imprimirSorteoOrdenadoDesc(List<List<Equipo>> equiposPorLigaOrdenadosDescendente) {
        for (List<Equipo> equiposDeLaLiga : equiposPorLigaOrdenadosDescendente) {
            // Si la lista tiene equipos, obtener el nombre de la liga del primer equipo
            if (!equiposDeLaLiga.isEmpty()) {
                String nombreLiga = equiposDeLaLiga.getFirst().getLiga();
                System.out.println("Liga: " + nombreLiga + " - Equipos:");

                // Imprimir todos los equipos de esa liga
                for (Equipo equipo : equiposDeLaLiga) {
                    System.out.println("  - " + equipo.getNombre());
                }
                System.out.println();
            }
        }
    }

    public List<Equipo> getEquipos() { return equipos; }
    public void setEquipos(List<Equipo> equipos) { this.equipos = equipos; }
}