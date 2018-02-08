package gov.medicaid.api;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import gov.medicaid.services.ProviderEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "PSM FHIR Server")
public class PsmApiServlet extends RestfulServer {

    private static final long serialVersionUID = 1L;

    @Autowired
    private ProviderEnrollmentService providerEnrollmentService;

    /**
     * The initialize method is automatically called when the servlet is starting up, so it can
     * be used to configure the servlet to define resource providers, or set up
     * configuration, interceptors, etc.
     */
    @Override
    protected void initialize() throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(
                this,
                getServletContext()
        );
        if (providerEnrollmentService == null) {
            throw new ServletException("Not initialized!");
        }

        this.setFhirContext(FhirContext.forDstu3());
        this.setDefaultPrettyPrint(true);
        this.setDefaultResponseEncoding(EncodingEnum.JSON);

        List<IResourceProvider> resourceProviders = new ArrayList<>();
        resourceProviders.add(new TaskResourceProvider(providerEnrollmentService));
        setResourceProviders(resourceProviders);
    }

    public void setProviderEnrollmentService(ProviderEnrollmentService providerEnrollmentService) {
        this.providerEnrollmentService = providerEnrollmentService;
    }
}
