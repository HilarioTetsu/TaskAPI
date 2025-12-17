package com.springboot.app.testdata;

import com.springboot.app.models.entities.PrioridadTarea;
import java.util.HashMap;
import java.util.Map;

public class PrioridadTareaTestDataBuilder {

   
    private static final Map<Short, String> MAPA_PRIORIDADES = new HashMap<>();

    static {
        MAPA_PRIORIDADES.put((short) 1, "BAJA");
        MAPA_PRIORIDADES.put((short) 2, "MEDIA");
        MAPA_PRIORIDADES.put((short) 3, "ALTA");
        MAPA_PRIORIDADES.put((short) 4, "URGENTE");
    }

    
    private Short id = 2;
    private String prioridadTipo = MAPA_PRIORIDADES.get(id); 


    public PrioridadTareaTestDataBuilder withId(Short id) {
        this.id = id;
        if (MAPA_PRIORIDADES.containsKey(id)) {
            this.prioridadTipo = MAPA_PRIORIDADES.get(id);
        }
        return this;
    }

   
    public PrioridadTareaTestDataBuilder withPrioridadTipo(String prioridadTipo) {
        this.prioridadTipo = prioridadTipo;
        return this;
    }

    public PrioridadTarea build() {
        PrioridadTarea prioridad = new PrioridadTarea();
        prioridad.setId(id);
        prioridad.setPrioridadTipo(prioridadTipo);
        return prioridad;
    }
}