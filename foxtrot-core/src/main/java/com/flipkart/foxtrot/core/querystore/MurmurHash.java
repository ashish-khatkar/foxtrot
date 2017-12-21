package com.flipkart.foxtrot.core.querystore;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import org.apache.hadoop.hbase.util.Bytes;

import java.util.Arrays;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.sematext.hbase.ds.RowKeyDistributorByHashPrefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MurmurHash implements RowKeyDistributorByHashPrefix.Hasher {
    private Integer numOfPartitions;
    private Integer defaultPartition;

    private static final byte[][] PREFIXES;

    private static Logger logger = LoggerFactory.getLogger(MurmurHash.class);

    static {
        PREFIXES = new byte[256][];
        for (int i = 0; i < 256; i++) {
            PREFIXES[i] = new byte[]{(byte) i};
        }
    }

    public MurmurHash(@Min(0) @Max(256) Integer numOfPartitions, @Min(1) Integer defaultPartition) {
        if (defaultPartition < numOfPartitions) {
            this.numOfPartitions = numOfPartitions;
            this.defaultPartition = defaultPartition;
        } else {
            throw new IllegalArgumentException("maxBuckets should be in 1..256 range");
        }
    }

    @Override
    public byte[] getHashPrefix(byte[] rowKey) {
        try {
            HashFunction hashFunction = Hashing.murmur3_128();
            HashCode hashCode = hashFunction.hashBytes(rowKey);
            Integer partition = Math.abs(hashCode.asInt()) % numOfPartitions;
            String key = String.format("000%s",Integer.toHexString(partition));
            key= key.substring(key.length()-3);
            return Bytes.toBytes(key + "-");
        } catch (Throwable t) {
            logger.error("MurmurHash: Error occurred while calculating Partition for key" + Bytes.toString(rowKey));
            return Bytes.toBytes(defaultPartition.toString());
        }
    }

    @Override
    public byte[][] getAllPossiblePrefixes() {
        return Arrays.copyOfRange(PREFIXES, 0, this.numOfPartitions);
    }

    @Override
    public int getPrefixLength(byte[] bytes) {
        return 1;
    }

    @Override
    public String getParamsToStore() {
        return numOfPartitions.toString();
    }

    @Override
    public void init(String str) {
        this.numOfPartitions = Integer.valueOf(str);
        this.defaultPartition = 0;
    }
}
