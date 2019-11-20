package ro.pss.holidayforms.pdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.*;

import static java.util.stream.Collectors.*;

@Slf4j
public class PDFGenerator {

    public static PDDocument fillHolidayRequest(HolidayRequest holidayRequest) {
        PDDocument pdfDocument = null;

        if (holidayRequest == null) {
            return null;
        }

        try {
            String inputTemplate = "src/main/resources/static/pdf/PSSHolidayRequestForm.pdf";
            //String output = "src/main/resources/static/pdf/"
            //        + holidayRequest.getType() + "_" + user.getName() + "_" + holidayRequest.getDateFrom() + ".pdf";

            pdfDocument = PDDocument.load(new File(inputTemplate));
            PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

            if (acroForm != null) {
                PDTextField field = (PDTextField) acroForm.getField("Requester");
                field.setValue(holidayRequest.getRequester().getName());
                field = (PDTextField) acroForm.getField("Department");
                field.setValue(holidayRequest.getRequester().getDepartment());
                field = (PDTextField) acroForm.getField("DaysNo");
                field.setValue(holidayRequest.getNumberOfDays() + "");
                field = (PDTextField) acroForm.getField("Type");
                field.setValue(holidayRequest.getType().name());
                field = (PDTextField) acroForm.getField("DateFrom");
                field.setValue(holidayRequest.getDateFrom().toString());
                field = (PDTextField) acroForm.getField("Replacer");
                field.setValue(holidayRequest.getSubstitutionRequests().stream().map(e -> e.getSubstitute().getName()).collect(Collectors.joining(", ")));
                field = (PDTextField) acroForm.getField("Date");
                field.setValue(holidayRequest.getCreationDate().toString());
                field = (PDTextField) acroForm.getField("Approver1");
                List<User> approvers = new ArrayList<>(holidayRequest.getApprovalRequests().stream().map(a -> a.getApprover()).collect(toList()));
                field.setValue(approvers.get(0).getName());
                field = (PDTextField) acroForm.getField("Approver2");
                field.setValue(approvers.get(1).getName());
                pdfDocument.getDocumentCatalog().getAcroForm().flatten();
            }

        } catch (IOException e) {
            log.error("Error while creating PDF", e);
        }
        return pdfDocument;
    }
}
