package org.guanzon.cas.parameter;

import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.parameter.model.Model_Region;
import org.json.simple.JSONObject;

public class Region extends Parameter{
    Model_Region poModel;
    
    @Override
    public void initialize() {
        psRecdStat = Logical.YES;
        
        poModel = new Model_Region();
        poModel.setApplicationDriver(poGRider);
        poModel.setXML("Model_Region");
        poModel.setTableName("Region");
        poModel.initialize();
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
            
            if (poModel.getRegioneName().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Region must not be empty.");
                return poJSON;
            }
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public Model_Region getModel() {
        return poModel;
    }
    
    @Override
    public JSONObject searchRecord(String value, boolean byCode) {
        poJSON = ShowDialogFX.Search(poGRider,
                getSQ_Browse(),
                value,
                "ID»Region",
                "sRegionID»sRegionNm",
                "sRegionID»sRegionNm",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sProvIDxx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject searchRecordWithStatus(String value, boolean byCode) {
        poJSON = ShowDialogFX.Search(poGRider,
                getSQ_Browse(),
                value,
                "ID»Region»Record Status",
                "sRegionID»sRegionNm»cRecdStat",
                "sRegionID»sRegionNm»cRecdStat",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sProvIDxx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
}