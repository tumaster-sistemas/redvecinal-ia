package com.redvecinalia.runner;

import com.redvecinalia.model.FAQ;
import com.redvecinalia.model.Tramite;
import com.redvecinalia.repository.FAQRepository;
import com.redvecinalia.repository.TramiteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FAQRepository faqRepository;
    private final TramiteRepository tramiteRepository;

    public DataInitializer(FAQRepository faqRepository, TramiteRepository tramiteRepository) {
        this.faqRepository = faqRepository;
        this.tramiteRepository = tramiteRepository;
    }

    @Override
    public void run(String... args) {
        if (faqRepository.count() > 0) {
            System.out.println("[DataInitializer] FAQ ya poblada (" + faqRepository.count() + " registros). Saltando carga.");
            return;
        }

        System.out.println("[DataInitializer] Poblando 10 FAQs de Villa El Salvador...");

        faqRepository.save(crearFaq("Tramite",
            "Como saco mi certificado de obra en la municipalidad de VES",
            "Acude a la oficina de Tramite Documentario de la Municipalidad de Villa El Salvador (Av. Juan Pablo II cdra. 1) con tu DNI, solicitud simple y recibo de luz. Horario: lun-vie 8:00-13:00 y 14:00-16:30."));

        faqRepository.save(crearFaq("Tramite",
            "Donde pago el impuesto predial",
            "En la caja de la Municipalidad de VES o en agencias del Banco de la Nacion con tu codigo de contribuyente. Tambien online en munives.gob.pe, seccion Tramites en linea, Impuesto predial."));

        faqRepository.save(crearFaq("Salud",
            "Donde esta el centro de salud mas cercano a La Union",
            "El Centro de Salud La Union (VES) esta en la Av. Central cdra. 10 de la AAHH La Union. Atiende lun-sab 7:00-13:00 y 14:00-19:00. Telefono: consultar directorio Minsa."));

        faqRepository.save(crearFaq("Salud",
            "Mi hijo tiene fiebre alta que centro atiende de noche",
            "Acude al servicio de emergencia del hospital mas cercano o llama a SAMU al 106 (linea gratuita, 24 horas). En VES, verifica que puesto de salud tiene turno nocturno."));

        faqRepository.save(crearFaq("Empleo",
            "Como formalizo mi negocio de comida en el parque",
            "Primero obten tu RUC en SUNAT (sunat.gob.pe o agencia VES). Luego solicita la licencia de funcionamiento en la municipalidad con tu RUC, DNI y recibo de luz. La inspeccion tecnica de seguridad es obligatoria para giros de alimentos."));

        faqRepository.save(crearFaq("Empleo",
            "Donde hay cursos tecnicos gratuitos en VES",
            "El CETPRO mas cercano ofrece carreras tecnicas gratuitas. Tambien el programa Jovenes a la Obra del MTPE convoca anualmente en VES. Revisar plataformas.empleo.gob.pe."));

        faqRepository.save(crearFaq("Empleo",
            "Como obtengo RUC",
            "Ingresa a sunat.gob.pe, seccion Tramites, Inscripcion al RUC. Necesitas DNI vigente y correo electronico. El tramite es inmediato y gratuito. Si prefieres atencion presencial, hay una agencia SUNAT en Lima Sur."));

        faqRepository.save(crearFaq("Denuncia",
            "Quiero reportar alumbrado publico malogrado",
            "Llama al 165 (linea gratuita de la municipalidad de VES) o reporta en munives.gob.pe, seccion Servicios, Reporte de alumbrado. Indica la direccion exacta del poste sin luz."));

        faqRepository.save(crearFaq("Denuncia",
            "Vi robos cerca del mercado a quien llamo",
            "Si hay delito en curso, marca 105 (PNP). Para denuncias formales, acude a la comisaria de VES. Tambien puedes contactar al serenazgo de VES para patrullaje inmediato en la zona."));

	faqRepository.save(crearFaq("Denuncia",
    	"Como denuncio violencia familiar",
    	"Si sufres violencia familiar o conoces a alguien en esa situacion, tienes estas opciones:\n\n1. Linea 100 del MIMP (gratis, 24 horas, confidencial): marca 100 desde cualquier telefono.\n2. Comisaria de Villa El Salvador (PNP): para presentar la denuncia formal. Lleva tu DNI.\n3. Centro de Emergencia Mujer (CEM) de Villa El Salvador: atencion psicologica y legal gratuita del MIMP.\n\nSi hay peligro inminente, marca 105 (PNP) o 106 (SAMU) para ayuda inmediata."));

        faqRepository.save(crearFaq("Servicios",
            "No tengo agua hace 3 dias en la AAHH Union como reporto a Sedapal",
            "Llama al 119 (atencion Sedapal 24 horas) o reporta en sedapal.com.pe, seccion Reporte de averias. Indica tu direccion exacta y el tiempo sin servicio."));

        System.out.println("[DataInitializer] 10 FAQs cargadas correctamente.");

        if (tramiteRepository.count() == 0) {
            System.out.println("[DataInitializer] Poblando tramites...");

            tramiteRepository.save(crearTramite("Tramite",
                "Certificado de obra",
                "Documento que acredita la finalizacion de una obra constructiva.",
                "1. Reune DNI, solicitud simple y recibo de luz.\n2. Acude a Tramite Documentario de la Municipalidad de VES (Av. Juan Pablo II cdra. 1).\n3. Paga la tasa correspondiente.\n4. Recoge el certificado en el plazo indicado.",
                "https://munives.gob.pe"));

            tramiteRepository.save(crearTramite("Tramite",
                "Impuesto predial",
                "Tributo municipal anual sobre la propiedad de predios urbanos y rusticos.",
                "1. Obten tu codigo de contribuyente en la municipalidad.\n2. Paga en la caja de la Municipalidad de VES o en el Banco de la Nacion.\n3. Tambien puedes pagar online en munives.gob.pe.",
                "https://munives.gob.pe"));

            tramiteRepository.save(crearTramite("Empleo",
                "Formalizacion de negocio de alimentos",
                "Tramite para obtener licencia de funcionamiento de un negocio de comida.",
                "1. Obten tu RUC en SUNAT (sunat.gob.pe).\n2. Solicita licencia de funcionamiento en la Municipalidad de VES con RUC, DNI y recibo de luz.\n3. Aprueba la inspeccion tecnica de seguridad (obligatoria para alimentos).",
                "https://sunat.gob.pe"));

            tramiteRepository.save(crearTramite("Empleo",
                "Inscripcion al RUC",
                "Registro Unico de Contribuyentes, necesario para cualquier actividad economica formal.",
                "1. Ingresa a sunat.gob.pe, seccion Tramites, Inscripcion al RUC.\n2. Ten listo tu DNI vigente y correo electronico.\n3. El tramite es inmediato y gratuito.\n4. Alternativamente, acude a la agencia SUNAT en Lima Sur.",
                "https://sunat.gob.pe"));

            tramiteRepository.save(crearTramite("Empleo",
                "Cursos tecnicos gratuitos (CETPRO / Jovenes a la Obra)",
                "Programas de formacion tecnica gratuita para VES.",
                "1. Acude al CETPRO mas cercano para carreras tecnicas.\n2. Revisa la convocatoria anual de Jovenes a la Obra del MTPE.\n3. Postula en plataformas.empleo.gob.pe.",
                "https://plataformas.empleo.gob.pe"));

            System.out.println("[DataInitializer] 5 tramites cargados correctamente.");
        }

        System.out.println("[DataInitializer] Carga inicial completa.");
    }

    private FAQ crearFaq(String categoria, String pregunta, String respuesta) {
        FAQ faq = new FAQ();
        faq.setCategoria(categoria);
        faq.setPregunta(pregunta);
        faq.setRespuesta(respuesta);
        return faq;
    }

    private Tramite crearTramite(String categoria, String titulo, String descripcion, String pasos, String url) {
        Tramite t = new Tramite();
        t.setCategoria(categoria);
        t.setTitulo(titulo);
        t.setDescripcion(descripcion);
        t.setPasos(pasos);
        t.setUrl(url);
        return t;
    }
}