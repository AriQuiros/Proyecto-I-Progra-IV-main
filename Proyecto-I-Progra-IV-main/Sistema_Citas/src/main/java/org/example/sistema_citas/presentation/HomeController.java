package org.example.sistema_citas.presentation;

import org.example.sistema_citas.dto.MedicoCardDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.example.sistema_citas.logic.Service;

import java.util.List;

@Controller
public class HomeController {

    private final Service service;

    public HomeController(Service service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String especialidad,
                        @RequestParam(required = false) String ciudad,
                        Model model) {
        List<MedicoCardDTO> medicos;
        if (especialidad != null || ciudad != null) {
            medicos = service.buscarMedicos(especialidad, ciudad);
        } else {
            medicos = service.getMedicosConHorarios();
        }
        model.addAttribute("medicos", medicos);
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

}
