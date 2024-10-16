package org.canisminor.assettransfer;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

@Contract(
        name = "DataAsset",
        info = @Info(
                title = "Data Asset Transfer",
                description = "Chaincode of Data Asset X Project",
                version = "0.0.1",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "canisminor@qq.com",
                        name = "CanisMinor",
                        url = "https://github.com/CanisMinor-1037"
                )
        )
)

@Default
public final class AssetTransfer implements ContractInterface {
    private final Genson genson = new Genson();

    private enum AssetTransferErrors {
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS
    }

    // Department
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean DepartmentExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String departmentJSON = stub.getStringState(id);

        return (departmentJSON != null && !departmentJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Department CreateDepartment(final Context ctx, final String id, final String name, final String attribute) {
        ChaincodeStub stub = ctx.getStub();

        if (DepartmentExists(ctx, id)) {
            String errorMessage = String.format("Department %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Department department = new Department(id, name, attribute);
        // Use Genson to convert the Asset into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(department);
        stub.putStringState(id, sortedJson);

        stub.setEvent("CreateDepartment", sortedJson.getBytes(StandardCharsets.UTF_8));
        return department;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Department ReadDepartment(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String departmentJSON = stub.getStringState(id);

        if (departmentJSON == null || departmentJSON.isEmpty()) {
            String errorMessage = String.format("Department %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Department department = genson.deserialize(departmentJSON, Department.class);
        return department;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Department UpdateDepartmentAttribute(final Context ctx, final String id, final String attribute) {
        ChaincodeStub stub = ctx.getStub();
        String departmentJSON = stub.getStringState(id);

        if (departmentJSON == null || departmentJSON.isEmpty()) {
            String errorMessage = String.format("Department %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Department newDepartment = genson.deserialize(departmentJSON, Department.class);
        newDepartment.setAttribute(attribute);
        String sortedJson = genson.serialize(newDepartment);
        stub.putStringState(id, sortedJson);

        stub.setEvent("UpdateDepartmentAttribute", sortedJson.getBytes(StandardCharsets.UTF_8));
        return newDepartment;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllDepartment(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        List<Department> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("Department", "Department9999999999999999999999999");
        for (KeyValue result : results) {
            Department department = genson.deserialize(result.getStringValue(), Department.class);
            queryResults.add(department);
        }

        final String reponse = genson.serialize(queryResults);
        return reponse;
    }

    // DataAsset
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean DataAssetExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String dataAssetJSON = stub.getStringState(id);

        return (dataAssetJSON != null && !dataAssetJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public DataAsset CreateDataAsset(final Context ctx, final String id, final String name, final String ownerId, final String policy, final String location, final String field, final String cid, final String aesKey, final int encType) {
        ChaincodeStub stub = ctx.getStub();

        if (DataAssetExists(ctx, id)) {
            String errorMessage = String.format("DataAsset %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        if (!DepartmentExists(ctx, ownerId)) {
            String errorMessage = String.format("Department %s does not exist", ownerId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        DataAsset dataAsset = new DataAsset(id, name, ownerId, policy, location, field, cid, aesKey, encType);
        String sortedJson = genson.serialize(dataAsset);
        stub.putStringState(id, sortedJson);

        stub.setEvent("CreateDataAsset", sortedJson.getBytes(StandardCharsets.UTF_8));
        return dataAsset;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public DataAsset ReadDataAsset(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String dataAssetJSON = stub.getStringState(id);

        if (dataAssetJSON == null || dataAssetJSON.isEmpty()) {
            String errorMessage = String.format("DataAsset %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        DataAsset dataAsset = genson.deserialize(dataAssetJSON, DataAsset.class);
        return dataAsset;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllDataAsset(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        List<DataAsset> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("DataAsset", "DataAsset9999999999999999999999999");
        for (KeyValue result : results) {
            DataAsset dataAsset = genson.deserialize(result.getStringValue(), DataAsset.class);
            queryResults.add(dataAsset);
        }

        final String response = genson.serialize(queryResults);
        return response;
    }

    // DBAsset
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean DBAssetExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String dbAssetJSON = stub.getStringState(id);

        return (dbAssetJSON != null && !dbAssetJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public DBAsset CreateDBAsset(final Context ctx, final String id, final String name, final String ownerId, final String policy, final String location, final String field, final String jdbcUrl, final String username, final String password, final String aesKey, final int encType) {
        ChaincodeStub stub = ctx.getStub();

        if (DBAssetExists(ctx, id)) {
            String errorMessage = String.format("DBAsset %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        if (!DepartmentExists(ctx, ownerId)) {
            String errorMessage = String.format("Department %s does not exist", ownerId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        DBAsset dbAsset = new DBAsset(id, name, ownerId, policy, location, field, jdbcUrl, username, password, aesKey, encType);
        String sortedJson = genson.serialize(dbAsset);
        stub.putStringState(id, sortedJson);

        stub.setEvent("CreateDBAsset", sortedJson.getBytes(StandardCharsets.UTF_8));
        return dbAsset;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public DBAsset ReadDBAsset(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String dbAssetJSON = stub.getStringState(id);

        if (dbAssetJSON == null || dbAssetJSON.isEmpty()) {
            String errorMessage = String.format("DBAsset %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        DBAsset dbAsset = genson.deserialize(dbAssetJSON, DBAsset.class);
        return dbAsset;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllDBAsset(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        List<DBAsset> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("DBAsset", "DBAsset9999999999999999999999999");
        for (KeyValue result : results) {
            DBAsset dbAsset = genson.deserialize(result.getStringValue(), DBAsset.class);
            queryResults.add(dbAsset);
        }

        final String response = genson.serialize(queryResults);
        return response;
    }

    // VideoAsset
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean VideoAssetExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String videoAssetJSON = stub.getStringState(id);

        return (videoAssetJSON != null && !videoAssetJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public VideoAsset CreateVideoAsset(final Context ctx, final String id, final String name, final String ownerId, final String policy, final String location, final String field, final String rstpUrl, final String aesKey, final int encType) {
        ChaincodeStub stub = ctx.getStub();

        if (VideoAssetExists(ctx, id)) {
            String errorMessage = String.format("VideoAsset %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        if (!DepartmentExists(ctx, ownerId)) {
            String errorMessage = String.format("Department %s does not exist", ownerId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        VideoAsset videoAsset = new VideoAsset(id, name, ownerId, policy, location, field, rstpUrl, aesKey, encType);
        String sortedJson = genson.serialize(videoAsset);
        stub.putStringState(id, sortedJson);

        stub.setEvent("CreateVideoAsset", sortedJson.getBytes(StandardCharsets.UTF_8));
        return videoAsset;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public VideoAsset ReadVideoAsset(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String videoAssetJSON = stub.getStringState(id);

        if (videoAssetJSON == null || videoAssetJSON.isEmpty()) {
            String errorMessage = String.format("DBAsset %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        VideoAsset videoAsset = genson.deserialize(videoAssetJSON, VideoAsset.class);
        return videoAsset;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllVideoAsset(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        List<VideoAsset> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("VideoAsset", "VideoAsset9999999999999999999999999");
        for (KeyValue result : results) {
            VideoAsset videoAsset = genson.deserialize(result.getStringValue(), VideoAsset.class);
            queryResults.add(videoAsset);
        }

        final String response = genson.serialize(queryResults);
        return response;
    }

    // DataAssetOrder
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean DataAssetOrderExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String dataAssetOrderJSON = stub.getStringState(id);

        return (dataAssetOrderJSON != null && !dataAssetOrderJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public DataAssetOrder CreateDataAssetOrder(final Context ctx, final String id, final String dataAssetId, final String applicantId, final int status) {
        ChaincodeStub stub = ctx.getStub();

        if (DataAssetOrderExists(ctx, id)) {
            String errorMessage = String.format("DataAssetOrder %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        if (!DataAssetExists(ctx, dataAssetId)) {
            String errorMessage = String.format("DataAsset %s does not exist", dataAssetId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        if (!DepartmentExists(ctx, applicantId)) {
            String errorMessage = String.format("Department %s does not exist", applicantId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        DataAssetOrder dataAssetOrder = new DataAssetOrder(id, dataAssetId, applicantId, status);
        String sortedJson = genson.serialize(dataAssetOrder);
        stub.putStringState(id, sortedJson);

        stub.setEvent("CreateDataAssetOrder", sortedJson.getBytes(StandardCharsets.UTF_8));
        return dataAssetOrder;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public DataAssetOrder ReadDataAssetOrder(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String dataAssetOrderJSON = stub.getStringState(id);

        if (dataAssetOrderJSON == null || dataAssetOrderJSON.isEmpty()) {
            String errorMessage = String.format("DataAssetOrder %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        DataAssetOrder dataAssetOrder = genson.deserialize(dataAssetOrderJSON, DataAssetOrder.class);
        return dataAssetOrder;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public DataAssetOrder UpdateDataAssetOrderStatus(final Context ctx, final String id, final int status) {
        ChaincodeStub stub = ctx.getStub();
        String dataAssetOrderJSON = stub.getStringState(id);

        if (dataAssetOrderJSON == null || dataAssetOrderJSON.isEmpty()) {
            String errorMessage = String.format("DataAssetOrder %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        DataAssetOrder newDataAssetOrder = genson.deserialize(dataAssetOrderJSON, DataAssetOrder.class);
        newDataAssetOrder.setStatus(status);
        String sortedJSON = genson.serialize(newDataAssetOrder);
        stub.putStringState(id, sortedJSON);

        stub.setEvent("UpdateDataAssetOrderStatus", sortedJSON.getBytes(StandardCharsets.UTF_8));
        return newDataAssetOrder;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllDataAssetOrder(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        List<DataAssetOrder> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("DataAssetOrder", "DataAssetOrder99999999999999999999");
        for (KeyValue result : results) {
            DataAssetOrder dataAssetOrder = genson.deserialize(result.getStringValue(), DataAssetOrder.class);
            queryResults.add(dataAssetOrder);
        }

        final String response = genson.serialize(queryResults);
        return response;
    }

    // DBAssetOrder
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean DBAssetOrderExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String dbAssetOrderJSON = stub.getStringState(id);

        return (dbAssetOrderJSON != null && !dbAssetOrderJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public DBAssetOrder CreateDBAssetOrder(final Context ctx, final String id, final String dbAssetId, final String applicantId, final String sql) {
        ChaincodeStub stub = ctx.getStub();

        if (DBAssetOrderExists(ctx, id)) {
            String errorMessage = String.format("DBAssetOrder %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        if (!DBAssetExists(ctx, dbAssetId)) {
            String errorMessage = String.format("DBAsset %s does not exist", dbAssetId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        if (!DepartmentExists(ctx, applicantId)) {
            String errorMessage = String.format("Department %s does not exist", applicantId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        DBAssetOrder dbAssetOrder = new DBAssetOrder(id, dbAssetId, applicantId, sql);
        String sortedJson = genson.serialize(dbAssetOrder);
        stub.putStringState(id, sortedJson);

        stub.setEvent("CreateDBAssetOrder", sortedJson.getBytes(StandardCharsets.UTF_8));
        return dbAssetOrder;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public DBAssetOrder ReadDBAssetOrder(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String dbAssetOrderJSON = stub.getStringState(id);

        if (dbAssetOrderJSON == null || dbAssetOrderJSON.isEmpty()) {
            String errorMessage = String.format("DBAssetOrder %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        DBAssetOrder dbAssetOrder = genson.deserialize(dbAssetOrderJSON, DBAssetOrder.class);
        return dbAssetOrder;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllDBAssetOrder(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        List<DBAssetOrder> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("DBAssetOrder", "DBAssetOrder99999999999999999999");
        for (KeyValue result : results) {
            DBAssetOrder dbAssetOrder = genson.deserialize(result.getStringValue(), DBAssetOrder.class);
            queryResults.add(dbAssetOrder);
        }

        final String response = genson.serialize(queryResults);
        return response;
    }

    // VideoAssetOrder
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean VideoAssetOrderExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String videoAssetOrderJSON = stub.getStringState(id);

        return (videoAssetOrderJSON != null && !videoAssetOrderJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public VideoAssetOrder CreateVideoAssetOrder(final Context ctx, final String id, final String videoAssetId, final String applicantId, final int status) {
        ChaincodeStub stub = ctx.getStub();

        if (VideoAssetOrderExists(ctx, id)) {
            String errorMessage = String.format("VideoAssetOrder %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        if (!VideoAssetExists(ctx, videoAssetId)) {
            String errorMessage = String.format("VideoAsset %s does not exist", videoAssetId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        if (!DepartmentExists(ctx, applicantId)) {
            String errorMessage = String.format("Department %s does not exist", applicantId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        VideoAssetOrder videoAssetOrder = new VideoAssetOrder(id, videoAssetId, applicantId, status);
        String sortedJson = genson.serialize(videoAssetOrder);
        stub.putStringState(id, sortedJson);

        stub.setEvent("CreateVideoAssetOrder", sortedJson.getBytes(StandardCharsets.UTF_8));
        return videoAssetOrder;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public VideoAssetOrder ReadVideoAssetOrder(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String videoAssetOrderJSON = stub.getStringState(id);

        if (videoAssetOrderJSON == null || videoAssetOrderJSON.isEmpty()) {
            String errorMessage = String.format("VideoAssetOrder %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        VideoAssetOrder videoAssetOrder = genson.deserialize(videoAssetOrderJSON, VideoAssetOrder.class);
        return videoAssetOrder;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public VideoAssetOrder VideoDataAssetOrderStatus(final Context ctx, final String id, final int status) {
        ChaincodeStub stub = ctx.getStub();
        String videoAssetOrderJSON = stub.getStringState(id);

        if (videoAssetOrderJSON == null || videoAssetOrderJSON.isEmpty()) {
            String errorMessage = String.format("VideoAssetOrder %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        VideoAssetOrder newVideoAssetOrder = genson.deserialize(videoAssetOrderJSON, VideoAssetOrder.class);
        newVideoAssetOrder.setStatus(status);
        String sortedJSON = genson.serialize(newVideoAssetOrder);
        stub.putStringState(id, sortedJSON);

        stub.setEvent("UpdateVideoAssetOrderStatus", sortedJSON.getBytes(StandardCharsets.UTF_8));
        return newVideoAssetOrder;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllVideoAssetOrder(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        List<VideoAssetOrder> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("VideoAssetOrder", "VideoAssetOrder99999999999999999999");
        for (KeyValue result : results) {
            VideoAssetOrder videoAssetOrder = genson.deserialize(result.getStringValue(), VideoAssetOrder.class);
            queryResults.add(videoAssetOrder);
        }

        final String response = genson.serialize(queryResults);
        return response;
    }

    // AttributeApplication
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AttributeApplicationExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String attributeApplicationJSON = stub.getStringState(id);

        return (attributeApplicationJSON != null && !attributeApplicationJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public AttributeApplication CreateAttributeApplication(final Context ctx, final String id, final String departmentId, final String attribute, final int status) {
        ChaincodeStub stub = ctx.getStub();

        if (AttributeApplicationExists(ctx, id)) {
            String errorMessage = String.format("AttributeApplication %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        if (!DepartmentExists(ctx, departmentId)) {
            String errorMessage = String.format("Department %s does not exist", departmentId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        AttributeApplication attributeApplication = new AttributeApplication(id, departmentId, attribute, status);
        String sortedJson = genson.serialize(attributeApplication);
        stub.putStringState(id, sortedJson);

        stub.setEvent("CreateAttributeApplication", sortedJson.getBytes(StandardCharsets.UTF_8));
        return attributeApplication;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public AttributeApplication ReadAttributeApplication(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String attributeApplicationJSON = stub.getStringState(id);

        if (attributeApplicationJSON == null || attributeApplicationJSON.isEmpty()) {
            String errorMessage = String.format("AttributeApplication %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        AttributeApplication attributeApplication = genson.deserialize(attributeApplicationJSON, AttributeApplication.class);
        return attributeApplication;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public AttributeApplication UpdateAttributeApplicationStatus(final Context ctx, final String id, final int status) {
        ChaincodeStub stub = ctx.getStub();
        String attributeApplicationJSON = stub.getStringState(id);

        if (attributeApplicationJSON == null || attributeApplicationJSON.isEmpty()) {
            String errorMessage = String.format("AttributeApplication %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        AttributeApplication newAttributeApplication = genson.deserialize(attributeApplicationJSON, AttributeApplication.class);
        newAttributeApplication.setStatus(status);
        String sortedJson = genson.serialize(newAttributeApplication);
        stub.putStringState(id, sortedJson);

        stub.setEvent("UpdateAttributeApplicationStatus", sortedJson.getBytes(StandardCharsets.UTF_8));
        return newAttributeApplication;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAttributeApplication(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        List<AttributeApplication> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("AttributeApplication", "AttributeApplication9999999999999999999999999");
        for (KeyValue result : results) {
            AttributeApplication attributeApplication = genson.deserialize(result.getStringValue(), AttributeApplication.class);
            queryResults.add(attributeApplication);
        }

        final String response = genson.serialize(queryResults);
        return response;
    }

    // AttributeItem
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AttributeItemExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String attributeItemJSON = stub.getStringState(id);

        return (attributeItemJSON != null && !attributeItemJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public AttributeItem CreateAttributeItem(final Context ctx, final String id, final String content) {
        ChaincodeStub stub = ctx.getStub();

        if (AttributeItemExists(ctx, id)) {
            String errorMessage = String.format("AttributeItem %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        AttributeItem attributeItem = new AttributeItem(id, content);
        String sortedJson = genson.serialize(attributeItem);
        stub.putStringState(id, sortedJson);

        stub.setEvent("CreateAttributeItem", sortedJson.getBytes(StandardCharsets.UTF_8));
        return attributeItem;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public AttributeItem ReadAttributeItem(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String attributeItemJSON = stub.getStringState(id);

        if (attributeItemJSON == null || attributeItemJSON.isEmpty()) {
            String errorMessage = String.format("AttributeItem %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        AttributeItem attributeItem = genson.deserialize(attributeItemJSON, AttributeItem.class);
        return attributeItem;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public AttributeItem DeleteAttributeItem(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();

        String attributeItemJSON = stub.getStringState(id);

        if (attributeItemJSON == null || attributeItemJSON.isEmpty()) {
            String errorMessage = String.format("AttributeItem %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        AttributeItem attributeItem = genson.deserialize(attributeItemJSON, AttributeItem.class);

        stub.delState(id);
        stub.setEvent("DeleteAttributeItem", attributeItemJSON.getBytes(StandardCharsets.UTF_8));
        return attributeItem;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAttributeItem(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        List<AttributeItem> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("AttributeItem", "AttributeItem9999999999999999999999999");
        for (KeyValue result : results) {
            AttributeItem attributeItem = genson.deserialize(result.getStringValue(), AttributeItem.class);
            queryResults.add(attributeItem);
        }

        final String response = genson.serialize(queryResults);
        return response;
    }

}
