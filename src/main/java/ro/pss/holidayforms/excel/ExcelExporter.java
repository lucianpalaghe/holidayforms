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

    private static int COLUMN_NAME_FONT_HEIGHT = 14;
    private static boolean COLUMN_NAME_FONT_BOLD = true;
    private static int CONTENT_FONT_HEIGHT = 12;
    private static boolean CONTENT_FONT_BOLD = false;
    private static int COLUMN_NAME_WIDTH_CHAR = 30;
    private static int COLUMNS_WIDTH_CHAR = 15;

    public byte[] doGetExcelByteArray(int startMonth, int endMonth) {
        Workbook workbook = new XSSFWorkbook();
        populateWorkbookWithData(workbook, startMonth, endMonth);
        return getByteArrayFromWorkbook(workbook);
    }

    private void populateWorkbookWithData(Workbook workbook, int startMonth, int endMonth) {
        try {
            Sheet sheet = workbook.createSheet(MessageRetriever.get("sheetName"));
            sheet.setDefaultColumnWidth(15); // characters
            int startRow = 1;

            // set the column names
            Row row = sheet.createRow(0);
            row.setHeight((short) (4 * sheet.getDefaultRowHeight()));
            Cell cell = null;
            String[] sheetColNames = MessageRetriever.get("sheetColNames").split(",");
            int i;
            for (i = 0; i < sheetColNames.length; i++) {
                cell = row.createCell(i);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(sheetColNames[i]));
                cell.setCellStyle(getCellStyleForColumnNamesGrey(workbook));
            }

            String[] monthsColNames = MessageRetriever.get("monthsNamesShort").split(",");
            int cnt = i;
            for (int j = startMonth; j <= endMonth; j++) {
                //planning
                cell = row.createCell(cnt++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(monthsColNames[j]) + " " + MessageRetriever.get("sheetPlanning"));
                cell.setCellStyle(getCellStyleForColumnNamesMonths(cnt - 1, workbook));
                //request
                cell = row.createCell(cnt++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(monthsColNames[j]) + " " + MessageRetriever.get("sheetRequest"));
                cell.setCellStyle(getCellStyleForColumnNamesMonths(cnt - 1, workbook));
            }
            cell = row.createCell(cnt++);
            cell.setCellValue(workbook.getCreationHelper().createRichTextString(MessageRetriever.get("sheetColTotalControl") + " " + MessageRetriever.get("sheetPlanning")));
            cell.setCellStyle(getCellStyleForColumnNamesMonths(cnt - 1, workbook));
            cell = row.createCell(cnt++);
            cell.setCellValue(workbook.getCreationHelper().createRichTextString(MessageRetriever.get("sheetColTotalControl") + " " + MessageRetriever.get("sheetRequest")));
            cell.setCellStyle(getCellStyleForColumnNamesMonths(cnt - 1, workbook));
            cell = row.createCell(cnt);
            cell.setCellValue(workbook.getCreationHelper().createRichTextString(MessageRetriever.get("sheetColNotUsedDays")));
            cell.setCellStyle(getCellStyleForColumnNamesGrey(workbook));

            // populate with data
            List<User> users = userRepository.findAll();
            for (int u = 0; u < users.size(); u++) {
                int col = 0;
                User user = users.get(u);
                row = sheet.createRow(u + startRow);
                cell = row.createCell(col++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString((u + startRow) + ""));
                cell.setCellStyle(getCellStyleForContentGrey(workbook));
                cell = row.createCell(col++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(user.getName()));
                cell.setCellStyle(getCellStyleForContentUserNamesAndCO(workbook));
                cell = row.createCell(col++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(user.getAvailableVacationDays() + ""));
                cell.setCellStyle(getCellStyleForContentUserNamesAndCO(workbook));
                DashboardData data = dataService.getDashboardData(user.getEmail());
                for (int m = startMonth; m <= endMonth; m++) {
                    cell = row.createCell(col++);
                    cell.setCellValue(workbook.getCreationHelper().createRichTextString(data.getChartPlannedDays()[m] == 0 ? "" : data.getChartPlannedDays()[m] + ""));
                    cell.setCellStyle(getCellStyleForContentMonths(col - 1, workbook));
                    cell = row.createCell(col++);
                    cell.setCellValue(workbook.getCreationHelper().createRichTextString(data.getChartVacationDays()[m] == 0 ? "" : data.getChartVacationDays()[m] + ""));
                    cell.setCellStyle(getCellStyleForContentMonths(col - 1, workbook));
                }
                int plannedDays = IntStream.of(data.getChartPlannedDays()).sum();
                int requestDays = IntStream.of(data.getChartVacationDays()).sum();
                cell = row.createCell(col++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(plannedDays + ""));
                cell.setCellStyle(getCellStyleForContentMonths(col - 1, workbook));
                cell = row.createCell(col++);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString(requestDays + ""));
                cell.setCellStyle(getCellStyleForContentMonths(col - 1, workbook));
                cell = row.createCell(col);
                cell.setCellValue(workbook.getCreationHelper().createRichTextString((user.getAvailableVacationDays() - requestDays) + ""));
                cell.setCellStyle(getCellStyleForContentGrey(workbook));
            }
            autosizeAllColumns(sheet);
            // sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 30));
            sheet.createFreezePane(3, 1, 3, 1);
            log.info("excel file exported succesfully");
        } catch (Exception e) {
            log.error("error making excel", e);
        }
    }

    private static void autosizeAllColumns(Sheet sheet) {
        for (int i = 0; i < sheet.getLastRowNum(); i++) {
            if (i == 1) {
                sheet.setColumnWidth(i, 256 * COLUMN_NAME_WIDTH_CHAR);
            } else {
                sheet.setColumnWidth(i, 256 * COLUMNS_WIDTH_CHAR);
            }
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

    private static CellStyle getCellStyleForColumnNamesGrey(Workbook workbook) {
        XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
        XSSFCellStyle style = xssfWorkbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.FINE_DOTS);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        XSSFFont font = xssfWorkbook.createFont();
        font.setFontHeight(COLUMN_NAME_FONT_HEIGHT);
        font.setBold(COLUMN_NAME_FONT_BOLD);
        style.setFont(font);
        return style;
    }

    private static CellStyle getCellStyleForColumnNamesMonths(int colIndex, Workbook workbook) {
        XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
        XSSFCellStyle style = xssfWorkbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        if (colIndex % 2 == 1) {
            style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            style.setBorderRight(BorderStyle.THIN);
        } else {
            style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            style.setBorderRight(BorderStyle.MEDIUM);
        }
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = xssfWorkbook.createFont();
        font.setFontHeight(COLUMN_NAME_FONT_HEIGHT);
        font.setBold(COLUMN_NAME_FONT_BOLD);
        style.setFont(font);
        return style;
    }

    private static CellStyle getCellStyleForContentGrey(Workbook workbook) {
        XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
        XSSFCellStyle style = xssfWorkbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.FINE_DOTS);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        XSSFFont font = xssfWorkbook.createFont();
        font.setFontHeight(CONTENT_FONT_HEIGHT);
        font.setBold(CONTENT_FONT_BOLD);
        style.setFont(font);
        return style;
    }

    private static CellStyle getCellStyleForContentUserNamesAndCO(Workbook workbook) {
        XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
        XSSFCellStyle style = xssfWorkbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        XSSFFont font = xssfWorkbook.createFont();
        font.setFontHeight(CONTENT_FONT_HEIGHT);
        font.setBold(CONTENT_FONT_BOLD);
        style.setFont(font);
        return style;
    }

    private static CellStyle getCellStyleForContentMonths(int colIndex, Workbook workbook) {
        XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
        XSSFCellStyle style = xssfWorkbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        if (colIndex % 2 == 1) {
            style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            style.setBorderRight(BorderStyle.THIN);
        } else {
            style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            style.setBorderRight(BorderStyle.MEDIUM);
        }
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = xssfWorkbook.createFont();
        font.setFontHeight(CONTENT_FONT_HEIGHT);
        font.setBold(CONTENT_FONT_BOLD);
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
