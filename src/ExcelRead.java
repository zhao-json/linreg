import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author Jason Zhao and Isaac Rozen 
 * Description: 
 * 		ExcelRead takes in a excel
 *      file and translates columns of x, y, and weights into HashMaps These
 *      will be used to generate Arrays for regressions Generates errors and
 *      success alerts
 */
public class ExcelRead extends GUI {

	/**
	 * @param filname
	 *            name of excel file
	 * @param title
	 *            HashMap for storing column titles
	 * @param x
	 *            HashMap stores x values
	 * @param y
	 *            HashMap stores y values
	 * @param w
	 *            HashMap stores weight values
	 * @throws IOException
	 *             Reads in excel files and interprets x, y, weight values from
	 *             columns
	 */
	public void run(String fileName, HashMap<Integer, String> title,
			HashMap<Integer, Double> x, HashMap<Integer, Double> y,
			HashMap<Integer, Double> w) throws IOException {

		// Sets up error Alert
		Alert error = new Alert(AlertType.ERROR);
		error.setTitle("Error");
		error.setHeaderText("Error Found!");

		// Excel file into input stream
		File myFile = new File(fileName);
		FileInputStream fis = new FileInputStream(myFile);

		// captures case where file is not excel file
		XSSFWorkbook myWorkBook = null;
		try {
			// Finds the workbook instance for XLSX file
			myWorkBook = new XSSFWorkbook(fis);
		} catch (Exception e) {
			// Alert if not excel file and exits program
			error.setContentText("Not Excel File.");
			error.showAndWait();
			e.printStackTrace();
			System.exit(0);
		}

		// Return first sheet from the excel workbook
		XSSFSheet mySheet = myWorkBook.getSheetAt(0);

		// Get iterator to all the rows in current sheet
		Iterator<Row> rowIterator = mySheet.iterator();

		// Traversing over each row of XLSX file
		while (rowIterator.hasNext()) {

			// scans row by row
			Row row = rowIterator.next();

			// For each row, iterate through each columns
			Iterator<Cell> cellIterator = row.cellIterator();

			// scans all cells in row
			while (cellIterator.hasNext()) {
				// reads in cell
				Cell cell = cellIterator.next();

				// determines cell type and stores in corresponding HashMap
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
					// first row for titles
					if (cell.getRowIndex() == 0) {
						// stores titles and column numbers into HashMap
						title.put(cell.getColumnIndex(), cell.toString());
					}
					break;
				case Cell.CELL_TYPE_NUMERIC:

					int firstColumn = 0;
					int secondColumn = 1;
					int thirdColumn = 2;
					if (cell.getColumnIndex() == firstColumn) {
						// stores keys and row number into HashMap
						x.put(cell.getRowIndex(), cell.getNumericCellValue());
					}
					if (cell.getColumnIndex() == secondColumn) {
						y.put(cell.getRowIndex(), cell.getNumericCellValue());
					}
					if (cell.getColumnIndex() == thirdColumn) {
						w.put(cell.getRowIndex(), cell.getNumericCellValue());
					}
					break;
				default:
					break;
				}
			}
		}

		// Check we have at least 2 x-y pairs
		int minSize = 2;
		if (x.size() < minSize || y.size() < minSize) {
			error.setContentText("Not enough x-y pairs to form a line");
			error.showAndWait();
			System.exit(0);
		}

		// Check we have a y for each x
		if (x.size() != y.size()) {
			error.setContentText("Invalid x-y pairs; make sure each x has a y!");
			error.showAndWait();
			System.exit(0);
		}

		// Alert for successful updating
		Alert success = new Alert(AlertType.INFORMATION);
		success.setTitle("Success");
		success.setHeaderText("Successfully Read!");
		success.setContentText("The file was read successfully!");
		success.showAndWait();

		// closes both WorkBooks
		myWorkBook.close();
	}
}
