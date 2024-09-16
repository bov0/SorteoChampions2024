import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class App {

    public static final File EQUIPOS = new File("SorteoChampions.json");

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Leer los equipos desde el archivo JSON
            SorteoChampions sorteoChampions = objectMapper.readValue(EQUIPOS, SorteoChampions.class);

            SorteoChampions sorteo = new SorteoChampions(sorteoChampions.getEquipos());

            sorteo.realizarSorteo();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
