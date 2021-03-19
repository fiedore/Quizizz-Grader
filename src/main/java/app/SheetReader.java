package app;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SheetReader {

    public static Map<Integer, List<String>> getData(String filename) throws IOException {
        Map<Integer, List<String>> data = new HashMap<>();
        Workbook workbook;

        try (FileInputStream file = new FileInputStream(filename)) {
            workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            int i = 0;
            for (Row row : sheet) {
                List<String> rowList = new ArrayList<>();
                data.put(i, rowList);
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            rowList.add(cell.getRichStringCellValue().getString());
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                rowList.add(cell.getDateCellValue() + "");
                            } else {
                                rowList.add(cell.getNumericCellValue() + "");
                            }
                            break;
                        case BOOLEAN:
                            rowList.add(cell.getBooleanCellValue() + "");
                            break;
                        case FORMULA:
                            rowList.add(cell.getCellFormula() + "");
                            break;
                        default:
                            rowList.add(" ");
                    }
                }
                i++;
            }

        }
        return data;
    }

}
