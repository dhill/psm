package gov.medicaid.api;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import gov.medicaid.entities.CMSUser;
import gov.medicaid.entities.Enrollment;
import gov.medicaid.entities.EnrollmentStatus;
import gov.medicaid.entities.Entity;
import gov.medicaid.entities.Organization;
import gov.medicaid.entities.Person;
import gov.medicaid.entities.Role;
import gov.medicaid.entities.dto.ViewStatics;
import gov.medicaid.services.PortalServiceException;
import gov.medicaid.services.ProviderEnrollmentService;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Task;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class TaskResourceProvider implements IResourceProvider {
    private final ProviderEnrollmentService providerEnrollmentService;

    public TaskResourceProvider(ProviderEnrollmentService providerEnrollmentService) {
        this.providerEnrollmentService = providerEnrollmentService;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Task.class;
    }

    @Read
    public Task getResourceById(@IdParam IdType id) throws PortalServiceException {
        Long enrollmentId = id.getIdPartAsLong();
        CMSUser systemUser = new CMSUser();
        Role role = new Role();
        role.setDescription(ViewStatics.ROLE_SYSTEM_ADMINISTRATOR);
        systemUser.setRole(role);
        Enrollment enrollment = providerEnrollmentService.getTicketDetails(systemUser, enrollmentId);
        if (enrollment == null) {
            return null;
        }

        return enrollmentToTask(enrollment);
    }

    private Task enrollmentToTask(Enrollment enrollment) {
        DomainResource requester = getRequester(enrollment);

        Task task = new Task();
        task.addIdentifier(getIdentifier(enrollment));
        task.setStatus(getStatus(enrollment));
        task.setIntent(Task.TaskIntent.PROPOSAL);
        task.setRequester(new Task.TaskRequesterComponent(new Reference(requester)));
        task.addContained(requester);

        return task;
    }

    private DomainResource getRequester(Enrollment enrollment) {
        Entity entity = enrollment.getDetails().getEntity();
        if (entity instanceof Person) {
            return personToPractitioner((Person) entity);
        } else if (entity instanceof Organization) {
            return organizationToFhirOrganization((Organization) entity);
        }

        return null;
    }

    private org.hl7.fhir.dstu3.model.Organization organizationToFhirOrganization(
            Organization organization
    ) {
        return null;
    }

    private Practitioner personToPractitioner(Person person) {
        Practitioner practitioner = new Practitioner();
        practitioner.setId("#" + Long.toString(person.getId()));
        if (person.getSsn() != null) {
            practitioner.addIdentifier(new Identifier()
                    .setSystem("http://hl7.org/fhir/sid/us-ssn")
                    .setValue(person.getSsn())
            );
        }
        practitioner.addIdentifier(npi(person));
        practitioner.addName(new HumanName()
                .addPrefix(person.getPrefix())
                .addGiven(person.getFirstName())
                .addGiven(person.getMiddleName())
                .setFamily(person.getLastName())
                .addSuffix(person.getSuffix())
        );
        practitioner.addAddress(
                address(person.getContactInformation().getAddress())
        );

        return practitioner;
    }

    private Address address(gov.medicaid.entities.Address address) {
        if (address == null) {
            return null;
        } else {
            return new Address()
                    .addLine(address.getAttentionTo())
                    .addLine(address.getLine1())
                    .addLine(address.getLine2())
                    .setCity(address.getCity())
                    .setState(address.getState())
                    .setPostalCode(address.getZipcode());
        }
    }

    private Identifier npi(Entity entity) {
        if (entity.getNpi() == null) {
            return null;
        } else {
            return new Identifier()
                    .setSystem("http://hl7.org/fhir/sid/us-npi")
                    .setValue(entity.getNpi());
        }
    }

    private Task.TaskStatus getStatus(Enrollment enrollment) {
        EnrollmentStatus enrollmentStatus = enrollment.getStatus();
        if (enrollmentStatus == null) {
            return Task.TaskStatus.NULL;
        }

        if ("Draft".equals(enrollmentStatus.getDescription())) {
            return Task.TaskStatus.DRAFT;
        } else if ("Pending".equals(enrollmentStatus.getDescription())) {
            return Task.TaskStatus.REQUESTED;
        } else if ("Rejected".equals(enrollmentStatus.getDescription())) {
            return Task.TaskStatus.REJECTED;
        } else if ("Approved".equals(enrollmentStatus.getDescription())) {
            return Task.TaskStatus.ACCEPTED;
        } else {
            return Task.TaskStatus.NULL;
        }
    }

    private Identifier getIdentifier(Enrollment enrollment) {
        Identifier identifier = new Identifier();
        identifier.setValue(Long.toString(enrollment.getTicketId()));
        return identifier;
    }
}
