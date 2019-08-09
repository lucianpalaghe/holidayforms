package ro.pss.holidayforms.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.User;

import java.io.File;
import java.io.IOException;

public class PDFGenerator {

    public static PDDocument fillHolidayRequest(HolidayRequest holidayRequest, User user) throws IOException {

        PDDocument pdfDocument = null;

        if (holidayRequest == null || user == null)
            return null;

        try{
            String inputTemplate = "src/main/resources/static/pdf/PSSHolidayRequestForm.pdf";
            String output = "src/main/resources/static/pdf/"
                    + holidayRequest.getType() + "_" + user.getName() + "_" + holidayRequest.getDateFrom() + ".pdf";

            pdfDocument = PDDocument.load(new File(inputTemplate));
            PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

            if (acroForm != null) {
                PDTextField field = (PDTextField) acroForm.getField("Requester");
                field.setValue(user.getName());
                field = (PDTextField) acroForm.getField("Department");
                field.setValue(user.getDepartment());
                field = (PDTextField) acroForm.getField("DaysNo");
                field.setValue(holidayRequest.getNumberOfDays() + "");
                field = (PDTextField) acroForm.getField("Type");
                field.setValue(holidayRequest.getType().name());
                field = (PDTextField) acroForm.getField("DateFrom");
                field.setValue(holidayRequest.getDateFrom().toString());
                field = (PDTextField) acroForm.getField("Replacer");
                field.setValue(holidayRequest.getSubstitute().getName());
                field = (PDTextField) acroForm.getField("Date");
                field.setValue(holidayRequest.getCreationDate().toString());
                field = (PDTextField) acroForm.getField("Approver1");
                field.setValue(holidayRequest.getApprovalRequests().get(0).getApprover().getName());
                field = (PDTextField) acroForm.getField("Approver2");
                field.setValue(holidayRequest.getApprovalRequests().get(1).getApprover().getName());
            }

            pdfDocument.save(output);

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (pdfDocument != null)
                pdfDocument.close();
        }

        return pdfDocument;
    }
}
