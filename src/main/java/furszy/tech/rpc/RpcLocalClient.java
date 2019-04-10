package furszy.tech.rpc;

import com.google.common.collect.ObjectArrays;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by mati on 28/01/17.
 */
public class RpcLocalClient {

    private static final Logger logger = Logger.getLogger(RpcLocalClient.class);

    private File dir;
    private String pivxExecutablePath;

    public RpcLocalClient(String pivxExecutablePath, String datadir) {
        this.pivxExecutablePath = pivxExecutablePath;
        this.dir = (datadir == null) ? null : new File(datadir);
    }

    public String getBlockCount(){
        return execCommand("getblockcount");
    }

    public String getBestBlockHash() {
        return execCommand("getbestblockhash");
    }

    public String getZerocoinBalance() {
        return execCommand("getzerocoinbalance");
    }

    public String getRawTransaction(String txid) {
        return execCommand("getrawtransaction", txid);
    }

    public Result getBlock(String blockHash) {
        return new Result( execCommand("getblock", blockHash));
    }

    public BigDecimal getBalance() {
        String bal = execCommand("getbalance");
        return new BigDecimal(bal.trim());
    }

    public String getNewAddress() {
        return execCommand("getnewaddress");
    }

    /**
     * getaccumulatorwitness "commitmentCoinValue, coinDenomination"
     * @return
     */
    public String getAccWitness(BigInteger commitmentCoinValue, int denom){
        return execCommand("getaccumulatorwitness", commitmentCoinValue.toString(16), denom);
    }

    public String sendRawTransaction(String hex){
        return execCommand("sendrawtransaction", hex);
    }

    public String mintZerocoin(int amount){
        return execCommand("mintzerocoin", amount);
    }

    /**
     * return a list..
     * @param fVerbose
     * @param fMatureOnly
     * @return
     */
    public JsonArray listMintedZerocoins(boolean fVerbose, boolean fMatureOnly){
        return wrapJsonArray(execCommand("listmintedzerocoins",fVerbose, fMatureOnly));
    }


    /**
     *
     * @param includeSpent
     * @param filterDenom ---> 0 means every denom
     * @return
     */
    public JsonArray exportzerocoins(boolean includeSpent, int filterDenom){
        if (filterDenom == 0){
            return wrapJsonArray(execCommand("exportzerocoins", includeSpent));
        }else{
            return wrapJsonArray(execCommand("exportzerocoins", includeSpent, filterDenom));
        }

    }

    public String spendrawzerocoin(String serial, String randomness, int denom, String privkey, String address){
        return execCommand("spendrawzerocoin", serial, randomness, denom, privkey, address);
    }

    /**
     * "spendzerocoinmints mints_list (\"address\") \n"
     *             "\nSpend zPIV mints to a PIV address.\n" +
     *             HelpRequiringPassphrase() + "\n"
     *
     *             "\nArguments:\n"
     *             "1. mints_list     (string, required) A json array of zerocoin mints serial hashes\n"
     *             "2. \"address\"     (string, optional, default=change) Send to specified address or to a new change address.\n"
     *
     *             "\nResult:\n"
     *             "{\n"
     *             "  \"txid\": \"xxx\",             (string) Transaction hash.\n"
     *             "  \"bytes\": nnn,              (numeric) Transaction size.\n"
     *             "  \"fee\": amount,             (numeric) Transaction fee (if any).\n"
     *             "  \"spends\": [                (array) JSON array of input objects.\n"
     *             "    {\n"
     *             "      \"denomination\": nnn,   (numeric) Denomination value.\n"
     *             "      \"pubcoin\": \"xxx\",      (string) Pubcoin in hex format.\n"
     *             "      \"serial\": \"xxx\",       (string) Serial number in hex format.\n"
     *             "      \"acc_checksum\": \"xxx\", (string) Accumulator checksum in hex format.\n"
     *             "    }\n"
     *             "    ,...\n"
     *             "  ],\n"
     *             "  \"outputs\": [                 (array) JSON array of output objects.\n"
     *             "    {\n"
     *             "      \"value\": amount,         (numeric) Value in PIV.\n"
     *             "      \"address\": \"xxx\"         (string) PIV address or \"zerocoinmint\" for reminted change.\n"
     *             "    }\n"
     *             "    ,...\n"
     *             "  ]\n"
     *             "}\n"
     * @param
     * @return
     */
    public String spendzerocoinmints(String mints_list, String address){
        return execCommand("spendzerocoinmints", mints_list, address);
    }

    private String[] paramsToStringArray(Object... params){
        String[] paramsRet = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            paramsRet[i] = params[i].toString();
        }
        return paramsRet;
    }

    private String execCommand(String commandStr, Object... params){
        String[] command;
        if (dir != null) {
            command = new String[]{pivxExecutablePath, "-datadir=" + dir.getAbsolutePath(), commandStr};
        }else {
            command = new String[]{pivxExecutablePath, commandStr};
        }
        String[] finalCommand = ObjectArrays.concat(command, paramsToStringArray(params), String.class);
        //System.out.println("Executing: " + Arrays.toString(finalCommand));
        return executeCommand(finalCommand);
    }


    private String executeCommand(String[] command){
        StringBuilder output = new StringBuilder();
        Process p;
        try {

            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            output.append(readStream(p.getInputStream()));

            if(output.length() == 0){
                // Mark this as an error
                output.append(readStream(p.getErrorStream()));
            }
            p.destroyForcibly();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
        return output.toString();
    }


    private String readStream(InputStream stream) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(stream));

        String line = "";
        while ((line = reader.readLine())!= null) {
            output.append(line + "\n");
        }
        reader.close();
        return output.toString();
    }


    // json
    private JsonArray wrapJsonArray(String json) {
        return new JsonParser().parse(json).getAsJsonArray();
    }
}