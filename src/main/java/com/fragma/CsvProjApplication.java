package com.fragma;

import com.fragma.config.InputDataConfig;
import com.fragma.config.PersistenceConfig;
import com.fragma.config.SMTPMailThreadConfig;
import com.fragma.config.ThymeleafConfig;
import com.fragma.dao.ReportDAO;
import com.fragma.dto.ReportDTO;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.TextAlignment;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.bind.SchemaOutputResolver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
@EnableScheduling
@Import({ThymeleafConfig.class, SMTPMailThreadConfig.class, InputDataConfig.class, PersistenceConfig.class})
public class CsvProjApplication implements ApplicationRunner {

    static Logger LOG = LoggerFactory.getLogger(CsvProjApplication.class);

    private TemplateEngine templateEngine;
    @Autowired
    private Session session;
    @Autowired
    private ReportDAO dao;
    @Autowired
    private SMTPMailThreadConfig mailThreadConfig;

    @Autowired
    private InputDataConfig inputDataConfig;

    @Autowired
    private TaskScheduler taskScheduler;
    StringWriter st;
    XSSFWorkbook workbook;
    ReportDTO dto = new ReportDTO();
    File file;
    //key-sheet name value-sheet data
    Map<String, List<Object[]>> reportMap = new LinkedHashMap<>();

    @Autowired
    public CsvProjApplication(TemplateEngine templateEngine) {

        this.templateEngine = templateEngine;
    }


    public static void main(String[] args) {
        SpringApplication.run(CsvProjApplication.class, args);

        LOG.info("MAIN STARTED");
        SpringApplication.run(CsvProjApplication.class, args);

        LOG.info("MAIN ENDED");

    }



    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            //System.out.println("run started");
            LOG.info("run startedCsV");
            List<String> businessDates = args.getOptionValues("businessDate");
            Date businessDate;
            String bdString;
            System.out.println("==============run started===============");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

            if (businessDates != null && businessDates.size() > 0) {
                bdString = businessDates.get(0);
                businessDate = dateFormat.parse(bdString);
            } else {
                Calendar instance = Calendar.getInstance();
                instance.add(Calendar.DAY_OF_MONTH, -1);
                businessDate = instance.getTime();
            }


            DateFormat formatter = new SimpleDateFormat("YYYY-MM-DD");
            String dateToDisplayInReport = formatter.format(businessDate);
            LOG.info("businessDate =" + businessDate);
            LOG.info("dateToDisplayInReport =" + dateToDisplayInReport);
            dto.setDateToDisplayInReport(dateToDisplayInReport);
            createExcelFile();
            getCountForTotalCasesAndTotalFailedCases();

            double totalCases = dto.getTotalCases();
            double totalFailedCases = dto.getTotalFailedCases();

            double failurePercentage = (totalFailedCases / totalCases) * 100;
            LOG.info("failure percentage :" + failurePercentage);
            long failurePercentageAfterRounding = Math.round(failurePercentage);
            dto.setFailurePercentage(failurePercentageAfterRounding);
            Context context = new Context();
            context.setVariable("dto", dto);
            st = new StringWriter();
            templateEngine.process("report", context, st);

            LOG.info("after process()=====" + st.toString());


            sendMail();
            LOG.info("=====Mail Sent Succesfully=====");
        } catch (Exception e) {
            LOG.info("error is :" + e);
            e.printStackTrace();
        }
    }

    private void sendMail() {
        try {
            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            //msg.addHeader("Content-type", "application/excel; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress(mailThreadConfig.getFromAddress(), mailThreadConfig.getFromName()));

            msg.setReplyTo(InternetAddress.parse(mailThreadConfig.getFromAddress(), false));

            msg.setSubject(mailThreadConfig.getSubject(), "UTF-8");

            msg.setContent(st.toString(), "text/html");


            MimeBodyPart messageBodyPart1 = new MimeBodyPart();

            MimeBodyPart messageBodyPart2 = new MimeBodyPart();
            messageBodyPart1.addHeader("Content-type", "text/HTML; charset=UTF-8");
            messageBodyPart1.addHeader("format", "flowed");
            messageBodyPart1.addHeader("Content-Transfer-Encoding", "8bit");
            //messageBodyPart1.setText(st.toString());
            messageBodyPart1.setContent(st.toString(), "text/html");

            DataSource source = new FileDataSource(mailThreadConfig.getLocation());
            messageBodyPart2.setDataHandler(new DataHandler(source));

            //  messageBodyPart2.setFileName(new File("F:/examplefile.xlsx").getName());
            messageBodyPart2.setFileName(new File(mailThreadConfig.getLocation()).getName());
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart2);
            multipart.addBodyPart(messageBodyPart1);
            msg.setContent(multipart);
            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailThreadConfig.getToAddress(), false));
            //msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(mailThreadConfig.getCcAddress(), false));
            Transport.send(msg);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void createExcelFile() throws Exception {
        workbook = new XSSFWorkbook();
        //get data for writing to first sheet
        List<Object[]> listFirstQueryData = dao.getDataForFirstQuery();
        String sheetNameInFile = inputDataConfig.getSheetName();
        reportMap.put(sheetNameInFile, listFirstQueryData);


        int rownum = 0;
        for (Map.Entry<String, List<Object[]>> keyValue : reportMap.entrySet()) {

            String sheetName = keyValue.getKey();
            List<Object[]> sheetList = keyValue.getValue();

            XSSFSheet sheet = workbook.createSheet(sheetName);
            rownum = 0;
            for (Object[] object : sheetList) {
                Row currentDatarow = sheet.createRow(rownum++);
                int cellnum = 0;
                for (Object obj : object) {
                    Cell cell = currentDatarow.createCell(cellnum++);
                    //      cell.setCellValue((String) obj);
                    if (obj instanceof String) {
                        cell.setCellValue((String) obj);
                    } else if (obj instanceof Integer) {
                        cell.setCellValue((Integer) obj);
                    } else if (obj instanceof Double) {
                        double doubleValue = (Double) obj;
                        cell.setCellValue((int) doubleValue);
                    } else if (obj instanceof Float) {
                        float floatValue = (Float) obj;
                        cell.setCellValue((int) floatValue);
                    } else {
                        LOG.info("else in Runner obj is :" + obj);
                        if (obj == null) {
                            LOG.info("setting null as empty String :" + obj);
                            obj = "";
                            cell.setCellValue(String.valueOf(obj));
                        }
                    }
                }
            }

            try {
                FileOutputStream out = new FileOutputStream(new File(mailThreadConfig.getLocation()));
                workbook.write(out);
                out.close();
                LOG.info(mailThreadConfig.getLocation() + " written successfully on disk.");
            } catch (Throwable e) {
                //LOG.info();
                e.printStackTrace();
            }
        }
    }//createExcelFile()

    public void getCountForTotalCasesAndTotalFailedCases() {
        dao.getTotalCasesCount(dto);
        dao.getTotalFailedCasesCount(dto);
    }

}
