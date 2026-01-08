package com.springboot.app.testdata;

import java.util.HashMap;
import java.util.Map;

import com.springboot.app.models.entities.TareaStatus;

public class TareaStatusTestDataBuilder {

   
    private static final Map<Short, String> MAPA_ESTATUS = new HashMap<>();

    static {
        MAPA_ESTATUS.put((short) 1, "EN PROCESO");
        MAPA_ESTATUS.put((short) 2, "DETENIDO");
        MAPA_ESTATUS.put((short) 3, "CANCELADO");
        MAPA_ESTATUS.put((short) 4, "PENDIENTE");
        MAPA_ESTATUS.put((short) 5, "COMPLETADO");
    }

    
    private Short id = 1;
    private String status = MAPA_ESTATUS.get(id); 


    public TareaStatusTestDataBuilder withId(Short id) {
        this.id = id;
       
        if (MAPA_ESTATUS.containsKey(id)) {
            this.status = MAPA_ESTATUS.get(id);
        }
        return this;
    }

   
    public TareaStatusTestDataBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public TareaStatus build() {
        TareaStatus tareaStatus = new TareaStatus();
        tareaStatus.setId(id);
        tareaStatus.setStatus(status);
        
        return tareaStatus;
    }
}