package org.example.sistema_citas.presentation;

import jakarta.servlet.http.HttpSession;
import org.example.sistema_citas.dto.HorarioDTO;
import org.example.sistema_citas.dto.MedicoCardDTO;
import org.example.sistema_citas.logic.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/medicos")
public class MedicoController {
    private final Service service;

    public MedicoController(Service service) {
        this.service = service;
    }

    @GetMapping
    public Iterable<Medico> getAllMedicos() {
        return service.findMedicosByEstado("APROBADO");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medico> getMedicoById(@PathVariable Integer id) {
        return service.findMedicoById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Medico createMedico(@RequestBody Medico medico) {
        return service.saveMedico(medico);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedico(@PathVariable Integer id) {
        service.deleteMedicoById(id);
        return ResponseEntity.noContent().build();
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

        List<Medico> medicos = (List<Medico>) service.findAllMedicoAprobados();
        int currentIndex = medicos.indexOf(medico);

        int prevIndex = (currentIndex > 0) ? currentIndex - 1 : medicos.size() - 1;
        int nextIndex = (currentIndex < medicos.size() - 1) ? currentIndex + 1 : 0;

        model.addAttribute("prevId", medicos.get(prevIndex).getId());
        model.addAttribute("nextId", medicos.get(nextIndex).getId());

        return "Medico/cronograma";
    }

    @GetMapping("/prev/{id}")
    public String medicoPrevio(@PathVariable("id") Integer id, Model model) {
        Optional<Medico> prevId = service.findMedicoById(id);
        return "redirect:/medico/cronograma/" + prevId;
    }

    @GetMapping("/next/{id}")
    public String medicoSiguiente(@PathVariable("id") Integer id, Model model) {
        Optional<Medico> nextId = service.findMedicoById(id);
        return "redirect:/medico/cronograma/" + nextId;
    }

    @GetMapping("/show")
    public String verCitasMedico(Model model, HttpSession session, @RequestParam(required = false) String estado, @RequestParam(required = false) String paciente) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null || !"MEDICO".equals(usuario.getRol())) {
            return "redirect:/usuarios/login";
        }

        Medico medico = service.findMedicoById(usuario.getId()).orElse(null);
        List<Cita> citas = service.buscarCitasPorMedico(medico, estado, paciente);
        model.addAttribute("doctor", medico);
        model.addAttribute("citas", citas);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("pacienteSeleccionado", paciente);

        return "Medico/citas";
    }

    @GetMapping("/perfil")
    public String perfilMedico(Model model, HttpSession session) {
        Medico medicoSession = (Medico) session.getAttribute("medico");

        if (medicoSession == null) {
            return "redirect:/usuarios/login";
        }

        Medico medico = service.findMedicoById(medicoSession.getId()).orElse(null);

        if (medico != null) {
            MedicoCardDTO perfilDTO = service.convertirAMedicoPerfilDTO(medico);
            model.addAttribute("perfil", perfilDTO);
        }

        return "Medico/perfil";
    }

    @GetMapping("/settings")
    public String editarPerfilMedico(Model model, HttpSession session) {
        Medico medicoSession = (Medico) session.getAttribute("medico");

        if (medicoSession == null) {
            return "redirect:/usuarios/login";
        }

        Medico medico = service.findMedicoById(medicoSession.getId()).orElse(null);

        if (medico != null) {
            MedicoCardDTO perfilDTO = service.convertirAMedicoPerfilDTO(medico);
            model.addAttribute("perfil", perfilDTO);
        }
        model.addAttribute("diasSemana", List.of("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"));
        return "Medico/settings";
    }

    @PostMapping("/editar")
    public String actualizarPerfil(@ModelAttribute("perfil") MedicoCardDTO dto,
                                   @RequestParam("fotoPerfil") MultipartFile archivoImagen,
                                   HttpSession session) {

        Medico medicoSession = (Medico) session.getAttribute("medico");
        Medico medico = service.findMedicoByIdConHorarios(medicoSession.getId());

        if (medico != null) {
            medico.getUsuario().setNombre(dto.getNombre());
            medico.setEspecialidad(dto.getEspecialidad());
            medico.setCiudad(dto.getCiudad());
            medico.setInstalacion(dto.getInstalacion());
            medico.setCostoConsulta(dto.getCostoConsulta());
            medico.setFrecuencia(dto.getFrecuencia());

            if (!archivoImagen.isEmpty()) {
                try {
                    String nombreArchivo = UUID.randomUUID() + "_" + archivoImagen.getOriginalFilename();
                    String ruta = "C:/sistema-citas/perfiles";

                    File carpeta = new File(ruta);
                    if (!carpeta.exists()) {
                        carpeta.mkdirs();
                    }

                    Path rutaCompleta = Paths.get(ruta, nombreArchivo);
                    Files.write(rutaCompleta, archivoImagen.getBytes());

                    medico.setImagen(nombreArchivo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            service.eliminarHorariosByDoctor(medico);
            medico.getHorarios().clear();

            for (HorarioDTO horarioDTO : dto.getHorarios()) {
                String horaInicioStr = horarioDTO.getHoraInicio();
                String horaFinStr = horarioDTO.getHoraFin();

                if (horaInicioStr != null && !horaInicioStr.isBlank()
                        && horaFinStr != null && !horaFinStr.isBlank()) {

                    int horaInicio = Integer.parseInt(horaInicioStr.split(":")[0]);
                    int horaFin = Integer.parseInt(horaFinStr.split(":")[0]);

                    if (horaInicio >= 6 && horaFin <= 22 && horaInicio < horaFin) {
                        Horario h = new Horario();
                        h.setDia(convertirDiaAEntero(horarioDTO.getDiaSemana()));
                        h.setHorainicio(horaInicio);
                        h.setHorafin(horaFin);
                        h.setDoctor(medico);
                        medico.getHorarios().add(h);
                    }
                }
            }
            service.saveMedico(medico);
            session.setAttribute("medico", medico);
        }

        return "redirect:/medicos/perfil";
    }



    private int convertirDiaAEntero(String diaSemana) {
        return switch (diaSemana) {
            case "Lunes" -> 1;
            case "Martes" -> 2;
            case "Miércoles" -> 3;
            case "Jueves" -> 4;
            case "Viernes" -> 5;
            case "Sábado" -> 6;
            case "Domingo" -> 7;
            default -> 0;
        };
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

    @GetMapping("/verNota")
    public String verNotaCita(@RequestParam Integer id, Model model) {
        Optional<Cita> cita = service.findCitaById(id);
        cita.ifPresent(c -> model.addAttribute("cita", c));
        return "Medico/nota";
    }

    @GetMapping("/confirmar")
    public String mostrarFormularioConfirmar(@RequestParam Integer id, Model model) {
        Optional<Cita> cita = service.findCitaById(id);
        cita.ifPresent(c -> model.addAttribute("cita", c));
        return "Medico/CitaAtendida";
    }

    @PostMapping("/confirmar")
    public String confirmarCita(@RequestParam("citaId") Integer id,
                                @RequestParam("notas") String notas,
                                RedirectAttributes redirectAttrs) {
        Cita cita = service.findCitaById(id).orElse(null);
        if (cita != null) {
            cita.setNotas(notas);
            cita.setEstado("CONFIRMADA");
            service.saveCita(cita);
            redirectAttrs.addFlashAttribute("mensaje", "Cita confirmada y notas guardadas.");
        }
        return "redirect:/medicos/show";
    }

    @GetMapping("/cancelar")
    public String cancelarCita(@RequestParam("citaId") Integer citaId, RedirectAttributes redirectAttributes) {
        Optional<Cita> opt = service.findCitaById(citaId);
        if (opt.isPresent()) {
            Cita cita = opt.get();
            cita.setEstado("CANCELADA");
            service.saveCita(cita);
            redirectAttributes.addFlashAttribute("mensaje", "La cita ha sido cancelada exitosamente.");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "No se encontró la cita a cancelar.");
        }
        return "redirect:/medicos/show";
    }

}
