package bosh.tech;

import java.util.Iterator;
import java.util.List;

import com.google.protobuf.ByteString;
import io.netty.handler.ssl.OpenSsl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import static java.nio.charset.StandardCharsets.UTF_8;

public class AssociationChaincode extends ChaincodeBase
{
    private static Log _logger = LogFactory.getLog(AssociationChaincode.class);

    @Override
    public Response init(ChaincodeStub stub)
    {
        return newSuccessResponse("init successfully");
    }

    @Override
    public Response invoke(ChaincodeStub stub)
    {
        String func = stub.getFunction();
        List<String> args = stub.getParameters();
        if(func.equals("invoke"))
        {
            return invoke(stub,args);
        }
        if(func.equals("delete"))
        {
            return delete(stub,args);
        }
        if(func.equals("query"))
        {
            return query(stub,args);
        }
        if(func.equals("queryhistory"))
        {
            return queryhistory(stub,args);
        }
        if(func.equals("count"))
        {
            return count(stub,args);
        }
        return newErrorResponse("invalid invoke function name");
    }

    private Response invoke(ChaincodeStub stub, List<String>args)
    {
        String key = args.get(0);
        String val = args.get(1);
        stub.putStringState(key, val);

        return newSuccessResponse("invoke success");
    }

    private Response delete(ChaincodeStub stub, List<String>args)
    {
        String key = args.get(0);
        stub.delState(key);
        return newSuccessResponse("del success");
    }
    private Response query(ChaincodeStub stub, List<String>args)
    {
        String key = args.get(0);
        String val = stub.getStringState(key);
        return newSuccessResponse(val, ByteString.copyFrom(val, UTF_8).toByteArray());
    }
    private Response queryhistory(ChaincodeStub stub, List<String>args)
    {
        String key = args.get(0);
        String val = "";
        QueryResultsIterator<KeyModification> res = stub.getHistoryForKey(key);
        Iterator<KeyModification> it = res.iterator();
        boolean flag = false;
        while (it.hasNext())
        {
            KeyModification km = it.next();
            if(km.isDeleted())
            {
                continue;
            }
            if(val.equals(""))
            {
                val = val + "[";
            }
            if(!flag)
            {
                val = val + km.getStringValue();
                flag = true;
            }else
            {
                val = val + "," + km.getStringValue() ;
            }

        }
        if(!val.equals(""))
        {
            val = val + "]";
        }
        return newSuccessResponse(val, ByteString.copyFrom(val, UTF_8).toByteArray());
    }
    private Response count(ChaincodeStub stub, List<String>args)
    {
        String key = args.get(0);
        String val = stub.getStringState(key);

        if(val.equals(""))
        {
            stub.putStringState(key,"1");
        }else
        {
            int temp = Integer.parseInt(val) + 1;
            val = temp + "";
            stub.putStringState(key, val);
        }


        return newSuccessResponse(val, ByteString.copyFrom(val, UTF_8).toByteArray());
    }
    public static void main(String[] args)
    {
        new AssociationChaincode().start(args);
    }
}
