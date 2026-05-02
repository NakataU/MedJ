package com.medj.service.impl;

import com.medj.entities.Appointment;
import com.medj.entities.AppointmentDocuments;
import com.medj.entities.Document;
import com.medj.entities.Practitioner;
import com.medj.exception.MedJEntityNotFound;
import com.medj.exception.MedJFileAlreadyExists;
import com.medj.repositories.*;
import com.medj.service.IAppointmentService;
import com.medj.view.inView.AppointmentInView;
import com.medj.view.outView.AppointmentOutView;
import com.medj.view.outView.DocumentListOutView;
import com.medj.view.outView.PractitionerOutView;
import com.medj.view.outView.SpecialtyOutView;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentService implements IAppointmentService {
    Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private IAppointmentRepository appointmentRepository;

    @Autowired
    private IPractitionerRepository practitionerRepository;

    @Autowired
    private IDocumentRepository documentRepository;

    @Autowired
    private ISpecialtyRepository specialtyRepository;

    @Autowired
    private IAppointmentDocumentsRepository appointmentDocumentsRepository;

    @Autowired
    private ModelMapper modelMapper;

    //VERSION WITH SAVING MULTIPLE DOCUMENTS WHILE CREATING APPOINTMENT
 //    @Override
//    public AppointmentOutView addOne(AppointmentInView appointment, MultipartFile[] documents) throws Exception {
//
//        // 1️⃣ Validate practitioner
//        if (appointment.getPractitionerId() != null &&
//                !practitionerRepository.existsById(appointment.getPractitionerId())) {
//            throw new RuntimeException("There is no practitioner with id: " + appointment.getPractitionerId());
//        }
//
//        List<DocumentListOutView> appointmentDocuments = new ArrayList<>();
//        List<Document> savedDocuments = new ArrayList<>();
//
//        // 2️⃣ Process each file
//        if (documents != null) {
//            for (MultipartFile document : documents) {
//                String checksum = generateChecksum(document, "SHA-256");
//
//                if (documentRepository.existsByChecksumAndIsActive(checksum, true)) {
//                    throw new MedJFileAlreadyExists("The file already exists: " + document.getOriginalFilename());
//                }
//
//                // Save file locally
//                String uploadDir = "./uploads";
//                File dir = new File(uploadDir);
//                if (!dir.exists()) dir.mkdirs();
//
//                Path filePath = Paths.get(uploadDir, document.getOriginalFilename());
//                Files.write(filePath, document.getBytes());
//
//                // Save metadata to DB
//                Document doc = new Document();
//                doc.setFileName(document.getOriginalFilename());
//                doc.setContentType(document.getContentType());
//                doc.setPath(filePath.toString());
//                doc.setSize(document.getSize());
//                doc.setChecksum(checksum);
//                doc.setUploadedByUserId(1L); // Replace with actual user ID if available
//                doc.setIsActive(true);
//
//                Document savedDoc = documentRepository.save(doc);
//                savedDocuments.add(savedDoc);
//
//                // Map to output DTO
//                appointmentDocuments.add(modelMapper.map(savedDoc, DocumentListOutView.class));
//            }
//        }
//
//        // 3️⃣ Map appointment and save
//        Appointment mapped = modelMapper.map(appointment, Appointment.class);
//        mapped.setUserId(1L); // Replace with actual user ID
//        mapped.setIsActive(true);
//
//        Appointment savedAppointment = appointmentRepository.save(mapped);
//
//        // 4️⃣ Link documents to appointment
//        for (Document doc : savedDocuments) {
//            AppointmentDocuments appointmentDocument = new AppointmentDocuments();
//            appointmentDocument.setAppointmentId(savedAppointment.getId());
//            appointmentDocument.setDocumentId(doc.getId());
//            appointmentDocument.setIsActive(true);
//
//            appointmentDocumentsRepository.save(appointmentDocument);
//        }
//
//        // 5️⃣ Map final output
//        AppointmentOutView result = modelMapper.map(savedAppointment, AppointmentOutView.class);
//        result.setAppointmentDocuments(appointmentDocuments);
//
//        return result;
//    }


    @Override
    public AppointmentOutView addOne(AppointmentInView appointment) throws Exception {

        // 1️⃣ Validate practitioner
        if (appointment.getPractitionerId() != null &&
                !practitionerRepository.existsById(appointment.getPractitionerId())) {
            throw new RuntimeException("There is no practitioner with id: " + appointment.getPractitionerId());
        }


        // 3️⃣ Map appointment and save
        Appointment mapped = modelMapper.map(appointment, Appointment.class);
        mapped.setUserId(1L); // Replace with actual user ID
        mapped.setIsActive(true);

        Appointment savedAppointment = appointmentRepository.save(mapped);

        for (Long doc : appointment.getDocuments()) {
            AppointmentDocuments appointmentDocument = new AppointmentDocuments();
            appointmentDocument.setAppointmentId(savedAppointment.getId());
            appointmentDocument.setDocumentId(doc);
            appointmentDocument.setIsActive(true);

            appointmentDocumentsRepository.save(appointmentDocument);
        }

        // 5️⃣ Map final output

        return modelMapper.map(savedAppointment, AppointmentOutView.class);
    }

    @Override
    public Page<AppointmentOutView> findAll(Pageable pageable) {

        String logId = UUID.randomUUID().toString();
        log.info("{} : findAll start", logId);

        Page<Appointment> appointments =
                appointmentRepository.findAllAppointmentsByIsActive(pageable);

        return appointments.map(appointment -> {

            AppointmentOutView view =
                    modelMapper.map(appointment, AppointmentOutView.class);

            Practitioner practitioner =
                    practitionerRepository
                            .findPractitionerByIdAndByIsActive(appointment.getPractitionerId());

            if(practitioner != null) {
                PractitionerOutView practitionerOutView =
                        modelMapper.map(practitioner, PractitionerOutView.class);
                view.setPractitioner(practitionerOutView);

            }else{
                throw new MedJEntityNotFound("There is no such practitioner");
            }
                      return view;
        });
    }

    @Override
    public Optional<AppointmentOutView> findById(Long id) {
        return appointmentRepository.findById(id)
                .map(appointment -> {

                    AppointmentOutView appointmentView = modelMapper.map(appointment, AppointmentOutView.class);

                    Optional.ofNullable(appointment.getPractitionerId())
                            .flatMap(practitionerRepository::findById)
                            .ifPresent(practitioner -> {
                                PractitionerOutView practitionerView = modelMapper.map(practitioner, PractitionerOutView.class);

                                Optional.ofNullable(practitioner.getSpecialtyId())
                                        .flatMap(specialtyRepository::findById)
                                        .ifPresent(specialty -> {
                                            SpecialtyOutView specialtyView = modelMapper.map(specialty, SpecialtyOutView.class);
                                            practitionerView.setSpecialty(specialtyView);
                                        });

                                appointmentView.setPractitioner(practitionerView);
                            });

                    return appointmentView;
                });
    }

    @Override
    public AppointmentOutView addPractitionerToAppointment(Long id, Long practitionerId) {
        Appointment appointment = appointmentRepository.findAppointmentByIdAndByIsActive(id);

        if (appointment == null) {
            throw new MedJEntityNotFound("There is no such appointment");
        }

        Practitioner practitioner = practitionerRepository.findPractitionerByIdAndByIsActive(practitionerId);

        if (practitioner == null) {
            throw new MedJEntityNotFound("There is no such practitioner");
        }

        appointment.setPractitionerId(practitionerId);
        appointmentRepository.save(appointment);

        AppointmentOutView result = modelMapper.map(appointment, AppointmentOutView.class);
        PractitionerOutView resultPractitioner = modelMapper.map(practitioner, PractitionerOutView.class);
        result.setPractitioner(resultPractitioner);

        return result;
    }

    @Override
    @Transactional
    public AppointmentOutView updateAppointment(Long id, AppointmentInView newAppointment) {

        Appointment appointment =
                appointmentRepository.findAppointmentByIdAndByIsActive(id);
                        if(appointment == null){
                                throw new MedJEntityNotFound("There is no such appointment");
                        }
        Practitioner practitioner;

        if (!appointment.getPractitionerId().equals(newAppointment.getPractitionerId())) {

            practitioner = practitionerRepository
                    .findPractitionerByIdAndByIsActive(newAppointment.getPractitionerId());
                    if(practitioner == null){
                            throw new MedJEntityNotFound("There is no such practitioner");
                    }
            appointment.setPractitionerId(practitioner.getId());

        } else {
            practitioner = practitionerRepository
                    .findPractitionerByIdAndByIsActive(appointment.getPractitionerId());
            if(practitioner == null){
                throw new MedJEntityNotFound("There is no such practitioner");
            }
        }

        appointment.setName(newAppointment.getName());
        appointment.setPlace(newAppointment.getPlace());
        appointment.setDate(newAppointment.getDate());

        appointmentRepository.save(appointment);

        AppointmentOutView result =
                modelMapper.map(appointment, AppointmentOutView.class);

        PractitionerOutView practitionerView =
                modelMapper.map(practitioner, PractitionerOutView.class);

        result.setPractitioner(practitionerView);

        return result;
    }

    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findAppointmentByIdAndByIsActive(id);

        if(appointment == null){
            throw new MedJEntityNotFound("There is no such appointment");
        }

        appointment.setIsActive(false);

        appointmentRepository.save(appointment);

        List<AppointmentDocuments> appointmentDocuments = appointmentDocumentsRepository.findAppointmentDocumentsByAppointmentIdAndIsActive(id);

        for(AppointmentDocuments appointmentDocument: appointmentDocuments){
            appointmentDocument.setIsActive(false);
            appointmentDocumentsRepository.save(appointmentDocument);
        }
    }


    public static String generateChecksum(MultipartFile file, String algorithm) throws Exception {
        return DocumentService.generateChecksum(file, algorithm);
    }
}
