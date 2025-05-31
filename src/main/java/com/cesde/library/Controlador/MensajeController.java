package com.cesde.library.Controlador;


import com.cesde.library.Modelo.Mensajes;
import com.cesde.library.Repositorio.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mensajes")
@CrossOrigin(origins = "http://localhost:2007")
public class MensajeController {

    @Autowired
    private final MensajeRepository mensajeRepository;

    public MensajeController(MensajeRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    @GetMapping
    public List<Mensajes> listarMensajes() {
        return mensajeRepository.findAll();
    }

    @PostMapping
    public Mensajes guardarMensaje(@RequestBody Mensajes mensaje) {
        return mensajeRepository.save(mensaje);
    }
}
