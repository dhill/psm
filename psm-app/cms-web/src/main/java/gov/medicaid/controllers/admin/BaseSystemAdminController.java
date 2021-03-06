/*
 * Copyright 2012-2013 TopCoder, Inc.
 *
 * This code was developed under U.S. government contract NNH10CD71C.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gov.medicaid.controllers.admin;

import java.util.logging.Logger;
import gov.medicaid.interceptors.HandlebarsInterceptor;
import gov.medicaid.services.LookupService;
import gov.medicaid.services.RegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * A base controller class that other classes will extend that provides logging, auditing, and additional services.
 *
 * <p>
 * <b>Thread Safety</b> This class is mutable and not thread safe, but used in thread safe manner by framework.
 * </p>
 *
 * @author argolite, TCSASSEMBLER
 * @version 1.0
 */
@Controller
public abstract class BaseSystemAdminController {

    /**
     * It will be used to log all errors.
     *
     * It is injected by the container, may have any value, is fully mutable, but not expected to change after
     * dependency injection.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Registration service.
     */
    private RegistrationService registrationService;

    /**
     * Lookup service.
     */
    private LookupService lookupService;


    /**
     * Empty constructor.
     */
    protected BaseSystemAdminController() {
    }

    /**
     * Directs all exceptions encountered by subclasses to a generic error page.
     *
     * @param request the request that resulted in an exception
     * @param ex      the exception encountered
     * @return the error view name
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(
            HttpServletRequest request,
            Exception ex
    ) {
        ModelAndView view = new ModelAndView("error");
        view.addObject("exception", ex);
        HandlebarsInterceptor.addCommonVariables(request, view);
        return view;
    }

    /**
     * Ensure the object is properly initialized
     */
    protected void init() {}

    /**
     * Gets the value of the field <code>logger</code>.
     *
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Gets the value of the field <code>registrationService</code>.
     * @return the registrationService
     */
    public RegistrationService getRegistrationService() {
        return registrationService;
    }

    /**
     * Sets the value of the field <code>registrationService</code>.
     * @param registrationService the registrationService to set
     */
    public void setRegistrationService(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * Gets the value of the field <code>lookupService</code>.
     * @return the lookupService
     */
    public LookupService getLookupService() {
        return lookupService;
    }

    /**
     * Sets the value of the field <code>lookupService</code>.
     * @param lookupService the lookupService to set
     */
    public void setLookupService(LookupService lookupService) {
        this.lookupService = lookupService;
    }
}
