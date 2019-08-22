package ro.pss.holidayforms.excel;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.UserRepository;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.dashboard.DashboardData;
import ro.pss.holidayforms.service.DashboardService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@NoArgsConstructor
@AllArgsConstructor
public class ExcelExporter {
    @Autowired
    private DashboardService dataService;
    @Autowired
    private UserRepository userRepository;

    public byte[] doGetExcelByteArray() {
        Workbook workbook = new XSSFWorkbook();
        populateWorkbookWithData(workbook);
        return getByteArrayFromWorkbook(workbook);
    }

    private void populateWorkbookWithData(Workbook workbook) {
        try {
            Sheet sheet = workbook.createSheet(MessageRetriever.get("sheetName"));
            int startRow = 1;

            // set the column names
            Row row = sheet.createRow(0);
            Cell cell = null;
            String[] sheetColNames = MessageRetriever.get("sheetColNames").split(",");
            int i;
            for (i = 0; i < sheetColNames.length; i++) {
                cell = row.createCell(i);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(sheetColNames[i]));
                cell.setCellStyle(getCellStyleForColumnNames(workbook));
            }

            String[] monthsColNames = MessageRetriever.get("monthsNamesShort").split(",");
            int cnt = i;
            for (int j = 0; j < monthsColNames.length; j++) {
                //planning
                cell = row.createCell(cnt++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(monthsColNames[j]) + " " + MessageRetriever.get("sheetPlanning"));
                cell.setCellStyle(getCellStyleForColumnNames(workbook));
                //request
                cell = row.createCell(cnt++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(monthsColNames[j]) + " " + MessageRetriever.get("sheetRequest"));
                cell.setCellStyle(getCellStyleForColumnNames(workbook));
            }
            cell = row.createCell(cnt++);
            cell.setCellValue(workbook.getCreationHelper().createRichTextString(MessageRetriever.get("sheetColTotalControl") + " " + MessageRetriever.get("sheetPlanning")));
            cell.setCellStyle(getCellStyleForColumnNames(workbook));
            cell = row.createCell(cnt++);
            cell.setCellValue(workbook.getCreationHelper().createRichTextString(MessageRetriever.get("sheetColTotalControl") + " " + MessageRetriever.get("sheetRequest")));
            cell.setCellStyle(getCellStyleForColumnNames(workbook));
            cell = row.createCell(cnt);
            cell.setCellValue(workbook.getCreationHelper().createRichTextString(MessageRetriever.get("sheetColNotUsedDays")));
            cell.setCellStyle(getCellStyleForColumnNames(workbook));

            // populate with data
            List<User> users = userRepository.findAll();
            for (int u = 0; u < users.size(); u++) {
                int col = 0;
                User user = users.get(u);
                row = sheet.createRow(u + startRow);
                cell = row.createCell(col++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString((u + startRow) + ""));
                cell = row.createCell(col++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(user.getName()));
                cell = row.createCell(col++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(user.getAvailableVacationDays() + ""));

                DashboardData data = dataService.getDashboardData(user.getEmail());
                for (int m = 0; m < MessageRetriever.get("monthsNamesShort").split(",").length; m++) {
                    cell = row.createCell(col++);
                    cell.setCellValue(workbook.getCreationHelper().createRichTextString(data.getChartPlannedDays()[m] == 0 ? "" : data.getChartPlannedDays()[m] + ""));
                    cell = row.createCell(col++);
                    cell.setCellValue(workbook.getCreationHelper().createRichTextString(data.getChartVacationDays()[m] == 0 ? "" : data.getChartVacationDays()[m] + ""));
                }
                int plannedDays = IntStream.of(data.getChartPlannedDays()).sum();
                int requestDays = IntStream.of(data.getChartVacationDays()).sum();
                cell = row.createCell(col++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(plannedDays + ""));
                cell = row.createCell(col++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(requestDays + ""));
                cell = row.createCell(col);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString((user.getAvailableVacationDays() - requestDays) + ""));
            }
            autosizeAllColumns(sheet);
         //   sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 30));
            sheet.createFreezePane(0, 1);
            log.info("excel file exported succesfully");
        } catch (Exception e) {
            log.error("error making excel", e);
        }

    }

    private static void autosizeAllColumns(Sheet sheet) {
        for (int i = 0; i < sheet.getLastRowNum(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static byte[] getByteArrayFromWorkbook(Workbook workbook) {
        ByteArrayOutputStream wbAttach = new ByteArrayOutputStream();
        try {
            workbook.write(wbAttach);
        } catch (IOException e) {
            log.error("Error creating export excel", e);
        } finally {
            try {
                wbAttach.close();
            } catch (IOException e) {
                log.error("Error creating export excel", e);
            }
        }
        return wbAttach.toByteArray();
    }

    private static CellStyle getCellStyleForColumnNames(Workbook workbook) {
        XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
        XSSFCellStyle style = xssfWorkbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = xssfWorkbook.createFont();
        font.setFontHeight(13);
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void writeFile(Workbook workbook, File file) {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
        } catch (IOException e) {
            log.error("Error writing file", e);
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (Exception e) {
                    log.error("Error writing file", e);
                }
            }
        }
    }

}
