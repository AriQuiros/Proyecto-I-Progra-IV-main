package org.example.sistema_citas.presentation;

import org.example.sistema_citas.logic.Horario;
import org.example.sistema_citas.logic.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/horarios")
public class HorarioController {
    private final Service service;

    public HorarioController(Service service) {
        this.service = service;
    }

    @GetMapping
    public Iterable<Horario> getAllHorarios() {
        return service.findAllHorarios();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Horario> getHorarioById(@PathVariable Integer id) {
        return service.findHorarioById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Horario createHorario(@RequestBody Horario horario) {
        return service.saveHorario(horario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHorario(@PathVariable Integer id) {
        service.deleteHorarioById(id);
        return ResponseEntity.noContent().build();
    }
}