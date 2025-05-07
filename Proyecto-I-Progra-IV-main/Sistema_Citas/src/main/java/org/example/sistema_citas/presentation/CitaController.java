package org.example.sistema_citas.presentation;

import org.example.sistema_citas.logic.Cita;
import org.example.sistema_citas.logic.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/citas")
public class CitaController {
    private final Service service;

    public CitaController(Service service) {
        this.service = service;
    }

    @GetMapping
    public Iterable<Cita> getAllCitas() {
        return service.findAllCitas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> getCitaById(@PathVariable Integer id) {
        return service.findCitaById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Cita createCita(@RequestBody Cita cita) {
        return service.saveCita(cita);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCita(@PathVariable Integer id) {
        service.deleteCitaById(id);
        return ResponseEntity.noContent().build();
    }
}
