package com.fragma.dao;

import com.fragma.config.InputDataConfig;
import com.fragma.dto.ReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public class ReportDAO {

    @Autowired
    @Qualifier("hiveJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    Logger LOGGER = LoggerFactory.getLogger(ReportDAO.class);

    @Autowired
    InputDataConfig inputDataConfig;


    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public String formateDate(java.sql.Date date) {

        String text = dateFormat.format(date);
        return text;
    }

    public List<Object[]> getDataForFirstQuery() throws Exception {


        String query1 = inputDataConfig.getQueryForExcelSheetData();

        LOGGER.info("query for sheet data :" + query1);
        //System.out.println("=====================================");
        List<Object[]> listQuery1 = executeQuery(query1);

        LOGGER.info(" data size in DAO :" + listQuery1.size());
        return listQuery1;

    }



    private List<Object[]> executeQuery(String query) throws Exception {


        final boolean[] header = {true};
        List<Object[]> results = new ArrayList<Object[]>();
        int count=0;

        jdbcTemplate.query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(query);

                LOGGER.info("Prepared Statment before bind :" + ps.toString());

                return ps;
            }
        }, new RowMapper<Object[]>() {
            @Override
            public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {

                //System.out.println("mapRow()-------");

                int columnCount = rs.getMetaData().getColumnCount();

                List<Object> columnNames = new ArrayList<>();

                if (header[0]) {
                    for (int i = 1; i <= columnCount; i++) {
                        columnNames.add(rs.getMetaData().getColumnName(i));
                        LOGGER.info("Columnn Name: " + rs.getMetaData().getColumnName(i));
                        //System.out.println("column name :"+rs.getMetaData().getColumnName(i));
                    }
                    header[0] = false;
                    results.add(columnNames.toArray());
                    columnNames.clear();

                    populateData(rs, columnCount, columnNames);
                } else {
                    populateData(rs, columnCount, columnNames);
                }
                return columnNames.toArray();
            }

            private void populateData(ResultSet rs, int columnCount, List<Object> columnNames) throws SQLException {


                for (int i = 1; i <= columnCount; i++) {
                    Object data = null;
                    if (rs.getObject(i) != null) {
                        data = rs.getObject(i);
                    }
                    columnNames.add(data);
                }

                results.add(columnNames.toArray());
            }
        });
        return results;
    }


    public void  getTotalCasesCount(ReportDTO dto) {
        jdbcTemplate.query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(inputDataConfig.getQueryForTotalCases());

                LOGGER.info("query for total cases  :" + inputDataConfig.getQueryForTotalCases());
                LOGGER.info("Prepared Statement before bind for totalcases query =" + ps.toString());

                return ps;
            }
        }, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {

                String dateFromDb = resultSet.getString(1);
              //  dto.setDateToDisplayInReport(dateFromDb);
               // LOGGER.info("date from db in DAO :"+dateFromDb);
                int totalCases = resultSet.getInt(2);
                LOGGER.info("total cases count in DAO :" + totalCases);
                dto.setTotalCases(totalCases);
            }
        });

    }

    public void getTotalFailedCasesCount(ReportDTO dto) {
        jdbcTemplate.query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(inputDataConfig.getQueryForTotalFailedCases());

                LOGGER.info("query for total failed cases  :" + inputDataConfig.getQueryForTotalFailedCases());
                LOGGER.info("Prepared Statement before bind for totalfailedcases query =" + ps.toString());

                return ps;
            }
        }, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {

                String dateFromDbForTotalFailedCases = resultSet.getString(1);
                LOGGER.info("date from db for total failed cases in DAO :"+dateFromDbForTotalFailedCases);
                int totalFailedCases = resultSet.getInt(2);
                LOGGER.info("total failed cases count in DAO :" + totalFailedCases);
                dto.setTotalFailedCases(totalFailedCases);
            }
        });

    }






}//class
