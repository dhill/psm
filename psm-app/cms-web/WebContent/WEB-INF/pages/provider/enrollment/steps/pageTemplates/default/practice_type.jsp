<%--
    JSP Fragment for provider type selection form.

    @author j3_guile
    @version 1.0
 --%>

<%@page import="gov.medicaid.binders.ProviderTypeFormBinder"%>
<%@page import="gov.medicaid.entities.dto.ViewStatics"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<c:set var="formIdPrefix" value="practice_type"></c:set>

<div class="radioPanel">
    <div class="section">
        <input type="hidden" name="formNames" value="<%= ViewStatics.PRACTICE_TYPE_FORM %>">
        <div id="RadioWrapper" class="row">
            <label>Do you maintain your own private practice?</label>
            <div>
                <c:set var="formName" value="_04_maintainsOwnPrivatePractice"></c:set>
                <c:set var="formValue" value="${requestScope[formName]}"></c:set>
                <c:url var="rebindUrl" value="/provider/enrollment/steps/rebind" />
                <label class="span">
                  <input onchange="javascript:submitFormById('enrollmentForm', '${rebindUrl}')" type="radio" value="Y" name="${formName}" ${formValue eq 'Y' ? 'checked' : ''}>
                  Yes
                </label>
                <label class="span">
                  <input onchange="javascript:submitFormById('enrollmentForm', '${rebindUrl}')" type="radio" value="N" name="${formName}" ${formValue eq 'N' ? 'checked' : ''}>
                  No
                </label>
            </div>
        </div>
        <div id="switchRadioWrapper" class="row">
            <label>Are you employed and/or independently contracted by a group practice?</label>
            <div>
                <c:set var="formName" value="_04_employedOrContractedByGroup"></c:set>
                <c:set var="formValue" value="${requestScope[formName]}"></c:set>
                <label class="span">
                  <input onchange="javascript:submitFormById('enrollmentForm', '${rebindUrl}')" type="radio" value="Y" name="${formName}" ${formValue eq 'Y' ? 'checked' : ''}>
                  Yes
                </label>
                <label class="span">
                  <input onchange="javascript:submitFormById('enrollmentForm', '${rebindUrl}')" type="radio" value="N" name="${formName}" ${formValue eq 'N' ? 'checked' : ''}>
                  No
                </label>
            </div>
        </div>
        <div class="clearFixed"></div>
    </div>
    <div class="tl"></div>
    <div class="tr"></div>
    <div class="bl"></div>
    <div class="br"></div>
</div>
