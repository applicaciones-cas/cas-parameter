package org.guanzon.cas.parameter.model;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.iface.GEntity;
import org.json.simple.JSONObject;

public class Model_TownCity implements GEntity{
    LogWrapper logwrapr = new LogWrapper("Model_TownCity", "cas-error.log");
    
    private final String XML = "Model_TownCity.xml";
    
    GRider poGRider;                //application driver
    CachedRowSet poEntity;          //rowset
    JSONObject poJSON;              //json container
    int pnEditMode;                 //edit mode
    
    Model_Province poProvince;
    
    public Model_TownCity(GRider value) {
        if (value == null) {
            System.err.println("Application Driver is not set.");
            System.exit(1);
        }

        poGRider = value;

        poProvince = new Model_Province(poGRider);
        
        initialize();
    }

    @Override
    public String getColumn(int columnIndex) {
        try {
            return poEntity.getMetaData().getColumnLabel(columnIndex);
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
        }
        return "";
    }

    @Override
    public int getColumn(String columnName) {
        try {
            return MiscUtil.getColumnIndex(poEntity, columnName);
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
        }
        return -1;
    }

    @Override
    public int getColumnCount() {
        try {
            return poEntity.getMetaData().getColumnCount();
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
        }

        return -1;
    }

    @Override
    public int getEditMode() {
        return pnEditMode;
    }

    @Override
    public String getTable() {
        return "TownCity";
    }

    @Override
    public Object getValue(int columnIndex) {
        try {
            return poEntity.getObject(columnIndex);
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
        }
        return null;
    }

    @Override
    public Object getValue(String columnName) {
        try {
            return poEntity.getObject(MiscUtil.getColumnIndex(poEntity, columnName));
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
        }
        return null;
    }

    @Override
    public JSONObject setValue(int columnIndex, Object value) {
        try {
            poJSON = MiscUtil.validateColumnValue(System.getProperty("sys.default.path.metadata") + XML, MiscUtil.getColumnLabel(poEntity, columnIndex), value);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poEntity.updateObject(columnIndex, value);
            poEntity.updateRow();

            poJSON = new JSONObject();
            poJSON.put("result", "success");
            poJSON.put("value", getValue(columnIndex));
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }

        return poJSON;
    }

    @Override
    public JSONObject setValue(String colunmName, Object value) {
        poJSON = new JSONObject();

        try {
            return setValue(MiscUtil.getColumnIndex(poEntity, colunmName), value);
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return poJSON;
    }

    @Override
    public JSONObject newRecord() {
        pnEditMode = EditMode.ADDNEW;

        //replace with the primary key column info
        setTownId(MiscUtil.getNextCode(getTable(), "sTownIDxx", false, poGRider.getConnection(), ""));

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject openRecord(String townId) {
        poJSON = new JSONObject();

        String lsSQL = MiscUtil.makeSelect(this);

        //replace the condition based on the primary key column of the record
        lsSQL = MiscUtil.addCondition(lsSQL, "sTownIDxx = " + SQLUtil.toSQL(townId));

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            if (loRS.next()) {
                for (int lnCtr = 1; lnCtr <= loRS.getMetaData().getColumnCount(); lnCtr++) {
                    setValue(lnCtr, loRS.getObject(lnCtr));
                }

                MiscUtil.close(loRS);
                
                //connect to other table
                poJSON = poProvince.openRecord((String) getValue("sProvIDxx"));
                if (!((String)poJSON.get("result")).equals("success")) 
                    System.err.println("Province.openRecord: " + poJSON.toJSONString());
                
                pnEditMode = EditMode.READY;

                poJSON = new JSONObject();
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record to load.");
            }
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }

        return poJSON;
    }
    
    @Override
    public JSONObject updateRecord() {
        poJSON = new JSONObject();
        
        if (pnEditMode != EditMode.READY){
            poJSON.put("result", "error");
            poJSON.put("message", "No record was loaded.");
        } else {
            pnEditMode = EditMode.UPDATE;
            poJSON.put("result", "success");
        }
        
        return poJSON;
    }

    @Override
    public JSONObject saveRecord() {
        poJSON = new JSONObject();

        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            String lsSQL;
            
            setModifyingId(poGRider.getUserID());
            setModifiedDate(poGRider.getServerDate());
            
            if (pnEditMode == EditMode.ADDNEW) {
                //replace with the primary key column info
                setTownId(MiscUtil.getNextCode(getTable(), "sTownIDxx", false, poGRider.getConnection(), ""));

                lsSQL = MiscUtil.makeSQL(this);

                if (!lsSQL.isEmpty()) {
                    if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0) {
                        poJSON = new JSONObject();
                        poJSON.put("result", "success");
                        poJSON.put("message", "Record saved successfully.");
                    } else {
                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", poGRider.getErrMsg());
                    }
                } else {
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "No record to save.");
                }
            } else {
                Model_TownCity loOldEntity = new Model_TownCity(poGRider);

                //replace with the primary key column info
                poJSON = loOldEntity.openRecord(this.getTownId());

                if ("success".equals((String) poJSON.get("result"))) {
                    //replace the condition based on the primary key column of the record
                    lsSQL = MiscUtil.makeSQL(this, loOldEntity, "sTownIDxx = " + SQLUtil.toSQL(this.getTownId()));

                    if (!lsSQL.isEmpty()) {
                        if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0) {
                            poJSON = new JSONObject();
                            poJSON.put("result", "success");
                            poJSON.put("message", "Record saved successfully.");
                        } else {
                            poJSON = new JSONObject();
                            poJSON.put("result", "error");
                            poJSON.put("message", poGRider.getErrMsg());
                        }
                    } else {
                        poJSON = new JSONObject();
                        poJSON.put("result", "success");
                        poJSON.put("message", "No updates has been made.");
                    }
                } else {
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "Record discrepancy. Unable to save record.");
                }
            }
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid update mode. Unable to save record.");
            return poJSON;
        }
        
        if (((String) poJSON.get("result")).equals("success")) openRecord((String) getValue(0));

        return poJSON;
    }
    
    public JSONObject setTownId(String townId){
        return setValue("sTownIDxx", townId);
    }
    
    public String getTownId(){
        return (String) getValue("sTownIDxx");
    }
    
    public JSONObject setTownName(String townName){
        return setValue("sTownName", townName);
    }
    
    public String getTownName(){
        return (String) getValue("sTownName");
    }
    
    public JSONObject setZipCode(String zipCode){
        return setValue("sZippCode", zipCode);
    }
    
    public String getZipCode(){
        return (String) getValue("sZippCode");
    }
    
    public JSONObject setProvinceId(String provinceId){
        setValue("sProvIDxx", provinceId);
        poProvince.openRecord((String) getValue("sProvIDxx"));
        
        return poJSON;
    }
    
    public String getProvinceId(){
        return (String) getValue("sProvIDxx");
    }
    
    public String getProvinceName(){
        if (poProvince.getEditMode() == EditMode.UPDATE)
            return poProvince.getProvinceName();
        else
            return "";
    }
    
    public JSONObject setMunicpalCode(String municipalCode){
        return setValue("sMuncplCd", municipalCode);
    }
    
    public String getMunicpalCode(){
        return (String) getValue("sMuncplCd");
    }
    
    public JSONObject hasRoute(boolean hasRoute){
        return setValue("cHasRoute", hasRoute == true ? "1" : "0");
    }
    
    public boolean hasRoute(){
        return "1".equals((String) getValue("cHasRoute"));
    }
    
    public JSONObject isBlacklisted(boolean isBlacklisted){
        return setValue("cBlackLst", isBlacklisted == true ? "1" : "0");
    }
    
    public boolean isBlacklisted(){
        return "1".equals((String) getValue("cBlackLst"));
    }
    
    public JSONObject setRecordStatus(String recordStatus){
        return setValue("cRecdStat", recordStatus);
    }
    
    public String getRecordStatus(){
        return (String) getValue("cRecdStat");
    }
    
    public JSONObject setModifyingId(String modifyingId){
        return setValue("sModified", modifyingId);
    }
    
    public String getModifyingId(){
        return (String) getValue("sModified");
    }
    
    public JSONObject setModifiedDate(Date modifiedDate){
        return setValue("dModified", modifiedDate);
    }
    
    public Date getModifiedDate(){
        return (Date) getValue("dModified");
    }

    @Override
    public void list() {
        Method[] methods = this.getClass().getMethods();

        System.out.println("--------------------------------------------------------------------");
        System.out.println("LIST OF PUBLIC METHODS FOR " + this.getClass().getName() + ":");
        System.out.println("--------------------------------------------------------------------");
        for (Method method : methods) {
            System.out.println(method.getName());
        }
    }

    private void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);
            
            poEntity.updateString("cBlackLst", Logical.NO);
            poEntity.updateString("cHasRoute", Logical.YES);
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
}