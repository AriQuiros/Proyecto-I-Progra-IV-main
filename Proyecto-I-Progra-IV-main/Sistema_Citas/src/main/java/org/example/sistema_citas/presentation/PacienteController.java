package org.example.sistema_citas.presentation;

import jakarta.servlet.http.HttpSession;
import org.example.sistema_citas.dto.MedicoCardDTO;
import org.example.sistema_citas.dto.PacienteDTO;
import org.example.sistema_citas.logic.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    private final Service service;

    public PacienteController(Service service) {
        this.service = service;
    }

    @GetMapping
    public Iterable<Paciente> getAllPacientes() {
        return service.findAllPacientes();
    }

    @GetMapping("/{id}")
    public String getPacienteById(@PathVariable Integer id, HttpSession session) {
        Optional<PacienteDTO> pacienteOpt = service.findPacienteDTOById(id);

        if (pacienteOpt.isPresent()) {
            session.setAttribute("paciente", pacienteOpt.get());
            session.setAttribute("pacienteId", id);
            return "redirect:/pacientes/perfil/" + id;
        } else {
            return "redirect:/error/paciente-no-encontrado";
        }
    }

    @PostMapping
    public Paciente createPaciente(@RequestBody Paciente paciente) {
        return service.savePaciente(paciente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaciente(@PathVariable Integer id) {
        service.deletePacienteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/perfil/{id}")
    public String mostrarPerfilPaciente(@PathVariable Integer id, Model model, HttpSession session) {
        Integer pacienteIdSesion = (Integer) session.getAttribute("pacienteId");
        if (pacienteIdSesion == null || !pacienteIdSesion.equals(id)) {
            return "redirect:/usuarios/login";
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getId().equals(id)) {
            return "redirect:/usuarios/login";
        }

        Optional<Paciente> pacienteOpt = service.findPacienteById(id);
        if (pacienteOpt.isPresent()) {
            model.addAttribute("paciente", pacienteOpt.get());
            return "Paciente/perfil";
        }

        return "redirect:/usuarios/login";
    }

    @GetMapping("/confirmarcita")
    public String confirmarCita(@RequestParam Integer medicoId,
                                @RequestParam String fecha,
                                @RequestParam String hora,
                                HttpSession session,
                                Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null || !"PACIENTE".equals(usuario.getRol())) {
            return "redirect:/usuarios/login";
        }

        Optional<Paciente> pacienteOpt = service.findPacienteByUsuario(usuario);
        Optional<Medico> medicoOpt = service.findMedicoById(medicoId);

        if (pacienteOpt.isEmpty() || medicoOpt.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("paciente", pacienteOpt.get());
        model.addAttribute("medico", medicoOpt.get());
        model.addAttribute("fecha", fecha);
        model.addAttribute("hora", hora);

        return "Paciente/confirmarcita";
    }

    @PostMapping("/confirmarcita")
    public String guardarCita(@RequestParam Integer medicoId,
                              @RequestParam String fecha,
                              @RequestParam String hora,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null || !"PACIENTE".equals(usuario.getRol())) {
            return "redirect:/usuarios/login";
        }

        Optional<Paciente> pacienteOpt = service.findPacienteByUsuario(usuario);
        Optional<Medico> medicoOpt = service.findMedicoById(medicoId);

        if (pacienteOpt.isEmpty() || medicoOpt.isEmpty()) {
            return "redirect:/";
        }

        LocalDateTime fechaHora = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                .atTime(Integer.parseInt(hora.split(":")[0]), 0);

        Cita cita = new Cita();
        cita.setDoctor(medicoOpt.get());
        cita.setPaciente(pacienteOpt.get());
        cita.setFechaHora(fechaHora);
        cita.setEstado("PENDIENTE");

        service.saveCita(cita);

        redirectAttributes.addFlashAttribute("mensaje", "La cita fue reservada correctamente.");
        return "redirect:/pacientes/citas";
    }

    @GetMapping("/citas")
    public String verCitasPaciente(HttpSession session,
                                   @RequestParam(required = false) String estado,
                                   @RequestParam(required = false) String medico,
                                   Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"PACIENTE".equals(usuario.getRol())) {
            return "redirect:/usuarios/login";
        }

        Optional<Paciente> pacienteOpt = service.findPacienteByUsuario(usuario);
        if (pacienteOpt.isEmpty()) {
            return "redirect:/";
        }

        Paciente paciente = pacienteOpt.get();

        List<Cita> citas = service.buscarCitasPorPaciente(paciente, estado, medico);

        model.addAttribute("paciente", paciente);
        model.addAttribute("citas", citas);
        model.addAttribute("estado", estado);
        model.addAttribute("medico", medico);

        return "/Paciente/historico";
    }

    @GetMapping("/indexPaciente")
    public String mostrarIndexPaciente(HttpSession session,
                                       @RequestParam(required = false) String especialidad,
                                       @RequestParam(required = false) String ciudad,
                                       Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null || !"PACIENTE".equals(usuario.getRol())) {
            return "redirect:/usuarios/login";
        }

        Optional<Paciente> pacienteOpt = service.findPacienteByUsuario(usuario);
        if (pacienteOpt.isEmpty()) {
            return "redirect:/usuarios/login";
        }

        if (especialidad == null) especialidad = "";
        if (ciudad == null) ciudad = "";

        List<MedicoCardDTO> medicos = service.buscarMedicos(especialidad, ciudad);

        model.addAttribute("paciente", pacienteOpt.get());
        model.addAttribute("medicos", medicos);
        model.addAttribute("especialidad", especialidad);
        model.addAttribute("ciudad", ciudad);

        return "Paciente/indexPaciente";
    }

    @GetMapping("/cronograma/{id}")
    public String mostrarCronograma(@PathVariable Integer id, Model model) {
        Optional<Medico> medicoOpt = service.findMedicoById(id);
        if (!medicoOpt.isPresent()) {
            return "redirect:/";
        }
        Medico medico = medicoOpt.get();
        MedicoCardDTO medicoCardDTO = service.convertirAMedicoPerfilDTO(medico);
        model.addAttribute("medico", medicoCardDTO);

        List<Medico> medicos = (List<Medico>) service.findAllMedicos();
        int currentIndex = medicos.indexOf(medico);

        int prevIndex = (currentIndex > 0) ? currentIndex - 1 : medicos.size() - 1;
        int nextIndex = (currentIndex < medicos.size() - 1) ? currentIndex + 1 : 0;

        model.addAttribute("prevId", medicos.get(prevIndex).getId());
        model.addAttribute("nextId", medicos.get(nextIndex).getId());

        return "Paciente/cronograma2";
    }

    @GetMapping("/verNota")
    public String verNotaCita(@RequestParam("id") Integer citaId, Model model) {
        Optional<Cita> cita = service.findCitaById(citaId);
        cita.ifPresent(c -> model.addAttribute("cita", c));
        return "Paciente/nota";
    }

}