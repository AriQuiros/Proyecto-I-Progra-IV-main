package org.example.sistema_citas.presentation;

import jakarta.servlet.http.HttpSession;
import org.example.sistema_citas.logic.Medico;
import org.example.sistema_citas.logic.Service;
import org.example.sistema_citas.logic.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final Service service;

    @Autowired
    public AdminController(Service service) {
        this.service = service;
    }

    @GetMapping("/doctores")
    public String mostrarDoctores(Model model) {
        List<Medico> medicos = (List<Medico>) service.findAllMedicos();
        model.addAttribute("medicos", medicos);
        return "Administrador/doctores";
    }

    @GetMapping("/aprobarMedico/{id}")
    public String aprobarMedico(@PathVariable Integer id) {
        Optional<Medico> medicoOpt = service.findMedicoById(id);
        if (medicoOpt.isPresent()) {
            Medico medico = medicoOpt.get();
            medico.setEstado("APROBADO");
            service.saveMedico(medico);
        }
        return "redirect:/admin/doctores";
    }

    @PostMapping("/rechazarMedico/{id}")
    public String rechazarMedico(@PathVariable Integer id) {
        service.deleteMedicoById(id);
        return "redirect:/admin/doctores";
    }

}