/**
 * 
 */
package main.java.utility;

import main.java.model.TransactionWrapper;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author doquocanh-macbook
 *
 */
public class ExportUtils {

	/**
	 * 
	 */
	public ExportUtils() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Export transaction history to an excel file
	 * 
	 * @param transactions List of transactions that needs to write to file
	 * @param filePath Directory where the new file will be located
	 * 
	 * @return True if excel file is successfully created. Otherwise returns False
	 */
	public static boolean writeToExcel(List<TransactionWrapper> transactions, String filePath) {
		// pre-condition check
		if (transactions == null || transactions.size() <= 0) {
			return false; // Don't create excel file
		}
		
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Transactions");
        int rowNumber = 0;
        HSSFRow row = sheet.createRow(rowNumber++);
        
        // Create headers
        row.createCell(0).setCellValue("Date");
        row.createCell(1).setCellValue("Time");
        row.createCell(2).setCellValue("Stock Symbol");
        row.createCell(3).setCellValue("Company");
        row.createCell(4).setCellValue("Price");
        row.createCell(5).setCellValue("Quantity");
        row.createCell(6).setCellValue("Payment");
        row.createCell(7).setCellValue("Balance");

        try (FileOutputStream fileOut = new FileOutputStream(filePath + File.separator + "transactions.xls")){
        	// Write transaction details to each row in excel file
        	// Each transaction is associated with a row
        	for (TransactionWrapper t : transactions) {
        		HSSFRow newRow = sheet.createRow(rowNumber++);
        		newRow.createCell(0).setCellValue(t.getTransactionDate());
        		newRow.createCell(1).setCellValue(t.getTransactionTime());
        		newRow.createCell(2).setCellValue(t.getStockCode());
        		newRow.createCell(3).setCellValue(t.getStockCompany());
        		newRow.createCell(4).setCellValue(t.getPrice());
        		newRow.createCell(5).setCellValue(t.getAmount());
        		newRow.createCell(6).setCellValue(t.getTransactionPayment());
        		newRow.createCell(7).setCellValue(t.getTransactionPayment());
        	}
			wb.write(fileOut);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			try {
				wb.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
