package org.guanzon.cas.parameter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.parameter.model.Model_Color;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

public class Color extends Parameter{
    Model_Color poModel;
    List<Model_Color> poModelList;
    
    @Override
    public void initialize() {
        psRecdStat = Logical.YES;
        
        poModel = new Model_Color();
        poModel.setApplicationDriver(poGRider);
        poModel.setXML("Model_Color");
        poModel.setTableName("Color");
        poModel.initialize();
        poModelList = new ArrayList<>();
    }
    
    @Override
    public JSONObject isEntryOkay() {
        poJSON = new JSONObject();
        
        if (poGRider.getUserLevel() < UserRight.SYSADMIN){
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to save record.");
            return poJSON;
        } else {
            poJSON = new JSONObject();
            
            if (poModel.getColorId().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Color must not be empty.");
                return poJSON;
            }
            
            if (poModel.getDescription() == null || poModel.getDescription().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Color must not be empty.");
                return poJSON;
            }
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public Model_Color getModel() {
        return poModel;
    }
    
    @Override
    public JSONObject searchRecord(String value, boolean byCode) {
        String lsCondition = "";

        if (psRecdStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
            }

            lsCondition = "cRecdStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "cRecdStat = " + SQLUtil.toSQL(psRecdStat);
        }

//        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), lsCondition);
        System.out.println("get query = " + getSQ_Browse());
        poJSON = ShowDialogFX.Search(poGRider,
                getSQ_Browse(),
                value,
                "ID»Description",
                "sColorIDx»sDescript",
                "sColorIDx»sDescript",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sColorIDx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject searchRecordWithStatus(String value, boolean byCode) {
        String lsCondition = "";

        if (psRecdStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
            }

            lsCondition = "cRecdStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "cRecdStat = " + SQLUtil.toSQL(psRecdStat);
        }

        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), lsCondition);

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
               "ID»Description",
                "sColorIDx»sDescript",
                "sColorIDx»sDescript",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sColorIDx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    
   // ENABLE this Block of codes if list is needed else do not
//    
    public JSONObject ColorList() {
          StringBuilder lsSQL = new StringBuilder("SELECT" +
           "   sColorIDx" +
           " , sDescript" +
           " , sMnColorx" +
           " , cRecdStat" +
           " FROM Color");

        // Use SQLUtil.toSQL for handling the dates
//        String condition = "sColorIDx = " + SQLUtil.toSQL(fsColorID);
//        lsSQL.append(MiscUtil.addCondition("", condition));
//        lsSQL.append(" ORDER BY a.nLedgerNo ASC");

        System.out.println("Executing SQL: " + lsSQL.toString());

        ResultSet loRS = poGRider.executeQuery(lsSQL.toString());
        JSONObject poJSON = new JSONObject();

        try {
            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                poModelList = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set

                    System.out.println("sColorIDx: " + loRS.getString("sColorIDx"));
                    System.out.println("sDescript: " + loRS.getString("sDescript"));
                    System.out.println("cRecdStat: " + loRS.getString("cRecdStat"));
                    System.out.println("------------------------------------------------------------------------------");

                    poModelList.add(Color(loRS.getString("sColorIDx")));
                    poModelList.get(poModelList.size() - 1)
                            .openRecord(loRS.getString("sColorIDx"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");

            } else {
                poModelList = new ArrayList<>();
//                addInvLedger();
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found .");
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        System.out.println("RESULT == " + poJSON);
        return poJSON;
    }
    
    private Model_Color Color (String colorID) {
        Model_Color object = new ParamModels(poGRider).Color();

        JSONObject loJSON = object.openRecord(colorID);

        if ("success".equals((String) loJSON.get("result"))) {
            return object;
        } else {
            return new ParamModels(poGRider).Color();
        }
    }
    public int getListCount() {
        return poModelList.size();
    }
    public Model_Color Color(int row) {
        return poModelList.get(row);
    }
}