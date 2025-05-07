package org.example.sistema_citas.presentation;

import jakarta.servlet.http.HttpSession;
import org.example.sistema_citas.dto.MedicoCardDTO;
import org.example.sistema_citas.logic.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final Service service;

    public UsuarioController(Service service) {
        this.service = service;
    }

    @GetMapping("/list")
    public Iterable<Usuario> getAllUsuarios() {
        return service.findAllUsuarios();
    }

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(required = false) String previourl,
                               @RequestParam(required = false) Integer medicoId,
                               @RequestParam(required = false) String fecha,
                               @RequestParam(required = false) String hora,
                               HttpSession session) {

        if (previourl != null) {
            session.setAttribute("previourl", previourl);
            session.setAttribute("medicoId", medicoId);
            session.setAttribute("fecha", fecha);
            session.setAttribute("hora", hora);
        }
        return "Login/View";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/register")
    public String mostrarRegistro() {return "Login/RegisterType";}

    @GetMapping("/doctor")
    public String mostrarDoctor() {return "Medico/registro";}

    @PostMapping("/doctorAdd")
    public String registrarDoctor(@ModelAttribute Medico medico,
                                  @ModelAttribute Usuario usuario,
                                  RedirectAttributes redirectAttributes) {
        if (service.existeUsuarioConNombre(usuario.getNombre())) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya está en uso.");
            return "redirect:/usuarios/doctor";
        }

        usuario.setRol("MEDICO");
        medico.setUsuario(usuario);
        medico.setEstado("PENDIENTE");
        service.saveMedico(medico);
        redirectAttributes.addFlashAttribute("esperaAprobacion", true);
        return "redirect:/";
    }

    @GetMapping("/patient")
    public String mostrarPaciente() {return "Paciente/registro";}

    @PostMapping("/patientAdd")
    public String registrarPaciente(@ModelAttribute Paciente paciente,
                                    @ModelAttribute Usuario usuario,
                                    RedirectAttributes redirectAttributes) {
        if (service.existeUsuarioConNombre(usuario.getNombre())) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya está en uso.");
            return "redirect:/usuarios/patient";
        }

        usuario.setRol("PACIENTE");
        paciente.setUsuario(usuario);
        service.savePaciente(paciente);
        return "redirect:/usuarios/login";
    }

    @GetMapping("/search")
    public String mostrarSearch(){return "index";}

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Integer id) {
        return service.findUsuarioById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Usuario createUsuario(@RequestBody Usuario usuario) {
        return service.saveUsuario(usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Integer id) {
        service.deleteUsuarioById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/loginAccept")
    public String login(@RequestParam String user_id,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        Usuario usuario = service.findUsuarioByNombreAndClave(user_id, password);

        if (usuario != null) {
            session.setAttribute("usuario", usuario);

            switch (usuario.getRol()) {
                case "ADMINISTRADOR":
                    return "redirect:/admin/doctores";
                case "MEDICO":
                    Optional<Medico> medicoOpt = service.findMedicoByUsuario(usuario);
                    if (medicoOpt.isPresent()) {
                        Medico medico = medicoOpt.get();
                        if (!"APROBADO".equals(medico.getEstado())) {
                            model.addAttribute("error", "Su cuenta de médico aún no ha sido aprobada.");
                            return "Login/View";
                        }
                        session.setAttribute("medico", medico);
                        return "redirect:/medicos/show";
                    }
                    break;
                case "PACIENTE":
                    String previourl = (String) session.getAttribute("previourl");

                    if (previourl != null && previourl.equals("/pacientes/confirmarcita")) {
                        Integer medicoId = (Integer) session.getAttribute("medicoId");
                        String fecha = (String) session.getAttribute("fecha");
                        String hora = (String) session.getAttribute("hora");

                        session.removeAttribute("previourl");
                        session.removeAttribute("medicoId");
                        session.removeAttribute("fecha");
                        session.removeAttribute("hora");

                        return "redirect:/pacientes/confirmarcita?medicoId=" + medicoId + "&fecha=" + fecha + "&hora=" + hora;
                    }
                    if (previourl != null && previourl.equals("/medicos/confirmarcita")) {
                        Integer medicoId = (Integer) session.getAttribute("medicoId");
                        String fecha = (String) session.getAttribute("fecha");
                        String hora = (String) session.getAttribute("hora");

                        session.removeAttribute("previourl");
                        session.removeAttribute("medicoId");
                        session.removeAttribute("fecha");
                        session.removeAttribute("hora");

                        return "redirect:/pacientes/confirmarcita?medicoId=" + medicoId + "&fecha=" + fecha + "&hora=" + hora;
                    }

                    return "redirect:/pacientes/indexPaciente";
            }
        }
        model.addAttribute("error", "Credenciales incorrectas");
        return "Login/View";
    }
}


