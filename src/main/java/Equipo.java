import java.util.List;

public class Equipo {
    private String nombre;
    private String liga;
    private int bombo;

    public Equipo() {}

    public Equipo(String nombre, String liga, int bombo) {
        this.nombre = nombre;
        this.liga = liga;
        this.bombo = bombo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLiga() {
        return liga;
    }

    public void setLiga(String liga) {
        this.liga = liga;
    }

    public int getBombo() {
        return bombo;
    }

    public void setBombo(int bombo) {
        this.bombo = bombo;
    }

    @Override
    public String toString() {
        return  nombre + ", " + liga + ", bombo" + bombo;
    }
}
